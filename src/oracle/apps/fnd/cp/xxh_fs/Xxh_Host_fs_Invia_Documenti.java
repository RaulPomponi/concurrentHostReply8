package oracle.apps.fnd.cp.xxh_fs;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import reply.healthy.base.erp.cp.ConcurrentProgramBase;
/*
 * Concurrent SANTER: Invio Ordini Firmati
 * Eseguibile XXH_FS_INVIA_DOCUMENTI_HOST
 * Produzione CONCURRENT_PROGRAM_ID=336436
 * 
 */
public class Xxh_Host_fs_Invia_Documenti extends ConcurrentProgramBase {
	String path_files = "";

	protected void doService() {
		String oggetto = "";
		String destinatari = "";
		String corpo_mail = "";
		String[] attach = new String[]{";"};
		String[] attachNOC = new String[]{";", ";"};
		String pdf_file = "";
		String ordine = "";
		String errore = "";
		this.updateExitCode(0);
		this.log.debug("Dentro doService V 1.0", new Object[0]);

		try {
			String EmailForn;
			try {
				setOrg();//MOAC
				Statement stmt = this.conn.createStatement();
				EmailForn = "UPDATE XXH_ORDINI_PDF_DA_FIRMARE  SET ERRORE = NULL  WHERE SELEZIONE_INVIO = 'Y'  AND NVL(INVIO_DISABILITATO,'N') = 'N' ";
				stmt.executeUpdate(EmailForn);
				this.conn.commit();
				stmt.close();
			} catch (Exception var27) {
				errore = "Errore update tabella XXH_ORDINI_PDF_DA_FIRMARE:" + var27.getMessage();
				this.log.error(errore, var27);
				throw new Xxh_fs_Exception();
			}

			String NotifMethod = "";
			EmailForn = "";
			String FaxForn = "";
			String buyerId = "";
			String stato = "";
			String IdRiga = "";
			String Mail = "";
			String orderType = "";
			String origine = "";

			try {
				String Ordini = "{call XXH_FIX_ORDINI_PKG.GET_DOCS_TO_SEND_BY_PEC(?)}"; // aggiungere il parametro ORG_ID
				CallableStatement POrdini = this.conn.prepareCall(Ordini);
				POrdini.registerOutParameter(1, -10);
				POrdini.executeUpdate();
				ResultSet ROrdini = (ResultSet) POrdini.getObject(1);

				while (ROrdini.next()) {
					this.log.debug("Dentro while ordini", new Object[0]);
					errore = "";
					stato = "I";
					IdRiga = ROrdini.getString("ROWID");
					NotifMethod = ROrdini.getString("MODALITA");
					EmailForn = ROrdini.getString("MAIL");
					FaxForn = ROrdini.getString("FAX");
					ordine = ROrdini.getString("ID_ORDINE");
					buyerId = ROrdini.getString("AGENT_ID");
					orderType = ROrdini.getString("TYPE_LOOKUP_CODE");
					origine = ROrdini.getString("ORIGINE");
					this.log.debug(NotifMethod.toString() + " EmailForn " + EmailForn, new Object[0]);

// Questa IF non ha senso 	NotifMethod è sempre EMAIL				
					if (NotifMethod == null) {
						errore = "Metodo di notifica non compilato";
						stato = "E";
						this.log.debug("errore " + errore, new Object[0]);
					} else {
						EmailForn = ROrdini.getString("MAIL");
						FaxForn = ROrdini.getString("FAX");
						if (NotifMethod.equals("PRINT")) {
							errore = "Metodo di notifica non valido";
							stato = "E";
							this.log.debug("errore " + errore, new Object[0]);
						}

						if (NotifMethod.equals("FAX") && FaxForn == null) {
							errore = "Numero FAX non compilato";
							stato = "E";
							this.log.debug("errore " + errore, new Object[0]);
						}

						if (NotifMethod.equals("EMAIL") && EmailForn == null) {
							errore = "Destinatario MAIL non compilato";
							stato = "E";
							this.log.debug("errore " + errore, new Object[0]);
						}

						if (NotifMethod.equals("PEC") && EmailForn == null) {
							errore = "Destinatario MAIL PEC non compilato";
							stato = "E";
							this.log.debug("errore " + errore, new Object[0]);
						}
					}
// Fine if senza senso					
					
					this.log.debug("orderType " + orderType, new Object[0]);
					this.log.debug("NotifMethod Error " + errore, new Object[0]);
					if (stato.equals("I")) {
						CallableStatement PMail = null;
						String sRecipientInfo="XXH_FIX_ORDINI_PKG.GET_RECIPIENT_INFO(";
						this.log.debug("origine " + origine, new Object[0]);
						if (origine.equalsIgnoreCase("R")) {
							sRecipientInfo+=ordine+", "+NotifMethod+", "+buyerId+", R)";
							Mail = "{call XXH_FIX_ORDINI_PKG.GET_RECIPIENT_INFO(?,?,?,?,?)}";
							PMail = this.conn.prepareCall(Mail);
							PMail.setString(1, ordine);
							PMail.setString(2, NotifMethod);
							PMail.setString(3, buyerId);
							PMail.setString(4, "R");
							PMail.registerOutParameter(5, -10);
						} else {
							sRecipientInfo+=ordine+", "+NotifMethod+", "+buyerId+", NULL)";
							Mail = "{call XXH_FIX_ORDINI_PKG.GET_RECIPIENT_INFO(?,?,?,?,?)}";
							PMail = this.conn.prepareCall(Mail);
							PMail.setString(1, ordine);
							PMail.setString(2, NotifMethod);
							PMail.setString(3, buyerId);
							PMail.setString(4, (String) null);
							PMail.registerOutParameter(5, -10);
						}

						this.log.debug("Mail sql :" + sRecipientInfo, new Object[0]);
						PMail.executeUpdate();
						ResultSet RMail = (ResultSet) PMail.getObject(5);
						this.log.debug("NotifMethod " + NotifMethod, new Object[0]);
						this.log.debug("buyerId " + buyerId, new Object[0]);
						this.log.debug("ordine " + ordine, new Object[0]);

						while (RMail.next()) {
							this.log.debug("Dentro while mail", new Object[0]);
							if (RMail.getString("MITTENTE_MAIL") == null) {
								errore = "Mittente MAIL non compilato";
								stato = "E";
							}

							this.log.debug("stato prima :" + stato, new Object[0]);
							if (stato.equals("I")) {
								Xxh_fs_mail.set_mittente(RMail.getString("MITTENTE_MAIL"), RMail.getString("PWD_MAIL"));
								Xxh_fs_mail.set_smpt(RMail.getString("SMPT"));
								if (RMail.getString("DESTINATARIO_MAIL") != null) {
									destinatari = RMail.getString("DESTINATARIO_MAIL");
								} else {
									destinatari = EmailForn;
								}

								this.log.debug("Mittente :" + RMail.getString("MITTENTE_MAIL"), new Object[0]);
								this.log.debug("SMPT :" + RMail.getString("SMPT"), new Object[0]);
								this.log.debug("To :" + destinatari, new Object[0]);
								oggetto = RMail.getString("OGGETTO");
								corpo_mail = RMail.getString("CORPO");
								String esitoMail;
								if (NotifMethod.equals("PEC")) {
									System.out.println("NotifMethod = PEC");
									pdf_file = String.valueOf(this.path_files) + "out/"
											+ ROrdini.getString("NOME_FILE");
								} else if (NotifMethod.equals("FAX")) {
									System.out.println("NotifMethod = FAX");
									esitoMail = "/data2/PROD/interfacce/fax_server/in/";
									if (ROrdini.getString("NOME_FILE").endsWith(".p7m")) {
										pdf_file = String.valueOf(esitoMail) + ROrdini.getString("NOME_FILE")
												.replaceAll(".p7m", "").replaceAll(".pdf", "_1.pdf");
									} else {
										pdf_file = String.valueOf(esitoMail) + ROrdini.getString("NOME_FILE");
									}
								} else if (ROrdini.getString("NOME_FILE").endsWith(".p7m")) {
									System.out.println("NotifMethod = p7m");
									pdf_file = String.valueOf(this.path_files) + "out/"
											+ ROrdini.getString("NOME_FILE");
								} else {
									System.out.println("NotifMethod = in");
									pdf_file = String.valueOf(this.path_files) + "in/"
											+ ROrdini.getString("NOME_FILE");
								}
								attach[0] = pdf_file;
								attachNOC = new String[]{";", ";"};
								System.out.println("attach" + attach[0]);
								this.log.debug("tipo =" + ordine, new Object[]{this});
								if ("O".equals(origine)) {
									esitoMail = this.urlNOC(ordine);
									this.log.debug("sUrlNOC =" + esitoMail, new Object[]{this});
									if (esitoMail != null) {
										this.log.debug("Sto allegando il NOC" + this.path_files + "NOC.docx",
												new Object[0]);
										attachNOC[0] = attach[0];
										attachNOC[1] = String.valueOf(this.path_files) + "NOC.docx";
									}
								}

								try {
									esitoMail = "KO";
									if (!attachNOC[1].equals(";")) {
										System.out.println("Invio con NOC");
										esitoMail = Xxh_fs_mail.send_mail(destinatari, oggetto, corpo_mail, attachNOC);
									} else {
										System.out.println("Invio senza NOC");
										esitoMail = Xxh_fs_mail.send_mail(destinatari, oggetto, corpo_mail, attach);
									}
									this.log.debug("esitoMail " + esitoMail, new Object[0]);

									if (esitoMail.equals("KO")) {
										throw new Xxh_fs_Exception();
									}
								} catch (Exception var28) {
									Exception e = var28;
									errore = "Errore invio " + NotifMethod;
									stato = "E";
									this.log.error("Errore invio mail :" + var28.toString(), var28);
									DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:m:s");
									this.log.error("Data errore " + formatter.format(new Date()), new Object[0]);
									this.log.error("Mittente :" + RMail.getString("MITTENTE_MAIL"), new Object[0]);
									this.log.error("destinatari :" + destinatari, new Object[0]);

									for (int i = 0; i < e.getStackTrace().length; ++i) {
										this.log.error("Stack Errore :" + e.getStackTrace()[i].toString(),
												new Object[0]);
									}

									this.updateExitCode(2);
								}
							}
						}

						RMail.close();
						PMail.close();
					}

					this.log.debug("Path Files :" + this.path_files, new Object[0]);
					this.log.debug("Destinatari :" + destinatari, new Object[0]);
					this.log.debug("Stato :" + stato, new Object[]{0});

					try {
						String plsql = "declare begin apps.XXH_FAX_PKG.UPDATE_INVIO('" + stato + "','" + IdRiga + "','"
								+ errore + "'); end;";
						CallableStatement plProc = this.conn.prepareCall(plsql);
						plProc.execute();
					} catch (Exception var26) {
						var26.printStackTrace();
						this.log.error("Errore update XXH_ORDINI_PDF_DA_FIRMARE :" + var26.toString(), var26);
					}
				}

				ROrdini.close();
				POrdini.close();
			} catch (Exception var29) {
				var29.printStackTrace();
				this.log.error("Errore lettura XXH_ORDINI_PDF_DA_FIRMARE :" + var29.toString(), var29);
				throw new Xxh_fs_Exception();
			}
		} catch (Xxh_fs_Exception var30) {
			this.log.error("Errore Xxh_fs_Exception errore:" + var30.getMessage(), var30);
			this.updateExitCode(2);
		}

	}

