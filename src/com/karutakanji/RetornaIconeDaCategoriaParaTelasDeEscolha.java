package com.karutakanji;

public class RetornaIconeDaCategoriaParaTelasDeEscolha 
{
	public static int retornarIdIconeDaCategoria(String categoria)
	{
		if(categoria.compareTo("Adjetivos") == 0)
		{
			return R.drawable.categoria_adjetivo;
		}
		else if(categoria.compareTo("Cotidiano") == 0)
		{
			return R.drawable.categoria_cotidiano;
		}
		else if(categoria.compareTo("Educação") == 0)
		{
			return R.drawable.categoria_educacao;
		}
		else if(categoria.compareTo("Geografia") == 0)
		{
			return R.drawable.categoria_geografia;
		}
		else if(categoria.compareTo("Lazer") == 0)
		{
			return R.drawable.categoria_lazer;
		}
		else if(categoria.compareTo("Lugar") == 0)
		{
			return R.drawable.categoria_lugar;
		}
		else if(categoria.compareTo("Natureza") == 0)
		{
			return R.drawable.categoria_natureza;
		}
		else if(categoria.compareTo("Saúde") == 0)
		{
			return R.drawable.categoria_saude;
		}
		else if(categoria.compareTo("Supermercado") == 0)
		{
			return R.drawable.categoria_supermercado;
		}
		else if(categoria.compareTo("Tempo") == 0)
		{
			return R.drawable.categoria_tempo;
		}
		else if(categoria.compareTo("Trabalho") == 0)
		{
			return R.drawable.categoria_trabalho;
		}
		else
		{
			return R.drawable.categoria_verbo;
		}
	}

}
