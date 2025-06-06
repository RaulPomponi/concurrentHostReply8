package it.xview.cp.trakCare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

import com.intersystems.jdbc.IRISDataSource;

import it.xview.cp.util.ConcurrentHostConnection;

/*
 * Concurrent 	
 * 				Programma		XXH: TC Adeguamenti Massivi
 * 				Abbreviazione	TC_ADEGUAMENTI_MASSIVI
 * 			
 * Tabella 		XXH_PREFATTURE_ESITI
 * Package		XXH_PREFATTURE_ESITI_PKG
 */
public class AdeguamentiMassivi {
	public int exitStatus=0;
	private Connection    connORA;
	private Connection   connIRIS;
	private ConcurrentHostConnection chc;
	private String sqlOracle = "select ... " +
			" from ...  "+
			" WHERE ORG_ID = :1";// and num_prefattura='2021PFT000040'";
	private int numFields = 4;
	private String sqlIris = "Custom_DWHViews.PREFATTURE_RegistraIncasso";
	private String oraEsito = "XXH_PREFATTURE_ESITI_PKG.ESITO_FATTURE";
	private String requestId;
	private String sOrgId="81";
	

	public static void main(String[] args) throws Exception{
//		String arg0 ="/data1/PRE/apps/apps_st/appl/xxh/12.0.0/bin/TCFatturaCumulativa";
//		String arg1 = "TCFatturaCumulativa FCP_REQID=21588099 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=5261 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"T\"";
		String arg0=args[0];
		String arg1=args[1];  
		
		AdeguamentiMassivi obj = new AdeguamentiMassivi(arg0,arg1);
        int exitStatus=0;
		try {
			obj.run();
		} catch (Exception e) {
			System.out.println("Errore Incassi");
			e.printStackTrace();
			obj.exitStatus=2;
		}
        System.exit(obj.exitStatus);
    }
	private void run() {
			System.out.println("run Adeguamenti Massivi ");
			copyEbs2TC(sqlOracle,sqlIris,oraEsito,numFields);
	}
	public void copyEbs2TC(String oraRS, String tcProc, String oraProcEsito, int numFieldsTC) {
	      ResultSet rs=null;
	      PreparedStatement stmt = null;
	      try{
	        stmt = connORA.prepareStatement(oraRS);
	        stmt.setString(1,sOrgId);
	        rs=stmt.executeQuery();
	        int nCol = rs.getMetaData().getColumnCount();
	        String[] param = new String[nCol];
	        while(rs.next()) {
	        	for(int i=0;i<nCol;i++) {
	        		param[i]=rs.getString(i+1);
	        	}
	        	String esito = callProcIRIS(tcProc, param,numFieldsTC);
	        	if(oraProcEsito!=null) {
	        		callOraProcEsito(oraProcEsito, param, esito);
	        	}
	        	
	        }
	      }catch(Exception e)
	      {
	        e.printStackTrace();
	        this.exitStatus=2;
	      }		
	}
	public AdeguamentiMassivi(String p1, String p2) {
		try {
	        chc = new ConcurrentHostConnection(p1,p2);
	        connORA = chc.getConnection();
	        HashMap arguments =chc.getConcurrentParameter();
	        sOrgId = (String)arguments.get("P_ORG_ID");
	        chc.setOrg(Integer.parseInt(sOrgId) );
	        System.out.println("ORG_ID = " +sOrgId);
	        requestId = String.valueOf(chc.getRequestId());
        	String ambiente="TEST";
	        if(p1.indexOf("PROD")>0) ambiente="PROD"; 
			connIRIS = getConnessioneIRIS(ambiente);
		} catch (Exception e) {
			e.printStackTrace();
			this.exitStatus=1;
		}
	}

	
	public Connection getConnORA() {
		return connORA;
	}
	public void setConnORA(Connection connORA) {
		this.connORA = connORA;
	}
    private Connection getConnessioneIRIS(String ambiente) throws Exception{
	    System.out.println("********************************************************************************");
	    String connectionOracle = "jdbc:IRIS://10.160.81.13:51773/TRAIN";
	    if(ambiente.equals("PROD")) connectionOracle = "jdbc:IRIS://istc-livetcsio.fpg.local:51773/LIVE";
	    Connection conn = null;
	    System.out.println("Connessione al database IRIS di Produzione ("+connectionOracle+") in corso...");
	    IRISDataSource ds = new IRISDataSource ();
	    ds.setURL (connectionOracle);
	    ds.setUser ("OracleUser");
	    ds.setPassword ("0racleUser1");
	    conn = ds.getConnection ();
	    conn.setAutoCommit(true);
	    System.out.println("==> Connessione al database IRIS completata");
	    System.out.println("********************************************************************************");
	    return conn;
    }
	public String callProcIRIS (String procFullName, String[] paramORA, int numFields) throws SQLException {
		String esito=null;
		String placeHolder="";
		String[] param = new String[numFields];
		for(int i=0;i<numFields;i++){
			param[i]=paramORA[i];
		}
    	System.out.println("Richiesta a TC : call "+procFullName);
		for(int i=0;i<numFields;i++) {
			placeHolder+=i==0?"?":",?";
			System.out.println("   " +i+") " + param[i]);
		}
		placeHolder+=placeHolder.length()>0?",?":"?";

		CallableStatement stmt;
		stmt = connIRIS.prepareCall("call "+procFullName+"("+placeHolder+")");
		int i=0;
		for(i=0;i<param.length;i++) {
			stmt.setString(i+1, param[i]);
		}
		stmt.registerOutParameter(i+1,Types.VARCHAR);
        java.sql.ResultSet rsIris = stmt.executeQuery();
        while(rsIris.next()) {
     	   esito=rsIris.getString(1);
     	   ResultSetMetaData rsmd = rsIris.getMetaData();
     	   for(int q=0; q<rsmd.getColumnCount();q++) {
     		   System.out.println(q +" - "+rsmd.getColumnName(q+1)+"="+rsIris.getString(q+1));
     	   }
        }
        rsIris.close();
        
        stmt.close();
    	System.out.println("Risposta da TC : "+esito);
        return esito;
	}
	

	public void callOraProcEsito (String procFullName, String[] param, String esito) throws SQLException {
		String placeHolder="";
    	System.out.println("Richiesta a ORA : call "+procFullName);
		for(int i=0;i<param.length;i++) {
			placeHolder+=i==0?"?":",?";
			System.out.println("   " +i+") " + param[i]);
		}
		System.out.println("   esito) " + esito);
		System.out.println("   requestID) " + requestId);
		placeHolder+=placeHolder.length()>0?",?,?":"?,?"; // gli ultimi parametri sono esito e requestID

		CallableStatement stmt;
		stmt = connORA.prepareCall("call "+procFullName+"("+placeHolder+")");
		int i=0;
		for(i=0;i<param.length;i++) {
			stmt.setString(i+1, param[i]);
		}
		stmt.setString(i+1, esito);
		stmt.setString(i+2, requestId);
		stmt.executeQuery();
        stmt.close();
	}

	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

}
