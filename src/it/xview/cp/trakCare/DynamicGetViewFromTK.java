package it.xview.cp.trakCare;
/*
 * XVIEW: Acquisizione Dati da TrakCare - DB IRIS - Intersystems
   XXH: Acquisizione Dati da TrakCare - DB IRIS - Intersystems
 * XXX_XVIEW_TRANSFERT_TK_APPS
 * TRANSFERT_TK_APPS
 */
import com.intersystems.jdbc.IRISDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.PecReadBean;

public class DynamicGetViewFromTK {
	private int exitStatus = 0;
	private ConcurrentHostConnection chc;
	private Connection connORA;
	private String requestId;
	private Connection connIRIS;
	private String vwhere;

	private final String pkgName = "XXX_XVIEW_TRANSFERT_TK_PKG";
	
	public DynamicGetViewFromTK(String p1, String p2) {
        try {
			chc = new ConcurrentHostConnection(p1,p2);
			connORA = chc.getConnection();
			HashMap arguments =chc.getConcurrentParameter();
	        requestId = String.valueOf(chc.getRequestId());
        	String ambiente="TEST";
	        if(p1.indexOf("PROD")>0) ambiente="PROD"; 
			connIRIS = getConnessioneIRIS(ambiente);
			vwhere = (String)arguments.get("NOME_QUERY");
			vwhere = vwhere.replaceAll("\"", "");
			System.out.println("Condizioni " + vwhere);
		    String sqlGet = "SELECT nome_query,       query_trakcare,       insert_apps,       tabella_apps,       numero_colonne   from t_query_trackcare where upper(nome_query) = upper('" + vwhere + "')";
			
			
		} catch (Exception e) {
			this.exitStatus=1;
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception{
//		String arg0 ="/data1/PRE/apps/apps_st/appl/xxh/12.0.0/bin/TCFatturaCumulativa";
//		String arg1 = "TCFatturaCumulativa FCP_REQID=21588099 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=5261 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"T\"";
		String arg0=args[0];
		String arg1=args[1];  
		
		DynamicGetViewFromTK tk = new DynamicGetViewFromTK(arg0,arg1);
		try {
			if(tk.exitStatus==0) tk.run();
		} catch (Exception e) {
			System.out.println("Errore Incassi");
			e.printStackTrace();
			tk.exitStatus =2;
		}
		System.exit(tk.exitStatus);
	}
    private void run() {
        System.out.println(" INIZIO PROGRAMMA DI ACQUISIZIONE DynamicGetViewFromTKPRE");
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        PreparedStatement psDyn = null;
        PreparedStatement psDyn2 = null;
        ResultSet rs = null;
        ResultSet rsDyn = null;
        String sqlGet = "SELECT nome_query,       query_trakcare,       insert_apps,       tabella_apps,       numero_colonne   from t_query_trackcare where upper(nome_query) = upper('" + vwhere + "')";
        System.out.println(" sqlGet= " + sqlGet);

        try {
			Statement st = connORA.createStatement();;
			st.executeUpdate("alter session set NLS_NUMERIC_CHARACTERS='.,' ");
			st.close();

			ps = connORA.prepareStatement(sqlGet);
			rs = ps.executeQuery();
			while( rs.next()){
			   System.out.println("Avvio sincronizzazione vista " + rs.getString("tabella_apps"));
			   int nr_col = rs.getInt("numero_colonne");

			   /* CR700
			   st = connORA.createStatement();
			   st.executeUpdate("DELETE " + rs.getString("tabella_apps"));
			   st.close();
			   */
			   String esitoBkp = eseguiPrepare(rs.getString("tabella_apps"));
			   if("OK".equals(esitoBkp)) {
				   System.out.println(" TABELLA " + rs.getString("tabella_apps") + " SVUOTATA");
				   int righe = 0;
				   String sqlGetDyn = rs.getString("query_trakcare");
				   String sqlSet = rs.getString("insert_apps");
				   try {
					psDyn = connIRIS.prepareStatement(sqlGetDyn);
					   rsDyn = psDyn.executeQuery();
					   while(rsDyn.next()) { //Ciclo sui dati InterSystem
					      psDyn2 = connORA.prepareStatement(sqlSet);
					      int esito;
					      for(esito = 1; esito <= nr_col; ++esito) {
					         psDyn2.setString(esito, rsDyn.getString(esito));
					      }
					      esito = psDyn2.executeUpdate();
					      righe += esito;
					      psDyn2.close();
					   }
				} catch (Exception e) {
					this.exitStatus=5;
					e.printStackTrace();
					String esitoRestore = eseguiRestore(rs.getString("tabella_apps") );
					System.out.println("Eseguito Restore " + esitoRestore);
				}
				   System.out.println(" Fine del ciclo principale ");
				   System.out.println(" Tabella " + rs.getString("tabella_apps") + " popolata con " + righe + " record");
			   }else {
				   System.out.println("Errore nell'esecuzione del Backup ");
				   this.exitStatus=4;
			   }
			}
		} catch (SQLException e1) {
			this.exitStatus=3;
			e1.printStackTrace();
		}
        try {
			rs.close();
			ps.close();
			connIRIS.close();
			connORA.close();
		} catch (Exception e) {
	         if (rs != null) {            
	        	 try {rs.close();} catch (SQLException sqle) {}         
	         }
	         if (ps != null) {
	        	 try {ps.close();} catch (SQLException sqle) {}         
	         }
	         if (connIRIS != null) {
	        	 try {connIRIS.close();} catch (SQLException sqle) {}         
	         }
	         if (connORA != null) {
	        	 try {connORA.close();} catch (SQLException sqle) {}         
	         }
		}
        
        
        
	}

	private Connection getConnessioneIRIS(String ambiente) throws Exception{
	    System.out.println("********************************************************************************");
	    String connectionOracle = "jdbc:IRIS://10.160.81.13:51773/TRAIN";
	    // Test nuova connessione 20250520
	    connectionOracle = "jdbc:IRIS://10.160.81.81:51773/UPGRADE";
	    if(ambiente.equals("PROD")) connectionOracle = "jdbc:IRIS://istc-livetcsio.fpg.local:51773/LIVE";
	    Connection conn = null;
	    System.out.println("Connessione al database IRIS di "+ambiente +" ("+connectionOracle+") in corso...");
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

	private String eseguiPrepare(String tableName) {
		String nomeProcedura = "EXECUTE_PREPARE";
		return eseguiProcedura(nomeProcedura,tableName);
	}
	private String eseguiRestore(String tableName) {
		String nomeProcedura = "EXECUTE_RESTORE";
		return eseguiProcedura(nomeProcedura,tableName);
	}

	private String eseguiProcedura(String nomeProcedura, String tableName) {
			String ret = null;
	    	try {
	            CallableStatement cs = null;
	            String call = "call "+ pkgName +"."+nomeProcedura +"(?,?)";
	            cs = chc.getConnection().prepareCall(call);
	            cs.setString(1,tableName); 
				cs.registerOutParameter(2, java.sql.Types.VARCHAR);
	            cs.executeUpdate();
	        	ret =cs.getString(2);
	            cs.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            exitStatus=99;
	        }
			return ret;
	}
}
	
