package it.xview.cp.mail;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.mail.MailerBean;

public class SendEmailOfficeAuthentication {

//    private static final Logger LOGGER = Logger.getAnonymousLogger();

	public static final String DB_UPDATE = "update xxh_dati_770_files set REQUEST_ID_INVIO_MAIL = :1 where file_id=:2";
	public static final String DB_OBJECT = "APPS.XXH_DATI_770_MAIL_V";
	public static final String DB_WHERE = "anno=:ANNO and org_id=:ORG_ID";
	public static final String DB_ORDER = "VENDOR_ID";
	public static final String SELECT = "SELECT ID_UPDATE, mittente, password, smtpHost, smtpPort, destinatariA, destinatariCc, destinatariCcn, corpo, allegati, oggetto FROM ";
	private String sql="SELECT '0' ID_UPDATE, '' mittente, '' password, '' smtpHost, '' smtpPort, '' destinatariA, '' destinatariCc, '' destinatariCcn, '' corpo, '' allegati, '' oggetto FROM DUAL";

	private static final String SERVIDOR_SMTP = "smtp.office365.com";
    private static final int PORTA_SERVIDOR_SMTP = 587;
    private static  String CONTA_PADRAO = "GM.consulenti@fbf-isola.it";
    private static  String SENHA_CONTA_PADRAO = "Pasquino21";
    private  String from = "";
    private  String to = "";
    private String subject = "";
    private String messageContent = "";

    

	public void runProgramTest() {

		try {
	        List<MailerBean> rows;
			rows = getAllMailTest();
			logga("Recuperate "+rows.size()+" MAIL da inviare.");
			if(rows!=null && !rows.isEmpty()) {
		        for(MailerBean row : rows) {
		        	logga("Invio mail FILE ID :"+row.getIdUpdate());
		        	sendEmail(row);
		        }
			}

	        logga("STOP Mailer");
		} catch (Exception e) {
	        logga("STOP Mailer ERRORE");
	        logga("Request Completed ERROR : "+ e.getMessage());
		}
	}

	public void runProgram(ConcurrentHostConnection chc) {

		String dbObject=null;
		String dbWhere=null;
		String dbOrder=null;
		String dbUpdate=null;
		int reqId = chc.getRequestId();
		try {
			HashMap<String,Object> parameters = chc.getConcurrentParameter();
			logga("REQUEST_ID : " +reqId);
			dbObject= (String)parameters.get("DB_OBJECT");
			dbWhere= (String)parameters.get("DB_WHERE");
			dbOrder= (String)parameters.get("DB_ORDER");
			dbUpdate= (String)parameters.get("DB_UPDATE");
			
			setSql(SELECT+dbObject+" WHERE "+dbWhere+" ORDER BY "+dbOrder);
			logga(sql);
	        List<MailerBean> rows;
			rows = getAllMail(chc.getConnection());
			logga("Recuperate "+rows.size()+" MAIL da inviare.");
	        int i=0;
			if(rows!=null && !rows.isEmpty()) {
		        for(MailerBean row : rows) {
		        	logga("Invio mail FILE ID :"+row.getIdUpdate());
		        	sendEmail(row);
		        	if(dbUpdate!=null && dbUpdate.length()>10) {
		        		executeUpdate(chc.getConnection(), dbUpdate, row.getIdUpdate(),reqId);
		        	}
		        }
			}

	        logga("STOP Mailer");
		} catch (Exception e) {
	        logga("STOP Mailer ERRORE");
	        logga("Request Completed ERROR : "+ e.getMessage());
		}
	}

