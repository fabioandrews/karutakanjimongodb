package bancodedados;

import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;

/*o modo Treinamento não deveria precisar de internet para jogar. Por isso, ao inves de carregarmos as categorias do bd 
 * toda vez que o jogador quiser treinar, carregaremos a lista toda de palavras do BD a cada
 * 3 iniciadas que o usuário faz na aplicação. 
 * Nessa classe tb armazenamos as vezes iniciadas e a versao atual do sistema*/
public class ArmazenaTudoParaJogoOffline 
{
	private static ArmazenaTudoParaJogoOffline instancia;
	
	private ArmazenaTudoParaJogoOffline()
	{
		
	}
	
	public static ArmazenaTudoParaJogoOffline getInstance()
	{
		if(instancia == null)
		{
			instancia = new ArmazenaTudoParaJogoOffline();
		}
		
		return instancia;
	}
	
	/*para casos de falta de internet, o usuario deveria pelo menos poder usar o modo treinamento. Por isso vamos carregar da memoria locl a versao atual do sistema*/
	public String getVersaoMaisRecenteDoSistemaLocalmente(Context contextoAplicacao)
	{
		SharedPreferences configuracoesSalvar = contextoAplicacao.getSharedPreferences("versao_mais_recente", Context.MODE_PRIVATE);
		String versaoMaisRecente = configuracoesSalvar.getString("versao_mais_recente", "");
		return versaoMaisRecente;
	}
	
	public void armazenarVersaoMaisRecenteDoSistemaLocalmente(Context contextoAplicacao, String versaoMaisRecente)
	{
		SharedPreferences configuracoesSalvar = contextoAplicacao.getSharedPreferences("versao_mais_recente", Context.MODE_PRIVATE);
		SharedPreferences.Editor editorConfig = configuracoesSalvar.edit();
		editorConfig.putString("versao_mais_recente", versaoMaisRecente);
		editorConfig.commit();
	}
	
	public int getQuantasVezesAAplicacaoFoiReiniciada(Context contextoAplicacao)
	{
		SharedPreferences configuracoesSalvar = contextoAplicacao.getSharedPreferences("quantas_vezes_a_aplicacao_foi_reiniciada", Context.MODE_PRIVATE);
		int quantasVezesAAplicacaoFoiIniciada = configuracoesSalvar.getInt("quantas_vezes_a_aplicacao_foi_reiniciada", 0);
		return quantasVezesAAplicacaoFoiIniciada;
	}
	
	public void aumentarQuantasVezesAAplicacaoFoiReiniciada(Context contextoAplicacao) {
		int quantasVezesAAplicacaoFoiIniciada = this.getQuantasVezesAAplicacaoFoiReiniciada(contextoAplicacao);
		SharedPreferences configuracoesSalvar = contextoAplicacao.getSharedPreferences("quantas_vezes_a_aplicacao_foi_reiniciada", Context.MODE_PRIVATE);
		SharedPreferences.Editor editorConfig = configuracoesSalvar.edit();
		int novoQuantasVezes = quantasVezesAAplicacaoFoiIniciada + 1;
		editorConfig.putInt("quantas_vezes_a_aplicacao_foi_reiniciada", novoQuantasVezes);
		editorConfig.commit();
	}
	
