package oracle.apps.fnd.cp.xxh_fs;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
//import oracle.apps.fnd.cp.xxh_fs.ReadMail.1;
import reply.healthy.base.erp.cp.ConcurrentProgramBase;
//XXH: Ricezione consegna Ordini/Rilasci
public class ReadMailOLD extends ConcurrentProgramBase {
	public static String pathFile = "";
	public static String reqID = "";
	public  int orgID = 81;
	public static String USERNAME = "";
	public static String PASSWORD = "";
	public static String pop3 = "";
	public static String portaPop3 = "";
	public static String stringGen = "";
	public int retValue = 0;
	public static String retString = "";
	public static String pDataDal = "";
	public static String categoryID = "";

	protected void doService() throws Exception {
      System.out.println("--receiveEmail--");
      String errore = "";

      String infoMail;
      PreparedStatement PinfoMail;
      ResultSet RinfoMail;
      try {
    	  setOrg() ;
         infoMail = "Select category_id from  FND_DOCUMENT_CATEGORIES where attribute1 ='MAIL_ORDINI'";
         PinfoMail = this.conn.prepareStatement(infoMail);
         RinfoMail = PinfoMail.executeQuery();

         while(true) {
            if (!RinfoMail.next()) {
               RinfoMail.close();
               PinfoMail.close();
               break;
            }

            errore = null;
            categoryID = RinfoMail.getString("category_id");
         }
      } catch (Exception var194) {
         this.log.debug("Errore category_id:" + var194.toString(), new Object[]{0});
         throw new Xxh_fs_Exception();
      }

      try {
         infoMail = "Select distinct replace(replace(mittente_mail,'-nomail-',null),'-no-mail-') USER_NAME,PWD_MAIL,POP3,PORTA_POP3 from xxh_gestione_mail where pop3 is not null and porta_pop3 is not null ";
         PinfoMail = this.conn.prepareStatement(infoMail);
         RinfoMail = PinfoMail.executeQuery();

         while(RinfoMail.next()) {
            try {
               errore = null;
               USERNAME = RinfoMail.getString("USER_NAME");
               PASSWORD = RinfoMail.getString("PWD_MAIL");
               pop3 = RinfoMail.getString("POP3");
               portaPop3 = RinfoMail.getString("PORTA_POP3");
               this.log.debug("Dopo recupero info mail da leggere per " + USERNAME, new Object[0]);
               Properties props = new Properties();
               props.put("mail.pop3.debug", "true");
               props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
               props.put("mail.pop3.socketFactory.fallback", "true");
               props.put("mail.pop3.socketFactory.port", portaPop3);
               props.put("mail.pop3.disablecapa", "true");
               props.put("mail.pop3.port", portaPop3);
               props.put("mail.pop3.host", pop3);
               props.put("mail.pop3.user", USERNAME);
               props.put("mail.store.protocol", "pop3");
           		Authenticator auth = new Authenticator() {
	        		// override the getPasswordAuthentication method
	        		protected PasswordAuthentication getPasswordAuthentication() {
	        			return new PasswordAuthentication(USERNAME, PASSWORD);
	        		}
           		};
               Session session = Session.getDefaultInstance(props, auth);
               Store store = session.getStore("pop3s");
               System.out.println("prima di connect");
               store.connect(pop3, USERNAME, PASSWORD);
               System.out.println("dopo connect");
               Folder inbox = store.getFolder("INBOX");
               inbox.open(1);
               Message[] messages = inbox.getMessages();
               System.out.println("Numero -->" + messages.length);
               int maxMex = messages.length;
               SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
               Date dateDal = formatter1.parse(pDataDal);
               System.out.println("###dateDal -->" + dateDal);
               int maxMailAfterDate = 0;

               try {
                  String infoOggetto = "Select distinct Oggetto OGGETTO from xxh_gestione_mail  where pop3 is not null\tand porta_pop3 is not null and replace(replace(mittente_mail,'-nomail-',null),'-no-mail-')  = ? and pwd_mail = ?";
                  PreparedStatement PinfoOggetto = this.conn.prepareStatement(infoOggetto, 1004, 1008);
                  PinfoOggetto.setString(1, USERNAME);
                  PinfoOggetto.setString(2, PASSWORD);
                  ResultSet RinfoOggetto = PinfoOggetto.executeQuery();

                  label2404:
                  for(int i = 0; i < messages.length; ++i) {
                     int tipoNum = 0;
                     String tipoPosta = "";
                     String dataRicezione = "";
                     int readMailOrdineId = 0;
                     System.out.println("i=" + i);
                     Message mess = messages[maxMex - i - 1];

                     while(RinfoOggetto.next()) {
                        stringGen = RinfoOggetto.getString("OGGETTO");
                        if (mess.getSubject().contains("ACCETTAZIONE: " + stringGen.substring(0, stringGen.indexOf("&NUM_ORDINE") - 1)) || mess.getSubject().contains("CONSEGNA: " + stringGen.substring(0, stringGen.indexOf("&NUM_ORDINE") - 1))) {
                           OutputStream os = null;
                           ByteArrayOutputStream baos = null;
                           os = new ByteArrayOutputStream();
                           mess.writeTo(os);
                           ByteArrayOutputStream bos = (ByteArrayOutputStream)os;
                           byte[] value = bos.toByteArray();
                           if (os.toString().contains("(\"posta certificata\")") && mess.getSubject().contains("ACCETTAZIONE:")) {
                              System.out.println("Posta Certificata solo inviata" + mess.getSubject() + "\n");
                              tipoNum = 1;
                              tipoPosta = "Certificata";
                           }

                           if (os.toString().contains("(\"posta ordinaria\")") && mess.getSubject().contains("ACCETTAZIONE:")) {
                              System.out.println("Posta Ordinaria" + mess.getSubject() + "\n");
                              tipoNum = 1;
                              tipoPosta = "Ordinaria";
                           }

                           if (mess.getSubject().contains("CONSEGNA:")) {
                              System.out.println("Posta Certificata Consegnata" + mess.getSubject() + "\n");
                              tipoNum = 2;
                              tipoPosta = "Certificata";
                           }

                           Enumeration e = mess.getAllHeaders();

                           label2398:
                           while(true) {
                              Header header;
                              do {
                                 if (!e.hasMoreElements()) {
                                    SimpleDateFormat formatter2 = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
                                    Date dateRicezioneConv = formatter2.parse(dataRicezione);
                                    System.out.println("dateRicezioneConv =" + dateRicezioneConv);
                                    if (dateRicezioneConv.compareTo(dateDal) >= 0) {
                                       System.out.println("Mail da elaborare");

                                       String idRead;
                                       try {
                                          idRead = "{? = call apps.xxh_read_mail_po_pkg.get_mail_already_processed(?,?,?)}";
                                          CallableStatement PAOrd = this.conn.prepareCall(idRead);
                                          System.out.println("call apps.xxh_read_mail_po_pkg.get_mail_already_processed");
                                          System.out.println("mess.getSubject() = " +mess.getSubject());
                                          System.out.println("tipoNum = "+String.valueOf(tipoNum));
                                          System.out.println("orgID = "+String.valueOf(orgID));
                                          PAOrd.setString(2, mess.getSubject() + ".eml");
                                          PAOrd.setString(3, String.valueOf(tipoNum));
                                          PAOrd.setString(4, String.valueOf(orgID));
                                          PAOrd.registerOutParameter(1, 2);
                                          PAOrd.execute();
                                          this.retValue = PAOrd.getInt(1);
                                          PAOrd.close();
                                          System.out.println("retValue =" + this.retValue);
                                       } catch (Exception var178) {
                                          this.log.debug("Errore apps.xxh_read_mail_po_pkg.get_mail_already_processed:" + var178.toString(), new Object[]{0});
                                          throw new Xxh_fs_Exception();
                                       }

                                       if (this.retValue == 0) {
                                          System.out.println("Select ID");

                                          try {
                                             idRead = "Select  xxh_read_mail_ordini_s.nextval ID from dual";
                                             PreparedStatement PidRead = this.conn.prepareStatement(idRead);
                                             ResultSet RidRead = PidRead.executeQuery();

                                             while(true) {
                                                if (!RidRead.next()) {
                                                   RidRead.close();
                                                   PidRead.close();
                                                   break;
                                                }

                                                errore = null;
                                                readMailOrdineId = RidRead.getInt("ID");
                                             }
                                          } catch (Exception var183) {
                                             this.log.debug("Errore sequenza  xxh_read_mail_ordini_s:" + var183.toString(), new Object[]{0});
                                             throw new Xxh_fs_Exception();
                                          }

                                          System.out.println("pstmt Insert ");
                                          Format formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                          String dateRicezioneConvString = formatter.format(dateRicezioneConv);
                                          Throwable var200 = null;
                                          CallableStatement PStrAtt = null;

                                          PreparedStatement pstmt;
                                          try {
                                             pstmt = this.conn.prepareStatement(String.format("INSERT INTO xxh_read_mail_ordini_all (READ_MAIL_ORDINE_ID, FILE_NAME, FILE_DATA, DATA_RICEZIONE_MAIL, DATA_ELABORAZIONE, ORG_ID, REQUEST_ID, TIPO_NUM, TIPO_POSTA) VALUES (?,?,EMPTY_BLOB(),?,to_char(sysdate,'DD-MON-RRRR'),?,?,?,?)"));

                                             try {
                                                pstmt.setInt(1, readMailOrdineId);
                                                pstmt.setString(2, mess.getSubject() + ".eml");
                                                pstmt.setString(3, dateRicezioneConvString);
                                                pstmt.setInt(4, orgID);
                                                pstmt.setInt(5, Integer.parseInt(reqID));
                                                pstmt.setInt(6, tipoNum);
                                                pstmt.setString(7, tipoPosta);
                                                pstmt.executeUpdate();
                                             } finally {
                                                if (pstmt != null) {
                                                   pstmt.close();
                                                }

                                             }
                                          } catch (Throwable var182) {
                                             if (var200 == null) {
                                                var200 = var182;
                                             } else if (var200 != var182) {
                                                var200.addSuppressed(var182);
                                             }

       //                                      throw var200;
                                          }

                                          System.out.println("Record inserted");
                                          var200 = null;
                                          PStrAtt = null;

                                          try {
                                             pstmt = this.conn.prepareStatement(String.format("SELECT file_data FROM xxh_read_mail_ordini_all WHERE read_mail_ordine_id = ? FOR UPDATE"));

                                             try {
                                                pstmt.setInt(1, readMailOrdineId);
                                                Throwable var35 = null;
                                                Object var36 = null;

                                                try {
                                                   ResultSet rset = pstmt.executeQuery();

                                                   try {
                                                      while(rset.next()) {
                                                         Blob blobU = rset.getBlob(1);
                                                         Throwable var39 = null;
                                                         Object var40 = null;

                                                         try {
                                                            BufferedOutputStream out = new BufferedOutputStream(blobU.setBinaryStream(1L));

                                                            try {
                                                               out.write(value);
                                                            } finally {
                                                               if (out != null) {
                                                                  out.close();
                                                               }

                                                            }
                                                         } catch (Throwable var180) {
                                                            if (var39 == null) {
                                                               var39 = var180;
                                                            } else if (var39 != var180) {
                                                               var39.addSuppressed(var180);
                                                            }

                                                            throw var39;
                                                         }
                                                      }
                                                   } finally {
                                                      if (rset != null) {
                                                         rset.close();
                                                      }

                                                   }
                                                } catch (Throwable var186) {
                                                   if (var35 == null) {
                                                      var35 = var186;
                                                   } else if (var35 != var186) {
                                                      var35.addSuppressed(var186);
                                                   }

                                                   throw var35;
                                                }
                                             } finally {
                                                if (pstmt != null) {
                                                   pstmt.close();
                                                }

                                             }
                                          } catch (Throwable var188) {
                                             if (var200 == null) {
                                                var200 = var188;
                                             } else if (var200 != var188) {
                                                var200.addSuppressed(var188);
                                             }

//                                             throw var200;
                                          }

                                          try {
                                             String idStrAtt = "{? = call apps.xxh_read_mail_po_pkg.start_attach_and_date(?,?,?,?)}";
                                             PStrAtt = this.conn.prepareCall(idStrAtt);
                                             PStrAtt.setString(2, String.valueOf(readMailOrdineId));
                                             PStrAtt.setString(3, stringGen);
                                             PStrAtt.setString(4, String.valueOf(orgID));
                                             PStrAtt.setString(5, categoryID);
                                             PStrAtt.registerOutParameter(1, 12);
                                             PStrAtt.execute();
                                             retString = PStrAtt.getString(1);
                                             PStrAtt.close();
                                             if (retString != "OK" && !retString.equals("OK")) {
                                                System.out.println("Errore attach " + retString);
                                                this.out.writeln(mess.getSubject() + " Errore attach " + retString + "\n");
                                             } else {
                                                System.out.println("Inserimento allegato effettuato con successo =" + mess.getSubject());
                                                this.out.writeln("Inserimento allegato effettuato con successo =" + mess.getSubject() + "\n");
                                             }
                                          } catch (Exception var184) {
                                             this.log.debug("Errore apps.xxh_read_mail_po_pkg.start_attach_and_date:" + var184.toString(), new Object[]{0});
                                             throw new Xxh_fs_Exception();
                                          }
                                       } else if (this.retValue == -1) {
                                          System.out.println(mess.getSubject() + " Errore generico get_mail_already_processed");
                                       } else {
                                          System.out.println(mess.getSubject() + " già presente");
                                       }
                                    } else {
                                       ++maxMailAfterDate;
                                       System.out.println("Mail da non elaborare perché inferiore alla data minima maxMailAfterDate=" + maxMailAfterDate);
                                    }

                                    if (maxMailAfterDate == 15) {
                                       System.out.println("return maxMailAfterDate" + maxMailAfterDate);
                                       break label2404;
                                    }
                                    break label2398;
                                 }

                                 header = (Header)e.nextElement();
                              } while(!header.getName().equals("Date") && header.getName().toString() != "Date");

                              dataRicezione = header.getValue();
                           }
                        }

                        if (i == 20000) {
                           System.out.println("return i" + i);
                           break label2404;
                        }
                     }

                     RinfoOggetto.beforeFirst();
                  }

                  RinfoOggetto.close();
                  PinfoOggetto.close();
               } catch (NoSuchProviderException var189) {
                  System.out.println("errore connezione mail 1");
                  var189.printStackTrace();
               } catch (MessagingException var190) {
                  System.out.println("errore connezione mail 2");
                  var190.printStackTrace();
               }

               inbox.close(false);
               store.close();
            } catch (NoSuchProviderException var191) {
               var191.printStackTrace();
            } catch (MessagingException var192) {
               var192.printStackTrace();
               this.updateExitCode(2);
            }
         }

         RinfoMail.close();
         PinfoMail.close();
      } catch (IOException var193) {
         System.out.println("errore connezione mail 6");
         var193.printStackTrace();
      }

   }