	private void executeUpdate(Connection conn, String dbUpdate, String idUpdate, int requestId) throws SQLException {
        CallableStatement cs = conn.prepareCall(dbUpdate);
        cs.setInt(1, requestId);
        cs.setString(2, idUpdate);
        cs.execute();
        cs.close();
        conn.commit();
		
		
	}
    
    
    public void sendEmail(MailerBean mail) {
    	logga("START"); 
    	CONTA_PADRAO=mail.getMittente();
    	SENHA_CONTA_PADRAO=mail.getPassword();
        final Session session = Session.getInstance(this.getEmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
            	System.out.println("Autenticazione su " + CONTA_PADRAO);
                return new PasswordAuthentication(CONTA_PADRAO, SENHA_CONTA_PADRAO);
            }

        });
    	logga("Autenticazione ok"); 

        try {
            final Message message = new MimeMessage(session);
            logga("DEBUG 1");
//        	MailerBean mail = getFromTest365();
        	from = mail.getMittente();
            logga("DEBUG 2");
        	to = mail.getDestinatariA();
            logga("DEBUG 3");
        	subject = mail.getOggetto();
            logga("DEBUG 4");
        	messageContent = mail.getCorpo();
            logga("DEBUG 5");
            message.setFrom(new InternetAddress(from));
        	logga("Assegnazione ok"); 

            String[] to = mail.getDestinatariA().split(";");
            for(int i = 0; i <= to.length - 1; ++i) {
               message.addRecipient(RecipientType.TO, new InternetAddress(to[i]));
            }
        	logga("getDestinatariA ok"); 
            
            if(mail.getDestinatariCc() != null) {
                String[] cc = mail.getDestinatariCc().split(";");
                for(int i = 0; i <= cc.length - 1; ++i) {
                   message.addRecipient(RecipientType.CC, new InternetAddress(cc[i]));
                }
            }
        	logga("getDestinatariCC ok"); 
            if(mail.getDestinatariCcn() != null) {
                String[] ccn = mail.getDestinatariCcn().split(";");
                for(int i = 0; i <= ccn.length - 1; ++i) {
                   message.addRecipient(RecipientType.BCC, new InternetAddress(ccn[i]));
                }
            }
        	logga("getDestinatariCCN ok"); 
	         BodyPart messageBodyPart = new MimeBodyPart();
	         messageBodyPart.setContent(mail.getCorpo(), "text/html");
	         Multipart multipart = new MimeMultipart();
	         multipart.addBodyPart(messageBodyPart);
	        logga("Multipart ok"); 
	         if (mail.getAllegati()!=null && mail.getAllegati().length>0 && !mail.getAllegati()[0].equals(";")) {
	            for(int i = 0; i <= mail.getAllegati().length - 1; ++i) {
	               String filename = mail.getAllegati()[i];
	           	logga("Allegato : "+filename); 
	               messageBodyPart = new MimeBodyPart();
	               DataSource source = new FileDataSource(filename);
	               messageBodyPart.setDataHandler(new DataHandler(source));
	               messageBodyPart.setFileName(filename.substring(filename.lastIndexOf("/") + 1));
	               multipart.addBodyPart(messageBodyPart);
	            }
	         }

	         try {
	         	logga("setContent 1"); 
	            message.setContent(multipart);
	         	logga("setContent 2"); 
	         } catch (Exception e) {
	            logga("Errore setContent", e);
	         }
        	
            
            message.setSubject(subject);
//            message.setText(messageContent);
            message.setSentDate(new Date());
         	logga("setSubject ok"); 
            Transport.send(message);
         	logga("Mail inviata"); 
        } catch (final MessagingException ex) {
            logga("Errore invio: " + ex.getMessage(), ex);
        }
    	logga("END"); 
    }

    public Properties getEmailProperties() {
        final Properties config = new Properties();
        config.put("mail.smtp.auth", "true");
        config.put("mail.smtp.starttls.enable", "true");
        config.put("mail.smtp.host", SERVIDOR_SMTP);
        config.put("mail.smtp.port", PORTA_SERVIDOR_SMTP);
        return config;
    }

    public static void main(final String[] args) {
/*
    	ConcurrentHostConnection chc = null;
		try {
//			String arg0="/data1/PRE/apps/apps_st/appl/xxh/12.0.0/bin/testConcurrent";
//			String arg1="testConcurrent FCP_REQID=22909265 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=1216 FCP_USERNAME=\"CONCORRENTE\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"param 1\" \"2021/10/19 00:00:00\"";  
			String arg0=args[0];
			String arg1=args[1];  
			
			chc = new ConcurrentHostConnection(args[0],args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendEmailOfficeAuthentication s =new SendEmailOfficeAuthentication();
    	s.runProgram(chc);
*/  
		SendEmailOfficeAuthentication s =new SendEmailOfficeAuthentication();
		s.runProgramTest();
    }
	private MailerBean getRowTest() {
		String mittente = "cu770gac@gemelliacasa.it";//"GM.consulenti@fbf-isola.it";//
		String password =  "cu@770.GAC";//"Pasquino21";//
		String smtpHost = "smtp.office365.com";//
		String smtpPort = "587";
		String destinatariA="r.pomponi@xview.it";
		String destinatariCc = null;
		String destinatariCcn = null;
		String corpo="<H1>TestVPN body</H1> <B>CIAO</B>";
//		String[] allegati= {"/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/out/81_2021_INTESTAZIONE.pdf"};//{"C:\\temp\\test.pdf"};
		String[] allegati= {"C:\\temp\\cu\\test.pdf"};
		String oggetto="Test oggetto 2";
		return new MailerBean("0",mittente, password, smtpHost, smtpPort, destinatariA, destinatariCc, destinatariCcn, oggetto, corpo, allegati);
		
	}
	private List<MailerBean> getAllMailTest()throws SQLException {
		List<MailerBean> rows = new ArrayList<MailerBean>();
		rows.add(getRowTest());
		return rows;
	}

	public List<MailerBean> getAllMail(Connection conn) throws SQLException {
		List<MailerBean> rows = new ArrayList<MailerBean>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = conn.prepareStatement(getSql());
		rs = ps.executeQuery();
		
		while(rs.next()) {
				MailerBean row= new MailerBean();
				row.setIdUpdate(rs.getString("ID_UPDATE")); 
				row.setMittente(rs.getString("MITTENTE")); 
				row.setPassword(rs.getString("PASSWORD")); 
				row.setSmtpHost(rs.getString("SMTPHOST")); 
				row.setSmtpPort(rs.getString("SMTPPORT")); 
				row.setDestinatariA(rs.getString("DESTINATARIA")); 
				row.setDestinatariCc(rs.getString("DESTINATARICC")); 
				row.setDestinatariCcn(rs.getString("DESTINATARICCN")); 
				row.setCorpo(rs.getString("CORPO")); 
				row.setOggetto(rs.getString("OGGETTO")); 
				row.setAllegati(rs.getString("ALLEGATI"));
				rows.add(row);
		}
		try {
			if(rs!=null)rs.close();
			if(ps!=null)ps.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logga("Eccezione nella chisura della connessione: "+ e.getMessage());
		}
		return rows;
	}

	
	protected static void logga(String msg) {
		System.out.println(msg);
	}
	private static void logga(String passo, Exception e) {
		System.out.println(passo + ": " + e.getMessage());
		System.out.println(passo + ": " + e.toString());

		for (int i = 0; i < e.getStackTrace().length; ++i) {
			System.out.println(passo + ": " + e.getStackTrace()[i].toString());
		}
		
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

}