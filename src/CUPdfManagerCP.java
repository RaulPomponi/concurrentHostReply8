
import java.sql.SQLException;
// select  S.VENDOR_ID from AP_SUPPLIERS S where NUM_1099='08802841000' or VAT_REGISTRATION_NUM='08802841000'
import java.util.HashMap;

import it.xview.cp.util.ConcurrentHostConnection;

public class CUPdfManagerCP {
	/*
	 * Riferimenti al concurrent EBS
	 * Eseguibile : 
	 * 			CUPdfManagerCP
	 * 			XXH PAL Consulting Solution
	 * Programma
	 * 			CUPdfManagerCP
	 * 			CU_PDF_MANAGER
	 * 			XXH PAL Consulting Solution
	 * 
	 * 			Parametri
	 * 				ORG_ID
	 * 				ANNO
	 */

	public static void main(String[] args) throws Exception {
		String arg0 ="/PRODUZIONE";
//		String arg1 = "PRE FCP_REQID=1 FCP_LOGIN=\"APPS/ASR0m1C1pUt4098ARu8w\" FCP_USERID=5261 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"T\"";
		String arg1 = "PROD FCP_REQID=1 FCP_LOGIN=\"APPS/g3mpr0da9p5\" FCP_USERID=5261 FCP_USERNAME=\"SB002449\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 \"81\" \"T\"";
	CUPdfManagerCP thisCP = new CUPdfManagerCP();
		thisCP.run(arg0, arg1);
	}

	private void run(String string1, String string2) throws Exception {
		ConcurrentHostConnection chc = new ConcurrentHostConnection(string1,string2);
		int requestId = chc.getRequestId();
		chc.log("Request ID = "+requestId);
//		HashMap mapParam = chc.getConcurrentParameter();
//		chc.printHashMap(mapParam);
		String filePath = "C:\\temp\\cu\\2022\\out\\";//"C:\\temp\\cu\\pdf\\";// chc.getProfileValue("XXH_CU_770_PATH_AE");
		chc.log("Path = " + filePath);
		String orgId = "81";//(String)mapParam.get("ORG_ID");
		String anno = "2022";//(String)mapParam.get("ANNO");
		String sqlConfig = "SELECT cod_ateco, cod_fisc_intermediario, firma_comunicazione, firma_presentazione, firma_sostituto FROM xxh.xxh_dati_770_conf where versione = 0 and org_id=:1";
		HashMap<String,String> mapConfig = chc.getSingleRow(sqlConfig, orgId);

		String srcFileName = "CU_"+orgId+"_"+anno+".pdf";
		CUPdfManager pdfManager = new CUPdfManager();
		pdfManager.setOrgId(Integer.parseInt(orgId) );
		pdfManager.setTempPath(filePath+"/");
		pdfManager.setFullPdfFileName(filePath+"/"+srcFileName);
		pdfManager.setFirma(mapConfig.get("firma_sostituto"));
		pdfManager.setSuffissoCU(orgId + "_" + anno + "_");
		pdfManager.setCodiceAteco(mapConfig.get("cod_ateco"));
		pdfManager.setCodFiscIntermediario(mapConfig.get("cod_fisc_intermediario"));
		pdfManager.setFirmaComunicazione(mapConfig.get("firma_comunicazione"));
		pdfManager.setFirmaPresentazione(mapConfig.get("firma_presentazione"));
		try {
			//pdfManager.run();
			pdfManager.mergePdfCU("C:\\temp\\cu\\2022\\pdf", "C:\\temp\\cu\\2022\\out");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			chc.log(e1.getMessage());
		}
		chc.log("Lista dei PDF creati:");
		for(String str : pdfManager.getLista()) {
			String pCodiceFiscale=str==pdfManager.INTESTAZIONE?"0":str;
			chc.log("DEBUG pCodiceFiscale = "+pCodiceFiscale);
			String pRequestId=String.valueOf(requestId);
			String  pFileName = pdfManager.getTempPath()+pdfManager.getSuffissoCU()+ str+".pdf";
			chc.log("    "+pFileName);
			String[] param = {pCodiceFiscale,pRequestId,pFileName};
			try {
				chc.log("**** XXH_DATI_770_PKG.SET_FILES ("+ pCodiceFiscale+" , "+pRequestId+" , "+pFileName+")");
//				chc.callProc("XXH_DATI_770_PKG.SET_FILES", param);
			} catch (Exception e) {
				chc.log("Errore nella registrazione nella tabella xxh_dati_770_files : "+ pFileName );
				chc.log(e.getMessage());
			}
		}
		
		chc.closeConnection();
		chc.log("FINE");
		
	}

}