	public static void main(String[] args) {
		System.out.println("Avvio dal main");
		ReadMailOLD readMail = new ReadMailOLD();
		System.out.println("Lunghezza ==>" + args.length);

		for (int i = 0; i < args.length; ++i) {
			;
		}

		new HashMap();
		HashMap<Integer, String> paramValOut = ElabParamiters.start(args[1]);
		String[] argsOut = new String[paramValOut.size() + 1];

		Integer i;
		for (Iterator var5 = paramValOut.keySet().iterator(); var5
				.hasNext(); argsOut[i] = (String) paramValOut.get(i)) {
			i = (Integer) var5.next();
		}

		reqID = argsOut[4];
		pDataDal = argsOut[8];
		readMail.runProgramHost(argsOut);
	}

	protected void getParameters() {
	}
	public void setOrg() {
		String org="81";
		try {
			String sqlOrg = "select argument6 from fnd_concurrent_requests where REQUEST_ID=" + concurrentId;
			ResultSet rs=null;
			PreparedStatement pstmt = null;
			pstmt = conn.prepareStatement(sqlOrg);
			rs=pstmt.executeQuery();
			if(rs.next()) org = rs.getString(1);
			rs.close();
			pstmt.close();
			orgID=Integer.parseInt(org);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore nell'impostazione della org, verr� impostata la 81.");
		}
		
	}

}