import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import it.xview.cp.util.ConcurrentHostConnection;

public class SplitCu {
	private final int numPercipienti=200;
	public int exitStatus=0;
	private Connection    connORA;
	private ConcurrentHostConnection chc;
	private String requestId;
	private String sPath = "C:\\temp\\cu\\split\\";
	private String sFileName ="CU_81_2022_A";
	private String recA;
	private String recB;
	private String recZ;
	private String annoRedd="2022";
	private String sqlRecA ="select riga from xxh_cu_rec where ANNOREDD=:1 and TIPORECORD='A'";
	private String sqlRecB ="select riga from xxh_cu_rec where ANNOREDD=:1 and TIPORECORD='B'";
	private String sqlRecZ ="select riga from xxh_cu_rec where ANNOREDD=:1 and TIPORECORD='Z'";
	private String sqlNumFile ="select round(round((max(idriga)-2)/"+numPercipienti +",2)+0.99,0) maxD from xxh_cu_rec where ANNOREDD=:1 and TIPORECORD='D'";
	private String sqlRec ="select riga from xxh_cu_rec where ANNOREDD=2022 and TIPORECORD not in('A','B','Z')  and idriga BETWEEN :1 and :2 order by idriga";
	private String sqlStartEnd = "SELECT min(idriga) I_START, max(idriga)-1 I_END  FROM xxh_cu_rec where ANNOREDD=2022 and TIPORECORD='D' and idriga BETWEEN ";
	public SplitCu(String p1, String p2) {
		try {
	        chc = new ConcurrentHostConnection(p1,p2);
	        connORA = chc.getConnection();
	        HashMap arguments =chc.getConcurrentParameter();
	        requestId = String.valueOf(chc.getRequestId());
		} catch (Exception e) {
			e.printStackTrace();
			this.exitStatus=1;
		}
	}
	public static void main(String[] args) throws Exception{
		String arg0 ="/PRODUZIONE";
//		String arg1 = "PRE FCP_REQID=1 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=5261 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"T\"";
		String arg1 = "PROD FCP_REQID=31966212 FCP_LOGIN=\"APPS/g3mpr0da9p5\" FCP_USERID=5261 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"T\"";
//		String arg0=args[0];
//		String arg1=args[1];  
		
		SplitCu sc = new SplitCu(arg0,arg1);
		sc.run();
        System.out.println("FINE");
        System.exit(sc.exitStatus);
    }
	private void run() throws  Exception {
		String sNumFile = chc.getSingleValue(sqlNumFile, annoRedd);
		System.out.println("Verranno creati " + sNumFile + " file");
		String recA = chc.getSingleValue(sqlRecA, annoRedd);
		String recB = chc.getSingleValue(sqlRecB, annoRedd);
		String recZ = chc.getSingleValue(sqlRecZ, annoRedd);
		int iStart=3;
		int iNewEnd=2;
		for(int i=0;i<Integer.parseInt(sNumFile);i++) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(sPath+sFileName+"_"+i+".txt", true));
			writer.write(recA);writer.newLine();
			writer.write(recB);writer.newLine();
			int iEnd = (i+1)*numPercipienti;
			iStart=iNewEnd+1;
			System.out.println("File " + i);
//			System.out.println("     Start = " + iStart);
//			System.out.println("     End =  " + iEnd);
			HashMap<String,String> sStartEnd = chc.getSingleRow(sqlStartEnd+iStart+" and "+iEnd, null);
//			iStart=iNewEnd+1;
			PreparedStatement ps =connORA.prepareStatement(sqlRec);
			ps.setString(1, sStartEnd.get("i_start"));
			ps.setString(2, sStartEnd.get("i_end"));
			iNewEnd=Integer.parseInt(sStartEnd.get("i_end"));
			System.out.println("     i_start = " + sStartEnd.get("i_start"));
			System.out.println("     i_end =  " + sStartEnd.get("i_end"));
//			System.out.println("     iNewEnd =  " + iNewEnd);
			ResultSet rs = ps.executeQuery();

			while(rs.next()) {
				writer.write(rs.getString("riga"));writer.newLine();
				//if(i==2)System.out.println(rs.getString("riga"));
			}
			writer.write(recZ);writer.newLine();
			writer.flush();
			rs.close();
			ps.close();
		}
		
		chc.closeConnection();
	}

}
