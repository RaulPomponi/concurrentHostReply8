package it.xview.cp;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;


public class CUPdfManager {
	public static void main(String[] args) throws IOException {
		System.out.println("RUN");
		CUPdfManager cuPdfManager = new CUPdfManager();
		//cuPdfManager.setCodiceAteco("881000");
		//String nome=cuPdfManager.getName("C:\\temp\\z\\CUR22.pdf");
		//System.out.println("Nome = "+nome);
		cuPdfManager.setTempPath("C:\\temp\\z\\");
		cuPdfManager.setFullPdfFileName("C:\\temp\\z\\CUR22.pdf");
		cuPdfManager.setCodFiscIntermediario("   ");
		cuPdfManager.setFirmaComunicazione("   ");
		cuPdfManager.setFirmaPresentazione("  ");
		cuPdfManager.setFirma("Marco Elefanti");
		cuPdfManager.setSuffissoCU("81_2021_");
		//cuPdfManager.makePrimaPagina();
		cuPdfManager.run();
		
		System.out.println("STOP");
	}
	private int orgId;
	private String tempPath;
	private String fullPdfFileName="";
	private String firma;
	private String suffissoCU="";
	private String codiceAteco = "";
	private String codFiscIntermediario=""; 
	private String firmaComunicazione=""; 
	private String firmaPresentazione="";
	private int firmaX= 320;
	private int firmaY=365;
	private List<String> lista =new ArrayList<String>();
	public final String INTESTAZIONE="INTESTAZIONE";
	public CUPdfManager() {
	}
	public void run() throws IOException {
		System.out.println("Inizio processo");
		String fileCopied = fullPdfFileName.replace(".pdf", "_OK.pdf");
		Path srcF = (Path)Paths.get(fullPdfFileName);
		Path destF = (Path)Paths.get(fileCopied);
		Files.copy(srcF,destF, StandardCopyOption.REPLACE_EXISTING);

		String startsPageComma = getStartsPageComma(fullPdfFileName);
		System.out.println("Variabili di lavoro:");
		System.out.println("OrgId '"+orgId+"'");
		System.out.println("TempPath '"+tempPath+"'");
		System.out.println("FullPdfFileName '"+fullPdfFileName+"'");
		System.out.println("Firma '"+firma+"'");
		System.out.println("suffissoCU '"+suffissoCU+"'");
		System.out.println("codiceAteco '"+codiceAteco+"'");
		System.out.println("codFiscIntermediario '"+codFiscIntermediario+"'");
		System.out.println("firmaComunicazione '"+firmaComunicazione+"'");
		System.out.println("firmaPresentazione '"+firmaPresentazione+"'");
		makePrimaPagina();
		System.out.println("**Creata prima pagina ");

		List<String> nomiFile = splitPdfCU(fullPdfFileName, startsPageComma, tempPath);
		renameAllFile(tempPath, tempPath,nomiFile);
		System.out.println("Fine processo");
	}
	public List<String> splitPdfCU(String fullPdfFileName, String startsPageComma, String tempPath) {
		String[] starts = startsPageComma.split(",");
		List<String> nomiFile = new ArrayList<String>();
		File pdfFile = new File(fullPdfFileName);
		int fromPage=0;
		int toPage=0;
		
		try {
			PDDocument pdfDocument = PDDocument.load(pdfFile);
//			PDFTextStripper reader = new PDFTextStripper();
//			int k=1;

			
			for(int i=0;i<starts.length;i++) {//1369  starts.length
				try {
					Splitter splitter = new Splitter();
					fromPage=Integer.parseInt(starts[i]);
					toPage=Integer.parseInt(starts[i+1]);
					splitter.setStartPage(fromPage);
					splitter.setEndPage(toPage);
					splitter.setSplitAtPage(toPage - fromPage  );

					List<PDDocument> lst =splitter.split(pdfDocument);

					PDDocument pdfDocPartial = lst.get(0);
					String fileName = tempPath+"TEMP_"+suffissoCU+ i +".pdf";
					File f = new File(fileName);
					pdfDocPartial.save(f);
					String blank = "pag.";
					doIt(fileName, blank , fileName);
					setFirmaX(320);
					setFirmaY(280);
					firma(fileName, firma , fileName,true);
					pdfDocPartial.close();
					nomiFile.add(fileName);
				}catch(ArrayIndexOutOfBoundsException aie) {
					System.out.println("Si è verificato un errore di indice nel documento " + i +" da "+fromPage+" a "+toPage);
				}
			}
			pdfDocument.close();
		} catch (Exception e) {
			System.out.println("Exception splitPdfCU : "+ e.getMessage());
		}
		
		return nomiFile;
	}
	/*
	 * Sbianchetta e mette il numero di pagina corretto
	 */
    @SuppressWarnings("deprecation")
	public void doIt( String file, String message, String  outfile ) throws IOException
    {
        try (PDDocument doc = PDDocument.load(new File(file)))
        {
            PDFont font = PDType1Font.HELVETICA_BOLD;
            float fontSize = 8.0f;
            int i=0;
            for( PDPage page : doc.getPages() )
            {
            	i++;
                PDRectangle pageSize = page.getMediaBox();
                float stringWidth = font.getStringWidth( message )*fontSize/1000f;
                // calculate to center of the page
                int rotation = page.getRotation();
                boolean rotate = rotation == 90 || rotation == 270;
                float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
                float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
                float centerX = rotate ? pageHeight/2f : (pageWidth - stringWidth)/2f;
//                float centerY = rotate ? (pageWidth - stringWidth)/2f : pageHeight/2f;

                // append the content to the existing stream
                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true))
                {
                    contentStream.setNonStrokingColor(Color.WHITE); //gray background
                    contentStream.fillRect(230, 2, 100, 10);
                    
                    contentStream.beginText();
                    // set font and font size
                    contentStream.setFont( font, fontSize );
                    // set text color to red
                    contentStream.setNonStrokingColor(Color.BLACK);
 //                   contentStream.setNonStrokingColor(200, 200, 200); //gray background
                    if (rotate)
                    {
                        // rotate the text according to the page rotation
                        contentStream.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, centerX, pageHeight-20));
                    }
                    else
                    {
                        contentStream.setTextMatrix(Matrix.getTranslateInstance(520, 2));

                    }
                    contentStream.showText("Pag. "+i);
                    contentStream.endText();
                    
                }
            }

            doc.save( outfile );
        }
    }
