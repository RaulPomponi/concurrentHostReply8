package it.xview.cp.util;


public class TestPath {

	public static void main(String[] args) {
		TestPath t = new TestPath();
		t.run();

	}
	private void run() {
		String p = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("out := " +p);
		System.out.println("JAVA_TOP := " +System.getenv("JAVA_TOP"));
		
	}
	public TestPath() {
		
	}

}
