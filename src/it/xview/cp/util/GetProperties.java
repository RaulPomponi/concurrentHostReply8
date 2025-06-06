package it.xview.cp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class GetProperties {

	public static void main(String[] args) {
		System.out.println("wsdlEI=" + GetProperties.readProperties("wsdlEI") + "|");

	}
	public static String fileProp() {
	    String javaTop = System.getenv("JAVA_TOP");
		return javaTop + "/oracle/apps/fnd/cp/xxh_ws/config_files/config.properties";
	}

		   public static String readProperties(String prop) {
		      System.out.println("Start readProperties");
		      String javaTop = System.getenv("JAVA_TOP");
		      System.out.println("JAVA_TOP="+javaTop);
		      String fileProp = fileProp();
		      String val = new String("");
		      Properties properties = new Properties();
		      try {
		         properties.load(new FileInputStream(fileProp));


		         val = properties.getProperty(prop);
		         if (val == null) {
		            val = "";


		         }else {
		        	 val=val.replace("$JAVA_TOP", javaTop);
		        	 if(javaTop.indexOf("/PROD/")>1) {
		        		 System.out.println("Ambiente di Produzione - "+prop+"="+val);
			        	 val=val.replace("$AMBIENTE", "");
		        	 }else {
		        		 System.out.println("Ambiente di Test - "+prop+"="+val);
			        	 val=val.replace("$AMBIENTE", "_TEST");
		        	 }
		         }
		      } catch (IOException var5) {
		         System.out.println("Eccezione e= " + var5.getMessage());
		      }
		      return val;
		   }

}
