package it.xview.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Test {

	public static void main(String[] args) {
		String fileName = null;
		String pathFile = System.getenv("FND_SECURE");
		File curDir = new File(pathFile);
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isFile() && f.getName().indexOf(".dbc")>1){
            	fileName=f.getAbsolutePath();
                System.out.println(fileName);
            }
        }
 		File file = new File(fileName);
		FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null){
			    int i = line.indexOf("APPS_JDBC_URL");
			    String val="";
			    if(i==0) {
			    	val = line.substring(line.indexOf("=")+1);
				    System.out.println(val);
			    }
			}		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
