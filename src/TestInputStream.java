import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;


public class TestInputStream {
	String fn = "C:\\temp\\cu\\2022\\pdf\\0_CUR23.pdf";//"C:\\temp\\TestPDF.pdf";
	public static void main(String[] args) {
		
		TestInputStream tsi = new TestInputStream();
		tsi.run();
	}
    public void run() {
        try{
            //String arg = args[0];
        	File initialFile = new File(fn);
            //jpg
            InputStream in =  new FileInputStream(initialFile);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[131072];
            int n = 0;
            System.out.println("-------------------------------------");
            while (-1!=(n=in.read(buf)))
            {
               out.write(buf, 0, n);
               System.out.println(buf);

            }
            System.out.println("-------------------------------------");
            out.close();
            in.close();
            byte[] response = out.toByteArray();
            System.out.println("*-------------------------------------");
            System.out.println(response);
            System.out.println("*-------------------------------------");
            
            FileOutputStream fos = new FileOutputStream(fn + "_2.pdf");
            fos.write(response);
            fos.close();
         }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	public TestInputStream() {
    	
    }

}