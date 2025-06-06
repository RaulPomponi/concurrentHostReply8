package xview;

import java.io.IOException;

import it.xview.cp.CUPdfManager;

public class CUPdfManagerTest {

	public static void main(String[] args) {
		System.out.println("INIZIO");

		String filePath = "C:\\temp\\appo";
		String srcFileName = "CU_232_2021.pdf";
		int orgId = 232;
		String anno = "2021";
		String firmaSostituto = "TEST Guido Rasi";
		String codAteco="721909";
		String cod_fisc_intermediario="DLFMSM62E27L219U";
		String firma_comunicazione="Guido Rasi";
		String firma_presentazione="Massimo Dalfelli";
		
		CUPdfManager pdfManager = new CUPdfManager();
		pdfManager.setOrgId(orgId );
		pdfManager.setTempPath(filePath+"\\");
		pdfManager.setFullPdfFileName(filePath+"\\"+srcFileName);
		pdfManager.setFirma(firmaSostituto);
		pdfManager.setSuffissoCU(orgId + "_" + anno + "_");
		pdfManager.setCodiceAteco(codAteco);
		pdfManager.setCodFiscIntermediario(cod_fisc_intermediario);
		pdfManager.setFirmaComunicazione(firma_comunicazione);
		pdfManager.setFirmaPresentazione(firma_presentazione);
		try {
				pdfManager.run();
		} catch (java.nio.file.NoSuchFileException e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("FINE");

	}

}
