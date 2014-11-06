package com.karutakanji;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import lojinha.ConcreteDAOAcessaDinheiroDoJogador;
import lojinha.DAOAcessaDinheiroDoJogador;

import bancodedados.ArmazenaTudoParaJogoOffline;
import bancodedados.ChecaVersaoAtualDoSistemaTask;
import bancodedados.DadosPartidaParaOLog;
import bancodedados.EnviarDadosDaPartidaParaLogTask;
import bancodedados.KanjiTreinar;
import bancodedados.SolicitaKanjisParaTreinoTask;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PorterDuff;

public class MainActivity extends ActivityDoJogoComSom implements View.OnClickListener
{
	private ChecaVersaoAtualDoSistemaTask checaVersaoAtual;
	final static int[] SCREENS = {R.id.telaatualizeojogo, R.id.telainicialnormal};
	private boolean haConexaoComInternet;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		switchToScreen(R.id.telainicialnormal);
		
		ArmazenaTudoParaJogoOffline armazenaQuantasVezesAAplicacaoFoiIniciada = 
				ArmazenaTudoParaJogoOffline.getInstance();
		armazenaQuantasVezesAAplicacaoFoiIniciada.aumentarQuantasVezesAAplicacaoFoiReiniciada(this);
		
		this.haConexaoComInternet = this.temConexaoComInternet();
		if(haConexaoComInternet == true)
		{
			this.checaVersaoAtual = new ChecaVersaoAtualDoSistemaTask(this);
			this.checaVersaoAtual.execute("");
			
		}
		else
		{
			//nao ha conexao com internet. Apenas valores armazenados localmente serao usados
			this.executarProcedimentoErroConexao();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void irAoModoCasual(View view)
	{
		if(this.haConexaoComInternet == true)
		{
			try
			{
				Intent criaTelaModoCasual =
						new Intent(MainActivity.this, ModoCasual.class);
				startActivity(criaTelaModoCasual);
			}
			catch(Exception e)
			{
				Writer writer = new StringWriter();
		    	PrintWriter printWriter = new PrintWriter(writer);
		    	e.printStackTrace(printWriter);
		    	String s = writer.toString();
		    	Context context = getApplicationContext();
		        Toast t = Toast.makeText(context, s, Toast.LENGTH_LONG);
		        t.show();
			}
		}
	}
	
	
	
	public void irAoModoTreinamento(View view)
	{
		try
		{
			ArmazenaMostrarRegrasTreinamento conheceValorMostrarRegrasTreinamento = 
												ArmazenaMostrarRegrasTreinamento.getInstance();
			boolean deveMostrarRegrasTreinamento = 
					conheceValorMostrarRegrasTreinamento.getMostrarRegrasDoTreinamento(this);
			if(deveMostrarRegrasTreinamento == false)
			{
				Intent criaTelaModoTreinamento =
						new Intent(MainActivity.this, EscolherCategoriasModoTreinamento.class);
				startActivity(criaTelaModoTreinamento);
			}
			else
			{
				//as regras do modo treinamento deveriam ser mostradas
				Intent criaTelaModoTreinamento =
						new Intent(MainActivity.this, MostrarRegrasModoTreinamento.class);
				startActivity(criaTelaModoTreinamento);
			}
		}
		catch(Exception e)
		{
			Writer writer = new StringWriter();
	    	PrintWriter printWriter = new PrintWriter(writer);
	    	e.printStackTrace(printWriter);
	    	String s = writer.toString();
	    	Context context = getApplicationContext();
	        Toast t = Toast.makeText(context, s, Toast.LENGTH_LONG);
	        t.show();
		}
		
	}
	
	 public void fazerToast(String mensagem)
	 {
		 Toast t = Toast.makeText(this, mensagem, Toast.LENGTH_LONG);
		  t.show();
	 }
	 
	 public void irADadosPartidasAnteriores(View v)
	 {
		 if(this.haConexaoComInternet)
		 {
			 Intent criaTelaDadosAnteriores =
						new Intent(MainActivity.this, DadosPartidasAnteriores.class);
				startActivity(criaTelaDadosAnteriores);
		 }
	 }
	 
	 public void irALojinha(View v)
	 {
		 Intent criaLojinha =
					new Intent(MainActivity.this, LojinhaMaceteKanjiActivity.class);
			startActivity(criaLojinha);
	 }
	 
	 public void adicionarDinheirinho(View v)
	 {
			
			DAOAcessaDinheiroDoJogador acessaDinheiroDoJogador = ConcreteDAOAcessaDinheiroDoJogador.getInstance();
			acessaDinheiroDoJogador.adicionarCredito(1500, this);
			int creditoAtual = acessaDinheiroDoJogador.getCreditoQuePossui(this);
			
	 }
	 
	 public void irParaConfiguracoes(View view)
	 {
		 Intent irParaConfiguracoes =
					new Intent(MainActivity.this, Configuracoes.class);
			startActivity(irParaConfiguracoes);
	 }
	 
	 @Override
		protected void onPause()
		{
			
			//TocadorMusicaBackground.getInstance().pausarTocadorMusica();
			if(this.isFinishing())
			{
				Toast.makeText(MainActivity.this, "is finishing will stop service", Toast.LENGTH_SHORT).show();
				Intent iniciaMusicaFundo = new Intent(MainActivity.this, BackgroundSoundService.class);
				stopService(iniciaMusicaFundo);
			}
			super.onPause();
			
		}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
	
	void switchToScreen(int screenId) {
		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
		    findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
		}
	}
	
