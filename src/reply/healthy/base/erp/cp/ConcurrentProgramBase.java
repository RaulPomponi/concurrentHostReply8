package reply.healthy.base.erp.cp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import oracle.apps.fnd.cp.request.CpContext;
import oracle.apps.fnd.cp.request.JavaConcurrentProgram;
import oracle.apps.fnd.util.NameValueType;
import oracle.apps.fnd.util.ParameterList;
import reply.healthy.base.erp.cp.dao.OutLogFilesDAO;
import reply.healthy.base.erp.cp.log.MyLogger;
import reply.healthy.base.erp.cp.log.MyOuter;

public abstract class ConcurrentProgramBase implements JavaConcurrentProgram {
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ITALIAN);
  
  private HashMap<String, String> cpParameters = new HashMap<>();
  
  private String pHost;
  
  private String pPort;
  
  private String pService;
  
  private int exitCode = 0;
  
  protected String concurrentId;
  
  protected MyLogger log = MyLogger.getInstance();
  
  protected MyOuter out = MyOuter.getInstance();
  
  protected Connection conn;
  
  private boolean isErrConn =false;
  
  public void runProgram(CpContext cpContext) {
    try {
      this.log.setLogFile(cpContext.getLogFile());
      this.out.setOutFile(cpContext.getOutFile());
      ParameterList parameterList = cpContext.getParameterList();
      while (parameterList.hasMoreElements()) {
        NameValueType parameter = parameterList.nextParameter();
        this.cpParameters.put(parameter.mName, parameter.mValue);
      } 
      getCommonParameters();
      this.concurrentId = String.valueOf(cpContext.getReqDetails().getRequestId());
      this.conn = cpContext.getJDBCConnection();
      getParameters();
      doService();
    } catch (Exception e) {
      this.log.error(e.getMessage(), e);
      updateExitCode(2);
    } 
    cpContext.getReqCompletion().setCompletion(this.exitCode, "");
  }
  
  public void runProgramHost(String[] args) {
    try {
      for (int i = 0; i < args.length; i++) {
        this.cpParameters.put(String.valueOf(i), args[i]);
       // System.out.println(i + " - ["+String.valueOf(i) + "]-[" +args[i]+"]");
      }
      System.out.println("args0 = " + args[0]);
      System.out.println("args1 = " + args[1]);
      String hpAppsUserPwd = getParam(null, 1);
      this.concurrentId = getParam(null, 4);
      if(getParam("P_HOST", 5)==null || getParam("P_HOST", 5).length()==0) {
    	  System.out.println("PARAMETRI PASSATI IN MODO ERRATO");
//    	  if("/data2/PROD/apps/apps_st/appl/xxh/12.0.0/bin/XXHFS_CpInviaDocumentiPec".equals(args[0])) {
    	  if(args[0].indexOf("data2/PROD")>0) {
    		  System.out.println("Connessione al DB di Produzione");
    		  this.isErrConn=true;
    		    hpAppsUserPwd="APPS/g3mpr0da9p5";
    		    this.pHost = "exa01-scan.exadata.policlinicogemelli.it"; 
    		    this.pPort = "1521" ;
    		    this.pService = "PROD" ;
    		    this.cpParameters.put("P_PATH_PDF", "/data2/PROD/interfacce/fax_server/");
    			String s=args[1];//"XXHFS_CpInviaDocumentiPec FCP_REQID=21155250 FCP_LOGIN=\"APPS/g3mpr0da9p5\" FCP_USERID=3796 FCP_USERNAME=\"SB001317\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0  \"exa01-scan.exadata.policlinicogemelli.it\" \"1521\" \"PROD\" \"TEST/data2/PROD/interfacce/fax_server/\"";
    			int p1 = s.indexOf("FCP_REQID=") + "FCP_REQID=".length();
    			int p2 = s.indexOf(" ",p1);
    		    this.concurrentId=s.substring(p1, p2);
      			System.out.println("concurrentId : -"+this.concurrentId+"-");
//    	  }else if("/data1/PRE/apps/apps_st/appl/xxh/12.0.0/bin/XXHFS_CpInviaDocumentiPec".equals(args[0])) {
    	  }else if(args[0].indexOf("data1/PRE")>0) {
    		 System.out.println("Connessione al DB di PRE");
  		    this.isErrConn=true;
  		    hpAppsUserPwd="APPS/ASR0m1C1pUt4098ARu8w";
  		    this.pHost = "oebs12pre.rm.unicatt.it"; 
  		    this.pPort = "1523" ;
  		    this.pService = "PRE" ;
  		    this.cpParameters.put("P_PATH_PDF", "/data1/PRE/interfacce/fax_server/");
  			String s=args[1];//"XXHFS_CpInviaDocumentiPec FCP_REQID=21155250 FCP_LOGIN=\"APPS/g3mpr0da9p5\" FCP_USERID=3796 FCP_USERNAME=\"SB001317\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0  \"exa01-scan.exadata.policlinicogemelli.it\" \"1521\" \"PROD\" \"TEST/data2/PROD/interfacce/fax_server/\"";
  			int p1 = s.indexOf("FCP_REQID=") + "FCP_REQID=".length();
  			int p2 = s.indexOf(" ",p1);
  		    this.concurrentId=s.substring(p1, p2);
  			System.out.println("concurrentId : -"+this.concurrentId+"-");
    	  }else {
    		 System.out.println("Connessione al DB di TEST");
  		    this.isErrConn=true;
  		    hpAppsUserPwd="APPS/A2p0p2s2TE5t1G3m377i";
  		    this.pHost = "oebs12t.unicatt.rm.it"; 
  		    this.pPort = "1522" ;
  		    this.pService = "TESTR12" ;
  		    this.cpParameters.put("P_PATH_PDF", "/data1/TESTR12/interfacce/fax_server/");
  			String s=args[1];//"XXHFS_CpInviaDocumentiPec FCP_REQID=21155250 FCP_LOGIN=\"APPS/g3mpr0da9p5\" FCP_USERID=3796 FCP_USERNAME=\"SB001317\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0  \"exa01-scan.exadata.policlinicogemelli.it\" \"1521\" \"PROD\" \"TEST/data2/PROD/interfacce/fax_server/\"";
  			int p1 = s.indexOf("FCP_REQID=") + "FCP_REQID=".length();
  			int p2 = s.indexOf(" ",p1);
  		    this.concurrentId=s.substring(p1, p2);
  			System.out.println("concurrentId : -"+this.concurrentId+"-");
    	  }
    	  
      }else {
    	  getCommonParameters();
      }
      this.conn = newJDBCConnection(hpAppsUserPwd);
      OutLogFilesDAO outLogFilesDAO = new OutLogFilesDAO(this.conn);
      System.out.println("concurrentId="+this.concurrentId);
      String outFile = outLogFilesDAO.shorcut(this.concurrentId).getOutFile();
      this.out.setOutFile(outFile);
      getParameters();
      doService();
    } catch (Exception e) {
      this.log.error(e.getMessage(), e);
      updateExitCode(2);
    } finally {
      try {
        if (this.conn != null) {
          this.conn.close();
          this.log.debug("Connessione CHIUSA", new Object[0]);
        } 
      } catch (Exception err) {
        err.printStackTrace();
        this.log.error("Errore close:" + err.getMessage(), err);
      } 
    } 
    if (this.exitCode > 1)
      System.exit(1); 
    System.exit(0);
  }
  
  protected String getParam(String cpKey, int argIndex) {
    String value, key = String.valueOf(argIndex);
    if (this.cpParameters.containsKey(cpKey)) {
      key = cpKey;
      value = this.cpParameters.get(key);
    } else if (this.cpParameters.containsKey(key)) {
      value = this.cpParameters.get(key);
    } else {
      throw new IllegalArgumentException(String.format("Parameter %s not found", new Object[] { key }));
    } 
    return value;
  }
  
  protected Date getParamDate(String cpKey, int argIndex) {
    String value = getParam(cpKey, argIndex);
    if (!"".equals(value) && value != null)
      try {
        return SDF.parse(value);
      } catch (Exception e) {
        throw new IllegalArgumentException(String.format("Failed parse of date %s", new Object[] { value }));
      }  
    return null;
  }
  
  protected void getCommonParameters() {
    this.pHost = getParam("P_HOST", 5);
    this.pPort = getParam("P_PORT", 6);
    this.pService = getParam("P_SERVICE", 7);
  }
  
  private Connection newJDBCConnection(String hpAppsUserPwd) throws ClassNotFoundException, SQLException {
    StringTokenizer t = new StringTokenizer(hpAppsUserPwd, "/");
    String user = t.nextToken();
    String password = t.nextToken();
    Formatter formatter = new Formatter();
    String connString = formatter.format(
        "jdbc:oracle:thin:@%s:%s/%s", new Object[] { this.pHost, 
          this.pPort, 
          this.pService }).toString();
    formatter.close();
    System.out.println("CONNESSIONE ("+connString +"),"+ user);
    String driverName = "oracle.jdbc.driver.OracleDriver";
    Class.forName("oracle.jdbc.driver.OracleDriver");
    Connection conn = DriverManager.getConnection(connString, user, password);
    conn.setAutoCommit(false);
    return conn;
  }
  
  protected void updateExitCode(int newExitCode) {
    if (newExitCode > this.exitCode)
      this.exitCode = newExitCode; 
  }
  
  protected abstract void doService() throws Exception;
  
  protected abstract void getParameters();
}
