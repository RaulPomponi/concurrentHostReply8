package it.xview.cp.util;

public class PecReadBean {
	String 	nomeFile;
	String 	stato;
	String 	oggetto;
	String 	dataRicezione;
	byte[]	contenuto;
	
	public String getNomeFile() {
		return nomeFile;
	}
	public void setNomeFile(String nomeFile) {
		this.nomeFile = nomeFile;
	}
	public String getStato() {
		return stato;
	}
	public void setStato(String stato) {
		this.stato = stato;
	}
	public String getOggetto() {
		return oggetto;
	}
	public void setOggetto(String oggetto) {
		this.oggetto = oggetto;
	}
	public String getDataRicezione() {
		return dataRicezione;
	}
	public void setDataRicezione(String dataRicezione) {
		this.dataRicezione = dataRicezione;
	}
	public byte[] getContenuto() {
		return contenuto;
	}
	public void setContenuto(byte[] contenuto) {
		this.contenuto = contenuto;
	}
	public String toString() {
		String s = "{";
		s+=nomeFile==null?"NULL":nomeFile;
		s+=", ";
		s+=stato==null?"NULL":stato;
		s+=", ";
		s+=oggetto==null?"NULL":oggetto;
		s+=", ";
		s+=dataRicezione==null?"NULL":dataRicezione;
		s+=", ";
		try {
			s+=contenuto.length;
		} catch (Exception e) {
			s+="NULL";
		}
		
		return s+"}";
	}

}
