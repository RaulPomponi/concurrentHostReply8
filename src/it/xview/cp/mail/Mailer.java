package it.xview.cp.mail;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/*
 * 		Concurrent di riferimento XXH_MAILER
 * 		Parametri:
 * 					DB_OBJECT
 * 					DB_WHERE
 * 					DB_ORDER
 * 		 
 */
public class Mailer    {
    final String SERVIDOR_SMTP = "smtp.office365.com";
    final int PORTA_SERVIDOR_SMTP = 587;
    final String CONTA_PADRAO = "cu770gac@gemelliacasa.it";
    final String SENHA_CONTA_PADRAO = "cu@770.GAC";

    public static void main(String[] args) {
		Mailer m = new Mailer();
		MailerBean mail = m.getFromTest365();
		m.send365(mail);
//		m.sendMailNoAuth(m.getFromTest());
	}
	public static final String DB_UPDATE = "update xxh_dati_770_files set REQUEST_ID_INVIO_MAIL = :1, DATE_INVIO_MAIL = SYSDATE where file_id=:2";
	public static final String DB_OBJECT = "APPS.XXH_DATI_770_MAIL_V";
	public static final String DB_WHERE = "anno=:ANNO and org_id=:ORG_ID";
	public static final String DB_ORDER = "VENDOR_ID";
	public static final String SELECT = "SELECT ID_UPDATE, mittente, password, smtpHost, smtpPort, destinatariA, destinatariCc, destinatariCcn, corpo, allegati, oggetto FROM ";
	private String sql="SELECT '0' ID_UPDATE, '' mittente, '' password, '' smtpHost, '' smtpPort, '' destinatariA, '' destinatariCc, '' destinatariCcn, '' corpo, '' allegati, '' oggetto FROM DUAL";

    public void send365(MailerBean mail) {
        final Session session = Session.getInstance(this.getEmailProperties(), new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SERVIDOR_SMTP, SENHA_CONTA_PADRAO);
            }

        });

        try {
            final Message message = new MimeMessage(session);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(mail.getDestinatariA()));
            message.setFrom(new InternetAddress(mail.getMittente()));
            message.setSubject(mail.getOggetto());
            message.setText(mail.getCorpo());
            message.setSentDate(new Date());
            Transport.send(message);
        } catch (final MessagingException ex) {
            logga("Errore nell'invio della mail all'indirizzo " +mail.getDestinatariA() +" : " + ex.getMessage(), ex);
        }
    }

    public Properties getEmailProperties() {
        final Properties config = new Properties();
        config.put("mail.smtp.auth", "true");
        config.put("mail.smtp.starttls.enable", "true");
        config.put("mail.smtp.host", SERVIDOR_SMTP);
        config.put("mail.smtp.port", PORTA_SERVIDOR_SMTP);
        return config;
    }


	public  String sendMailNoAuth(MailerBean mail) {
		logga("Dentro sendMail");
	      String[] var10000 = new String[]{";"};
	      Properties props = System.getProperties();
	      Session session = null;
	      props.put("mail.transport.protocol", "smtp");
//	      props.put("mail.smtp.ssl.enable", "true");
	      props.put("mail.smtp.host", mail.getSmtpHost());
	      props.put("mail.smtp.port", mail.getSmtpPort());
	      props.put("mail.smtp.auth", "false");
	      if(mail.getPassword()!=null && mail.getPassword().trim().length()>0) props.put("mail.password", mail.getPassword());
	      props.put("mail.user", mail.getMittente());
//	      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//	      props.put("mail.starttls.required", true);
	      props.put("mail.debug", "false");
	      /* Modificato per problemi di compilazione*/
	      session = Session.getInstance(props, null);
//	      session.setDebug(true);
	      session.setDebug(false);

	      try {
	    	 logga(mail.toPrintString());
	         Message message = new MimeMessage(session);
	         message.setFrom(new InternetAddress(mail.getMittente(), mail.getMittente()));
	         message.setSubject(mail.getOggetto());
	         String[] to = mail.getDestinatariA().split(";");
	         for(int i = 0; i <= to.length - 1; ++i) {
	            message.addRecipient(RecipientType.TO, new InternetAddress(to[i]));
	         }
	         if(mail.getDestinatariCc() != null) {
		         String[] cc = mail.getDestinatariCc().split(";");
		         for(int i = 0; i <= cc.length - 1; ++i) {
		            message.addRecipient(RecipientType.CC, new InternetAddress(cc[i]));
		         }
	         }
	         if(mail.getDestinatariCcn() != null) {
		         String[] ccn = mail.getDestinatariCcn().split(";");
		         for(int i = 0; i <= ccn.length - 1; ++i) {
		            message.addRecipient(RecipientType.BCC, new InternetAddress(ccn[i]));
		         }
	         }
	         BodyPart messageBodyPart = new MimeBodyPart();
//	         messageBodyPart.setText(mail.getCorpo());
	         messageBodyPart.setContent(mail.getCorpo(), "text/html");
	         Multipart multipart = new MimeMultipart();
	         multipart.addBodyPart(messageBodyPart);
	         if (mail.getAllegati()!=null && mail.getAllegati().length>0 && !mail.getAllegati()[0].equals(";")) {
	            for(int i = 0; i <= mail.getAllegati().length - 1; ++i) {
	               String filename = mail.getAllegati()[i];
	               messageBodyPart = new MimeBodyPart();
	               DataSource source = new FileDataSource(filename);
	               messageBodyPart.setDataHandler(new DataHandler(source));
	               messageBodyPart.setFileName(filename.substring(filename.lastIndexOf("/") + 1));
	               multipart.addBodyPart(messageBodyPart);
	            }
	         }

	         try {
	            message.setContent(multipart);
	         } catch (Exception e) {
	            logga("Errore setContent", e);
	            return "KO";
	         }

	         try {
	            Transport transport = session.getTransport();
	            transport.connect(mail.getSmtpHost(),mail.getMittente(),mail.getPassword());
	            transport.sendMessage(message, message.getAllRecipients());
	            logga("Mail Inviata : " + mail.toPrintString());
	            return "OK";
	         } catch (Exception e) {
	            logga("Errore SEND mail : ", e);
	            return "KO";
	         }
	      } catch (Exception e) {
	         logga("Errore Invio mail", e);
	         return "KO";
	      }
	   }

	private static void logga(String passo, Exception e) {
		System.out.println(passo + ": " + e.getMessage());
		System.out.println(passo + ": " + e.toString());

		for (int i = 0; i < e.getStackTrace().length; ++i) {
			System.out.println(passo + ": " + e.getStackTrace()[i].toString());
		}
		
	}

	private static void logga(String msg) {
		System.out.println(msg);
	}

	private MailerBean getFromTest365() {
		String mittente = "cu770gac@gemelliacasa.it";//
		String password =  "cu@770.GAC";//
		String smtpHost = "smtp.office365.com";//
		String smtpPort = "587";
		String destinatariA="r.pomponi@xview.it";
		String destinatariCc = null;
		String destinatariCcn = null;
		String corpo="TestVPN body";
//		String[] allegati= {"/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/out/81_2021_INTESTAZIONE.pdf"};//{"C:\\temp\\test.pdf"};
		String[] allegati= {"C:\\temp\\test.pdf"};
		String oggetto="Test oggetto";
		return new MailerBean("0",mittente, password, smtpHost, smtpPort, destinatariA, destinatariCc, destinatariCcn, oggetto, corpo, allegati);
		
	}
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

}
