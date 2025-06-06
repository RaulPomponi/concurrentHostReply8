package xview;

public class test {

	public static void main(String[] args) {
			test.esegui("CUPdfManagerCP FCP_REQID=22926694 FCP_LOGIN=\"APPS/g3mt35tapp5\" FCP_USERID=1216 FCP_USERNAME=\"CONCORRENTE\" FCP_PRINTER=\"noprint\" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0  \"81\" \"2028\"");

	}

	private static void esegui(String string) {
		System.out.println(string);
		int s1 = string.indexOf("FCP_LOGIN")-1;
		int s2 ="FCP_LOGIN=\\\"APPS/".length();
		string = string.substring(s1+s2);
		s1 = string.indexOf("\"");
		System.out.println(string.substring(0, s1));
		
		
	}

}
