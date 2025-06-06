package it.xview.cp.mail;

public class MailerBean {
	public MailerBean() {
		destinatariCc=null;
		destinatariCcn=null;
		allegati=null;
		smtpPort="25";
	}
	public MailerBean(String idUpdate, String mittente, String password, String smtpHost, String smtpPort, String destinatariA,
			String destinatariCc, String destinatariCcn, String oggetto, String corpo, String[] allegati) {
		super();
		this.idUpdate=idUpdate;
		this.mittente = mittente;
		this.password = password;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.destinatariA = destinatariA;
		this.destinatariCc = destinatariCc;
		this.destinatariCcn = destinatariCcn;
		this.oggetto = oggetto;
		this.corpo = corpo;
		this.allegati = allegati;
	}
	private String idUpdate;
	private String mittente;
	private String password;
	private String smtpHost;
	private String smtpPort;
	private String destinatariA;
	private String destinatariCc=null;
	private String destinatariCcn=null;
	private String oggetto;
	private String corpo;
	private String[] allegati;
	public String getIdUpdate() {
		return idUpdate;
	}
	public void setIdUpdate(String idUpdate) {
		this.idUpdate = idUpdate;
	}
	public String getMittente() {
		return mittente;
	}
	public void setMittente(String mittente) {
		this.mittente = mittente;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	public String getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}
	public String getDestinatariA() {
		return destinatariA;
	}
	public void setDestinatariA(String destinatariA) {
		this.destinatariA = destinatariA;
	}
	public String getDestinatariCc() {
		return destinatariCc;
	}
	public void setDestinatariCc(String destinatariCc) {
		this.destinatariCc = destinatariCc;
	}
	public String getDestinatariCcn() {
		return destinatariCcn;
	}
	public void setDestinatariCcn(String destinatariCcn) {
		this.destinatariCcn = destinatariCcn;
	}
	public String getOggetto() {
		return oggetto;
	}
	public void setOggetto(String oggetto) {
		this.oggetto = oggetto;
	}
	public String getCorpo() {
		return corpo;
	}
	public void setCorpo(String corpo) {
		this.corpo = corpo;
	}
	public String[] getAllegati() {
		return allegati;
	}
	public void setAllegati(String[] allegati) {
		this.allegati = allegati;
	}
	public void setAllegati(String sAllegati) {
		this.allegati = sAllegati.split(";");
	}
	public String toPrintString() {
		String s = "Parametri Mail : "
				+ " \n mittente = " +  mittente
				+ " \n idUpdate = " +  idUpdate
				+ " \n password = " +  "*********"//password.toString()
				+ " \n smtpHost = " +  smtpHost
				+ " \n smtpPort = " +  smtpPort
				+ " \n destinatariA = " +  destinatariA
				+ " \n oggetto = " +  oggetto
				+ " \n corpo = " +  corpo;
		
		for(int i=0; i< allegati.length; i++) {
			s+= " \n allegato "+i+" = "+allegati[i].toString();
		}
		try {
			if(destinatariCc!=null & destinatariCc.length()>0) s= s+" \n destinatariCc = " +  destinatariCc;
		} catch (Exception e) {
			s= s+" \n destinatariCc = ";
		}
		try {
			if(destinatariCcn!=null & destinatariCcn.length()>0) s= s+ " \n destinatariCcn = " +  destinatariCcn;
		} catch (Exception e) {
			s= s+" \n destinatariCcn = ";
		}
		return s;

	}
}
