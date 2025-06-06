package it.xview.cp;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
/*
 *     	private String cpName = "XXH_CONVALIDA_MANDATI_CP";
 *   	private String cpDesc = "XXH: Convalida Mandati";
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;

import it.xview.cp.util.ConcurrentHostConnection;

public class ConvalidaMandatiCP {
	public int exitStatus=0;
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

	private String exitMsg = "Processo di convalida concluso correttamente.";
	private final String exitMsgDefault = "Processo di convalida concluso correttamente.";
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getProtocollo() {
		return protocollo;
	}

	public void setProtocollo(String protocollo) {
		this.protocollo = protocollo;
	}

	private Connection    connORA;
	private ConcurrentHostConnection chc;
	private String requestId;
	private String sOrgId="81";
	private String ambiente="";
	private String userId;
	private String protocollo="0";
	private String riga1=null;
	private String riga2=null;
	private String riga3=null;
	private String directory=null;
	private int firmaX=30;
	private int firmaY=450;
//	private boolean isOblique=true;
	private Color colore = Color.DARK_GRAY;
	String step="";

	public ConvalidaMandatiCP(String p1, String p2) {
		try {
	        chc = new ConcurrentHostConnection(p1,p2);
	        connORA = chc.getConnection();
	        HashMap arguments =chc.getConcurrentParameter();
	        sOrgId = (String)arguments.get("P_ORG_ID");
	        protocollo = (String)arguments.get("P_PROTOCOLLO");
	        chc.setOrg(Integer.parseInt(sOrgId) );
	        System.out.println("ORG_ID = " +sOrgId);
	        requestId = String.valueOf(chc.getRequestId());
	        userId=chc.getSingleValue("select REQUESTED_BY from fnd_concurrent_requests where request_id="+requestId, null);
	        exitMsg +="Richiesta concorrente " + requestId;
	        /*
	        String sql = "select xxx_gestione_mandati.get_riga1() RIGA1, xxx_gestione_mandati.get_riga2() RIGA2, xxx_gestione_mandati.get_riga3() RIGA3, xxx_gestione_mandati.get_path_mandati() DIR from dual";
	        HashMap<String,String> dual = chc.getSingleRow(sql, null);
	        riga1=dual.get("riga1");
	        riga2=dual.get("riga2");
	        riga3=dual.get("riga3");
	        directory=dual.get("dir");
        	*/
	        setPdfVar();
        	ambiente="TEST";
	        if(p1.indexOf("PROD")>0) ambiente="PROD"; 
	        setProtocollo((String)arguments.get("P_PROTOCOLLO"));
		} catch (Exception e) {
			e.printStackTrace();
			this.exitStatus=1;
			this.exitMsg="Errore nel processo di firma: " + e.getMessage() + " - Richiesta concorrente " + requestId;
		}
	}

	private void setPdfVar() throws SQLException {
		CallableStatement stmt;
		stmt = connORA.prepareCall("call XXX_GESTIONE_MANDATI.GET_INFO_CONVALIDA (?,?,?,?,?,?,?,?)");
		stmt.setString(1,userId);
		stmt.setString(2,sOrgId);
		stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
		stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
		stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
		stmt.registerOutParameter(6, java.sql.Types.VARCHAR);
		stmt.registerOutParameter(7, java.sql.Types.VARCHAR);
		stmt.registerOutParameter(8, java.sql.Types.VARCHAR);
		stmt.executeUpdate();
		riga1=stmt.getString(3);
		riga2=stmt.getString(4);
		riga3=stmt.getString(5);
		directory=stmt.getString(6);
		firmaX=Integer.parseInt( stmt.getString(7) );
		firmaY=Integer.parseInt( stmt.getString(8) );
		stmt.close();
		
		
	}

	public static void main(String[] args) {
		String arg0=args[0];
		String arg1=args[1];  

//		String arg0 ="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_CONVALIDA_MANDATI";
//		String arg1="XXH_CONVALIDA_MANDATI FCP_REQID=31188307 FCP_LOGIN=\"APPS/A2p0p2s2TE5t1G3m377i\" FCP_USERID=2115 FCP_USERNAME=\"00647412\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"841\" \"81\"";
//		String arg0="/data1/PRE/apps/apps_st/appl/gl/12.0.0/bin/XXH_FIRMA_ARSS_CP";
//		String arg1="XXH_FIRMA_ARSS_CP FCP_REQID=21994299 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=3135 FCP_USERNAME=\"DG\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"87\" \"81\"";
		ConvalidaMandatiCP obj = new ConvalidaMandatiCP(arg0, arg1);
		try {
			obj.run();
		} catch (Exception e) {
			System.out.println("Errore Convalida2");
			e.printStackTrace();
			if(obj.exitStatus==0) obj.exitStatus=2;
			
			if(obj.exitMsg.equals(obj.exitMsgDefault))obj.setExitMsg("Errore nel processo di convalida: " + e.getMessage() + " - Richiesta concorrente " + obj.getRequestId());

		}
//		obj.setExit();
		System.out.println("FINE " + obj.exitStatus + " - " + obj.exitMsg);
        System.exit(obj.exitStatus);

	}

	private void run() {
		String cursore = "select SRC_FILE, DST_FILE from xxh.xxh_firma_arss_files where PROTOCOLLO =:1";
		PreparedStatement stmt;
		ResultSet rs = null;
		try {
			stmt = connORA.prepareStatement(cursore);
			stmt.setString(1, protocollo);
			rs= stmt.executeQuery();
			String srcFile="";
			while(rs.next()) {
	    		try {
					srcFile = rs.getString("SRC_FILE");
					String dstFile = rs.getString("DST_FILE");
					step = "CONVALIDA TIMBRO PDF ";
					stampFirstPage(directory+ srcFile, directory+ dstFile);
					step = "CONVALIDA PROCEDURA ";
					String esito = valida(dstFile);
					if(!"OK".equals(esito)) {
						System.out.println("### " + step +" - " + srcFile  +": " + esito +" ###");
						exitStatus = 10;
					}
				} catch (Exception e) {
					System.out.println("### " + step +" - " + srcFile  +": " + e.getMessage() +" ###");
					exitStatus = 10;
				}
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void stampFirstPage( String file, String  outfile) throws IOException
    {
        try (PDDocument doc = PDDocument.load(new File(file)))
        {
        	colore = colore==null?Color.BLACK:colore;
            PDFont font = PDType1Font.HELVETICA_OBLIQUE;//isOblique?PDType1Font.HELVETICA_OBLIQUE:PDType1Font.HELVETICA;// .COURIER_OBLIQUE;
            float fontSize = 8.0f;
            PDPage page = doc.getPage(0); 
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true))
            {
				contentStream.beginText();
                // set font and font size
                contentStream.setFont( font, fontSize );
                contentStream.setNonStrokingColor(colore);
                contentStream.setTextMatrix(Matrix.getTranslateInstance(firmaX,firmaY));
                contentStream.showText(riga1);
                System.out.println(outfile+ " -> " +firmaY + " - " + riga1);
                
                if(riga2!=null) {
                    contentStream.setTextMatrix(Matrix.getTranslateInstance(firmaX,firmaY-fontSize-2));
                    contentStream.showText(riga2);
                    if(riga3!=null) {
                        contentStream.setTextMatrix(Matrix.getTranslateInstance(firmaX,firmaY-(2*fontSize)-4));
                        contentStream.showText(riga3);
                    }
                }
                contentStream.endText();
            }
            doc.save( outfile );
            doc.close();
        }
    }
    private String valida(String nomeFile) throws Exception {
    	HashMap<String,String> validatori = chc.getSingleRow("select VALIDATORE1, VALIDATORE2 from XXH_MANDATI_PDF_DA_FIRMARE where nome_file= :1", nomeFile);
        CallableStatement stmt;
        String esito="KO";
        stmt = connORA.prepareCall("{? = call XXX_GESTIONE_MANDATI.set_convalida(?,?)}");
        stmt.registerOutParameter(1,Types.VARCHAR);
        stmt.setString(2,String.valueOf(nomeFile));
        if(userId.equals(validatori.get("validatore1"))) stmt.setString(3,"DC1");
        if(userId.equals(validatori.get("validatore2"))) stmt.setString(3,"DC2");
        stmt.executeUpdate();
        esito = stmt.getString(1);
        return esito ;
    }

}
