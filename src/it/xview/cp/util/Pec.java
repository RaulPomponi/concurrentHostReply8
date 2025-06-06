package it.xview.cp.util;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;


public final class Pec {
	private static String MITTENTE = null;
	private static String SMTP = null;
 	private static String PWD = null;
	private static boolean debug = false;	
/*
 	private static final String MITTENTE = "direzioneict.test@pec.policlinicogemelli.it";
	private static final String SMTP = "smtps.pec.aruba.it";;	
	private static final String PWD = "D!rezioneict2023";
*/
	public static void main(String[] args) {
//		String args0=args[0];
//		String args1=args[1];
		String mitt="direzioneict.test@pec.policlinicogemelli.it";
		String pass="D!rezioneict2023";
		Pec.set_mittente(mitt, pass);
		String smtp="smtps.pec.aruba.it";
		Pec.set_smpt(smtp);
		try {
			List<PecReadBean> pecIn = new ArrayList<PecReadBean>();
			PecReadBean pecR = new PecReadBean();
			pecR.setOggetto("ACCETTAZIONE: Invio Ordine nr. 2303361");
			pecIn.add(pecR);
			PecReadBean pecR2 = new PecReadBean();
			pecR2.setOggetto("ACCETTAZIONE: Invio Ordine nr. 2303232");
			pecIn.add(pecR2);
			PecReadBean pecR3 = new PecReadBean();
			pecR3.setOggetto("ACCETTAZIONE: Test Invio");
			pecIn.add(pecR3);

			Pec.readAll(pecIn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String[] attach = {"C:\\temp\\TestPDF.pdf"};
//		Pec.send_mail("r.pomponi@xview.it", "NEW test PEC", "TEST dinamico", attach );
		

	}

	public static void set_mittente(String mitt, String pass) {
		MITTENTE = mitt;
		PWD = pass;
	}
	public static void set_smpt(String smtp) {
		SMTP = smtp;
	}



	public static void printError(String step, Exception e) {
			System.out.println(step + ": " + e.getMessage());
			System.out.println(step + ": " + e.toString());

			for (int i = 0; i < e.getStackTrace().length; ++i) {
				System.out.println(step + ": " + e.getStackTrace()[i].toString());
			}

	}
	public static String send_mail(PecBean pecBean) {
		return Pec.send_mail(
				pecBean.getDestinatari(),
				pecBean.getDestinatariCC(),
				pecBean.getDestinatariBCC(),
				pecBean.getOggetto(),
				pecBean.getCorpo_mess(),
				pecBean.getAttach()
				
				);
	}
	public static List<PecReadBean> readAll(List<PecReadBean> pecIn) throws Exception {
		List<PecReadBean> pecOut = new ArrayList<PecReadBean>();
	      Properties props = System.getProperties();

	      props.put("mail.pop3.debug", "true");
          props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
          props.put("mail.pop3.socketFactory.fallback", "true");
          String portaPop3 = "465";//"995";
          String pop3="pop3s.pec.aruba.it";
		props.put("mail.pop3.socketFactory.port", portaPop3);
          props.put("mail.pop3.disablecapa", "true");
          props.put("mail.pop3.port", portaPop3);
          props.put("mail.pop3.host", pop3);
          props.put("mail.pop3.user", MITTENTE);
          props.put("mail.store.protocol", "pop3");
	      final Session session = Session.getInstance(props, new Authenticator() {
	          @Override
	          protected PasswordAuthentication getPasswordAuthentication() {
	          	System.out.println("Autenticazione su " + MITTENTE);
	              return new PasswordAuthentication(MITTENTE, PWD);
	          }

	      });
	      session.setDebug(debug);
          Store store =  session.getStore("pop3s");
          store.connect (pop3,MITTENTE, PWD);
          System.out.println("dopo connect");
    	  Folder inbox = store.getFolder("INBOX");
          inbox.open(Folder.READ_ONLY);

          for(PecReadBean pecR : pecIn) {
        	  SearchTerm t2 = new SubjectTerm(pecR.getOggetto());//("CONSEGNA");//("Invio Ordine nr. 2303361");
        	  Date date = new Date();
        	  Calendar c = Calendar.getInstance();
        	  c.setTime(date);
        	  c.add(Calendar.DATE, - 7);
        	  Date thisWeek = c.getTime();
        	  SearchTerm t1 = new ReceivedDateTerm(ComparisonTerm.GT, thisWeek);
        	  SearchTerm term = new AndTerm(t1,t2);
              
              Message[] messages = inbox.search(t2); //inbox.getMessages();
              System.out.println(pecR.getOggetto() + " trovato " + messages.length);
              for (Message message : messages) {
            	  PecReadBean pecT = new PecReadBean();  
            	  pecT.setOggetto(pecR.getOggetto());
            	  pecT.setNomeFile(pecR.getNomeFile());
            	  System.out.println("Data invio 	: " + message.getSentDate() );
                	  System.out.println("Oggetto 		: " + message.getSubject());
                	  System.out.println("Oggetto 		: " + message.getFrom()[0].getType());
                      OutputStream os = null;
                      ByteArrayOutputStream baos = null;
                      os = new ByteArrayOutputStream();
                      message.writeTo(os);
                      ByteArrayOutputStream bos = (ByteArrayOutputStream)os;
                      byte[] value = bos.toByteArray();
                      String statoPec="N";
                      if (os.toString().contains("(\"posta certificata\")")) {
                    	  if(message.getSubject().contains("ACCETTAZIONE:")) statoPec="A";
                    	  if(message.getSubject().contains("AVVISO DI NON ACCETTAZIONE:")) statoPec="Y";
                    	  if(message.getSubject().contains("CONSEGNA:")) statoPec="C";
                    	  if(message.getSubject().contains("AVVISO DI MANCATA CONSEGNA:")) statoPec="Z";
                      }else if (os.toString().contains("(\"posta ordinaria\")")){
                    	  if(message.getSubject().contains("ACCETTAZIONE:")) statoPec="O";
                    	  if(message.getSubject().contains("AVVISO DI NON ACCETTAZIONE:")) statoPec="X";
                    	  if(message.getSubject().contains("AVVISO DI MANCATA CONSEGNA:")) statoPec="Q";
                      }else if (os.toString().contains("AVVISO DI MANCATA CONSEGNA")){
                    	  statoPec="Q";
                      }else if (os.toString().contains("AVVISO DI NON ACCETTAZIONE")){
                    	  statoPec="X";
                      }else if(("CONSEGNA: "+pecR.getOggetto()).equalsIgnoreCase(message.getSubject())){
                    	  statoPec="C";
                      }
                	  System.out.println("Stato ="+statoPec);
                	  
                	  System.out.println("*******************************************");
                	  if("N".equals(statoPec)) {
                    	  System.out.println("*******************************************");
                    	  System.out.println("*************      N       ****************");
                    	  System.out.println("*******************************************");
                	  }else {
                		  try {
							pecT.setStato(statoPec);
							  pecT.setContenuto(value);
							  SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
							  Date data;
							try {
								data = formatter.parse( message.getHeader("Date")[0]);
							} catch (ParseException e) {
								formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss (Z)");
								data = formatter.parse( message.getHeader("Date")[0]);
							}
							  String pattern = "dd-MM-yyyy HH:mm:ss";
							  DateFormat df = new SimpleDateFormat(pattern);
							  pecT.setDataRicezione( df.format(data));
							  pecOut.add(pecT);
						} catch (Exception e) {
	                    	System.out.println("**!!  ERRORE ESITO PEC SCARTATO  !!**");
							e.printStackTrace();
	                    	System.out.println("**!!  *************************  !!**");
						}
                		  
                	  }
              }
          }
          try {
              if (inbox != null && inbox.isOpen()) {
                  inbox.close(false); //Il parametro false indica che non si eliminano i messaggi cancellati
                  System.out.println("INBOX CHiuso");
              }
          } catch (MessagingException ex) {
              ex.printStackTrace();
          }
          store.close();
          System.out.println("FINE");
          return pecOut;
	}
	public static String send_mail(String destinatari, String destinatariCC, String destinatariBCC, String oggetto, String corpo_mess, String[] attach) {
      
		System.out.println("Mail da inviare:");
		System.out.println("destinatari "+ destinatari);
		System.out.println("oggetto "+oggetto);
		System.out.println("corpo_mess "+corpo_mess);
		System.out.println("attach"+ attach.toString());
	  String[] var10000 = new String[]{";"};
      Properties props = System.getProperties();
//      Session session = null;
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.ssl.enable", "true");
      props.put("mail.smtp.host", SMTP);
      props.put("mail.smtp.port", "465");
      props.put("mail.smtp.auth", "true");
      props.put("mail.password", PWD);
      props.put("mail.user", MITTENTE);
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.debug", "true");
      /* Modificato per problemi di compilazione*/
//      session = Session.getInstance(props, null);
      final Session session = Session.getInstance(props, new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
          	System.out.println("Autenticazione su " + MITTENTE);
              return new PasswordAuthentication(MITTENTE, PWD);
          }

      });

      
      
      session.setDebug(debug);

      try {
         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress(MITTENTE, MITTENTE));
         message.setSubject(oggetto);
         String[] to = destinatari.split(";");
         for(int i = 0; i <= to.length - 1; ++i) {
            message.addRecipient(RecipientType.TO, new InternetAddress(to[i]));
         }
         try {
			String[] cc = destinatariCC.split(";");
			 for(int i = 0; i <= to.length - 1; ++i) {
			    message.addRecipient(RecipientType.CC, new InternetAddress(cc[i]));
			 }
		} catch (Exception e) {
			System.out.println("WARNING Destinatario CC non inserito");
		}
         try {
			String[] bcc = destinatariBCC.split(";");
			 for(int i = 0; i <= to.length - 1; ++i) {
			    message.addRecipient(RecipientType.BCC, new InternetAddress(bcc[i]));
			 }
		} catch (Exception e) {
			System.out.println("Destinatario BCC non inserito");
		}

         BodyPart messageBodyPart = new MimeBodyPart();
         messageBodyPart.setText(corpo_mess);
         Multipart multipart = new MimeMultipart();
         multipart.addBodyPart(messageBodyPart);
         if (!attach[0].equals(";")) {
            for(int i = 0; i <= attach.length - 1; ++i) {
               String filename = attach[i];
               messageBodyPart = new MimeBodyPart();
               DataSource source = new FileDataSource(filename);
               messageBodyPart.setDataHandler(new DataHandler(source));
               String fileName = filename.substring(filename.lastIndexOf("/") + 1);
//               fileName = filename.substring(filename.lastIndexOf("\\") + 1);
               messageBodyPart.setFileName(fileName);
               multipart.addBodyPart(messageBodyPart);
            }
         }

         try {
            message.setContent(multipart);
         } catch (Exception var14) {
            printError("Errore setContent", var14);
            return "KO";
         }

         try {
            Transport transport = session.getTransport();
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            System.out.println("MAIL INVIATA");
            return "OK";
         } catch (Exception e1) {
            printError("Errore SEND mail", e1);
            return "KO";
         }
      } catch (Exception e) {
         printError("Errore Invio mail", e);
         return "KO";
      }
   }

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		Pec.debug = debug;
	}

}