package it.xview.cp.util;

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

public class NoReplyMail {
	private NoReplyMailerBean mail;
	public static void main(String[] args) {
		String[] allegati = {"C:\\temp\\TestPDF.pdf","C:\\temp\\TestPDF2.pdf"};
		
		NoReplyMailerBean mb = new NoReplyMailerBean(
				"r.pomponi@xview.it",
				null,
				"raul.pomponi@gmail.com",
				"Mail di test",
				"<hr><h1>Titolo</h1><p>Paragrafo</p><hr>",
				allegati);
		NoReplyMail m = new NoReplyMail(mb);
		m.send();

	}
	public NoReplyMail(NoReplyMailerBean  noReplyMailerBean ) {
		this.mail=noReplyMailerBean;
	}
	public boolean send() {
		boolean esito=false;
	      String[] var10000 = new String[]{";"};
	      Properties props = System.getProperties();
	      Session session = null;
	      props.put("mail.transport.protocol", "smtp");
//	      props.put("mail.smtp.ssl.enable", "true");
	      props.put("mail.smtp.host", mail.getSmtpHost());
	      props.put("mail.smtp.port", mail.getSmtpPort());
	      props.put("mail.smtp.auth", "false");
	      props.put("mail.user", mail.getMittente());
//	      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//	      props.put("mail.starttls.required", true);
	      props.put("mail.debug", "false");
	      /* Modificato per problemi di compilazione*/
	      session = Session.getInstance(props, null);
//	      session.setDebug(true);
	      session.setDebug(false);
	      System.out.println("1");

	      try {
	         Message message = new MimeMessage(session);
	         message.setFrom(new InternetAddress(mail.getMittente(), mail.getMittente()));
	         message.setSubject(mail.getOggetto());
		      System.out.println("2");
	         String[] to = mail.getDestinatariA().split(";");
	         for(int i = 0; i <= to.length - 1; ++i) {
	   	      System.out.println("3");
	            message.addRecipient(RecipientType.TO, new InternetAddress(to[i]));
	         }
	         if(mail.getDestinatariCc() != null) {
	   	      System.out.println("4");
		         String[] cc = mail.getDestinatariCc().split(";");
		         for(int i = 0; i <= cc.length - 1; ++i) {
		            message.addRecipient(RecipientType.CC, new InternetAddress(cc[i]));
		         }
	         }
	         if(mail.getDestinatariCcn() != null) {
	   	      System.out.println("5");
		         String[] ccn = mail.getDestinatariCcn().split(";");
		         for(int i = 0; i <= ccn.length - 1; ++i) {
		            message.addRecipient(RecipientType.BCC, new InternetAddress(ccn[i]));
		         }
	         }
	         BodyPart messageBodyPart = new MimeBodyPart();
		      System.out.println("6");

	         //	         messageBodyPart.setText(mail.getCorpo());
	         messageBodyPart.setContent(mail.getCorpo(), "text/html");
		      System.out.println("7");
	         Multipart multipart = new MimeMultipart();
	         multipart.addBodyPart(messageBodyPart);
		      System.out.println("8");
	         String msgAllegato = "";
	         if (mail.getAllegati()!=null && mail.getAllegati().length>0 && !mail.getAllegati()[0].equals(";")) {
	   	      System.out.println("9");
	            for(int i = 0; i <= mail.getAllegati().length - 1; ++i) {
	      	      System.out.println("10 " + mail.getAllegati()[i]);
	               String filename = mail.getAllegati()[i];
	               try {
					messageBodyPart = new MimeBodyPart();
				      System.out.println("11");
					   DataSource source = new FileDataSource(filename);
					   messageBodyPart.setDataHandler(new DataHandler(source));
					   messageBodyPart.setFileName(filename.substring(filename.lastIndexOf("/") + 1));
					      System.out.println("12");
					   multipart.addBodyPart(messageBodyPart);
				} catch (Exception e) {
					msgAllegato +="<p> INFO: Errore nell'inserimento dell'allegato " + filename + ".</p>";
					e.printStackTrace();
				}
	            }
	         }

	         try {
	            message.setContent(multipart);
	         } catch (Exception e) {
	            System.out.println("Errore setContent" +e.getMessage());
	            esito=false;
	         }

	         try {
	   	      System.out.println("13");
	            Transport transport = session.getTransport();
	            transport.connect(mail.getSmtpHost(),mail.getMittente(),null);
	  	      System.out.println("14");
	            transport.sendMessage(message, message.getAllRecipients());
	  	      System.out.println("15");
	            esito = true;
	         } catch (Exception e) {
	        	 System.out.println("Errore SEND mail : "+e.getMessage());
	            esito = false;
	         }
	      } catch (Exception e) {
	    	  System.out.println("Errore Invio mail"+e.getMessage());
	         esito=false;
	      }
		
		
		return esito;
	}

}
