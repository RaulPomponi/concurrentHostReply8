package it.xview.cp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class ConcurrentHostConnection {
	final String PRODUZIONE = "PROD";
	final String PRE  = "PRE";
	final String TEST  = "TESTR12";
	final String sJdbc = "jdbc:oracle:thin:@";
	private String dbUrl = null;
	private String user = "APPS";
	private String password = null;
	private Connection conn = null;
	private String prefissoLookup;
	private int requestId=0;
	public int getRequestId() {
		return requestId;
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	private long concurrentProgramId=0;
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ITALIAN);	
	final private String[] connProduzione = {"exa01-scan.exadata.policlinicogemelli.it:1521/PROD","APPS",""};

	// BOLLA
	//	final private String[] connProduzione = {"ebs-p-ora11-scan.db.policlinicogemelli.it:1521/PROD","APPS",""};
	final private String[] connPre = {"oebs12pre.rm.unicatt.it:1523/PRE","APPS",""};
	final private String[] connTest = {"oebs12t.unicatt.rm.it:1522/TESTR12","APPS",""};
	public static void main(String[] args) throws Exception {
		
		String arg0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_PEC_INVIA_ORDINI";
		String arg1="XXH_PEC_INVIA_ORDINI FCP_REQID=31159088 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=3135 FCP_USERNAME=\"DG\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"1\" \"81\" \"81_2303226_REV0_1_signed.pdf\"";
		ConcurrentHostConnection chc = new ConcurrentHostConnection(arg0,arg1);
		chc.getConnection();
//		chc.printHashMap(chc.getConcurrentParameter());
		String profilo = chc.getProfileValue("XXH_CU_770_PATH_AE");
		System.out.println("Profilo: " + profilo);
		chc.closeConnection();

/*
		HashMap<String,String> map = chc.getProperties("IRIS_CONNECTION", true);
		chc.printHashMap(map);
		chc.closeConnection();
*/		
	}
	public String getProfileValue(String nomeProfilo) {
		String profilo = getSingleValue("select FND_PROFILE.VALUE(:1) profilo from dual", nomeProfilo);
		System.out.println("Profilo "+nomeProfilo+" = " +profilo);
		return profilo;
	}
	@SuppressWarnings("unused")
	private ConcurrentHostConnection() {
		System.out.println("Costruttore privato");
	}
	public ConcurrentHostConnection(String arg0, String arg1) throws Exception {
		String[] connString =null;
		if(arg0.indexOf(PRODUZIONE)>0) {
			connString = connProduzione;
			prefissoLookup = "PROD_";
		}else if(arg0.indexOf(PRE)>0){
			connString = connPre;
			prefissoLookup = "PRE_";
		}else if(arg0.indexOf(TEST)>0) {
			connString = connTest;
			prefissoLookup = "TEST_";
		}else {
			throw new Exception("Nessuna connessione associata al parametro " + arg0);
		}
		this.dbUrl= connString[0];//getDbUrl();//connString[0];
		this.user=connString[1];
		this.password=getPwd(arg1);
		
		String sRequestId=arg1.substring(
				arg1.indexOf("FCP_REQID")+10);
		sRequestId = sRequestId.substring(0,sRequestId.indexOf(" "));
		requestId = Integer.parseInt(sRequestId);
	      System.out.println("RequestId : '" +requestId+"' - Connessione al database ORACLE " +dbUrl +" in corso...");
	      Class.forName("oracle.jdbc.driver.OracleDriver");
	      conn = DriverManager.getConnection(sJdbc+dbUrl,user,password);
	      conn.setAutoCommit(true);
	      Locale.setDefault(Locale.US);
//	      System.out.println("Connessione al database ORACLE completata");
	      setConcurrentProgramId();
//	      System.out.println("ConcurrentProgramId : '" +concurrentProgramId+"'");
	}
	private String getDbUrl() {
		String fileName = null;
		String pathFile = System.getenv("FND_SECURE");
		String s1="";
		String s2="";
		String s3="";
		
		File curDir = new File(pathFile);
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isFile() && ".dbc".equalsIgnoreCase(f.getName().substring(f.getName().length()-4)) ){
            	fileName=f.getAbsolutePath();
                System.out.println(fileName);
            }
        }
 		File file = new File(fileName);
		FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null){
			    int i1 = line.indexOf("DB_HOST");
			    int i2 = line.indexOf("DB_PORT");
			    int i3 = line.indexOf("TWO_TASK");
			    
			    if(i1==0) {
			    	s1 = line.substring(line.indexOf("=")+1);
			    }else 			    if(i2==0) {
			    	s2 = line.substring(line.indexOf("=")+1);
			    }else 			    if(i3==0) {
			    	s3 = line.substring(line.indexOf("=")+1);
			    }


			}		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s1+":"+s2+"/"+s3 ;
	}
	public ConcurrentHostConnection(String arg0, String arg1, boolean isLocalUS) throws Exception {
		String[] connString =null;
		if(arg0.indexOf(PRODUZIONE)>0) {
			connString = connProduzione;
			prefissoLookup = "PROD_";
		}else if(arg0.indexOf(PRE)>0){
			connString = connPre;
			prefissoLookup = "PRE_";
		}else if(arg0.indexOf(TEST)>0) {
			connString = connTest;
			prefissoLookup = "TEST_";
		}else {
			throw new Exception("Nessuna connessione associata al parametro " + arg0);
		}
		this.dbUrl=connString[0];
		this.user=connString[1];
		this.password=getPwd(arg1);
		
		String sRequestId=arg1.substring(
				arg1.indexOf("FCP_REQID")+10);
		sRequestId = sRequestId.substring(0,sRequestId.indexOf(" "));
		requestId = Integer.parseInt(sRequestId);
	      System.out.println("RequestId : '" +requestId+"' - Connessione al database ORACLE " +dbUrl +" in corso...");
	      Class.forName("oracle.jdbc.driver.OracleDriver");
	      conn = DriverManager.getConnection(sJdbc+dbUrl,user,password);
	      conn.setAutoCommit(true);
	      if(isLocalUS)Locale.setDefault(Locale.US);
	      else Locale.setDefault(Locale.ITALIAN);
//	      System.out.println("Connessione al database ORACLE completata");
	      setConcurrentProgramId();
//	      System.out.println("ConcurrentProgramId : '" +concurrentProgramId+"'");
	}
	public Connection getConnection() throws Exception {
	      return conn;
	}
	public void closeConnection() throws SQLException {
		conn.close();
		System.out.println("Connessione chiusa");
	}
	public long getConcurrentProgramId() {
		return concurrentProgramId;
	}
	private void setConcurrentProgramId() throws Exception {
		String sqlCP = "select max(concurrent_program_id) cpi FROM FND_CONCURRENT_REQUESTS where request_id = :1" ;
		PreparedStatement ps = this.conn.prepareStatement(sqlCP);
		ps.setInt(1, this.requestId);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			concurrentProgramId = rs.getLong("cpi");
		}
		rs.close();
		ps.close();
	}
	public HashMap<String,String> getProperties(String sGruppo, boolean hasPrefix) throws Exception {
		if(conn==null) getConnection();
		Statement stmt = null;
      String gruppo = hasPrefix?prefissoLookup+sGruppo:sGruppo;
      HashMap<String,String> map = new HashMap<String,String>();
      String query = "select MEANING NOME, DESCRIPTION VALORE from fnd_lookup_values where LANGUAGE = 'I' AND ENABLED_FLAG = 'Y' AND LOOKUP_TYPE = '"+ gruppo +"' order by lookup_code";
      ResultSet rs=null;
      try {
          stmt = conn.createStatement();
          rs = stmt.executeQuery(query);
          String valore;
          while (rs.next()) {
            valore =  rs.getString("VALORE");
            String nome = rs.getString("NOME");
            map.put( nome,valore);
          }
      } catch (SQLException e ) {
          e.printStackTrace();
      } catch (Exception e ) {
          e.printStackTrace();
      }finally{
        try{
            rs.close();
            stmt.close();
        }catch(Exception e){}
      }
      return map;
	}
	public void printHashMap (HashMap<String,Object> map) {
		map.entrySet().forEach(entry -> {
		    System.out.println(entry.getKey() + " : '" + entry.getValue()+"'");
		});
	}
	public HashMap<String,Object> getConcurrentParameter() throws Exception{
		if(conn==null) getConnection();
		HashMap<String,ConcurrentParameter> map = getConcurrentParameterDesc();
		HashMap<String,Object> mapParam = new HashMap<String,Object>();
		if(!map.isEmpty()) {
			String sqlArguments = "SELECT " +
					"ARGUMENT1, ARGUMENT2, ARGUMENT3, ARGUMENT4, ARGUMENT5, ARGUMENT6, ARGUMENT7, ARGUMENT8, ARGUMENT9, ARGUMENT10, ARGUMENT11, ARGUMENT12, ARGUMENT13, "+
					"ARGUMENT14, ARGUMENT15, ARGUMENT16, ARGUMENT17, ARGUMENT18, ARGUMENT19, ARGUMENT20, ARGUMENT21, ARGUMENT22, ARGUMENT23, ARGUMENT24, ARGUMENT25 "+
					"FROM FND_CONCURRENT_REQUESTS where request_id = :1" ;
			PreparedStatement ps = this.conn.prepareStatement(sqlArguments);
			ps.setInt(1, this.requestId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				map.entrySet().forEach(entry -> {
//					String arg = entry.getKey();
				    try {
						mapParam.put(entry.getValue().getEndUserColumnName(), rs.getString("ARGUMENT"+entry.getValue().getArgument()));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						mapParam.put(entry.getValue().getEndUserColumnName(), e.getMessage());
					}
				});
					
			}
			rs.close();
			ps.close();
		}
		return mapParam;
	}
	private HashMap<String,ConcurrentParameter> getConcurrentParameterDesc() throws SQLException{
		int i=0;
		HashMap<String,ConcurrentParameter> map = new HashMap<String,ConcurrentParameter>();
		PreparedStatement ps = this.conn.prepareStatement(queryParamInfo);
		ps.setLong(1, this.concurrentProgramId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			i++;
			ConcurrentParameter cp = new ConcurrentParameter();
			cp.setColumnSeqNum(rs.getInt("columnSeqNum"));
			cp.setDataType(rs.getString("dataType"));
			cp.setDefaultValue(rs.getString("defaultValue"));
			cp.setDisplayFlag(!rs.getString("displayFlag").equals("N"));
			cp.setEndUserColumnName(rs.getString("endUserColumnName"));
			cp.setRequiredFlag(!rs.getString("requiredFlag").equals("N"));
			cp.setArgument(i);
			map.put(String.valueOf(i), cp);
		}
		rs.close();
		ps.close();
		return map;
	}
		public java.util.Date dateValue(String value) {
		    if (!"".equals(value) && value != null)
		        try {
		          return SDF.parse(value);
		        } catch (Exception e) {
		          throw new IllegalArgumentException(String.format("Failed parse of date %s", new Object[] { value }));
		        }  
		      return null;
		}
	String queryParamInfo = "SELECT \r\n" + 
			"cp.concurrent_program_name CP_Name,  -- The Concurrent Program name\r\n" + 
			"dfcu.end_user_column_name endUserColumnName, -- The real argument name \r\n" + 
			"lv.meaning dataType, -- The data type of argument\r\n" + 
			"ffv.maximum_size, -- The lenght of the argument\r\n" + 
			"dfcu.required_flag requiredFlag, -- The argument required or not\r\n" + 
			"dfcu.display_flag displayFlag, -- The argument displayed or not on Oracle Form \r\n" + 
			"dfcu.default_value defaultValue, -- The default value of the argument\r\n" + 
			"dfcu.column_seq_num columnSeqNum -- The argument sequence number  \r\n" + 
			"FROM fnd_concurrent_programs_vl cp\r\n" + 
			"  LEFT OUTER JOIN fnd_descr_flex_col_usage_vl dfcu \r\n" + 
			"ON dfcu.descriptive_flexfield_name\r\n" + 
			"    ='$SRS$.'||cp.concurrent_program_name\r\n" + 
			"  LEFT OUTER JOIN fnd_flex_value_sets ffv \r\n" + 
			"ON ffv.flex_value_set_id = dfcu.flex_value_set_id\r\n" + 
			"  LEFT OUTER JOIN fnd_lookup_values_vl lv \r\n" + 
			"ON lv.lookup_code = ffv.format_type \r\n" + 
			"AND lv.lookup_type = 				'FIELD_TYPE'\r\n" + 
			"AND lv.enabled_flag = 'Y'\r\n" + 
			"AND lv.security_group_id = 0\r\n" + 
			"AND lv.view_application_id = 0\r\n" + 
			"WHERE  cp.CONCURRENT_PROGRAM_ID = :1\r\n" + 
			"ORDER BY dfcu.column_seq_num\r\n" ; 
    public String getSingleValue(String sql, String param)
    {
      String retVal="";
      ResultSet rs=null;
      PreparedStatement stmt = null;
      try{
        stmt = conn.prepareStatement(sql);
        if(param!=null)        stmt.setString(1,param);
        rs=stmt.executeQuery();
        if(rs.next()) retVal = rs.getString(1);
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
      return retVal;
    }
    public String getLookupValue(String meaning)
    {
      String retVal="";
      String sql = "select DESCRIPTION FROM fnd_lookup_values where LOOKUP_TYPE='STS_WS_CREDENZIALI' and language='I' and meaning = :1";
      ResultSet rs=null;
      PreparedStatement stmt = null;
      try{
        stmt = conn.prepareStatement(sql);
        stmt.setString(1,meaning);
        rs=stmt.executeQuery();
        if(rs.next()) retVal = rs.getString(1);
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
      return retVal;
    }
	public void log(String string) {
		System.out.println(string);
		
	}
	public HashMap<String,String> getSingleRow(String sql, String param) {
		HashMap<String,String> singleRow = new HashMap<String,String>();
	      ResultSet rs=null;
	      PreparedStatement stmt = null;
	      try{
	        stmt = conn.prepareStatement(sql);
	        if(param!=null)        stmt.setString(1,param);
	        rs=stmt.executeQuery();
	        if(rs.next()) {
	        	ResultSetMetaData col = rs.getMetaData();
	        	for(int i=0;i<col.getColumnCount();i++) {
	        		singleRow.put(col.getColumnName(i+1).toLowerCase(), rs.getString(col.getColumnName(i+1)));
	        	}
	        }
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
		
		return singleRow;
	}
	public void callProc (String procFullName, String[] param) throws SQLException {
		String placeHolder="";
		CallableStatement stmt;
		if(param!=null) {
			for(int i=0;i<param.length;i++) {
				placeHolder+=i==0?"?":",?";
			}
			stmt = conn.prepareCall("call "+procFullName+"("+placeHolder+")");
			for(int i=0;i<param.length;i++) {
				stmt.setString(i+1, param[i]);
			}
		}else {
			stmt = conn.prepareCall("call "+procFullName+"()");
		}
        stmt.executeUpdate();
        stmt.close();
	}

	public String callBigProc (String procFullName, String[] param) {
		String s ="OK";
		try {
			String placeHolder="";
			CallableStatement stmt;
			if(param!=null) {
				for(int i=0;i<param.length;i++) {
					placeHolder+=i==0?"?":",?";
				}
				stmt = this.conn.prepareCall("call "+procFullName+"("+placeHolder+")");
				for(int i=0;i<param.length;i++) {
					stmt.setString(i+1, param[i]);
				}
			}else {
				stmt = this.conn.prepareCall("call "+procFullName+"()");
			}
	        stmt.executeUpdate();
	        stmt.close();
	  	} catch (Exception e) {
			s=e.getMessage();
			e.printStackTrace();
		}
		return s;

	}
	
	public String[] callConcurrentProc (String procFullName, String[] param) {
		String s[] = {"OK","0"};
		try {
			String placeHolder="?,?"; //i primi due parametri devono essere sempre {errbuff out varchar2, retcode out number }
			CallableStatement stmt;
			if(param!=null) {
				for(int i=0;i<param.length;i++) {
					placeHolder+=",?";
				}
				stmt = this.conn.prepareCall("call "+procFullName+"("+placeHolder+")");
				stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
				stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
				for(int i=0;i<param.length;i++) {
					stmt.setString(i+2+1, param[i]);
				}
			}else {
				stmt = this.conn.prepareCall("call "+procFullName+"(?,?)");
				stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
				stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
			}
	        stmt.executeUpdate();
	        s[0] = stmt.getString(1);
	        s[1] = stmt.getString(2);
	        stmt.close();
	  	} catch (Exception e) {
			s[1] = "99";
			s[0]=e.getMessage();
			e.printStackTrace();
		}
		return s;

	}
	
	
	
	private String getPwd(String string) {
		int s1 = string.indexOf("FCP_LOGIN")-1;
		int s2 ="FCP_LOGIN=\\\"APPS/".length();
		string = string.substring(s1+s2);
		s1 = string.indexOf("\"");
		return string.substring(0, s1);
	}
	public void setOrg(int org) throws SQLException {
		String sql = "begin fnd_client_info.set_org_context("+org+");  mo_global.set_policy_context ('S', "+org+"); end;";
		CallableStatement stmt;
		stmt = conn.prepareCall(sql);
        stmt.executeUpdate();
        stmt.close();
		
	}
	public void printSessionParameters() {
	      String sql = "SELECT PARAMETER, VALUE from NLS_SESSION_PARAMETERS";
	      ResultSet rs=null;
	      PreparedStatement stmt = null;
	      try{
	        stmt = conn.prepareStatement(sql);
	        rs=stmt.executeQuery();
	        while(rs.next()) {
	        	System.out.println (rs.getString(1) +" = "+rs.getString(2));
	        }
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
	}
	
}
