package it.xview.cp.util;

public class PecBean {
	public PecBean(String nomeFile, String destinatari, String oggetto, String corpo_mess, String[] attach) {
		super();
		this.destinatari = destinatari;
		this.oggetto = oggetto;
		this.corpo_mess = corpo_mess;
		this.attach = attach;
	}
	public PecBean() {
	}
	String nomeFile;
	String destinatari; 
	String destinatariCC;
	String destinatariBCC;
	String oggetto;
	String corpo_mess; 
	String[] attach;
	public String getNomeFile() {
		return nomeFile;
	}
	public void setNomeFile(String nomeFile) {
		this.nomeFile = nomeFile;
	}
	public String getDestinatari() {
		return destinatari;
	}
	public void setDestinatari(String destinatari) {
		this.destinatari = destinatari;
	}
	public String getDestinatariCC() {
		return destinatariCC;
	}
	public void setDestinatariCC(String destinatariCC) {
		this.destinatariCC = destinatariCC;
	}
	public String getDestinatariBCC() {
		return destinatariBCC;
	}
	public void setDestinatariBCC(String destinatariBCC) {
		this.destinatariBCC = destinatariBCC;
	}
	public String getOggetto() {
		return oggetto;
	}
	public void setOggetto(String oggetto) {
		this.oggetto = oggetto;
	}
	public String getCorpo_mess() {
		return corpo_mess;
	}
	public void setCorpo_mess(String corpo_mess) {
		this.corpo_mess = corpo_mess;
	}
	public String[] getAttach() {
		return attach;
	}
	public void setAttach(String[] attach) {
		this.attach = attach;
	}
	public String toString() {
		String ret= 	"Nome File "+ nomeFile +" - "+ 
						"Destinatari:"+destinatari+" - "+
						"DestinatariCC:"+destinatariCC+" - "+
						"DestinatariBCC:"+destinatariBCC+" - "+
						"Oggetto:"+oggetto+" - "+
						"Corpo:"+corpo_mess+" - "+
						"Allegati:{";
		for(String s : attach) {
			ret+=s+";";
		}
		return ret+"}";
				
	}
}