	protected void getParameters() {
		this.path_files = this.getParam("P_PATH_PDF", 8);
	}

	public static void main(String[] args) {
		Xxh_Host_fs_Invia_Documenti xxhInviaDocumenti = new Xxh_Host_fs_Invia_Documenti();
		xxhInviaDocumenti.runProgramHost(args);
	}

	private String urlNOC(String numOrdine) {
		System.out.println("Cerco il NOC per "+numOrdine);
		String retVal = null;
		String sql = "select doc.url url from fnd_documents doc, fnd_attached_documents att, fnd_document_categories_tl cat, po_headers_all pha where 1=1 and cat.user_name='NOC' and cat.language='I' and att.CATEGORY_ID=cat.CATEGORY_ID and att.pk1_value=pha.po_header_id and doc.DOCUMENT_ID = att.DOCUMENT_ID and pha.po_header_id=:1";

		try {
			PreparedStatement pst = this.conn.prepareStatement(sql);
			pst.setString(1, numOrdine);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				retVal = rs.getString(1);
			}

			rs.close();
			pst.close();
		} catch (SQLException var6) {
			this.log.warn("Errore nella ricerca del NOC - " + var6.getMessage(), new Object[]{this});
		}

		return retVal;
	}
	public void setOrg() throws SQLException {
		String org="81";
		String sqlOrg = "select argument5 from fnd_concurrent_requests where REQUEST_ID=" + concurrentId;
		ResultSet rs=null;
		PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement(sqlOrg);
        rs=pstmt.executeQuery();
        if(rs.next()) org = rs.getString(1);
        rs.close();
        pstmt.close();
		
		String sql = "begin fnd_client_info.set_org_context("+org+");  mo_global.set_policy_context ('S', "+org+"); end;";
		CallableStatement stmt;
		stmt = conn.prepareCall(sql);
        stmt.executeUpdate();
        stmt.close();
		
	}
}