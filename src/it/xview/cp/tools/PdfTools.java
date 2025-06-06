package it.xview.cp.tools;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;



public class PdfTools {
	public static void main(String[] args) throws Exception {
		String file = "C:\\temp\\mandati\\in\\81_20244802283.pdf"; 
		String message = "Convalidato da Dalfelli";
		String msg2 = "dd/mm/yyyy HH:MI:SS";
		String msg3 = "QUI CI VA LA TERZA RIGA";
		
		String  outfile = "C:\\temp\\mandati\\in\\81_20244802283.pdf";  
		int firmaX = 30; 
		int firmaY = 450; 
		boolean isOblique = true;
		PdfTools.stampFirstPage(file, message,msg2,msg3, outfile, firmaX, firmaY, isOblique,Color.DARK_GRAY);
		System.out.println("FINE");
	}

	public static void stampFirstPage( String file, String riga1,String riga2,String riga3, String  outfile, int firmaX, int firmaY, boolean isOblique, Color colore ) throws IOException
    {
        try (PDDocument doc = PDDocument.load(new File(file)))
        {
        	colore = colore==null?Color.BLACK:colore;
            PDFont font = isOblique?PDType1Font.HELVETICA_OBLIQUE:PDType1Font.HELVETICA;// .COURIER_OBLIQUE;
            float fontSize = 10.0f;
            PDPage page = doc.getPage(0); 
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true))
            {
				contentStream.beginText();
                // set font and font size
                contentStream.setFont( font, fontSize );
                contentStream.setNonStrokingColor(colore);
                contentStream.setTextMatrix(Matrix.getTranslateInstance(firmaX,firmaY));
                contentStream.showText(riga1);
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
}
