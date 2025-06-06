package xview;
import com.intersystems.jdbc.IRISDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
public class DynamicGetViewFromTKPRE {
   private static String databaseIP = "oebs12pre.rm.unicatt.it";
   private static String databasePort = "1523";
   private static String databaseSID = "PRE";
   private static String databaseSchema = "APPS";
   private static String databasePassword = "ASR0m1C1pUt4098ARu8w";

   public static void main(String[] args) throws Exception {
      DynamicGetViewFromTKPRE t = new DynamicGetViewFromTKPRE();
      Connection connIRIS = null;
      Connection connORA = null;
      Statement st = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      PreparedStatement psDyn = null;
      PreparedStatement psDyn2 = null;
      ResultSet rs = null;
      ResultSet rsDyn = null;
      System.out.println(" INIZIO PROGRAMMA DI ACQUISIZIONE DynamicGetViewFromTKPRE");
      String vwhere = args[8];
      try {
		vwhere = vwhere.replaceAll("\"", "");
		  System.out.println("Condizioni " + vwhere);
	} catch (Exception e) {
		System.out.println("Errore nel recupero della condizione: " + e.getMessage());
		System.out.println("Parametri:");
		for(int i=0; i<args.length;i++) {
			System.out.println(i+" - "+args[i]);
		}
	} 
	  
      String sqlGet = "SELECT nome_query,       query_trakcare,       insert_apps,       tabella_apps,       numero_colonne   from t_query_trackcare where upper(nome_query) = upper('" + vwhere + "')";
      try {
         connIRIS = t.getConnessioneIRIS();
         connORA = t.getConnessioneORA();
         st = connORA.createStatement();
         int rsetw = st.executeUpdate("alter session set NLS_NUMERIC_CHARACTERS='.,' ");
         st.close();
         System.out.println(" sqlGet= " + sqlGet);
         ps = connORA.prepareStatement(sqlGet);
         rs = ps.executeQuery();
         for(boolean var14 = false; rs.next(); var14 = false) {
            System.out.println("Avvio sincronizzazione vista " + rs.getString("tabella_apps"));
            int nr_col = rs.getInt("numero_colonne");
            st = connORA.createStatement();
            st.executeUpdate("DELETE " + rs.getString("tabella_apps"));
            st.close();
            System.out.println(" TABELLA " + rs.getString("tabella_apps") + " SVUOTATA");
            String sqlGetDyn = rs.getString("query_trakcare");
            String sqlSet = rs.getString("insert_apps");
            psDyn = connIRIS.prepareStatement(sqlGetDyn);
            rsDyn = psDyn.executeQuery();
            int righe = 0;
            while(rsDyn.next()) {
               psDyn2 = connORA.prepareStatement(sqlSet);
               int esito;
               for(esito = 1; esito <= nr_col; ++esito) {
                  psDyn2.setString(esito, rsDyn.getString(esito));
               }
               esito = psDyn2.executeUpdate();
               righe += esito;
               psDyn2.close();
            }
            System.out.println(" Fine del ciclo principale ");
            System.out.println(" Tabella " + rs.getString("tabella_apps") + " popolata con " + righe + " record");
         }
         rs.close();
         ps.close();
         connIRIS.close();
         connORA.close();
      } catch (Exception var39) {
         var39.printStackTrace();
      } finally {
         if (rs != null) {            try {
               rs.close();
            } catch (SQLException var38) {               ;            }         }
         if (ps != null) {            try {
               ps.close();
            } catch (SQLException var37) {               ;            }         }
         if (connIRIS != null) {            try {
               connIRIS.close();
            } catch (SQLException var36) {               ;            }         }
         if (connORA != null) {            try {
               connORA.close();
            } catch (SQLException var35) {               ;            }         }
      }

   }

   private Connection getConnessioneIRIS() throws Exception {
      System.out.println("********************************************************************************");
      String connectionOracle = "jdbc:IRIS://10.160.81.13:51773/TRAIN";
      Connection conORA = null;
      System.out.println("Connessione al database IRIS di Produzione (" + connectionOracle + ") in corso...");
      IRISDataSource ds = new IRISDataSource();
      ds.setURL(connectionOracle);
      ds.setUser("OracleUser");
      ds.setPassword("0racleUser1");
      conORA = ds.getConnection();
      conORA.setAutoCommit(true);
      System.out.println("==> Connessione al database IRIS completata");
      System.out.println("********************************************************************************");
      return conORA;
   }

   private Connection getConnessioneORA() throws Exception {
      System.out.println("********************************************************************************");
      String dbUrl = databaseIP + ":" + databasePort + "/" + databaseSID;
      System.out.println("==> dbUrl: " + dbUrl);
      String connectionOracle = "jdbc:oracle:thin:@" + dbUrl;
      Connection conORA = null;
      System.out.println("==> Connessione al database ORACLE in corso...");
      Class.forName("oracle.jdbc.driver.OracleDriver");
      conORA = DriverManager.getConnection(connectionOracle, databaseSchema, databasePassword);
      conORA.setAutoCommit(true);
      Locale.setDefault(Locale.US);
      System.out.println("==> Connessione al database ORACLE completata");
      System.out.println("********************************************************************************");
      return conORA;
   }
}
