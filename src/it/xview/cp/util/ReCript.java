package it.xview.cp.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.crypto.spec.SecretKeySpec;
/*
 * Concurrent Host XXH_CRYPTO_CHANGE_PWD
 * P_OLD_PASSWORD
 */
// Password test Xview2021_1
// Password PRE Pre_123456
//Gem_123456
public class ReCript extends TempSecurityStorage {

	public static void main(String[] args) {
		ReCript cp = new ReCript();
		try {
			 cp.run(args[0], args[1]);
//			cp.run("/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_CRYPTO_CHANGE_PWD",
//					"XXH_CRYPTO_CHANGE_PWD FCP_REQID=25906568 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=3796 FCP_USERNAME=\"SB001317\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"Xview2021_1\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void run(String string1, String string2) throws Exception {
		ConcurrentHostConnection chc = new ConcurrentHostConnection(string1,string2);
		HashMap<String,Object> param = chc.getConcurrentParameter();
		String oldPwd = (String)param.get("P_OLD_PASSWORD");//"TEST";
		SecretKeySpec newKey = createSecretKey(chc.getConnection());
		SecretKeySpec oldKey = createSecretKey(oldPwd);
		String sUpdate = "UPDATE fnd_lookup_values " + 
				"set DESCRIPTION = :1 " + 
				"where LOOKUP_TYPE='STS_WS_CREDENZIALI' and language='I' and meaning=:2";
		String sql = "select MEANING, DESCRIPTION FROM fnd_lookup_values " + 
				"where LOOKUP_TYPE='STS_WS_CREDENZIALI' and language='I' and " + 
				"meaning like '%_PWD' and description is not null";
		String sCount = chc.getSingleValue("select count(*) from ("+ sql+")", null);
		int iCount = Integer.parseInt(sCount);
		String[][] sUpdParam = new String[2][iCount];
		PreparedStatement ps = chc.getConnection().prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		String s;
		int i=0;
		while(rs.next()) {
			sUpdParam[0][i]=rs.getString("MEANING");
			s=rs.getString("DESCRIPTION");
			s=decrypt(s, oldKey);
			System.out.println("Start recupero : "+ sUpdParam[0][i]);
			s=encrypt(s,newKey);
			sUpdParam[1][i]=s;
			System.out.println("End recupero : "+ sUpdParam[0][i]);
			i++;
		}
		rs.close();
		ps.close();
		
		PreparedStatement psU = chc.getConnection().prepareStatement(sUpdate);
		for(i=0;i<iCount;i++) {
			System.out.println("Start aggiornamento : "+ sUpdParam[0][i]);
			psU.setString(1, sUpdParam[1][i]);
			psU.setString(2, sUpdParam[0][i]);
			psU.executeUpdate();
			System.out.println("End aggiornamento : "+ sUpdParam[0][i]);
		}
		psU.close();
	}

}
