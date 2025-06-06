package it.xview.cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

// jdk 1.8.0_192
public class NumeraTxtFarmadati {

	public static void main(String[] args) {
		String sAbsoluteFileIn = "C:\\Users\\RaulPomponi\\Documents\\Umesh\\TR010.xml";
		String sAbsoluteFileOut = "C:\\Users\\RaulPomponi\\Documents\\Umesh\\TR010.out.txt";
		NumeraTxtFarmadati ntf = new NumeraTxtFarmadati(sAbsoluteFileIn, sAbsoluteFileOut );
		ntf.run();

	}
	String absoluteFileIn; 
	String absoluteFileOut;
	public NumeraTxtFarmadati(String sAbsoluteFileIn, String sAbsoluteFileOut) {
		this.absoluteFileIn=sAbsoluteFileIn;
		this.absoluteFileOut=sAbsoluteFileOut;
	}
	private void run() {
	    BufferedReader br = null;
	    PrintWriter pw = null; 
	    try {
	         br = new BufferedReader(new FileReader(absoluteFileIn));
	         pw =  new PrintWriter(new FileWriter(absoluteFileOut));
	         String line;
	         int i=1;
	         boolean go =false;
	         while ((line = br.readLine()) != null) {
	        	 if(line.indexOf("<RECORD>")>0) go=true;
	        	 if(line.indexOf("ALLRECORDSDATASET")>0) go=false;
	        	 if(go)pw.println(getZeri(i)+line);
	        	 else pw.println(line);
	        	 if(line.indexOf("</RECORD>")>0) i++;
	                
	         }
	         br.close();
	         pw.close();
	    }catch (Exception e) {
	         e.printStackTrace();
	    }
	    System.out.println("FINE");
	}
	private String getZeri(int num) {
		String sZeri="";
		String sNumero = String.valueOf(num);
		int zeri = 10-sNumero.length();
		for(int i=0;i<zeri;i++) {
			sZeri+="0";
		}
		return sZeri+sNumero;
	}

}