	//formato da string salva:
	//Formato de cada palavra: KANJI|categoria|hiragana|traducao|dificuldade|jlpt
	//Ex:Au|cotidiano|...;Kau|...
	public void salvarListaDePalavrasParaUsoFuturo(Context contextoAplicacao)
	{
		LinkedList<KanjiTreinar> todosOsKanjisDeTodasAsCategorias = 
										pegarTodosOsKanjisDeTodasAsCategorias();
		String stringTodosOsKanjisDeTodasAsCategorias = "";
		
		for(int i = 0; i < todosOsKanjisDeTodasAsCategorias.size(); i++)
		{
			KanjiTreinar umKanji = todosOsKanjisDeTodasAsCategorias.get(i);
			stringTodosOsKanjisDeTodasAsCategorias = 
					stringTodosOsKanjisDeTodasAsCategorias +
					umKanji.getKanji() + "|" + 
					umKanji.getCategoriaAssociada() + "|" +
					umKanji.getHiraganaDoKanji() + "|" +
					umKanji.getTraducaoEmPortugues() + "|" +
					String.valueOf(umKanji.getDificuldadeDoKanji()) + "|" +
					umKanji.getJlptAssociado() + ";";
		}
		
		SharedPreferences configuracoesSalvar = contextoAplicacao.getSharedPreferences("lista_de_palavras", Context.MODE_PRIVATE);
		SharedPreferences.Editor editorConfig = configuracoesSalvar.edit();
		editorConfig.putString("lista_de_palavras", stringTodosOsKanjisDeTodasAsCategorias);
		editorConfig.commit();
	}
	
	
	private LinkedList<KanjiTreinar> pegarTodosOsKanjisDeTodasAsCategorias()
	{
		LinkedList<String> categorias = 
				ArmazenaKanjisPorCategoria.pegarInstancia().getCategoriasDeKanjiArmazenadas();
		LinkedList<KanjiTreinar> todosOsKanjisDeTodasAsCategorias = new LinkedList<KanjiTreinar>();
		
		for(int i = 0; i < categorias.size(); i++)
		{
			String umaCategoria = categorias.get(i); 
			LinkedList<KanjiTreinar> kanjisDaCategoria = 
					ArmazenaKanjisPorCategoria.pegarInstancia().getListaKanjisTreinar(umaCategoria);
			for(int j = 0; j < kanjisDaCategoria.size(); j++)
			{
				KanjiTreinar umKanji = kanjisDaCategoria.get(j);
				todosOsKanjisDeTodasAsCategorias.add(umKanji);
			}
		}
		
		return todosOsKanjisDeTodasAsCategorias;
	}
	
	public boolean carregarListasDePalavrasSalvasAnteriormente(Context contextoAplicacao)
	{
		SharedPreferences configuracoesSalvar = contextoAplicacao.getSharedPreferences("lista_de_palavras", Context.MODE_PRIVATE);
		String todosOsKanjisDeTodasAsCategorias = configuracoesSalvar.getString("lista_de_palavras", "");
		
		if(todosOsKanjisDeTodasAsCategorias.length() == 0)
		{
			//nao deu para acessar essa lista salva anteriormente, entao nao faremos nada
			return false;
		}
		else
		{
			try
			{
				String cadaPalavraSeparada[] = todosOsKanjisDeTodasAsCategorias.split(";");
				for(int i = 0; i < cadaPalavraSeparada.length; i++)
				{
					String umaPalavra = cadaPalavraSeparada[i];
					String[] cadaCoisaQueFormaKanjiTreinarSeparado = umaPalavra.split("\\|");
					String kanji = cadaCoisaQueFormaKanjiTreinarSeparado[0];
					String categoria = cadaCoisaQueFormaKanjiTreinarSeparado[1];
					String hiragana = cadaCoisaQueFormaKanjiTreinarSeparado[2];
					String traducao = cadaCoisaQueFormaKanjiTreinarSeparado[3];
					String dificuldade = cadaCoisaQueFormaKanjiTreinarSeparado[4];
					int dificuldadeEmInt = Integer.valueOf(dificuldade);
					String jlpt = cadaCoisaQueFormaKanjiTreinarSeparado[5];
					
					KanjiTreinar umKanji = new KanjiTreinar(jlpt, categoria, kanji, traducao, hiragana, dificuldadeEmInt);
					ArmazenaKanjisPorCategoria.pegarInstancia().adicionarKanjiACategoria(categoria, umKanji);
					
				}
				
				return true;
			}
			catch(Exception e)
			{
				//se qualquer erro ocorreu, devemos tentar pegar a lista de palavras do servidor mesmo
				return false;
			}
		}
	}
	
}
