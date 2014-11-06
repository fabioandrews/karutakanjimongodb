package bancodedados;

public class SalaModoCasual 
{
	private int id_sala;
	private String emailDoCriador;
	private String danDoCriador;
	private String categoriasJuntas;
	private String quantasRodadas;
	
	public String getEmailDoCriador() {
		return emailDoCriador;
	}
	public void setEmailDoCriador(String emailCriador) {
		this.emailDoCriador = emailCriador;
	}
	public String getDanDoCriador() {
		return danDoCriador;
	}
	public void setDanDoCriador(String danDoCriador) {
		this.danDoCriador = danDoCriador;
	}
	public String getCategoriasJuntas() {
		return categoriasJuntas;
	}
	public void setCategoriasJuntas(String categoriasJuntas) {
		this.categoriasJuntas = categoriasJuntas;
	}
	public String getQuantasRodadas() {
		return quantasRodadas;
	}
	public void setQuantasRodadas(String quantasRodadas) {
		this.quantasRodadas = quantasRodadas;
	}
	public int getId_sala() {
		return id_sala;
	}
	public void setId_sala(int id_sala) {
		this.id_sala = id_sala;
	}

}
