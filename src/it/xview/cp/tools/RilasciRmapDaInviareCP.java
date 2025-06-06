package it.xview.cp.tools;
/*
 * XXH_RILASCI_RMAP_DA_INVIARE
 */
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.NoReplyMail;
import it.xview.cp.util.NoReplyMailerBean;

public class RilasciRmapDaInviareCP {
	private int exitStatus=0;
	private String exitMsg = "Processo di invio RilasciRmapDaInviare concluso correttamente.";
	private ConcurrentHostConnection chc;
	private String sGroupId;
	private String arg0;
	private String arg1;

	public static void main(String[] args) {
		RilasciRmapDaInviareCP obj = new RilasciRmapDaInviareCP();
		try {
			obj.setArg0(args[0]);
			obj.setArg1(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			
	 		String argsL0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_RILASCI_RMAP_DA_INVIARE";
			String argsL1="XXH_RILASCI_RMAP_DA_INVIARE FCP_REQID=31177282 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=5675 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"2\"";
			obj.setArg0(argsL0);
			obj.setArg1(argsL1);
			
			e.printStackTrace();
		}
		obj.run();
		obj.setEsito();
		System.exit(obj.exitStatus);
	}

	private void setEsito() {
		boolean ok = 0==exitStatus ;
		String nomeProc = "XXX_GESTIONE_GENERI_EXTRA.set_esito_invio";
		String msg = null;
		String esito ="Y";
		if(!ok) {
			esito="E";
			msg=exitMsg;
		}
		String[] param = {sGroupId,esito,msg};
		try {
			chc.callProc(nomeProc, param);
		} catch (SQLException e) {
			exitMsg = "Errore nella procedura di aggiornamento : " + nomeProc;
			exitStatus=2;
			e.printStackTrace();
		}
	}

	private void run() {
		PreparedStatement stmt = null;
		ResultSet rs=null;
		try {
			chc = new ConcurrentHostConnection(arg0,arg1);
			HashMap<String,Object> param = chc.getConcurrentParameter();
			sGroupId = (String)param.get("GROUP_ID");
			String sql = "select ABSOLUTE_PATH, " + 
					"MITTENTE, DESTINATARIO, OGGETTO, CORPO, NOMEFILE "+
					"from XXX_RILASCI_RMAP_DA_INVIARE_V " + 
					"where GROUP_ID = :1";
			stmt = chc.getConnection().prepareStatement(sql);
			stmt.setString(1,sGroupId);
			rs=stmt.executeQuery();
			while(rs.next()) {
				String sAllegati = rs.getString("NOMEFILE");
				String[] allegati = sAllegati.split(";");
				for(int i = 0; i<allegati.length; i++) {
					allegati[i] = rs.getString("ABSOLUTE_PATH") + allegati[i];
				}
				NoReplyMailerBean bean = new NoReplyMailerBean(rs.getString("DESTINATARIO"), null, null, 
						rs.getString("OGGETTO"), rs.getString("CORPO"), 
						allegati);
				bean.setMittente(rs.getString("MITTENTE"));
				NoReplyMail mail = new NoReplyMail(bean);
				boolean esitoInvio = mail.send();
				if(!esitoInvio) {
					this.setExitMsg("Errore generico nell'invio della mail");
					this.setExitStatus(1);
				}
			}
		} catch (Exception e) {
			this.exitMsg = e.getMessage();
			this.exitStatus = 1;
			e.printStackTrace();
	      }finally
        {
          try{
            rs.close();
            stmt.close();
          }catch(Exception e){}
       }
		
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public String getExitMsg() {
		return exitMsg;
	}

	public void setExitMsg(String exitMsg) {
		this.exitMsg = exitMsg;
	}

	public ConcurrentHostConnection getChc() {
		return chc;
	}

	public void setChc(ConcurrentHostConnection chc) {
		this.chc = chc;
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
