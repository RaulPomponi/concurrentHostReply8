package it.rp.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;

import it.xview.cp.util.ConcurrentHostConnection;

public class Test {
	String arg0 ="/data1/PRE/apps/apps_st/appl/po/12.0.0/bin/XXH_FIRMA_ARSS_CP";
	String arg1="XXH_FIRMA_ARSS_CP FCP_REQID=36371619 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=4295 FCP_USERNAME=\"00647818\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"2284\" \"81\"";
	ConcurrentHostConnection cp;
	public static void main(String[] args) {
		Test obj = new Test();
		try {
			obj.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void run() throws Exception {
		cp =new ConcurrentHostConnection(arg0, arg1);
		String sql = "";
		Statement st = cp.getConnection().createStatement();
		ResultSet rs = st.executeQuery(sql);
		int colCount = rs.getMetaData().getColumnCount();
		String separator = "|";
		for(int i=0;i<colCount;i++) {
			String riga = i==0?rs.getMetaData().getColumnName(i+1):separator+rs.getMetaData().getColumnName(i+1);
			System.out.println(riga);
		}
		while(rs.next()) {
			for(int i=0;i<colCount;i++) {
				String riga = i==0?rs.getObject(i+1).toString():separator+rs.getObject(i+1).toString();
				System.out.println(riga);
			}
		}
		rs.close();
		st.close();
		cp.closeConnection();
	}

}
