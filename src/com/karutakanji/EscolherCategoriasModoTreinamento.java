package com.karutakanji;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import bancodedados.ActivityQueEsperaAtePegarOsKanjis;
import bancodedados.AdapterListViewIconeETexto;
import bancodedados.ArmazenaKanjisPorCategoria;
import bancodedados.ArmazenaTudoParaJogoOffline;
import bancodedados.BuscaSalasModoCasualComArgumentoTask;
import bancodedados.CategoriaDeKanjiParaListviewSelecionavel;
import bancodedados.KanjiTreinar;
import bancodedados.MyCustomAdapter;
import bancodedados.MyCustomAdapter1Jogador;
import bancodedados.SolicitaKanjisParaTreinoTask;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.text.Html;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EscolherCategoriasModoTreinamento extends ActivityDoJogoComSom implements ActivityQueEsperaAtePegarOsKanjis, View.OnClickListener 
{
	private MyCustomAdapter1Jogador dataAdapter; 
	private String jlptEnsinarNaFerramenta = "4";
	private ProgressDialog loadingKanjisDoBd;
	private boolean mostrarDicasTreinamento; //sera que o usuario quer que aparecam os 4 ultimos kanjis no treinamento?
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_escolher_categorias_modo_treinamento);
		
		ImageView imageViewCheckbox = (ImageView) findViewById(R.id.checkbox_mostrar_dicas);
		imageViewCheckbox.setClickable(true);
		imageViewCheckbox.setOnClickListener(this);
		this.mostrarDicasTreinamento = true;
		
		ArmazenaTudoParaJogoOffline armazenaListaDeKanjisMemoriaInterna = 
						ArmazenaTudoParaJogoOffline.getInstance();
		int quantasVezesAAplicacaoReiniciou =
				armazenaListaDeKanjisMemoriaInterna.getQuantasVezesAAplicacaoFoiReiniciada(this);
		
		if(this.temConexaoComInternet() == false)
		{
			/*Toast t = Toast.makeText(this, "carregada lista de palavras da memoria local. Vezes reiniciadas=" + quantasVezesAAplicacaoReiniciou, Toast.LENGTH_LONG);
			t.show();*/
			boolean conseguiuCarregarListaLocal = armazenaListaDeKanjisMemoriaInterna.carregarListasDePalavrasSalvasAnteriormente(this);
			if(conseguiuCarregarListaLocal == true)
			{
				procedimentoAposCarregarKanjis();
			}
			else
			{
				//vai ter de pegar a lista do bd mesmo
				this.loadingKanjisDoBd = ProgressDialog.show(EscolherCategoriasModoTreinamento.this, getResources().getString(R.string.carregando_categorias), getResources().getString(R.string.por_favor_aguarde));
				SolicitaKanjisParaTreinoTask pegarKanjisTreino = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
			    pegarKanjisTreino.execute("");
			}
		}
		else
		{
			if(true)//quantasVezesAAplicacaoReiniciou % 3 == 0 || quantasVezesAAplicacaoReiniciou == 1)
			{
				//de 3 em 3 vezes que o app foi reiniciado, os kanjis deverao ser carregados do BD novamente
				this.loadingKanjisDoBd = ProgressDialog.show(EscolherCategoriasModoTreinamento.this, getResources().getString(R.string.carregando_categorias), getResources().getString(R.string.por_favor_aguarde));
				SolicitaKanjisParaTreinoTask pegarKanjisTreino = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
			    pegarKanjisTreino.execute("");
			}
			else
			{
				//caso contrario, carregaremos da memoria local do aplicativo no android
				/*Toast t = Toast.makeText(this, "carregada lista de palavras da memoria local. Vezes reiniciadas=" + quantasVezesAAplicacaoReiniciou, Toast.LENGTH_LONG);
				t.show();*/
				boolean conseguiuCarregarListaLocal = armazenaListaDeKanjisMemoriaInterna.carregarListasDePalavrasSalvasAnteriormente(this);
				if(conseguiuCarregarListaLocal == true)
				{
					procedimentoAposCarregarKanjis();
				}
				else
				{
					//vai ter de pegar a lista do bd mesmo
					this.loadingKanjisDoBd = ProgressDialog.show(EscolherCategoriasModoTreinamento.this, getResources().getString(R.string.carregando_categorias), getResources().getString(R.string.por_favor_aguarde));
					SolicitaKanjisParaTreinoTask pegarKanjisTreino = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
				    pegarKanjisTreino.execute("");
				}
			}
		} 
	}
	

	@Override
	public void procedimentoAposCarregarKanjis() 
	{
		//apos carregados os kanjis do bd, vamos salvar na memoria local do android tb
		ArmazenaTudoParaJogoOffline armazenaPalavrasMemoriaInterna =
								ArmazenaTudoParaJogoOffline.getInstance();
		armazenaPalavrasMemoriaInterna.salvarListaDePalavrasParaUsoFuturo(this);
		
		ArrayList<CategoriaDeKanjiParaListviewSelecionavel> listaDeCategorias = new ArrayList<CategoriaDeKanjiParaListviewSelecionavel>();
		  
		  LinkedList<String> categoriasDosKanjis = 
				  ArmazenaKanjisPorCategoria.pegarInstancia().getCategoriasDeKanjiArmazenadas(jlptEnsinarNaFerramenta);
		  
		  this.mostrarCategoriasParaOUsuario(categoriasDosKanjis);

	}
	
	private void mostrarCategoriasParaOUsuario(final LinkedList<String> categorias)
	{
		int tamanhoLista1 = categorias.size()/2;
		final String[] arrayCategorias = new String[tamanhoLista1];
		final String[] arrayCategorias2 = new String[categorias.size() - tamanhoLista1];
		int iteradorCategorias1 = 0;
		int iteradorCategorias2 = 0;
		
		for(int i = 0; i < categorias.size(); i++)
		{
			String umaCategoria = categorias.get(i);
			if(iteradorCategorias1 < arrayCategorias.length)
			{
				arrayCategorias[iteradorCategorias1] = umaCategoria;
				iteradorCategorias1 = iteradorCategorias1 + 1;
			}
			else
			{
				arrayCategorias2[iteradorCategorias2] = umaCategoria;
				iteradorCategorias2 = iteradorCategorias2 + 1;
			}
		}
		
		Integer[] imageId = new Integer[arrayCategorias.length];
		Integer[] imageId2 = new Integer[arrayCategorias2.length];
		
		for(int j = 0; j < arrayCategorias.length; j++)
		{
			String umaCategoria = arrayCategorias[j];
			int idImagem = RetornaIconeDaCategoriaParaTelasDeEscolha.retornarIdIconeDaCategoria(umaCategoria);
			imageId[j] = idImagem;
		}
		for(int k = 0; k < arrayCategorias2.length; k++)
		{
			String umaCategoria = arrayCategorias2[k];
			int idImagem = RetornaIconeDaCategoriaParaTelasDeEscolha.retornarIdIconeDaCategoria(umaCategoria);
			imageId2[k] = idImagem;
		}

	    
	    final boolean[] categoriaEstahSelecionada = new boolean[arrayCategorias.length];
	    final boolean[] categoriaEstahSelecionada2 = new boolean[arrayCategorias.length];
		for(int l = 0; l < arrayCategorias.length; l++)
		{
			categoriaEstahSelecionada[l] = false;
		}
		for(int m = 0; m < arrayCategorias2.length; m++)
		{
			categoriaEstahSelecionada2[m] = false;
		}
		
		//devo colocar em baixo do nome da categoria, quantas palavras ela tem
		final String[] arrayCategoriasComQuantasPalavras = new String[arrayCategorias.length];
		final String[] arrayCategoriasComQuantasPalavras2 = new String[arrayCategorias2.length];
		for(int a = 0; a < arrayCategorias.length; a++)
		{
			String umaCategoria = arrayCategorias[a];
			int quantasPalavrasTemACategoria = 
					ArmazenaKanjisPorCategoria.pegarInstancia().quantasPalavrasTemACategoria(umaCategoria);
			String textoDaCategoria = umaCategoria + " (" + String.valueOf(quantasPalavrasTemACategoria) + ")";
			String categoriaEscritaEmKanji = RetornaNomeCategoriaEscritaEmKanji.retornarNomeCategoriaEscritaEmKanji(umaCategoria);
			textoDaCategoria = textoDaCategoria + "\n" + categoriaEscritaEmKanji;
			arrayCategoriasComQuantasPalavras[a] = textoDaCategoria;
		}
		for(int b = 0; b < arrayCategorias2.length; b++)
		{
			String umaCategoria = arrayCategorias2[b];
			int quantasPalavrasTemACategoria = 
					ArmazenaKanjisPorCategoria.pegarInstancia().quantasPalavrasTemACategoria(umaCategoria);
			String textoDaCategoria = umaCategoria + " (" + String.valueOf(quantasPalavrasTemACategoria) + ")";
			String categoriaEscritaEmKanji = RetornaNomeCategoriaEscritaEmKanji.retornarNomeCategoriaEscritaEmKanji(umaCategoria);
			textoDaCategoria = textoDaCategoria + "\n" + categoriaEscritaEmKanji;
			arrayCategoriasComQuantasPalavras2[b] = textoDaCategoria;
		}
		
		
		//definindo fontes para os textos dessa tela...
		this.mudarFonteTituloEBotaoOkInformeAsCategoriasModoTreinamento();
		Typeface typeFaceFonteTextoListViewIconeETexto = this.escolherFonteDoTextoListViewIconeETexto();
		
		AdapterListViewIconeETexto adapter = new AdapterListViewIconeETexto(EscolherCategoriasModoTreinamento.this, arrayCategoriasComQuantasPalavras, imageId,typeFaceFonteTextoListViewIconeETexto,true,true,false);
		    adapter.setLayoutUsadoParaTextoEImagem(R.layout.list_item_icone_e_texto_menor);
			ListView list=(ListView) findViewById(R.id.listaCategoriasPesquisaSalas1);
		    
		        list.setAdapter(adapter);
		        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		                @Override
		                public void onItemClick(AdapterView<?> parent, View view,
		                                        int position, long id) 
		                {
		                    if(categoriaEstahSelecionada[position] == false)
		                    {
		                    	categoriaEstahSelecionada[position] = true;
		                    	ImageView imageView = (ImageView) view.findViewById(R.id.img);
		                    	imageView.setAlpha(255);
		                    	TextView textView = (TextView) view.findViewById(R.id.txt);
		                    	int alpha = 255;
		                    	textView.setTextColor(Color.argb(alpha, 0, 0, 0));
		                    }
		                    else
		                    {
		                    	categoriaEstahSelecionada[position] = false;
		                    	ImageView imageView = (ImageView) view.findViewById(R.id.img);
		                    	imageView.setAlpha(70);
		                    	TextView textView = (TextView) view.findViewById(R.id.txt);
		                    	int alpha = 70;
		                    	textView.setTextColor(Color.argb(alpha, 0, 0, 0));
		                    }
		                }
		            });
		        
		        
		        AdapterListViewIconeETexto adapter2 = new AdapterListViewIconeETexto(EscolherCategoriasModoTreinamento.this, arrayCategoriasComQuantasPalavras2, imageId2,typeFaceFonteTextoListViewIconeETexto,true,true,false);
		        adapter2.setLayoutUsadoParaTextoEImagem(R.layout.list_item_icone_e_texto_menor);
		        ListView list2=(ListView)findViewById(R.id.listaCategoriasPesquisaSalas2);
			        list2.setAdapter(adapter2);
			        list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			                @Override
			                public void onItemClick(AdapterView<?> parent, View view,
			                                        int position, long id) 
			                {
			                    if(categoriaEstahSelecionada2[position] == false)
			                    {
			                    	categoriaEstahSelecionada2[position] = true;
			                    	ImageView imageView = (ImageView) view.findViewById(R.id.img);
			                    	imageView.setAlpha(255);
			                    	TextView textView = (TextView) view.findViewById(R.id.txt);
			                    	int alpha = 255;
			                    	textView.setTextColor(Color.argb(alpha, 0, 0, 0));
			                    }
			                    else
			                    {
			                    	categoriaEstahSelecionada2[position] = false;
			                    	ImageView imageView = (ImageView) view.findViewById(R.id.img);
			                    	imageView.setAlpha(70);
			                    	TextView textView = (TextView) view.findViewById(R.id.txt);
			                    	int alpha = 70;
			                    	textView.setTextColor(Color.argb(alpha, 0, 0, 0));
			                    }
			                }
			            });

	 
		        
		//falta definir a ação para o botão ok desse popup das categorias
		  Button botaoOk = (Button) findViewById(R.id.confirmar_escolha_categorias_treinamento);
		  botaoOk.setOnClickListener(new Button.OnClickListener() 
		  {
			  public void onClick(View v) 
		      {
			    	String categoriasSeparadasPorVirgula = "";
			    	for(int n = 0; n < categoriaEstahSelecionada.length; n++)
			    	{
			    		if(categoriaEstahSelecionada[n] == true)
			    		{
			    			//o usuario quer procurar com essa categoria
			    			String umaCategoria = arrayCategorias[n];
			    			
			    			categoriasSeparadasPorVirgula = categoriasSeparadasPorVirgula + umaCategoria + ",";
			    			
			    		}
			    	}
			    	for(int o = 0; o < categoriaEstahSelecionada2.length; o++)
			    	{
			    		if(categoriaEstahSelecionada2[o] == true)
			    		{
			    			//o usuario quer procurar com essa categoria
			    			String umaCategoria = arrayCategorias2[o];
			    			
			    			categoriasSeparadasPorVirgula = categoriasSeparadasPorVirgula + umaCategoria + ",";
			    			
			    		}
			    	}
			    	
			    	
			    	if(categoriasSeparadasPorVirgula.length() > 1)
			    	{
			    		categoriasSeparadasPorVirgula = categoriasSeparadasPorVirgula.substring(0,categoriasSeparadasPorVirgula.length()-1);
			    		
			    		String[] arrayCategoriasSeparadasPorVirgula = categoriasSeparadasPorVirgula.split(",");
			    		
			    
			   			SingletonGuardaDadosDaPartida.getInstance().limparCategoriasEKanjis();
			   			 
			   			 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			   				for(int i = 0; i < arrayCategoriasSeparadasPorVirgula.length; i++)
			   				{
			   					String umaCategoria = arrayCategoriasSeparadasPorVirgula[i];
			   					LinkedList<KanjiTreinar> kanjisDaCategoria = 
			   								conheceKanjisECategorias.getListaKanjisTreinar(umaCategoria);
			   					SingletonGuardaDadosDaPartida.getInstance().adicionarNovaCategoriaESeusKanjis(umaCategoria, kanjisDaCategoria);
			 
			   				}
			   				
			   				Intent criaTelaModoTreinamento =
		   							new Intent(EscolherCategoriasModoTreinamento.this, ModoTreinamento.class);
			   				
			   				//setar o valor no singleton para passar p proxima tela
			   				SingletonMostrarDicasTreinamento.getInstance().setMostrarDicas(mostrarDicasTreinamento);
			   				
		   					startActivity(criaTelaModoTreinamento);
			   		}
			    	else
			   		 {
			   			 String mensagem = getResources().getString(R.string.erroEscolherCategorias);
			   			 Toast t = Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_LONG);
			   			 t.show();
			   		 }
			    		
			    }
		  });
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void procedimentoConexaoFalhou() 
	{
		//vamos tentar pegar as categorias localmente
		/*Toast t = Toast.makeText(this, "carregada lista de palavras da memoria local", Toast.LENGTH_LONG);
		t.show();*/
		ArmazenaTudoParaJogoOffline armazenaListaDeKanjisMemoriaInterna = 
				ArmazenaTudoParaJogoOffline.getInstance();
		boolean conseguiuCarregarListaLocal = armazenaListaDeKanjisMemoriaInterna.carregarListasDePalavrasSalvasAnteriormente(this);
		if(conseguiuCarregarListaLocal == true)
		{
			procedimentoAposCarregarKanjis();
		}
		else
		{
			//vai ter de pegar a lista do bd mesmo
			this.loadingKanjisDoBd = ProgressDialog.show(EscolherCategoriasModoTreinamento.this, getResources().getString(R.string.carregando_categorias), getResources().getString(R.string.por_favor_aguarde));
			SolicitaKanjisParaTreinoTask pegarKanjisTreino = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
		    pegarKanjisTreino.execute("");
		}
	}
	
	private boolean temConexaoComInternet() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private void mudarFonteTituloEBotaoOkInformeAsCategoriasModoTreinamento()
	{
		String fontPath = "fonts/Wonton.ttf";
	    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
	    TextView titulo = (TextView) findViewById(R.id.textoTituloEscolhaAsCategoriasModoTreinamento);
	    titulo.setTypeface(tf);
	    
	    Button botaoOk = (Button) findViewById(R.id.confirmar_escolha_categorias_treinamento);
	    botaoOk.setTypeface(tf);
	    
	    TextView textoMostrarDicas = (TextView) findViewById(R.id.texto_mostrar_dicas);
	    textoMostrarDicas.setTypeface(tf);
	    
	}
	
	private Typeface escolherFonteDoTextoListViewIconeETexto()
	{
		String fontPath = "fonts/Wonton.ttf";
	    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
	    return tf;
	}


	@Override
	public void onClick(View arg0) 
	{
		// TODO Auto-generated method stub
		switch(arg0.getId())
		{
			case R.id.checkbox_mostrar_dicas:
				ImageView imageViewCheckbox = (ImageView) findViewById(R.id.checkbox_mostrar_dicas);
				if(this.mostrarDicasTreinamento == true)
				{
					mostrarDicasTreinamento = false;
					imageViewCheckbox.setImageResource(R.drawable.checkbox_desmarcada_regras_treinamento);
				}
				else
				{
					mostrarDicasTreinamento = true;
					imageViewCheckbox.setImageResource(R.drawable.checkbox_marcada_regras_treinamento);
				}
				break;
		}
	}
	

}
