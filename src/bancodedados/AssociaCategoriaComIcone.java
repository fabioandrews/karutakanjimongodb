package bancodedados;

import java.util.Arrays;
import java.util.LinkedList;

import android.content.Context;

public class AssociaCategoriaComIcone 
{
	private static LinkedList<String> nomeCategoriasDoJogo = 
			new LinkedList<String>(Arrays.asList("Adjetivos", "Cotidiano", "Educação", "Geografia",
													"Lazer", "Lugar", "Natureza", "Saúde",
													"Supermercado","Tempo","Trabalho","Verbos"));
	private static LinkedList<String> nomeImagensCategoria =
			new LinkedList<String>(Arrays.asList("categoria_adjetivo_peq", "categoria_cotidiano_peq", "categoria_educacao_peq",
					"categoria_geografia_peq","categoria_lazer_peq", "categoria_lugar_peq", "categoria_natureza_peq", "categoria_saude_peq",
					"categoria_supermercado_peq","categoria_tempo_peq","categoria_trabalho_peq","categoria_verbo_peq"));
	/**
	 * pega o ID da imagem de uma categoria para vc usar setImageResource() no ImageView e setar imagem
	 * @param contextoDaActivity getAplicationContext() da Activity
	 * @param nomeCategoria nome de uma categoria do jogo
	 * @return o id para vc usar setImageResource passando o id para setar imagem ou -1 caso busca deu defeito
	 */
	public static int pegarIdImagemDaCategoria(Context contextoDaActivity, String nomeCategoria)
	{
		String nomeDaImagemAssociada = "";
		for(int i = 0; i < nomeCategoriasDoJogo.size(); i++)
		{
			String umaCategoria = nomeCategoriasDoJogo.get(i);
			if(umaCategoria.compareToIgnoreCase(nomeCategoria) == 0)
			{
				nomeDaImagemAssociada = nomeImagensCategoria.get(i);
			}
		}
		int idImagemCategoria = -1;
		if(nomeDaImagemAssociada.length() > 0)
		{
			idImagemCategoria = contextoDaActivity.getResources().getIdentifier(nomeDaImagemAssociada, "drawable", contextoDaActivity.getPackageName());
		}
		
		return idImagemCategoria;
	}
	

}
