package it.xview.cp.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.Pec;
import it.xview.cp.util.PecBean;
import it.xview.cp.util.TempSecurityStorage;



public class SendViewByPec {
	private String arg0;
	private String arg1;
	private String requestId ="0";
	private ConcurrentHostConnection chc;
	public int exitStatus=0;
	private String sqlCredenziali ="";
	private String sqlPec ="";
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

	private String exitMsg = "Processo di firma concluso correttamente.";
	private final String exitMsgDefault = "Processo di firma concluso correttamente.";
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public static void main(String[] args) {
		SendViewByPec obj = new SendViewByPec();
		obj.setArg0(args[0]);
		obj.setArg1(args[1]);
		obj.setSqlCredenziali("select  MITTENTE, PWD, SMTP, IS_CRIPTED from XXX_ISOLA_PEC_Credenziali_V");
		
		obj.setSqlPec("SELECT MAIL, MAIL_CC, MAIL_BCC, OGGETTO, CORPO, ABSOLUTE_NOME_FILE, NOME_FILE FROM XXX_ISOLA_PEC_DOC_V");
		obj.run();
		System.out.println(obj.getExitMsg());
		System.exit(obj.getExitStatus());

	}

	private void run() {
		try {
			chc = new ConcurrentHostConnection(arg0,arg1);

			HashMap<String,String> pecCredential = chc.getSingleRow(sqlCredenziali, null);
			SecretKeySpec sKey = TempSecurityStorage.createSecretKey(chc.getConnection());
			String pecPwd = "Y".equals(pecCredential.get("is_cripted") )? TempSecurityStorage.decrypt(pecCredential.get("pwd"), sKey):pecCredential.get("pwd");
			String pecUsr = pecCredential.get("mittente");
			String pecSmtp = pecCredential.get("smtp");
			List<PecBean> pecs=getListaPec();
			if(pecs==null || pecs.size()==0) {
				exitStatus = 999;
				exitMsg = "Nessuna mail da inviare.";
			}else {
				for(PecBean pecBean : pecs) {
					Pec.set_mittente(pecUsr, pecPwd);
					Pec.set_smpt(pecSmtp);
					String esitoSendMail = Pec.send_mail(pecBean);
					if("OK".equals(esitoSendMail)) {
						System.out.println("INVIATA : " + pecBean.toString());
					}else {
						System.out.println("ERRORE : " + pecBean.toString());
						this.setExitMsg("Errore di autenticazione o di invio : " + pecBean.toString());
						this.exitStatus=3;
					}
				}
			}

		
		} catch (Exception e) {
			this.exitStatus=1;
			this.exitMsg =  e.getMessage();
			try {
				chc.closeConnection();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		
	}

	public String getSqlCredenziali() {
		return sqlCredenziali;
	}

	public void setSqlCredenziali(String sqlCredenziali) {
		this.sqlCredenziali = sqlCredenziali;
	}

	public String getSqlPec() {
		return sqlPec;
	}

	public void setSqlPec(String sqlPec) {
		this.sqlPec = sqlPec;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg0() {
		return arg0;
	}

	public void setArg0(String arg0) {
		this.arg0 = arg0;
	}
	private List<PecBean> getListaPec() throws Exception {
		Connection conn = chc.getConnection();
		PreparedStatement ps = conn.prepareStatement(sqlPec);
		System.out.println("SQL - " + sqlPec);
		ResultSet rs = ps.executeQuery();
		List<PecBean> lista = new LinkedList<PecBean>();
		while(rs.next()) {
			PecBean unaPec = new PecBean();
			unaPec.setNomeFile(rs.getString("NOME_FILE"));
			unaPec.setDestinatari(rs.getString("MAIL"));
			unaPec.setDestinatariCC(rs.getString("MAIL_CC"));
			unaPec.setDestinatariBCC(rs.getString("MAIL_BCC"));
			unaPec.setOggetto(rs.getString("OGGETTO"));
			unaPec.setCorpo_mess(rs.getString("CORPO"));
			String[] attach = {rs.getString("ABSOLUTE_NOME_FILE")};
			unaPec.setAttach(attach);
			lista.add(unaPec);
		}
		rs.close();
		ps.close();
		return lista;
	}

}
