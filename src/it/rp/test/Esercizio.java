package it.rp.test;

public class Esercizio {

	public Esercizio() {
		System.out.println("Start");
	}

	public static void main(String[] args) {
		Esercizio e = new Esercizio();
		int[] iNumeri= {1,2,4,5,3,10,100,80,4,20};
		System.out.println("IN : {1,2,4,5,3,10,100,80,4,20}");
		String s="";
		s=e.test1(iNumeri,s);
		System.out.println("OUT : " + s);

	}

	private String test1(int[] iArray, String sOut) {
//		String sOut="";
//		System.out.println("test1");
		boolean check = false;
		for(int i=0;i<iArray.length;i++) {
			if(i+1<iArray.length && iArray[i]>iArray[i+1]) {
				int v1=iArray[i];
				int v2=iArray[i+1];
				iArray[i+1]=v1;
				iArray[i] = v2;
				check=true;
			}
		}
		if(check) {
			sOut=test1(iArray,sOut);
		}else {
			for(int i=0;i<iArray.length;i++) {
				sOut+="|"+iArray[i];
			}
			sOut=sOut.substring(1);
			check=false;
		}
		return sOut;
		
	}

}
