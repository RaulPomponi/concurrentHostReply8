package it.xview.cp.cbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.NoReplyMail;
import it.xview.cp.util.NoReplyMailerBean;

public class CreaInviaXmlCP {
	private ConcurrentHostConnection chc;
	private String exitMsg="OK";
	private String p_org_id = "81";
	private int exitStatus=0;
	private String arg0=null;
	private String arg1=null;
	private String sqlH="SELECT * FROM xxh.xxh_cbi_sepa_headers WHERE MsgId = :1 AND org_id = ";
	private String sqlL="SELECT A.*                              "
			+"	,(SELECT sum(B.amt_instdamt)          "
			+"	FROM xxh.xxh_cbi_sepa_lines B         "
			+"	WHERE A.vendor_id = B.vendor_id       "
			+"	AND A.msgid = B.msgid                 "
			+"	AND A.org_id = B.org_id) TOT_DISTINTA "
			+"FROM xxh.xxh_cbi_sepa_lines A           "
			+"WHERE A.MsgId = :1                      "
			+"AND A.msg_err IS NULL                   "
			+" ORDER BY vendor_id                     ";

	private String oh1 = "<?xml version=\"1.0\" encoding=\"utf-8\"?><CBIPaymentRequest xmlns=\"urn:CBI:xsd:CBIPaymentRequest.00.04.01\">"
			+"<GrpHdr><MsgId>rec_headers.MsgId</MsgId><CreDtTm>rec_headers.CreDtTm</CreDtTm><NbOfTxs>rec_headers.NbOfTxs</NbOfTxs><CtrlSum>rec_headers.CtrlSum</CtrlSum><InitgPty>"
			+"<Nm>rec_headers.InitgPty_Nm</Nm><Id><OrgId><Othr><Id>rec_headers.OrgId_Othr_Id</Id><Issr>rec_headers.OrgId_Othr_Issr</Issr></Othr></OrgId></Id></InitgPty></GrpHdr>"
			+"<PmtInf><PmtInfId>rec_headers.pmtinfid</PmtInfId><PmtMtd>rec_headers.pmtmtd</PmtMtd><BtchBookg>rec_headers.btchbookg</BtchBookg><PmtTpInf><InstrPrty>NORM</InstrPrty><SvcLvl><Cd>SEPA</Cd></SvcLvl></PmtTpInf>"
			+"<ReqdExctnDt><DtTm>rec_headers.reqdexctndt_dttm</DtTm></ReqdExctnDt>"
			+"<Dbtr><Nm>rec_headers.dbtr_nm</Nm></Dbtr>"
			+"<DbtrAcct><Id><IBAN>rec_headers.dbtracct_id_iban</IBAN></Id></DbtrAcct>"
			+"<DbtrAgt><FinInstnId><BICFI>rec_headers.bicfi</BICFI><ClrSysMmbId><MmbId>rec_headers.clrsysmmbid_mmbid</MmbId></ClrSysMmbId></FinInstnId></DbtrAgt>"
			+"<ChrgBr>SLEV</ChrgBr>";
	private String ch1="</PmtInf></CBIPaymentRequest>";
	private String lh="<CdtTrfTxInf><PmtId><InstrId>rec_distinte.EndToEndId</InstrId><EndToEndId>rec_distinte.EndToEndId</EndToEndId></PmtId><PmtTpInf><CtgyPurp><Cd>rec_distinte.pmttpinf_ctgypurp_cd</Cd>"
			+"</CtgyPurp></PmtTpInf><Amt><InstdAmt Ccy=\"rec_distinte.amt_ccy\">rec_distinte.amt_instdamt</InstdAmt></Amt>)"
			+"<CdtrAgt><FinInstnId><BICFI>rec_distinte.cdtragt_fininstnid_bicfi</BICFI></FinInstnId></CdtrAgt>)"
			+"<Cdtr><Nm>rec_distinte.cdtr_nm</Nm><PstlAdr><TwnNm>rec_distinte.pstladr_twnnm </TwnNm><Ctry>rec_distinte.pstladr_ctry </Ctry></PstlAdr></Cdtr><CdtrAcct><Id>"
			+"<IBAN>rec_distinte.cdtracct_id_iban</IBAN></Id></CdtrAcct><RmtInf>)"
			+"<Ustrd>rec.ustrd</Ustrd>)"
			+"</RmtInf></CdtTrfTxInf>";
	public static void main(String[] args) {
		CreaInviaXmlCP obj = new CreaInviaXmlCP();
//		obj.setArg0(args[0]);
//		obj.setArg1(args[1]);
		String args0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_TEST_HOST";
		String args1="XXH_TEST_HOST FCP_REQID=31164736 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=5675 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"Distinta81-070324-12:03:40\"";
		obj.setArg0(args0);
		obj.setArg1(args1);
		
		try {
			obj.run();
		} catch (Exception e) {
			obj.setExitMsg(e.getMessage());
			obj.setExitStatus(100);
		}
		System.out.println(obj.getExitStatus() + " - " + obj.getExitMsg());
		System.exit(obj.getExitStatus());
	}
	private void test() {
		System.out.println(oh1.replaceFirst("\\$pmtinfid", "Distinta81-070324-12:03:40"));
	}

	public void run() {
		String distinta=null;
		try {
			chc = new ConcurrentHostConnection(arg0, arg1);
			distinta = (String)chc.getConcurrentParameter().get("P_DISTINTA");
			chc.log(distinta);
		} catch (Exception e) {
			this.setExitMsg(e.getMessage());
			this.setExitStatus(101);
			e.printStackTrace();
		}
		if(exitStatus==0) {
			HashMap<String,String> rowH = chc.getSingleRow(sqlH+p_org_id, distinta);
			int importo=0;
			int conta_distinte=0;
			String hTag = sostituisci(rowH, oh1,"rec_headers");
			chc.log(hTag);

			ResultSet rs=null;
			PreparedStatement stmt = null;
			try {
				stmt = chc.getConnection().prepareStatement(sqlL);
				stmt.setString(1,distinta);
				rs=stmt.executeQuery();
			} catch (Exception e) {
				exitMsg=e.getMessage();
				exitStatus = 101;
				e.printStackTrace();
			}
			try {
				while(rs.next() && exitStatus==10) {
					String lTag = sostituisci(rowH, lh,"rec_headers");
					if(rs.getString("amt_ccy")!=null)
						lTag=lTag.replaceAll("rec_distinte.amt_ccy", rs.getString("amt_ccy"));
					else
						lTag=lTag.replaceAll("<CdtrAgt><FinInstnId><BICFI>rec_distinte.cdtragt_fininstnid_bicfi</BICFI></FinInstnId></CdtrAgt>)","");
					lTag=lTag; // ?????????????????????????
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				exitMsg=e.getMessage();
				exitStatus = 102;
				e.printStackTrace();
			}

		}
		try {
			chc.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	private String sostituisci(HashMap<String,String> map, String string, String recName) {
		String s=string;
		while(s.indexOf(recName)>1) {
			int start = s.indexOf(recName) + recName.length() + 1;
			int end = s.substring(start).indexOf("<");
			String sField = s.substring(start, start+end);
			s=s.replaceAll(recName+"."+sField, map.get(sField));
			chc.log(sField +"="+ map.get(sField));
		}
		return s;
	}

	public String getExitMsg() {
		return exitMsg;
	}

	public void setExitMsg(String exitMsg) {
		this.exitMsg = exitMsg;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public String getArg0() {
		return arg0;
	}

	public void setArg0(String arg0) {
		this.arg0 = arg0;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

}
