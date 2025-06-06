package it.xview.cp.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.NoReplyMail;
import it.xview.cp.util.NoReplyMailerBean;
import it.xview.cp.util.PecBean;

public class SendViewByMail {
	private String arg0;
	private String arg1;
	private ConcurrentHostConnection chc;

	public static void main(String[] args) {
		SendViewByMail obj = new SendViewByMail();
/*
 		String args0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_PEC_INVIA_ORDINI";
		String args1="XXH_PEC_INVIA_ORDINI FCP_REQID=31161465 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=3135 FCP_USERNAME=\"DG\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"1\" \"81\" \"81_2303226_REV0_1_signed.pdf\"";
 */
		
		try {
			obj.setArg0(args[0]);
			obj.setArg1(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			/*
	 		String args0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_PEC_INVIA_ORDINI";
			String args1="XXH_PEC_INVIA_ORDINI FCP_REQID=31161465 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=3135 FCP_USERNAME=\"DG\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"1\" \"81\" \"81_2303226_REV0_1_signed.pdf\"";
			obj.setArg0(args0);
			obj.setArg1(args1);
			*/
			e.printStackTrace();
		}
		obj.run();
		

	}
	private void run() {
		try {
			chc = new ConcurrentHostConnection(arg0,arg1);
			HashMap<String,Object> param = chc.getConcurrentParameter();
			String nomeVista = (String)param.get("P_VISTA_CONTENUTO");//"ZZ_ERR_CONCURRENT_V";
			String sqlVista = "SELECT * FROM " + nomeVista;
			String sqlMail = (String)param.get("P_VISTA_MAIL");//"ZZ_ERR_CONCURRENT_MAIL_V";
	        StringBuilder s = new StringBuilder();
	        boolean isNotEmpty=false;
		      ResultSet rs=null;
		      PreparedStatement stmt = null;
		      try{
		        stmt = chc.getConnection().prepareStatement(sqlVista);
		        rs=stmt.executeQuery();
	        	ResultSetMetaData col = rs.getMetaData();
	        	s.append("<TABLE border='1'><TR  style='background-color:#A9A9A9'>");
	        	String[] bgcolor = {"style='background-color:#F8F8FF'","style='background-color:#D3D3D3'"};
	        	for(int i=0;i<col.getColumnCount();i++) {
	        		s.append("<TH>"+ col.getColumnName(i+1).toUpperCase() + "</TH>");
	        	}
	        	s.append("</TH></TR>");
		        int k=0;
	        	while(rs.next()) {
		        	isNotEmpty=true;
		        	s.append("<TR "+ bgcolor[k++ % 2] +">");
		        	for(int i=0;i<col.getColumnCount();i++) {
		        		s.append("<TD>" + rs.getString(col.getColumnName(i+1)) +"</TD>");
		        	}
		        	s.append("</TR>");
		        }
//		        System.out.println(s.toString());
		      }catch(Exception e)
		      {
		        e.printStackTrace();
		      }finally
		        {
		          try{
		            rs.close();
		            stmt.close();
		          }catch(Exception e){}
		       }

		      if(isNotEmpty) {
		        	s.append("</TABLE>");
				    HashMap<String,String> mapMail = chc.getSingleRow("select * from "+sqlMail, null);
					NoReplyMailerBean mb = new NoReplyMailerBean(
						mapMail.get("destinatari"),
						mapMail.get("destinatari_cc"),
						mapMail.get("destinatari_bcc"),
						mapMail.get("oggetto"),
						s.toString(),
						null
					);
					NoReplyMail m = new NoReplyMail(mb);
					m.send();
		      }
		    chc.closeConnection();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

















	public String getArg0() {
		return arg0;
	}

	public void setArg0(String arg0) {
		this.arg0 = arg0;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}
	

}
