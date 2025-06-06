package it.xview.cp.rest4Buyer;
/*
 * XXH: Invio Vendors 4Buyer
 * XXH_4BUYER_POST_WS
 */
import java.util.HashMap;
import java.util.List;

import it.xview.cp.util.ConcurrentHostConnection;

public class RestOracleTo4Buyer {
	private String arg0;
	private String arg1;
	private int exitStatus=0;
	private String exitMsg = "Processo concluso correttamente.";
	private ConcurrentHostConnection chc;
	private String sOrgId;
	private String sRequestId;

	public static void main(String[] args) {
		String a0 = "/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_4BUYER_POST_WS";
		String a1 = "XXH_4BUYER_POST_WS FCP_REQID=31191263 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=5675 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"472\" \"CODICE_FORNITORE_FPG = 'DR35020'\"";
		RestOracleTo4Buyer obj = new RestOracleTo4Buyer(args[0], args[1]); //a0,a1);//
		try {
			obj.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(obj.exitMsg);
		System.exit(obj.exitStatus);
	}
	public RestOracleTo4Buyer(String string0, String string1) {
		arg0=string0;
		arg1=string1;
		try {
			chc = new ConcurrentHostConnection(arg0,arg1);
		} catch (Exception e) {
			e.printStackTrace();
			exitStatus=1;
		}
		System.out.println("");
		
	}
	private void run() throws Exception {
		sOrgId= (String)chc.getConcurrentParameter().get("P_ORG_ID");
		chc.setOrg(Integer.parseInt(sOrgId));
		String condizioneOracle = (String)chc.getConcurrentParameter().get("P_CONDIZIONE");
		sRequestId= String.valueOf( chc.getRequestId() );
		HashMap<String,String> lov = chc.getProperties("XXH_4BUYER_WS", false);
		String sApplicationSecret = lov.get("ApplicationSecret");// "F4EEAB15-E758-4959-BF5A-9131840DE897";
		String sApplicationKey = lov.get("ApplicationKey");//"2C376702-82BB-45C7-ADD4-DC46521B6B54";
		String apiToken = lov.get("API_TOKEN");//"https://fpgcollaudo.4buyer.it/gateway/api/authentication/token";
		String apiRequest = lov.get("API_IMP_FORNITORE");//"https://fpgcollaudo.4buyer.it/gateway/api/it/Services/OracleIsola/ImportVendor";
		String tableName = lov.get("IMPORT_VISTA");
		String beforeCall = lov.get("IMPORT_BeforCall");
		String afterCall = lov.get("IMPORT_AfterCall");
		String[] paramProc = {sOrgId, sRequestId};
		if(beforeCall!=null) chc.callProc(beforeCall, paramProc);

		RestApiCall rest = new RestApiCall(apiToken, apiRequest, sApplicationSecret, sApplicationKey);
		String autToken = rest.getAuthenticationToken();
        if (autToken != null) {
        	String sql = "SELECT * FROM " + tableName + " WHERE ORG_ID="+ sOrgId +" AND CODICE_FORNITORE_FPG = '" + condizioneOracle +"'";
        	HashMap<String, String> fields =  chc.getSingleRow(sql, null);
        	rest.printHashMap(fields);
        	List<HashMap<String,Object>> result = rest.postVendor(autToken, fields);
        	System.out.println("SQL: " + sql);
        	System.out.println("***************************");
        	rest.printHashMapList(result);
        	System.out.println("***************************");
        	String trueFalse = String.valueOf( result.get(0).get("result") );
        	if(!trueFalse.equalsIgnoreCase("true")) {
        		this.exitStatus = 10;
        		this.exitMsg = (String) result.get(0).get("message");
        	}
        }
		if(afterCall!=null) chc.callProc(afterCall, paramProc);

		
	}
	public int getExitStatus() {
		return exitStatus;
	}
	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}
	public String getExitMsg() {
		return exitMsg;
	}
	public void setExitMsg(String exitMsg) {
		this.exitMsg = exitMsg;
	}
	public String getsOrgId() {
		return sOrgId;
	}
	public void setsOrgId(String sOrgId) {
		this.sOrgId = sOrgId;
	}
	public String getsRequestId() {
		return sRequestId;
	}
	public void setsRequestId(String sRequestId) {
		this.sRequestId = sRequestId;
	}


}
