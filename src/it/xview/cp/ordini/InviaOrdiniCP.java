package it.xview.cp.ordini;
/*
 *  Programma 		XXH: Pec Invia Ordini e Rilasci
 *  Eseguibile 		XXH_PEC_INVIA_ORDINI
 *  Parametri
 *  			P_ORG_ID				FND_NUMBER
 *  			P_ARSS_REQUEST_ID		FND_NUMBER
 *  
 *  Responsabilità 
 *  			Amministratore di sistema
 *  Gruppo di Richieste
 *  			System Administrator Reports
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.apache.pdfbox.pdmodel.PDDocument;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.NoReplyMail;
import it.xview.cp.util.NoReplyMailerBean;
import it.xview.cp.util.TempSecurityStorage;
import it.xview.cp.util.Pec;
import it.xview.cp.util.PecBean;;


public class InviaOrdiniCP {
	private int exitStatus=0;
	private String exitMsg = "Processo di invio PEC concluso correttamente.";
	private ConcurrentHostConnection chc;
	private String sOrgId;
	private String sProtocollo;
	private String sArssRequestId;
//	private String sNomeFile;
	private String arg0;
	private String arg1;

	private String sqlInvio 	= "SELECT ORG_ID, ARSS_REQUEST_ID, MAIL, MAIL_CC, MAIL_BCC, NOME_FILE, ABSOLUTE_NOME_FILE, ID_ORDINE, TYPE_LOOKUP_CODE, ORIGINE, URL_NOC, OGGETTO, CORPO, FND_LOBS_ID FROM xxx_gestione_ordinipo_v where ORG_ID =:1 and ARSS_REQUEST_ID = :2";
	private String sqlCruscotto = "SELECT ORG_ID, ARSS_REQUEST_ID, MAIL, MAIL_CC, MAIL_BCC, NOME_FILE, ABSOLUTE_NOME_FILE, ID_ORDINE, TYPE_LOOKUP_CODE, ORIGINE, URL_NOC, OGGETTO, CORPO, FND_LOBS_ID FROM XXX_GESTIONE_ORDINIPO_ERR_V WHERE ORG_ID =:1 and  PROTOCOLLO = :2";
	private String sqlRilp 		= "SELECT ORG_ID, ARSS_REQUEST_ID, MAIL, MAIL_CC, MAIL_BCC, NOME_FILE, ABSOLUTE_NOME_FILE, ID_ORDINE, TYPE_LOOKUP_CODE, ORIGINE, URL_NOC, OGGETTO, CORPO, FND_LOBS_ID FROM XXX_GESTIONE_ORDINIPO_RILP_V WHERE ORG_ID =:1 ";
	
	private String sqlFolderInviato = "select XXX_XXVIEW_GESTIONE_ORDINIPO.get_folder_inviato(:1) FOLDER_INVIATO from dual ";
	private String sqlDelFile 		= "select XXX_XXVIEW_GESTIONE_ORDINIPO.DELETE_POST_INVIATO(:1) DELETE_INVIATO from dual ";
	
	private String sqlCredential="select distinct MITTENTE, PWD, SMTP from XXH.XXX_GESTIONE_ORDINIPO where ORG_ID=:1";
	public static void main(String[] args) {
/*		
		String args0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_PEC_INVIA_ORDINI";
		String args1="XXH_PEC_INVIA_ORDINI FCP_REQID=31159088 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=3135 FCP_USERNAME=\"DG\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"1\" \"81\" \"81_2303226_REV0_1_signed.pdf\"";
		InviaOrdiniCP inviaOrdini = new InviaOrdiniCP();
		inviaOrdini.arg0 = args0;//args[0];
		inviaOrdini.arg1 = args1;//args[1];
*/		
		/**/

		InviaOrdiniCP inviaOrdini = new InviaOrdiniCP();
		inviaOrdini.arg0 = args[0];
		inviaOrdini.arg1 = args[1];
		
		inviaOrdini.run();
		System.out.println(inviaOrdini.getExitMsg());
		System.exit(inviaOrdini.getExitStatus());
		

	}

	private void run() {
		int errCounter=100;
		String pecUsr = "";
		try {
			chc = new ConcurrentHostConnection(arg0,arg1);
			HashMap<String,Object> param = chc.getConcurrentParameter();
			sOrgId = (String)param.get("P_ORG_ID");
			sArssRequestId = (String)param.get("P_ARSS_REQUEST_ID");
			sProtocollo= (String)param.get("P_NOME_FILE");
			// Aggiunta per concurrent che riprocessa i documenti firmati ma non inviati (come per l'errore di connessione al db)
			if("RIPROVA".equals(sProtocollo))
				forzaProtocollo();
			//Fine aggiunta
			if(Integer.parseInt(sArssRequestId)>10) {
				String esitoApprova = approva(null);
				chc.log("Esito Approva : " + esitoApprova);
			}
			if(Integer.parseInt(sArssRequestId)==1) {
				String esitoApprova = approva(sProtocollo);
				chc.log("Esito Approva : " + esitoApprova);
			}
			if(Integer.parseInt(sArssRequestId)==0) {
				chc.log("Rilasci programmati");
			}
			
			
			if("232".equals(sOrgId)) {
				chc.log("Invio non previsto per CTC");
			}else {
				HashMap<String,String> pecCredential = chc.getSingleRow(sqlCredential, sOrgId);
				SecretKeySpec sKey = TempSecurityStorage.createSecretKey(chc.getConnection());
				String pecPwd = TempSecurityStorage.decrypt(pecCredential.get("pwd"), sKey);
				pecUsr = pecCredential.get("mittente");
				String pecSmtp = pecCredential.get("smtp");
				List<PecBean> pecs=null;
				if("1".equals(sArssRequestId)) {
					pecs=getListaPecCruscotto();
				}else if("0".equals(sArssRequestId)) {
					pecs=getListaPecRilp();
				}else{
					pecs=getListaPec();
				} 
				String status = "E";
				if(pecs==null || pecs.size()==0) {
					exitStatus = 999;
					exitMsg = "Nessuna mail da inviare.";
				}else {
					for(PecBean pecBean : pecs) {
						try {
							boolean fileOk = chekPdf(pecBean);
							if(fileOk) {
								Pec.set_mittente(pecUsr, pecPwd);
								Pec.set_smpt(pecSmtp);
								String esitoSendMail = Pec.send_mail(pecBean);
								if("OK".equals(esitoSendMail)) {
									System.out.println("INVIATA : " + pecBean.toString());
									status = "I";
								}else {
									System.out.println("ERRORE : " + pecBean.toString());
									this.setExitMsg("Errore di autenticazione o di invio : " + pecBean.toString());
									status = "E";
									this.exitStatus=3;
								}
							}else {
								System.out.println("ERRORE Lettura File: " + pecBean.toString());
								this.setExitMsg("Errore di lettura del file : " + pecBean.toString());
								status = "E";
								this.exitStatus=4;
							}
						} catch (Exception e) {
							status = "E";
							errCounter++;
							this.exitStatus=errCounter;
							this.setExitMsg("Errore "+ e.getMessage() +" : " + pecBean.toString());
							System.out.println("ERRORE : " +pecBean.toString());
		
							e.printStackTrace();
						}
						setPecEsito(pecBean.getNomeFile(), pecBean.getDestinatari(), status);
						if("I".equals(status)) {
							String folderInviato = chc.getSingleValue(sqlFolderInviato, sOrgId);
							if(!pecBean.getAttach()[0].equalsIgnoreCase(folderInviato+pecBean.getNomeFile())) {
								System.out.println("copyFile(" + pecBean.getAttach()[0]+","+ folderInviato+pecBean.getNomeFile()+")");
								copyFile(pecBean.getAttach()[0], folderInviato+pecBean.getNomeFile());
								String delFile = chc.getSingleValue(sqlDelFile, sOrgId);
								if("Y".equals(delFile)) {
									removeFile(pecBean.getAttach()[0]);
								}
							}else {
								System.out.println("Spostamento del file non necessario : " + pecBean.getAttach()[0]);
							}
							
						}
					}
				}
			}
			chc.closeConnection();
			
//			System.out.println(pecUsr+" - "+pecPwd);
		} catch (Exception e) {
			this.exitStatus=errCounter;
			if(errCounter==100) this.setExitMsg("Errore di autenticazione : " + pecUsr);
			e.printStackTrace();
		}
	}
	
	private void forzaProtocollo() { // Aggiunta del 08/05/2025
		String newProtocollo = chc.getSingleValue("SELECT XXH_FIRMA_ARSS_PROTOCOLLO_S.nextval prot from dual", null);
		String[] param = {newProtocollo,sOrgId};
		try {
			chc.callProc("XXH_FIRMA_ARSS_PKG.ADD_FILES", param);
		} catch (SQLException e) {
            exitStatus=199;
			e.printStackTrace();
		}
	}

	private String approva(String protocollo) {
		String esito="OK";
    	try {
            CallableStatement cs = null;
            String call = "call XXX_XXVIEW_GESTIONE_ORDINIPO.APPROVA(?,?,?,?)"; 
            cs = chc.getConnection().prepareCall(call);

            cs.setString(1,sArssRequestId); // PK
            cs.setString(2,protocollo); 
            cs.setString(3,String.valueOf(chc.getRequestId())); 
			cs.registerOutParameter(4, java.sql.Types.VARCHAR);
            cs.executeUpdate();
            esito = cs.getString(4);
            cs.close();
        } catch (Exception e) {
        	esito = e.getMessage();
            e.printStackTrace();
            exitStatus=99;
        }
    	return esito;
	}

	private void setPecEsito(String nomeFileCorrente, String destinatario, String status) {
    	try {
            CallableStatement cs = null;
            String call = "call XXX_XXVIEW_GESTIONE_ORDINIPO.SET_ESITO(?,?,?,?)";
            cs = chc.getConnection().prepareCall(call);
            cs.setInt(1,chc.getRequestId()); // PK
            cs.setString(2,nomeFileCorrente);
            cs.setString(3,destinatario);
            cs.setString(4,status);
            cs.executeUpdate();
            cs.close();
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus=99;
        }
		
	}

	private List<PecBean> getListaPec() throws Exception {
		Connection conn = chc.getConnection();
		PreparedStatement ps = conn.prepareStatement(sqlInvio);
		System.out.println("INVIO - " + sqlInvio+" : "+sOrgId+" : " + sArssRequestId);
		ps.setString(1, sOrgId);
		ps.setString(2, sArssRequestId);
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
			String noc = rs.getString("URL_NOC");
			if(noc==null) {
				String[] attach = {rs.getString("ABSOLUTE_NOME_FILE")};
				unaPec.setAttach(attach);
			}else {
				String[] attach = {rs.getString("ABSOLUTE_NOME_FILE"),rs.getString("URL_NOC")};
				unaPec.setAttach(attach);
			}
			String altriAllegatiId = rs.getString("FND_LOBS_ID");
			if(altriAllegatiId!=null) {
				System.out.println("Gestione Allegati aggiuntivi : FND_LOBS: " + altriAllegatiId);
				String[] altriAllegati = allegatiDaBlob(altriAllegatiId);
				String[] attach = new String[unaPec.getAttach().length + altriAllegati.length];
				int i=0;
				for(i=0; i< unaPec.getAttach().length;i++ ) {
					attach[i]=unaPec.getAttach()[i];
				}
				for(int k=0; k < altriAllegati.length; k++) {
					attach[i+k]=altriAllegati[k];
				}
				unaPec.setAttach(attach);
			}
			lista.add(unaPec);
			
		}
		rs.close();
		ps.close();
		return lista;
	}
	private List<PecBean> getListaPecCruscotto() throws Exception {
		Connection conn = chc.getConnection();
		PreparedStatement ps = conn.prepareStatement(sqlCruscotto);
		System.out.println("CRUSCOTTO "+sqlCruscotto+" : "+sOrgId+" : " + sProtocollo);
		ps.setString(1, sOrgId);
		ps.setString(2, sProtocollo);
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
			String noc = rs.getString("URL_NOC");
			if(noc==null) {
				String[] attach = {rs.getString("ABSOLUTE_NOME_FILE")};
				unaPec.setAttach(attach);
			}else {
				String[] attach = {rs.getString("ABSOLUTE_NOME_FILE"),rs.getString("URL_NOC")};
				unaPec.setAttach(attach);
			}
			String altriAllegatiId = rs.getString("FND_LOBS_ID");
			if(altriAllegatiId!=null) {
				System.out.println("Gestione Allegati aggiuntivi : FND_LOBS: " + altriAllegatiId);
				String[] altriAllegati = allegatiDaBlob(altriAllegatiId);
				String[] attach = new String[unaPec.getAttach().length + altriAllegati.length];
				int i=0;
				for(i=0; i< unaPec.getAttach().length;i++ ) {
					attach[i]=unaPec.getAttach()[i];
				}
				for(int k=0; k < altriAllegati.length; k++) {
					attach[i+k]=altriAllegati[k];
				}
				unaPec.setAttach(attach);
			}
			lista.add(unaPec);
			
		}
		rs.close();
		ps.close();
		return lista;
	}
	private List<PecBean> getListaPecRilp() throws Exception {
		Connection conn = chc.getConnection();
		PreparedStatement ps = conn.prepareStatement(sqlRilp);
		System.out.println("Rilasci Programmati "+sqlRilp+" : "+sOrgId);
		ps.setString(1, sOrgId);
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
			String noc = rs.getString("URL_NOC");
			if(noc==null) {
				String[] attach = {rs.getString("ABSOLUTE_NOME_FILE")};
				unaPec.setAttach(attach);
			}else {
				String[] attach = {rs.getString("ABSOLUTE_NOME_FILE"),rs.getString("URL_NOC")};
				unaPec.setAttach(attach);
			}
			String altriAllegatiId = rs.getString("FND_LOBS_ID");
			if(altriAllegatiId!=null) {
				System.out.println("Gestione Allegati aggiuntivi : FND_LOBS: " + altriAllegatiId);
				String[] altriAllegati = allegatiDaBlob(altriAllegatiId);
				String[] attach = new String[unaPec.getAttach().length + altriAllegati.length];
				int i=0;
				for(i=0; i< unaPec.getAttach().length;i++ ) {
					attach[i]=unaPec.getAttach()[i];
				}
				for(int k=0; k < altriAllegati.length; k++) {
					attach[i+k]=altriAllegati[k];
				}
				unaPec.setAttach(attach);
			}
			lista.add(unaPec);
			
		}
		rs.close();
		ps.close();
		return lista;
	}

	private void removeFile(String fileName) {
		 try {
	            Files.delete(Paths.get(fileName));
	        } catch (IOException e) {
	        	exitStatus=101;
	        	exitMsg= fileName + " - " + e.getMessage();
	        	
	            e.printStackTrace();
	        }		
	}
	private void copyFile(String fileNameSrc, String fileNameDst) {
		 try {
	            Files.copy(Paths.get(fileNameSrc),Paths.get(fileNameDst));
	        } catch (IOException e) {
	        	exitStatus=102;
	        	exitMsg= "Copia del file " + fileNameSrc + " al file " + fileNameDst +" non riuscita : " + e.getMessage();
	        	
	            e.printStackTrace();
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

	private boolean chekPdf(PecBean unaPec) {
		boolean retval=true;

//		File file = new File(folderSrc + srcFile);
//Patch signed2
		File file = new File(unaPec.getAttach()[0]);
		String checkWord = chc.getSingleValue("select max(nvl(CHECK_WORD,'A')) CHECK_WORD  from XXH.XXH_FIRMA_ARSS_CONFIG where org_id = :1", sOrgId);
        PDDocument pdfDocument = new PDDocument();
        try {
        	pdfDocument = PDDocument.load(file);
		} catch (IOException e1) {
			retval=false;
			e1.printStackTrace();
		}
        if(retval) {
			int numPag = pdfDocument.getPages().getCount();
			try {
		 		String fileName = file.getName();  
		 		
		        org.apache.pdfbox.text.PDFTextStripper reader = new org.apache.pdfbox.text.PDFTextStripper();
				String pageText = reader.getText(pdfDocument);
					if(pageText.indexOf(checkWord)>0) {
					    retval=true;
					}else {
					    retval=false;
					}
					
			} catch (Exception e) {
				retval=false;
			}
		 	 try {
		 		pdfDocument.close();
			} catch (IOException e) {
				System.out.println("Exception pdfDocument.close");
			}
        }
		return retval;
	}

	String[] allegatiDaBlob(
		    String idList 
		) throws Exception {
			String query = "select FILE_ID, FILE_NAME, FILE_CONTENT_TYPE, FILE_DATA from fnd_lobs where file_id in ("+idList+")";
			String dirTop = System.getenv("XXH_TOP");
			String nomeFile = dirTop+ File.separator + "attach"+File.separator +"InvioOrdini"+File.separator ;
			String nomiFile = "";
			System.out.println("nomeFile : " +nomeFile);

		    try (
		        PreparedStatement statement = chc.getConnection().prepareStatement(query);
		    ) {
		        try (
		            ResultSet resultSet = statement.executeQuery(); 
		        ) {
		            while (resultSet.next()) {
		            	String newAttach = nomeFile  + resultSet.getString("FILE_ID") +"_"+resultSet.getString("FILE_NAME");
			            FileOutputStream fileOutputStream = new FileOutputStream(new File(newAttach));
		                InputStream input = resultSet.getBinaryStream("FILE_DATA");
		                byte[] buffer = new byte[1024];
		                int bytesRead;
		                while ((bytesRead = input.read(buffer)) > 0) {
		                    fileOutputStream.write(buffer, 0, bytesRead);
		                }
		                nomiFile +=newAttach+";";
		            }
		            return nomiFile.split(";");
		        }
		    }
		}	
	
	
}
