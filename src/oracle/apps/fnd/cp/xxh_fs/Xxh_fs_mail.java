package oracle.apps.fnd.cp.xxh_fs;
/*
 * Eseguibile XXH_FS_INVIA_DOCUMENTI_HOST
 * Programma SANTER: Invio Ordini Firmati
 */


import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import oracle.apps.fnd.cp.request.CpContext;

public final class Xxh_fs_mail {
	private static String MITTENTE = null;
	private static String SMTP = null;
	private static String PWD = null;
	private static CpContext gb_ctx = null;

	public static void set_mittente(String mitt, String pass) {
		MITTENTE = mitt;
		PWD = pass;
	}

	public static void set_smpt(String smtp) {
		SMTP = smtp;
	}

	public static void set_appl(CpContext cont) {
		gb_ctx = cont;
	}

	public static void print_Error(String passo, Exception e) {
		if (gb_ctx == null) {
			System.out.println(passo + ": " + e.getMessage());
			System.out.println(passo + ": " + e.toString());

			for (int i = 0; i < e.getStackTrace().length; ++i) {
				System.out.println(passo + ": " + e.getStackTrace()[i].toString());
			}
		} else {
			gb_ctx.getLogFile().writeln(passo + ": " + e.getMessage(), 0);
			gb_ctx.getReqCompletion().setCompletion(2, "");
		}

	}

	public static String send_mail(String destinatari, String oggetto, String corpo_mess, String[] attach) {
      
		System.out.println("Mail da inviare:");
		System.out.println("destinatari "+ destinatari);
		System.out.println("oggetto "+oggetto);
		System.out.println("corpo_mess "+corpo_mess);
		System.out.println("attach NoNoc"+ attach[0]);
	  String[] var10000 = new String[]{";"};
      Properties props = System.getProperties();
      Session session = null;
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
      session = Session.getInstance(props, null);
      session.setDebug(true);

      try {
         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress(MITTENTE, MITTENTE));
         message.setSubject(oggetto);
         String[] to = destinatari.split(";");

         for(int i = 0; i <= to.length - 1; ++i) {
            message.addRecipient(RecipientType.TO, new InternetAddress(to[i]));
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
               messageBodyPart.setFileName(filename.substring(filename.lastIndexOf("/") + 1));
               multipart.addBodyPart(messageBodyPart);
            }
         }

         try {
            message.setContent(multipart);
         } catch (Exception var14) {
            print_Error("Errore setContent", var14);
            return "KO";
         }

         try {
            Transport transport = session.getTransport();
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            if (gb_ctx == null) {
               System.out.println("MAIL INVIATA");
            } else {
               gb_ctx.getLogFile().writeln("MAIL INVIATA", 0);
            }

            return "OK";
         } catch (Exception var13) {
            print_Error("Errore SEND mail", var13);
            return "KO";
         }
      } catch (Exception var15) {
         print_Error("Errore Invio mail", var15);
         return "KO";
      }
   }
}