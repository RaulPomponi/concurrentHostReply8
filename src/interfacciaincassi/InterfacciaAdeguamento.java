package interfacciaincassi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.Types;
import com.intersystems.jdbc.IRISDataSource;

import it.xview.cp.util.ConcurrentHostConnection;

import java.util.Locale;

public class InterfacciaAdeguamento {
//mettere connessione in base all'ambiente
    private static String  databaseIP = "oebs12pre.rm.unicatt.it";
    private static String  databasePort = "1523";
    private static String  databaseSID = "PRE";
    private static String  databaseSchema = "APPS";
    private static String  databasePassword = "g3mpr3app5";

    
    public static void main(String[] args) throws Exception{
        InterfacciaAdeguamento t = new InterfacciaAdeguamento();
        Connection   connIRIS = null;
        Connection    connORA = null;
        Statement          st = null;
        PreparedStatement  ps = null;
        CallableStatement  ps2 = null;
        CallableStatement  ps3_call_proc = null;
        ResultSet          rs = null;
        
        Statement          stt = null;
        PreparedStatement  pst = null;
        PreparedStatement  ps2t = null;
        ResultSet          rst = null;
          
        String            sqlGet;
        String            sqlSet;
        String            sqlInsertIntefaceOA;
        
        String            stepError = "0";
        
        int righe = 0;
        
        try {
        	String ambiente ="TEST";
        	if(args[0].indexOf("PROD")>0) ambiente="PROD"; 
           connIRIS = t.getConnessioneIRIS(ambiente);
           ConcurrentHostConnection chc = new ConcurrentHostConnection(args[0],args[1]);
           connORA  = chc.getConnection();//t.getConnessioneORA();
           
            // SET NLS
           st = connORA.createStatement();
           int rsetw = st.executeUpdate("alter session set NLS_NUMERIC_CHARACTERS='.,' ");
           st.close();
           
           // Vista Casse
           System.out.println("Avvio Interfaccia Oracle - TK" +
                 ""); 
          
            sqlGet = "select trx_number num_fattura, \n" +
            "                receipt_number numero_incasso,\n" + 
            "                to_char(receipt_date,'dd/mm/yyyy') data_incasso,\n" + 
            "                amount importo_incasso, \n" + 
            "                id_quota_fondo_tk id_quota, \n" + 
//            "                receipt_name modalita_pagamento, \n" +
            "                customer_id,\n" + 
            "                customer_number,\n" + 
            "                trx_date\n" + 
//            "                ,start_date_active" +         
            "           from xxh.xxh_interfaccia_adeg_oracle_tk \n" +
            "          where 1=1" +
            "           -- and start_date_active = trunc(sysdate) \n" +
            "            and (esito is null or esito != '1')";
            
            stepError = "1";
            
//            sqlSet = "call Custom_DWHViews.PREFATTURE_RegistraIncasso(?, ?, ?, ?, ?)";
            sqlSet = "call Custom_ITPG.MatchWin_RegistraIncasso(?, ?, ?, ?, ?, ?)";
//            sqlInsertIntefaceOA = "call xxh_adeguamenti_spot_pkg.xxh_upd_ins_table_interface_tk (?,?,?,?,?,?,?,?,?,?)";
            sqlInsertIntefaceOA = "call xxh_adeguamenti_spot_pkg.xxh_upd_ins_table_interface_tk (?,?,?,?,?,?,?,?,?)";
            
            ps = connORA.prepareStatement(sqlGet);
            rs = ps.executeQuery();
            stepError = "2";
            int rowNum=1;
            while (rs.next()){
				
				String numFattura = rs.getString("num_fattura");
				String dataIncasso = rs.getString("data_incasso");
				String importoIncasso = rs.getString("importo_incasso");
				String idQuota = rs.getString("id_quota");
//				String modalitaPagamento = rs.getString("modalita_pagamento");
				int customerId = rs.getInt("customer_id");
				String customerNumber = rs.getString("customer_number");
				java.sql.Date trxDate = rs.getDate("trx_date");
				String numeroIncasso = rs.getString("numero_incasso");
//				java.sql.Date  startDateActive = rs.getDate("start_date_active");
				String currentRow=   "\n numFattura 		  	"+numFattura 		  
									+"\n dataIncasso 			"+dataIncasso 		
									+"\n importoIncasso 		"+importoIncasso 		
									+"\n idQuota 				"+idQuota 		
//									+"\n modalitaPagamento 		"+modalitaPagamento 
									+"\n customerId 			"+customerId 			
									+"\n customerNumber 		"+customerNumber 		
									+"\n trxDate 				"+trxDate 			
									+"\n numeroIncasso 			"+numeroIncasso 		
									//+"\n startDateActive 		"+startDateActive 	
									;
			   System.out.println("Riga corrente: " + rowNum++ + currentRow);   
				
				
                stepError = "entro nel ciclo 2"; 
               ps2 = connIRIS.prepareCall(sqlSet);
                stepError = "2a"; 
               ps2.setString(1, numFattura);
                stepError = "2b"; 
               ps2.setString(2, dataIncasso);
                stepError = "2c"; 
               ps2.setString(3, importoIncasso);
                stepError = "2d"; 
                ps2.setString(4, idQuota);
                stepError = "2d"; 
               ps2.setString(5, numeroIncasso);
                stepError = "2e"; 
               ps2.registerOutParameter(6,Types.VARCHAR);// .VARCHAR);
                stepError = "2f"; 
               
               java.sql.ResultSet rsIris = ps2.executeQuery();
               stepError = "3";
               
               String esito = null; //ps2.getString(1);
               if(rsIris.next()) {
            	   esito=rsIris.getString(1);
               }
               System.out.println("esito: "+ esito);   
               if ("1".equals(esito)) { 
                    System.out.println(rs.getString("num_fattura") + " riconciliata ");  
                }
               else 
                   {
                   System.out.println(rs.getString("num_fattura") + " non riconciliata " +esito);
                 }
               
               ps2.close();

               
               System.out.println("Parametri call xxh_adeguamenti_spot_pkg.xxh_upd_ins_table_interface_tk (?,?,?,?,?,?,?,?,?)");
			   System.out.println("1 = " +rs.getInt("customer_id"));
               System.out.println("2 = '" +rs.getString("customer_number")+"'");
               System.out.println("3 = '" +rs.getString("num_fattura")+"'");
               System.out.println("4 = " +rs.getDate("trx_date"));
               System.out.println("5 = '" +rs.getString("numero_incasso")+"'");
               System.out.println("6 = '" +rs.getString("data_incasso")+"'");
               System.out.println("7 = " +rs.getString("importo_incasso"));
               System.out.println("8 = '" +esito);
               System.out.println("9 = " +idQuota);
               
               
               ps3_call_proc = connORA.prepareCall(sqlInsertIntefaceOA); 
               ps3_call_proc.setInt(1, rs.getInt("customer_id"));
               ps3_call_proc.setString(2, rs.getString("customer_number"));
               ps3_call_proc.setString(3, rs.getString("num_fattura"));
               ps3_call_proc.setDate(4, rs.getDate("trx_date"));
               ps3_call_proc.setString(5, rs.getString("numero_incasso"));
               ps3_call_proc.setString(6, rs.getString("data_incasso"));
               ps3_call_proc.setString(7, rs.getString("importo_incasso"));
//               ps3_call_proc.setDate(8, rs.getDate("start_date_active"));
               ps3_call_proc.setString(8, esito);
               ps3_call_proc.setString(9, idQuota);
                
               ps3_call_proc.executeUpdate(); 
               stepError = "4";
               ps3_call_proc.close();
               System.out.println("Procedura di aggiornamento eseguita.");
            }
            
            rs.close();
            ps.close();
          
           connIRIS.close();
           connORA.close();
            
        } catch (Exception e){
           System.out.println(stepError + " : "+e.getMessage()); 
           e.printStackTrace();
        } finally{
           if (rs != null) {
              try {rs.close();} catch (SQLException e) { /* ignored */}
           }
           if (ps != null) {
              try {ps.close();} catch (SQLException e) { /* ignored */}
           }
           if (connIRIS != null) {
              try {connIRIS.close();} catch (SQLException e) { /* ignored */}
           }
           if (connORA != null) {
              try {connORA.close();} catch (SQLException e) { /* ignored */}
           }
         }
      }
    