	public void mudarParaTelaAtualizeOJogo(String versaoMaisAtual)
	{
		switchToScreen(R.id.telaatualizeojogo);
		
		String stringMensagemAtualizeJogo = getResources().getString(R.string.mensagem_por_favor_atualize_o_jogo);
		stringMensagemAtualizeJogo = stringMensagemAtualizeJogo + versaoMaisAtual;
		
		TextView textViewAtualize = (TextView) findViewById(R.id.mensagemAtualizeOJogo);
		textViewAtualize.setText(stringMensagemAtualizeJogo);
	}
	
	public void mostrarErro(String erro)
	{
		Toast t = Toast.makeText(this, erro, Toast.LENGTH_LONG);
	    t.show();
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
	    	case R.id.karuta1_imageview:
	    		ImageView imageViewCarta = (ImageView) findViewById(R.id.karuta1_imageview);
	    		imageViewCarta.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
	    	break;
		}
	}
	
	private boolean temConexaoComInternet() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void executarProcedimentoErroConexao()
	{
		String versaoMaisRecente = 
				ArmazenaTudoParaJogoOffline.getInstance().getVersaoMaisRecenteDoSistemaLocalmente(this);
		ChecaVersaoAtualDoSistemaTask checaVersaoAtualDoSistema = new ChecaVersaoAtualDoSistemaTask(this);
		String versaoDoAndroidDoUsuario = 
			checaVersaoAtualDoSistema.getVersaoDoSistema();
		
		boolean conseguiuCarregarListaDePalavrasOffline = 
				ArmazenaTudoParaJogoOffline.getInstance().carregarListasDePalavrasSalvasAnteriormente(this);
		
		if(versaoMaisRecente.length() == 0 || conseguiuCarregarListaDePalavrasOffline == false)
		{
			//caso da primeira vez que o usuario inicie o jogo ele nao tenha conexao com internet, ele nao deve poder fazer nada pq nem a versao mais atual do sistema foi obtida uma unica vez
			this.mudarParaTelaAtualizeOJogo("");
			
			TextView textViewFalhaConexao = (TextView) findViewById(R.id.mensagemAtualizeOJogo);
			String mensagemFaltaConexao = getResources().getString(R.string.erro_falta_conexao_primeira_vez);
			textViewFalhaConexao.setText(mensagemFaltaConexao);
		}
		else if(versaoMaisRecente.compareTo(versaoDoAndroidDoUsuario) != 0)
		{
			this.mudarParaTelaAtualizeOJogo(versaoMaisRecente);
		}
		else
		{
			TextView textViewFalhaConexao = (TextView) findViewById(R.id.textAvisaAoUsuarioFaltaDeConexao);
			String mensagemFaltaConexao = getResources().getString(R.string.erro_falta_conexao);
			textViewFalhaConexao.setText(mensagemFaltaConexao);
		}
	}
}