/*
 * i percorsi devono contenere lo slash finale:
 * BASE_PATH = "c:\\temp\\pdf\\"
 */
    public void renameAllFile(String srcPath, String destPath, List<String> nomiFile) {
    	  File dir = new File(srcPath);
    	  File[] directoryListing = dir.listFiles();
    	  if (directoryListing != null) {
    	    for (File child : directoryListing) {
    	    	String oldFileName = child.getName();
    	    	if(nomiFile.contains(srcPath+oldFileName)) {
	    	    	String newName=getName(child.getAbsolutePath());
	    	    	child.renameTo(new File(destPath+suffissoCU+newName+".pdf"));
	    	    	lista.add(newName);
    	    	}
//    	      break;
    	    }
    	  }    	
    }

	public String getName(String fileName) {
		String s="";
		String causale=" ";
		try {
			File pdfFile=new File(fileName);
			PDDocument pdDoc = PDDocument.load(pdfFile);
			PDFTextStripper reader = new PDFTextStripper();
			String lines[] = reader.getText(pdDoc).split("\\r?\\n");
			causale=lines[238];
			/*
			for(int i=0;i<lines.length;i++) {
				System.out.println (i+" ) "+lines[i]);
			}
			*/
			for(int i=0;i<lines.length;i++) {
				if(lines[i].indexOf(codiceAteco )>0) {
					s=lines[i+1].split(" ")[0];
					break;
				}
			}
			if(!"A".equals(causale) && !"M".equals(causale)) {
				for(int i=0;i<lines.length;i++) {
					if(lines[i].indexOf(s+" 0 1")==0) {
						causale=lines[i+2];
						break;
					}
				}
			}
			pdDoc.close();
		} catch (Exception e) {
			System.out.println("Exception getName : "+ e.getMessage());
		}
		return s + "_" +causale;
	}
    public void firma( String file, String message, String  outfile, boolean isOblique ) throws IOException
    {
        try (PDDocument doc = PDDocument.load(new File(file)))
        {
            PDFont font = isOblique?PDType1Font.HELVETICA_OBLIQUE:PDType1Font.HELVETICA;// .COURIER_OBLIQUE;
            float fontSize = 12.0f;
            PDPage page = doc.getPage(0); 
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true))
            {
				contentStream.beginText();
                // set font and font size
                contentStream.setFont( font, fontSize );
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.setTextMatrix(Matrix.getTranslateInstance(firmaX,firmaY));
                contentStream.showText(message);
                contentStream.endText();
            }
            doc.save( outfile );
            doc.close();
        }
    }
        public void firmaImg( String file, String img, String  outfile ) throws IOException
        {
            try 
            (PDDocument doc = PDDocument.load(new File(file)))
            {
                PDPage page = doc.getPage(0); 
                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true))
                {
                	PDImageXObject image = PDImageXObject.createFromFile(img, doc);
    				contentStream.drawImage(image, 280,360);
                }
                doc.save( outfile );
                doc.close();
            }
    }
	public String getStartsPageComma(String fullPdfFileName) {
		String s="";
		File pdfFile = new File(fullPdfFileName);
		try {
			PDDocument pdfDocument = PDDocument.load(pdfFile);
			int numPag = pdfDocument.getPages().getCount();
			System.out.println("Il documento è composto da "+numPag +" pagine");
			
			PDFTextStripper reader = new PDFTextStripper();
			int i=1;
			//La prima pagina è la testata
			for(i=1;i<numPag;i++) {//1369
				reader.setStartPage(i);
				reader.setEndPage(i);
				String pageText = reader.getText(pdfDocument);
				
				if(pageText.indexOf("ANAGRAFICI")>0) {
					s+=i+",";
				}
			}
			s+=numPag+1;
		} catch (Exception e) {
			System.out.println("Exception getStartsPageComma : "+e.getMessage());
		}
		return s;
	}
	public void makePrimaPagina() {
		File pdfFile = new File(fullPdfFileName);
		
		try {
			PDDocument pdfDocument = PDDocument.load(pdfFile);
			Splitter splitter = new Splitter();
			splitter.setStartPage(1);
			splitter.setEndPage(1);
			splitter.setSplitAtPage(1);

			List<PDDocument> lst =splitter.split(pdfDocument);

			PDDocument pdfDocPartial = lst.get(0);
			String fileName = tempPath+suffissoCU+INTESTAZIONE +".pdf";
			File f = new File(fileName);
			pdfDocPartial.save(f);
			String blank=" ";
			
			doIt(fileName, blank , fileName);
			pdfDocPartial.close();
			pdfDocument.close();
			/* esiste già nel pdf fornito da AE
			if(codFiscIntermediario!=null && codFiscIntermediario.trim().length()>0) {
				setFirmaX(210);
				setFirmaY(442);
				firma(fileName, codFiscIntermediario , fileName,false);
			}
			*/
			if(firmaPresentazione!=null && firmaPresentazione.trim().length()>0) {
				setFirmaX(390);
				setFirmaY(390);
				firma(fileName, firmaPresentazione , fileName,true);
			}
			if(firmaComunicazione!=null && firmaComunicazione.trim().length()>0) {
				setFirmaX(390);
				setFirmaY(475);
				firma(fileName, firmaComunicazione , fileName,true);
			}
			lista.add(INTESTAZIONE);
		} catch (Exception e) {
			System.out.println("Exception makePrimaPagina : "+ e.getMessage());
		}
	}

	
	
	
	
	
	public String getTempPath() {
		return tempPath;
	}
	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}
	public String getFullPdfFileName() {
		return fullPdfFileName;
	}
	public void setFullPdfFileName(String fullPdfFileName) {
		this.fullPdfFileName = fullPdfFileName;
	}
	public String getFirma() {
		return firma;
	}
	public void setFirma(String firma) {
		this.firma = firma;
	}
	public int getOrgId() {
		return orgId;
	}
	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}
	public String getSuffissoCU() {
		return suffissoCU;
	}
	public void setSuffissoCU(String suffissoCU) {
		this.suffissoCU = suffissoCU;
	}
	public String getCodiceAteco() {
		return codiceAteco;
	}
	public void setCodiceAteco(String codiceAteco) {
		this.codiceAteco = codiceAteco;
	}
	public String getCodFiscIntermediario() {
		return codFiscIntermediario;
	}
	public void setCodFiscIntermediario(String codFiscIntermediario) {
		this.codFiscIntermediario = codFiscIntermediario==null?" ":codFiscIntermediario;
	}
	public String getFirmaComunicazione() {
		return firmaComunicazione;
	}
	public void setFirmaComunicazione(String firmaComunicazione) {
		this.firmaComunicazione = firmaComunicazione==null?" ":firmaComunicazione;
	}
	public String getFirmaPresentazione() {
		return firmaPresentazione;
	}
	public void setFirmaPresentazione(String firmaPresentazione) {
		this.firmaPresentazione = firmaPresentazione==null?" ":firmaPresentazione;
	}
	public int getFirmaX() {
		return firmaX;
	}
	public void setFirmaX(int firmaX) {
		this.firmaX = firmaX;
	}
	public int getFirmaY() {
		return firmaY;
	}
	public void setFirmaY(int firmaY) {
		this.firmaY = firmaY;
	}
	public List<String> getLista() {
		return lista;
	}

}
