package it.xview.cp.mail;

import it.xview.cp.util.ConcurrentHostConnection;

public class CUSendMailIsola extends SendEmailOffice365 {

    public static void main(final String[] args) {
    	CUSendMailGAC s =new CUSendMailGAC();
		ConcurrentHostConnection chc = null;
		try {
			chc = new ConcurrentHostConnection(args[0],args[1]);
			s.CONTA_PADRAO = "GM.consulenti@fbf-isola.it";
			s.SENHA_CONTA_PADRAO = "Pasquino21";
	    	s.runProgram(chc);
		} catch (Exception e) {
			e.printStackTrace();
            s.exitStatus=2;
		}
		System.exit(s.exitStatus);
    }

}
