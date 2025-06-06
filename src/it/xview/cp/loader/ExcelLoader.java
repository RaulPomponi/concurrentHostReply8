package it.xview.cp.loader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelLoader {

	private String nomeFile;
	private String[] colonne = new String[100];

	public ExcelLoader(String sFile) {
		this.nomeFile=sFile;
	}

	public static void main(String[] args) {
		String nomeFile = "C:\\Users\\RaulPomponi\\Documents\\Gemelli\\CR\\STS FPG\\Flusso1978.xlsx";
		ExcelLoader el = new ExcelLoader(nomeFile);
		System.out.println("Inizio");
		ArrayList<Cella[]> foglio = el.getAll();
		System.out.println("Foglio caricato in memoria");
		el.printFoglio(foglio);
		System.out.println("Fine");
	}

	private ArrayList<Cella[]> getAll() {
		 ArrayList<Cella[]> foglio = new  ArrayList<Cella[]>();
		try {
		    //POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(nomeFile));
		    //org.apache.poi.xssf.streaming.SXSSFWorkbook wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook();
			Workbook wb = WorkbookFactory.create(new File(nomeFile));
		    org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
		    org.apache.poi.ss.usermodel.Row row;
		    org.apache.poi.ss.usermodel.Cell cell;

		    int rows; // No of rows
		    rows = sheet.getPhysicalNumberOfRows();

		    int cols = 0; // Numero di colonna
		    int tmp = 0;

		    // Questo trucco garantisce di ottenere i dati correttamente anche se non iniziano dalle prime righe
		    /*
		    for(int i = 0; i < 10 || i < rows; i++) {
		        row = sheet.getRow(i);
		        if(row != null) {
		            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
		            if(tmp > cols) cols = tmp;
		        }
		    }
		     */
		    row = sheet.getRow(0); // la prima riga è l'intestazione
            for(int c = 0; c < cols; c++) {
                cell = row.getCell((short)c);
                colonne[c] = cell.getStringCellValue();
            }
		    
		    for(int r = 1; r < rows; r++) {
		        row = sheet.getRow(r);
		        Cella[] riga =new Cella[colonne.length];
		        if(row != null) {
		            for(int c = 0; c < cols; c++) {
		                cell = row.getCell((short)c);
		                Cella cella = new Cella();
		                if(cell != null) {
		                    cella.setNome(colonne[c]);
		                    cella.setValore(cell.getStringCellValue());
		                }else {
		                    cella.setNome(colonne[c]);
		                    cella.setValore(null);
		                }
		                riga[r-1]=cella;
		            }
		            foglio.add(riga);
		        }
		    }
		} catch(Exception ioe) {
		    ioe.printStackTrace();
		}		return null;
	}
	public void printFoglio(ArrayList<Cella[]> foglio) {
		for(Cella[] righe : foglio) {
			for(Cella cella : righe) {
				System.out.println(cella.toString());
			}
			System.out.println("************************************************");
		}
	}

}
