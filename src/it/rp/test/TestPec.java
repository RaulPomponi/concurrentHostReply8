package it.rp.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.Pec;
import it.xview.cp.util.PecReadBean;
import it.xview.cp.util.TempSecurityStorage;

public class TestPec {
	final static String MITTENTE = "acquisti.gemelli@pec.it";//"direzioneict.test@pec.policlinicogemelli.it";
	final static String PWD = "Rimagio2023!";//"D!rezioneict2023";
	final static String SMTP = "smtps.pec.aruba.it";

	public static void main(String[] args) {
		
		//TestPec obj = new TestPec();
		//obj.run();
		try {
	          	System.out.println("START " + LocalDateTime.now());
	          	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	          	TestPec obj = new TestPec();

	  			obj.run();
	  			System.out.println("FINE " + LocalDateTime.now());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private ConcurrentHostConnection chc; 
	private String sqlI 	= "select 'I' STATO, ARSS_REQUEST_ID, NOME_FILE, ORIGINE, OGGETTO, to_char(nvl(SEND_DATE,sysdate-7),'dd-MM-YYYY') STRING_DATE  from XXX_GESTIONE_ORDINIPO_I_V  WHERE ORG_ID =:1";

	private void run() {
		String args0="/data2/PROD/apps/apps_st/appl/xxh/12.0.0/bin/XXH_TEST_HOST";
		String args1="XXH_TEST_HOST FCP_REQID=37755451 FCP_LOGIN=\"APPS/g3mpr0da9p5\" FCP_USERID=5675 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"1Semestre2023\"";
		String sOrgId = "81";
		try {
			chc = new ConcurrentHostConnection(args0,args1);
			List<PecReadBean> listaPecI = getListaPecReadFromDB(sqlI);
			readAll(listaPecI);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void readOne() throws Exception {
		final String OGGETTO = "CONSEGNA: Invio Richiesta di Attingimento nr. 2305929.17"; // REQUEST_ID=37732244
	    
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
	      System.out.println("mail.pop3.socketFactory.port = " + props.getProperty("mail.pop3.socketFactory.port"));
	      System.out.println("mail.pop3.port = " + props.getProperty("mail.pop3.port"));
 
	      final Session session = Session.getInstance(props, new Authenticator() {
	          @Override
	          protected PasswordAuthentication getPasswordAuthentication() {
	          	System.out.println("Autenticazione su " + MITTENTE);
	              return new PasswordAuthentication(MITTENTE, PWD);
	          }

	      });
	      session.setDebug(false);
          Store store =  session.getStore("pop3s");
          
          store.connect (pop3,MITTENTE, PWD);
          System.out.println("dopo connect");
          Folder inbox = store.getFolder("INBOX");
          inbox.open(Folder.READ_ONLY);
          
          
             SearchTerm t2 = new SubjectTerm(OGGETTO);
			Date date = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DATE, -1);
			Date thisWeek = c.getTime();
			System.out.println(thisWeek.toLocaleString());
			SearchTerm t1 = new SentDateTerm(ComparisonTerm.GT, thisWeek);
			
			
			SearchTerm term = new AndTerm(t1,t2);
              
              
              Message[] messages = inbox.search(term); //inbox.getMessages();
              System.out.println(OGGETTO + " trovato " + messages.length);
              for (Message message : messages) {
            	  
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
                      }
                	  System.out.println("Stato ="+statoPec);
                	  
                	  System.out.println("*******************************************");
                	  if("N".equals(statoPec)) {
                    	  System.out.println("IF*******************************************");
                    	  System.out.println(message.getSubject());//(os.toString());
                    	  System.out.println("*******************************************");
                	  }else {
                    	  System.out.println("ELSE*******************************************");
                    	  System.out.println(message.getSubject());//(os.toString());
                    	  System.out.println("*******************************************");
                	  }
                	  
                	 
          }
          store.close();
	}
	private List<PecReadBean> getListaPecReadFromDB(String sqlPecR) throws SQLException, Exception {
		List<PecReadBean> lista =new ArrayList<PecReadBean>();
		PreparedStatement ps = chc.getConnection().prepareStatement(sqlPecR);
		ps.setString(1, "81");
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			PecReadBean pecR = new PecReadBean();
			pecR.setStato(rs.getString("STATO"));
			pecR.setNomeFile(rs.getString("NOME_FILE"));
			pecR.setOggetto(rs.getString("OGGETTO"));
			pecR.setDataRicezione(rs.getString("STRING_DATE"));
			lista.add(pecR);
		}
		rs.close();
		ps.close();
		return lista;
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
	      session.setDebug(false);
/*
	      Enumeration e =session.getProperties().propertyNames();
	      while (e.hasMoreElements()) {
	          String key = (String) e.nextElement();
	          System.out.println(key + " -- " + props.getProperty(key));
	        }
*/
          Store store =  session.getStore("pop3s");
          
//          System.out.println("prima di connect");
          store.connect (pop3,MITTENTE, PWD);
          System.out.println("dopo connect");
          Folder inbox = store.getFolder("INBOX");
          inbox.open(1);
          
          
          for(PecReadBean pecR : pecIn) {
              
        	SearchTerm t2 = new SubjectTerm(pecR.getOggetto());//("CONSEGNA");//("Invio Ordine nr. 2303361");
        	  
          	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
          	String dateInString = pecR.getDataRicezione();
          	Date dateCompare = formatter.parse(dateInString);	          

    	    
    	    SearchTerm t1 = new SentDateTerm(ComparisonTerm.GE, dateCompare);
			SearchTerm term = new AndTerm(t1,t2);
              
              Message[] messages = inbox.search(term); //inbox.getMessages();
              System.out.println(pecR.getOggetto() + " trovato " + messages.length);
              for (Message message : messages) {
				  PecReadBean pecT = new PecReadBean();  
				  pecT.setOggetto(pecR.getOggetto());
				  pecT.setNomeFile(pecR.getNomeFile());
				  System.out.println("Data invio 	: " + message.getSentDate() );
				  System.out.println("Oggetto 		: " + message.getSubject());
				  System.out.println("Oggetto2 		: " + message.getFrom()[0].getType());
              }
          }
          store.close();
          System.out.println("FINE");
          return pecOut;
	      
	      
	}

}