    private Connection getConnessioneIRIS(String ambiente) throws Exception{
    System.out.println("********************************************************************************");
    String connectionOracle = "jdbc:IRIS://10.160.81.13:51773/TRAIN";
    if(ambiente.equals("PROD")) connectionOracle = "jdbc:IRIS://istc-livetcsio.fpg.local:51773/LIVE";
    Connection conORA = null;
    System.out.println("Connessione al database IRIS di Produzione ("+connectionOracle+") in corso...");

     //Class.forName("oracle.jdbc.driver.OracleDriver");
    /*
    conORA = DriverManager.getConnection(connectionOracle,"OracleUser","0racleUser1");
    conORA.setAutoCommit(true);
    System.out.println("Connessione al database IRIS completata");
    */
    IRISDataSource ds = new IRISDataSource ();
    ds.setURL (connectionOracle);
    ds.setUser ("OracleUser");
    ds.setPassword ("0racleUser1");
    conORA = ds.getConnection ();
    conORA.setAutoCommit(true);
    System.out.println("==> Connessione al database IRIS completata");
    System.out.println("********************************************************************************");
    return conORA;
    }
   
   
   
   private Connection getConnessioneORA() throws Exception{
      String dbUrl = databaseIP + ":" + databasePort + "/" + databaseSID;
      String connectionOracle = "jdbc:oracle:thin:@"+dbUrl;
      Connection conORA = null;
      System.out.println("Connessione al database ORACLE in corso...");
      Class.forName("oracle.jdbc.driver.OracleDriver");
      conORA = DriverManager.getConnection(connectionOracle,databaseSchema,databasePassword);
      conORA.setAutoCommit(true);
      Locale.setDefault(Locale.US);
      System.out.println("Connessione al database ORACLE completata");
      return conORA;
   }
}


