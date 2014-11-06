package com.karutakanji;

/*esse singleton soh serve para armazenar se deve ser mostrada as dicas entre a tela de
 * escolha das categorias e a do jogo em si.
 * Nao poderia ser um extra passado no intent porque qd o usuario restarta o treino com as msm
 * categorias, o metodo onCreate() seria chamado novamente*/
public class SingletonMostrarDicasTreinamento 
{
	boolean mostrarDicas;
	private static SingletonMostrarDicasTreinamento  instancia;
	
	private SingletonMostrarDicasTreinamento()
	{
		
	}
	
	public static SingletonMostrarDicasTreinamento getInstance()
	{
		if(instancia == null)
		{
			instancia = new SingletonMostrarDicasTreinamento();
		}
		
		return instancia;
	}

	public boolean getMostrarDicas() {
		return mostrarDicas;
	}

	public void setMostrarDicas(boolean mostrarDicas) {
		this.mostrarDicas = mostrarDicas;
	}
	
	
}
