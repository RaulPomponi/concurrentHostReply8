package it.xview.cp.ordini;

import java.io.File;
import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import it.xview.cp.util.ConcurrentHostConnection;
import oracle.apps.fnd.cp.request.CpContext;
import oracle.jdbc.OracleCallableStatement;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class OUtil {
	private ConcurrentHostConnection chc;
	public OUtil(ConcurrentHostConnection concurrentHostConnection) {
		this.chc = concurrentHostConnection;
	}

	public void checkListaOK (String dir) throws Exception{
		File folder = new File(dir);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		HashMap<String,Object> p = chc.getConcurrentParameter();
		String dataDa = (String)p.get("P_DATA_DA");
		String OrgId = (String)p.get("P_ORG_ID");
		OrgId=OrgId==null?"81":OrgId;
	    PDFTextStripper pdfStripper = null;
	    String parsedText = null;
        int cnt = 0;
        int cntSigned = 0;
        int cntSignFilesOnFSNotInDB = 0;

		if (folder != null) {
	        String[] listOfFiles = folder.list();
	        Arrays.sort((Object[])listOfFiles);
	        logga("NUMERO TOTALE DI FILE DENTRO LA DIRECTORY " + dir + ": " + listOfFiles.length, 0);
	        String fileNameOriginal = "";
	        String fileName = "";
	        for (int i = 0; i < listOfFiles.length; i++) {
	        	File file = new File(dir + "/" + listOfFiles[i]);
				sdf.applyPattern("yyyy/MM/dd");
				String date1 = sdf.format(Long.valueOf(file.lastModified()));
				Date dataFile = (Date)sdf.parse(date1);
//				logga("Check org "+OrgId + " ... "+listOfFiles[i].indexOf(OrgId));
	            Date dateFrom = (Date)sdf.parse(dataDa);
				
				if (dataFile.compareTo(dateFrom) >= 0 && listOfFiles[i].indexOf(OrgId)==0 && file != null && file.exists() && file.getName().indexOf("81_1506574105_") == -1 && file.getName().length() > 4) {
	        	try {
		            fileNameOriginal = file.getName();
		            fileName = "";
		            logga("\nNomeFile da " + fileNameOriginal + " del " + date1 , 0);
		            if (fileNameOriginal.substring(2, 3).equals("_") || fileNameOriginal.substring(3, 4).equals("_")) {
		              try {
		                fileName = fileNameOriginal.substring(0, fileNameOriginal.indexOf("_", fileNameOriginal.indexOf("_") + 1));
		                fileName = fileName.substring(fileName.indexOf("_") + 1);
		              } catch (Exception exceptionErr) {
		                logga("errore File: " + fileNameOriginal, 0);
		              } 
		            } else {
		              fileName = fileNameOriginal.substring(0, fileNameOriginal.indexOf("_"));
		            } 
		            logga("... a " + fileName, 0);
		            if (file.length() == 0L) {
		              String msg = "Il File " + fileNameOriginal + " presente dentro la directory " + dir + " ha zero byte.\n";
		              msg = msg + "---------------------------------------------------------";
		              logga(msg, 0);
		              break;
		            } 
		            if (dataFile.compareTo(dateFrom) >= 0 && (fileNameOriginal.endsWith("_signed.pdf") || fileNameOriginal.endsWith("_signed2.pdf")) && ((fileNameOriginal.substring(0, 4).equals("232_") && OrgId.equals("232")) || (!fileNameOriginal.substring(0, 4).equals("232_") && !OrgId.equals("232")))) {
		              cntSigned++;
	//	              parser = new PDFParser(new FileInputStream(file));
	//	              parser.parse();
	//	              cosDoc = parser.getDocument();
		              pdfStripper = new PDFTextStripper();
		              PDDocument pdDoc = PDDocument.load(file);
		              
		              parsedText = pdfStripper.getText(pdDoc);
		              String text = parsedText;
		              String fileNameMeno = "";
		              if (fileName.length() > 7) {
		                fileNameMeno = fileName.substring(0, 7).concat("-").concat(fileName.substring(7, fileName.length()));
		                fileName = fileName.substring(0, 7).concat(".").concat(fileName.substring(7, fileName.length()));
		              } 
		              String myParsedText = parsedText.substring(0, parsedText.indexOf("Unit"));
		              logga("DEBUG IF NO 232 cerca Unit", 0);
		              myParsedText = myParsedText.replaceAll("\\n", "");
		              if (!parsedText.contains(fileName) && !parsedText.contains(fileNameMeno) && !myParsedText.contains(fileName) && !myParsedText.contains(fileNameMeno)) {
		                if (cnt == 0)
		                	logga("\nLISTA DEI FILE CON ANOMALIA:\n"); 
		                logga("NOME FILE CORROTTO: " + fileNameOriginal + "\n");
		                logga("***********************************************************");
		                logga("fileName " + fileName + " fileNameMeno " + fileNameMeno + " \n");
		                logga(myParsedText);
		                logga("***********************************************************");
		                cnt++;
		                String[] ORGANIZATIONS = getOrganizations();
		                String[] as1 = ORGANIZATIONS;
		                for (int k = 0; k < as1.length; k++) {
		                  String org = as1[k];
		                  logga("org=" + org, 0);
		                  if (parsedText.contains(org)) {
		                    int start = text.indexOf(org);
		                    String paritalParsedText = text.substring(start + org.length());
		                    String nOrdineRev = paritalParsedText.substring(0, paritalParsedText.indexOf(" "));
		                    String nOrdine = "";
		                    String rev = "";
		                    int indexRev = nOrdineRev.indexOf("-");
		                    if (indexRev == -1) {
		                      nOrdine = nOrdineRev.contains(".") ? nOrdineRev.replace(".", "") : nOrdineRev;
		                      rev = "0";
		                    } else {
		                      nOrdine = nOrdineRev.substring(0, indexRev);
		                      rev = nOrdineRev.substring(nOrdineRev.indexOf("-") + 1);
		                    }
		                    logga("CHECK FIRMA");
		                    /*
		                    String signed = readSignature(cpContext, file);
		                    String fileNameNew = nOrdine + "_REV" + rev + "_1_" + signed + ".pdf";
		                    out.writeln("NUM. ORDINE IN PDF: " + dir + fileNameNew + "\n");
		                    out.writeln("-----------------------------------------\n");
		                    break;
		                    */
		                  } 
		                } 
		              } 
		            } 
	        	}catch(Exception e) {
	        		logga("........ Errore File " +file.getName());
	        		logga("........ ........... " + e.getMessage());
	        	}
	          } else {
	        	  //logga("........ File non processato " +file.getName());
	          }
	        } 
	        logga("\nNUMERO DI FILE CORROTTI DENTRO LA DIRECTORY " + dir + ": " + cnt, 0);
	        logga("NUMERO DI FILE FIRMATI CON DATA DI ULTIMA MODIFICA >= [" + dataDa + "] DENTRO LA DIRECTORY " + dir + ": " + cntSigned, 0);
	        logga("NUMERO DI FILE FIRMATI PRESENTI SU FILESYSTEM MA NON SULLA LISTA DB (XXH_FAX_PKG.GET_SIGNED_FILE_NAMES), CON DATA >= [" + dataDa + "]: " + cntSignFilesOnFSNotInDB, 0);
	        logga("\nNUMERO DI FILE CORROTTI DENTRO LA DIRECTORY " + dir + ": " + cnt + "\n");
	        /*
	        if (dir.toUpperCase().contains("/in/".toUpperCase()) && cnt == 0)
	          inviaOrdiniFirmati(cpContext, dateFromSQL); 
	         */
		}		
		
	}
	  private String[] getOrganizations() {
		    String[] ORGANIZATIONS = { 
		        "RIC-", "GMS-", "PIA-", "TEC-", "SIO-", "SIC-", "BIB-", "ISI-", "FIS-", "LOG-", 
		        "FMA-", "SAN-", "CD-", "DMX-", "CIC-", "STA-", "GSD-", "DID-", "DSE-", "FOR-", 
		        "FPC-", "MST-", "FAR-" };
		    Connection conn = null;
		    CallableStatement cs = null;
		    ResultSet rs = null;
		    try {
		      conn = chc.getConnection();
		      cs = conn.prepareCall("{call XXH_FIX_ORDINI_PKG.GET_ORGANIZATIONS(?)}");
		      cs.registerOutParameter(1, -10);
		      cs.executeQuery();
		      rs = ((OracleCallableStatement)cs).getCursor(1);
		      String orgCodes = "";
		      while (rs.next()) {
		        String orgCode = rs.getString("ORGANIZATION_CODE");
		        orgCodes = orgCodes + orgCode + ";";
		      } 
		      if (orgCodes.length() > 0)
		        ORGANIZATIONS = orgCodes.split(";"); 
		      return ORGANIZATIONS;
		    } catch (Exception e) {
		      e.printStackTrace();
		      return null;
		    } finally {
		      if (rs != null)
		        try {
		          rs.close();
		        } catch (Exception e) {
		          return null;
		        }  
		      if (cs != null)
		        try {
		          cs.close();
		        } catch (Exception e) {
		          return null;
		        }  
		    } 
		  }

	private void logga(String string, int i) {
		System.out.println("LOG - " + string);
		
	}
	private void logga(String string) {
		System.out.println("OUT - " + string);
		
	}
}
