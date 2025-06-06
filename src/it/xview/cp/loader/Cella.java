package it.xview.cp.loader;

public class Cella {
	private String nome;
	private Object valore;
	public Cella(String nome, Object valore) {
		super();
		this.nome = nome;
		this.valore = valore;
	}
	public Cella() {
		
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Object getValore() {
		return valore;
	}
	public void setValore(Object valore) {
		this.valore = valore;
	}
	public String toString() {
		return this.nome + " = " + this.valore;
	}

}
