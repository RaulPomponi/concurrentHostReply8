package oracle.apps.fnd.cp.xxh_fs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import oracle.apps.fnd.cp.request.CpContext;
import oracle.apps.fnd.cp.request.JavaConcurrentProgram;
import oracle.apps.fnd.cp.request.LogFile;

public class Xxh_FS_Invia_Documenti implements JavaConcurrentProgram {
	public void runProgram(CpContext ctx) {
		LogFile log = ctx.getLogFile();
		String path_files = "";
		String oggetto = "";
		String destinatari = "";
		String corpo_mail = "";
		String[] attach = new String[]{";"};
		String pdf_file = "";
		String ordine = "";
		String errore = "";
		Connection conn = ctx.getJDBCConnection();
		ctx.getReqCompletion().setCompletion(0, "");
		String path_files_fax = "/data2/PROD/interfacce/fax_server/in/";
		log.writeln("Dentro runProgram", 0);

		try {
			try {
				Statement stmt = conn.createStatement();
				String sql = "UPDATE XXH_ORDINI_PDF_DA_FIRMARE SET errore= null where selezione = 'Y'   and  nvl(INVIO_DISABILITATO,'N') = 'N' ";
				stmt.executeUpdate(sql);
				conn.commit();
				stmt.close();
			} catch (Exception var49) {
				log.writeln("Errore update tabella XXH_ORDINI_PDF_DA_FIRMARE:" + var49.getMessage(), 0);
				errore = "Errore update tabella XXH_ORDINI_PDF_DA_FIRMARE:" + var49.getMessage();
				throw new Xxh_fs_Exception();
			}

			Xxh_fs_mail.set_appl(ctx);
			String NotifMethod = "";
			String EmailForn = "";
			String FaxForn = "";
			String buyerId = "";
			String stato = "";
			String IdRiga = "";
			String Mail = "";
			String orderType = "";
			String origine = "";
			path_files = ctx.getProfileStore().getProfile("XXH_FAX_PATH");

			try {
				String Ordini = "select 'EMAIL' modalita,  cf.EMAIL_ADDRESS  mail, cf.fax, opf.rowid, opf.NOME_FILE , opf.ID_ORDINE, opf.status,   r.agent_id  agent_id, ph.TYPE_LOOKUP_CODE , nvl(opf.origine,'O') origine from XXH_ORDINI_PDF_DA_FIRMARE opf, po_headers_all ph , po_releases_all r, po_vendor_contacts cf  where (selezione = 'Y' or selezione_invio='Y') and nvl(INVIO_DISABILITATO,'N') = 'N'   and ph.po_header_id = r.po_header_id  and opf.id_ordine =   r.po_release_id AND NVL(opf.origine,'O') = 'R'   AND ph.vendor_contact_id = cf.vendor_contact_id AND PH.VENDOR_SITE_ID = CF.VENDOR_SITE_ID  union select 'EMAIL' modalita,  cf.EMAIL_ADDRESS  mail, cf.fax, opf.rowid, opf.NOME_FILE , opf.ID_ORDINE, opf.status,  ph.agent_id  agent_id, ph.TYPE_LOOKUP_CODE , nvl(opf.origine,'O') origine from XXH_ORDINI_PDF_DA_FIRMARE opf, po_headers_all ph ,  po_vendor_contacts cf  where (selezione = 'Y' or selezione_invio='Y') and nvl(INVIO_DISABILITATO,'N') = 'N'   and opf.id_ordine =   ph.po_header_id    and  nvl(opf.origine,'O') = 'O'  AND ph.vendor_contact_id = cf.vendor_contact_id AND PH.VENDOR_SITE_ID=CF.VENDOR_SITE_ID";
				PreparedStatement POrdini = conn.prepareStatement(Ordini);
				ResultSet ROrdini = POrdini.executeQuery();

				while (ROrdini.next()) {
					log.writeln("Dentro while ordini", 0);
					errore = "";
					stato = "I";
					IdRiga = ROrdini.getString("rowid");
					NotifMethod = ROrdini.getString("modalita");
					EmailForn = ROrdini.getString("mail");
					FaxForn = ROrdini.getString("fax");
					ordine = ROrdini.getString("ID_ORDINE");
					buyerId = ROrdini.getString("agent_id");
					orderType = ROrdini.getString("TYPE_LOOKUP_CODE");
					origine = ROrdini.getString("origine");
					if (NotifMethod == null) {
						errore = "Metodo di notifica non compilato";
						stato = "E";
					} else {
						EmailForn = ROrdini.getString("mail");
						FaxForn = ROrdini.getString("fax");
						if (NotifMethod.equals("PRINT")) {
							errore = "Metodo di notifica non valido";
							stato = "E";
						}

						if (NotifMethod.equals("FAX") && FaxForn == null) {
							errore = "Numero FAX non compilato";
							stato = "E";
						}

						if (NotifMethod.equals("EMAIL") && EmailForn == null) {
							errore = "Destinatario MAIL non compilato";
							stato = "E";
						}

						if (NotifMethod.equals("PEC") && EmailForn == null) {
							errore = "Destinatario MAIL PEC non compilato";
							stato = "E";
						}
					}

					log.writeln("orderType " + orderType, 0);
					if (stato.equals("I")) {
						if (orderType.equalsIgnoreCase("PLANNED") && origine.equalsIgnoreCase("R")) {
							Mail = "  SELECT KEY_PROGRAMMA,        a.mittente_mail MITTENTE_MAIL,        DECODE (key_programma, 'PEC', a.pwd_mail, a.pwd_mail) PWD_MAIL,        SMPT,        XXH_FAX_PKG.GET_MAIL_OGGETTO (OGGETTO, '"
									+ ordine + "') OGGETTO," + "        XXH_FAX_PKG.GET_MAIL_CORPO (TESTO1, TESTO2,  '"
									+ ordine + "') CORPO," + "        DESTINATARIO_MAIL,"
									+ "        b.attribute11 ENTE_GESTORE"
									+ "   FROM XXH_GESTIONE_MAIL a, po_headers_all b, po_releases_all c"
									+ "   WHERE a.key_programma = '" + NotifMethod + "'"
									+ "        AND c.po_release_id =  '" + ordine + "'"
									+ "        AND a.ente_gestore = b.attribute11"
									+ "        AND b.po_header_id = c.po_header_id" + "        AND a.tipo_doc = 'R'";
						} else {
							Mail = "select KEY_PROGRAMMA, a.mittente_mail MITTENTE_MAIL, decode(key_programma,'PEC',p.pwd, a.pwd_mail) PWD_MAIL, SMPT, XXH_FAX_PKG.GET_MAIL_OGGETTO(OGGETTO, '"
									+ ordine + "') OGGETTO, " + "XXH_FAX_PKG.GET_MAIL_CORPO(TESTO1, TESTO2, '" + ordine
									+ "') CORPO, DESTINATARIO_MAIL "
									+ "from XXH_GESTIONE_MAIL a, fnd_user u,xxh_gestione_pec p  "
									+ "where key_programma = '" + NotifMethod + "' " + "and u.employee_id = '" + buyerId
									+ "' " + "and u.fax=p.mittente(+) and a.mittente_mail =    u.fax";
						}

						log.writeln("Mail sql :" + Mail, 0);
						PreparedStatement PMail = conn.prepareStatement(Mail);
						ResultSet RMail = PMail.executeQuery();
						log.writeln("NotifMethod " + NotifMethod, 0);
						log.writeln("buyerId " + buyerId, 0);
						log.writeln("ordine " + ordine, 0);
						if (RMail.next()) {
							log.writeln("Dentro while mail", 0);
							if (RMail.getString("MITTENTE_MAIL") == null) {
								errore = "Mittente MAIL non compilato";
								stato = "E";
							}

							log.writeln("stato prima :" + stato, 0);
							if (stato.equals("I")) {
								Xxh_fs_mail.set_mittente(RMail.getString("MITTENTE_MAIL"), RMail.getString("PWD_MAIL"));
								Xxh_fs_mail.set_smpt(RMail.getString("SMPT"));
								log.writeln("Mittente :" + RMail.getString("MITTENTE_MAIL"), 0);
								log.writeln("PWD :" + RMail.getString("PWD_MAIL"), 0);
								log.writeln("SMPT :" + RMail.getString("SMPT"), 0);
								if (NotifMethod.equals("FAX")) {
									destinatari = RMail.getString("DESTINATARIO_MAIL");
								} else {
									destinatari = EmailForn;
								}

								oggetto = RMail.getString("OGGETTO");
								corpo_mail = RMail.getString("CORPO");
								if (NotifMethod.equals("PEC")) {
									pdf_file = path_files + "/out/" + ROrdini.getString("NOME_FILE");
								} else if (NotifMethod.equals("FAX")) {
									if (ROrdini.getString("NOME_FILE").endsWith(".p7m")) {
										pdf_file = path_files_fax + ROrdini.getString("NOME_FILE")
												.replaceAll(".p7m", "").replaceAll(".pdf", "_1.pdf");
									} else {
										pdf_file = path_files_fax + ROrdini.getString("NOME_FILE");
									}
								} else if (ROrdini.getString("NOME_FILE").endsWith(".p7m")) {
									pdf_file = path_files + "/out/" + ROrdini.getString("NOME_FILE");
								} else {
									pdf_file = path_files + "/in/" + ROrdini.getString("NOME_FILE");
								}

								attach[0] = pdf_file;

								try {
									if (Xxh_fs_mail.send_mail(destinatari, oggetto, corpo_mail, attach).equals("KO")) {
										throw new Xxh_fs_Exception();
									}
								} catch (Exception var50) {
									Exception e = var50;
									errore = "Errore invio " + NotifMethod;
									stato = "E";
									log.writeln("Errore invio mail :" + var50.toString(), 0);
									DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:m:s");
									log.write("Data errore " + formatter.format(new Date()), 0);
									log.writeln("Mittente :" + RMail.getString("MITTENTE_MAIL"), 0);
									log.writeln("destinatari :" + destinatari, 0);

									for (int i = 0; i < e.getStackTrace().length; ++i) {
										log.writeln("Stack Errore :" + e.getStackTrace()[i].toString(), 0);
									}
								}
							}
						}

						RMail.close();
						PMail.close();
					}

					log.writeln("Path Files :" + path_files, 0);
					log.writeln("Destinatari :" + destinatari, 0);
					log.writeln("Stato :" + stato, 0);
					String path = "/data2/PROD/apps/apps_st/comn/java/classes/oracle/apps/fnd/cp/xxh_fs/";
					File file = new File(path + "send_order.txt");
					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException var48) {
							var48.printStackTrace();
						}
					}

					String data = "\n-------------------\nROW_ID: " + IdRiga + "\n" + "STATO: " + stato + "\n"
							+ "---------------------\n\n";

					try {
						FileWriter fileWritter = new FileWriter(file.getName(), true);
						BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
						bufferWritter.write(data);
						bufferWritter.close();
					} catch (IOException var47) {
						var47.printStackTrace();
					}

					try {
						String plsql = "declare begin apps.XXH_FAX_PKG.UPDATE_INVIO('" + stato + "','" + IdRiga + "','"
								+ errore + "'); end;";
						CallableStatement plProc = conn.prepareCall(plsql);
						plProc.execute();
						conn.commit();
					} catch (Exception var46) {
						log.writeln("Errore update XXH_ORDINI_PDF_DA_FIRMARE :" + var46.toString(), 0);
					}
				}

				ROrdini.close();
				POrdini.close();
			} catch (Exception var51) {
				log.writeln("Errore lettura XXH_ORDINI_PDF_DA_FIRMARE :" + var51.toString(), 0);
				throw new Xxh_fs_Exception();
			}
		} catch (Xxh_fs_Exception var52) {
			ctx.getReqCompletion().setCompletion(2, "");
		}

	}
}