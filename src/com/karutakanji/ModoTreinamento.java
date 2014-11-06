package com.karutakanji;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lojinha.ConcreteDAOAcessaDinheiroDoJogador;
import lojinha.DAOAcessaDinheiroDoJogador;
import lojinha.TransformaPontosEmCredito;

import bancodedados.KanjiTreinar;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ModoTreinamento extends ActivityDoJogoComSom implements OnClickListener
{
	private int nivel; 
	private int vidas; //jogador comeca com 4 vidas e vai diminuindo
	private int suaPontuacao;
	private LinkedList<KanjiTreinar> kanjisDasCartasNaTela;
	private LinkedList<KanjiTreinar> kanjisDasCartasNaTelaQueJaSeTornaramDicas;
	private LinkedList<KanjiTreinar> kanjisQueUsuarioErrou;
	private KanjiTreinar kanjiDaDica;
	
	private int quantosNiveisPassaram; //numero que so vai de 0 a 2. De 2 em 2 niveis, o usuario perceberah uma mudanca nas cartas ou nos kanjis 
	private LinkedList<KanjiTreinar> kanjisQuePodemVirarCartasNovas;
	private LinkedList<KanjiTreinar> kanjisQueJaViraramCartas; //no jogo inteiro
	private LinkedList<KanjiTreinar> ultimosKanjis; //os kanjis serao ensinados de 4 em 4. Esses 4 ensinados deveriam aparecer com maior frequencia na tela p o usuario memoriza-los
	
	private int quantasCartasHaveraoNaTela; //no nivel 5,9,13,17,21, 25, 29 e 33  esse numero aumenta
	private boolean naoHaMaisNovosKanjisParaSeCriar;
	
	private boolean mostrarDicas; //deve-se mostrar os 4 ultimos kanjis ou nao? Esse valor vem dos extras do intent da tela anterior usada para criar essa tela daqui
	
	final static int[] SCREENS = {R.id.tela_modo_treinamento,R.id.tela_observacao_novos_kanjis,R.id.tela_fim_do_modo_treinamento};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modo_treinamento);
		
		this.mostrarDicas = SingletonMostrarDicasTreinamento.getInstance().getMostrarDicas();
		
		this.comecarJogoPelaPrimeiraVez();
		if(this.mostrarDicas == true)
		{
			this.mostrarTelaObservacaoNovosKanjis();
		}
		else
		{
			this.mostrarTelaModoTreinamento(null);
		}
	}
	
	private void comecarJogoPelaPrimeiraVez()
	{	
		this.nivel = 1;
		this.vidas = 4;
		this.suaPontuacao = 0;
		this.quantosNiveisPassaram = 0;
		quantasCartasHaveraoNaTela = 4;
		kanjisQueJaViraramCartas = new LinkedList<KanjiTreinar>();
		ultimosKanjis = new LinkedList<KanjiTreinar>();
		kanjisQueUsuarioErrou = new LinkedList<KanjiTreinar>();
		this.kanjisDasCartasNaTela = new LinkedList<KanjiTreinar>();
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas = new LinkedList<KanjiTreinar>();
		
		naoHaMaisNovosKanjisParaSeCriar = false;
		
		TextView textNivel = (TextView) findViewById(R.id.nivel);
		String stringNivel = getResources().getString(R.string.nivel_sem_dois_pontos);
		stringNivel = stringNivel + " " + this.nivel;
		textNivel.setText(stringNivel);
		
		pegarTodosOsKanjisQuePodemVirarCartas();
		
		findViewById(R.id.karuta1_imageview).setOnClickListener(this);
		findViewById(R.id.karuta2_imageview).setOnClickListener(this);
		findViewById(R.id.karuta3_imageview).setOnClickListener(this);
		findViewById(R.id.karuta4_imageview).setOnClickListener(this);
		findViewById(R.id.karuta5_imageview).setOnClickListener(this);
		findViewById(R.id.karuta6_imageview).setOnClickListener(this);
		findViewById(R.id.karuta7_imageview).setOnClickListener(this);
		findViewById(R.id.karuta8_imageview).setOnClickListener(this);
		findViewById(R.id.karuta9_imageview).setOnClickListener(this);
		findViewById(R.id.karuta10_imageview).setOnClickListener(this);
		findViewById(R.id.karuta11_imageview).setOnClickListener(this);
		findViewById(R.id.karuta12_imageview).setOnClickListener(this);
		this.mudarFonteDosKanjis();
		
		TextView textoPontuacao = (TextView) findViewById(R.id.pontuacao);
		String pontuacao = getResources().getString(R.string.pontos);
		textoPontuacao.setText(pontuacao + String.valueOf(this.suaPontuacao));
		
		this.tornarCartasNaTelaClicaveisEVaziasNovamente();
		this.escolherKanjisParaONivel();
		this.gerarKanjiDaDica();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.modo_treinamento, menu);
		return true;
	}
	
	
	private void escolherNovosKanjis()
	{
		this.ultimosKanjis.clear();
		
		for(int i = 0; i < 4; i++)
		{
			KanjiTreinar kanjiTreinar = this.escolherUmNovoKanjiParaTreinar();
			 
			 if(kanjiTreinar == null)
			 {
				 //acabaram-se os kanjis que posso usar na tela
				 this.naoHaMaisNovosKanjisParaSeCriar = true;
				 break;
				 
			 }
			 else
			 {
				 this.ultimosKanjis.add(kanjiTreinar);
			 }
		}
	}
	
	public void mostrarTelaModoTreinamento(View v)
	{
		this.switchToScreen(R.id.tela_modo_treinamento);
		
		if(this.nivel == 1)
		{
			//no primeiro nivel, iremos iniciar a musica de fundo
			this.mudarMusicaDeFundo(R.raw.radiate);
		}
	}
	
	public void mostrarTelaObservacaoNovosKanjis()
	{
		this.switchToScreen(R.id.tela_observacao_novos_kanjis);
		this.mudarFonte4UltimosKanjisETitulo();
		TextView textoUltimoKanji1 = (TextView) findViewById(R.id.ultimokanji1);
		TextView textoUltimoKanji2 = (TextView) findViewById(R.id.ultimokanji2);
		TextView textoUltimoKanji3 = (TextView) findViewById(R.id.ultimokanji3);
		TextView textoUltimoKanji4 = (TextView) findViewById(R.id.ultimokanji4);
		TextView textoUltimoKanji1Furigana = (TextView) findViewById(R.id.ultimoKanji1Furigana);
		TextView textoUltimoKanji2Furigana = (TextView) findViewById(R.id.ultimoKanji2Furigana);
		TextView textoUltimoKanji3Furigana = (TextView) findViewById(R.id.ultimoKanji3Furigana);
		TextView textoUltimoKanji4Furigana = (TextView) findViewById(R.id.ultimoKanji4Furigana);
		TextView textoUltimoKanji1Traducao = (TextView) findViewById(R.id.ultimoKanji1Traducao);
		TextView textoUltimoKanji2Traducao = (TextView) findViewById(R.id.ultimoKanji2Traducao);
		TextView textoUltimoKanji3Traducao = (TextView) findViewById(R.id.ultimoKanji3Traducao);
		TextView textoUltimoKanji4Traducao = (TextView) findViewById(R.id.ultimoKanji4Traducao);
		
		this.mudarFonteTraducoesQuatroUltimosKanjis();
		
		if(this.ultimosKanjis.size() == 0)
		{
			mostrarTelaModoTreinamento(null);
		}
		else if(this.ultimosKanjis.size() == 1)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			String textoDicaKanji1 = ultimoKanji1.getKanji(); 
			textoUltimoKanji1.setText(textoDicaKanji1);
			textoUltimoKanji1Furigana.setText(ultimoKanji1.getHiraganaDoKanji());
			textoUltimoKanji1Traducao.setText("=   " + ultimoKanji1.getTraducaoEmPortugues());
			
			textoUltimoKanji2.setText("");
			textoUltimoKanji3.setText("");
			textoUltimoKanji4.setText("");
			textoUltimoKanji2Furigana.setText("");
			textoUltimoKanji3Furigana.setText("");
			textoUltimoKanji4Furigana.setText("");
			textoUltimoKanji2Traducao.setText("");
			textoUltimoKanji3Traducao.setText("");
			textoUltimoKanji4Traducao.setText("");
			
		}
		else if(this.ultimosKanjis.size() == 2)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			KanjiTreinar ultimoKanji2 = this.ultimosKanjis.get(1); 
			
			String textoDicaKanji1 = ultimoKanji1.getKanji(); 
			textoUltimoKanji1.setText(textoDicaKanji1);
			textoUltimoKanji1Furigana.setText(ultimoKanji1.getHiraganaDoKanji());
			textoUltimoKanji1Traducao.setText("=   " + ultimoKanji1.getTraducaoEmPortugues());
			String textoDicaKanji2 = ultimoKanji2.getKanji(); 
			textoUltimoKanji2.setText(textoDicaKanji2);
			textoUltimoKanji2Furigana.setText(ultimoKanji2.getHiraganaDoKanji());
			textoUltimoKanji2Traducao.setText("=   " + ultimoKanji2.getTraducaoEmPortugues());
			
			textoUltimoKanji3.setText("");
			textoUltimoKanji4.setText("");
			textoUltimoKanji3Furigana.setText("");
			textoUltimoKanji4Furigana.setText("");
			textoUltimoKanji3Traducao.setText("");
			textoUltimoKanji4Traducao.setText("");
		}
		else if(this.ultimosKanjis.size() == 3)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			KanjiTreinar ultimoKanji2 = this.ultimosKanjis.get(1); 
			KanjiTreinar ultimoKanji3 = this.ultimosKanjis.get(2); 
			
			String textoDicaKanji1 = ultimoKanji1.getKanji(); 
			textoUltimoKanji1.setText(textoDicaKanji1);
			textoUltimoKanji1Furigana.setText(ultimoKanji1.getHiraganaDoKanji());
			textoUltimoKanji1Traducao.setText("=   " + ultimoKanji1.getTraducaoEmPortugues());
			String textoDicaKanji2 = ultimoKanji2.getKanji(); 
			textoUltimoKanji2.setText(textoDicaKanji2);
			textoUltimoKanji2Furigana.setText(ultimoKanji2.getHiraganaDoKanji());
			textoUltimoKanji2Traducao.setText("=   " + ultimoKanji2.getTraducaoEmPortugues());
			String textoDicaKanji3 = ultimoKanji3.getKanji(); 
			textoUltimoKanji3.setText(textoDicaKanji3);
			textoUltimoKanji3Furigana.setText(ultimoKanji3.getHiraganaDoKanji());
			textoUltimoKanji3Traducao.setText("=   " + ultimoKanji3.getTraducaoEmPortugues());
			
			textoUltimoKanji4.setText("");
			textoUltimoKanji4Furigana.setText("");
			textoUltimoKanji4Traducao.setText("");
		}
		else if(this.ultimosKanjis.size() == 4)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			KanjiTreinar ultimoKanji2 = this.ultimosKanjis.get(1); 
			KanjiTreinar ultimoKanji3 = this.ultimosKanjis.get(2); 
			KanjiTreinar ultimoKanji4 = this.ultimosKanjis.get(3); 
			
			String textoDicaKanji1 = ultimoKanji1.getKanji(); 
			textoUltimoKanji1.setText(textoDicaKanji1);
			textoUltimoKanji1Furigana.setText(ultimoKanji1.getHiraganaDoKanji());
			textoUltimoKanji1Traducao.setText("=   " + ultimoKanji1.getTraducaoEmPortugues());
			String textoDicaKanji2 = ultimoKanji2.getKanji(); 
			textoUltimoKanji2.setText(textoDicaKanji2);
			textoUltimoKanji2Furigana.setText(ultimoKanji2.getHiraganaDoKanji());
			textoUltimoKanji2Traducao.setText("=   " + ultimoKanji2.getTraducaoEmPortugues());
			String textoDicaKanji3 = ultimoKanji3.getKanji(); 
			textoUltimoKanji3.setText(textoDicaKanji3);
			textoUltimoKanji3Furigana.setText(ultimoKanji3.getHiraganaDoKanji());
			textoUltimoKanji3Traducao.setText("=   " + ultimoKanji3.getTraducaoEmPortugues());
			String textoDicaKanji4 = ultimoKanji4.getKanji(); 
			textoUltimoKanji4.setText(textoDicaKanji4);
			textoUltimoKanji4Furigana.setText(ultimoKanji4.getHiraganaDoKanji());
			textoUltimoKanji4Traducao.setText("=   " + ultimoKanji4.getTraducaoEmPortugues());
		}
		
		TextView textoObservacao = (TextView) findViewById(R.id.observacao);
		if(this.nivel == 5 || this.nivel == 9 || this.nivel == 13 || this.nivel == 17 || this.nivel == 21 || this.nivel == 25 || this.nivel == 29 || this.nivel == 33)
		{
			textoObservacao.setVisibility(View.VISIBLE);
		}
		else
		{
			textoObservacao.setVisibility(View.INVISIBLE);
		}
		
	}
	
	private void escolherKanjisParaONivel()
	{	 
		 this.kanjisDasCartasNaTela.clear();
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.clear();
		 
		 
		 if(this.quantosNiveisPassaram >= 2 || nivel == 1)
		 {
			 //hora de mais novos kanjis
			 this.escolherNovosKanjis();
			 if(this.mostrarDicas == true)
			 {
				this.mostrarTelaObservacaoNovosKanjis(); //aquela tela que o usuario visualiza os novos kanjis
			 }
			 else
			 {
				this.mostrarTelaModoTreinamento(null);
			 }
			 
			 this.quantosNiveisPassaram = 0;
		 }
		  
		 if(naoHaMaisNovosKanjisParaSeCriar == true && this.quantosNiveisPassaram >= 2)
		 {
			 //nao tem mais utilidade esses ultimos kanjis
			 this.ultimosKanjis.clear();
		 }
		 
		//escolher kanjis velhos
		 LinkedList<KanjiTreinar> velhosKanjis = this.escolherKanjisNaoNovos(this.quantasCartasHaveraoNaTela - this.ultimosKanjis.size());
		 
		 LinkedList<KanjiTreinar> kanjisDaTela = new LinkedList<KanjiTreinar>();
		 
		 for(int g = 0; g < ultimosKanjis.size(); g++)
		 {
			 kanjisDaTela.add(ultimosKanjis.get(g));
		 }
		 for(int h = 0; h < velhosKanjis.size(); h++)
		 {
			 kanjisDaTela.add(velhosKanjis.get(h));
		 }
		 
		 Collections.shuffle(kanjisDaTela);
		 
		 
		 for(int i = 0; i < this.quantasCartasHaveraoNaTela; i++)
		 {
			 KanjiTreinar kanjiParaUmaCarta = kanjisDaTela.get(i);
			 
			 this.kanjisDasCartasNaTela.add(kanjiParaUmaCarta);
				 
				 if(i == 0)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 1)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 2)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 3)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 4)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 5)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 6)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 7)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 } 
				 else if(i == 8)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta9);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 9)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta10);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 10)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta11);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 11)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta12);
					 this.colocarTextoVerticalNaCarta(texto, kanjiParaUmaCarta.getKanji());
				 }
			 }
		 
	 }
	
	private KanjiTreinar escolherUmNovoKanjiParaTreinar()
	 {
		if(kanjisQuePodemVirarCartasNovas.size() <= 0)
		{
			return null;
		}
		else
		{
			Random geraNumAleatorio = new Random();
			 int posicaoKanjiEscolhido = geraNumAleatorio.nextInt(this.kanjisQuePodemVirarCartasNovas.size());
			 
			 KanjiTreinar kanjiEscolhido = this.kanjisQuePodemVirarCartasNovas.remove(posicaoKanjiEscolhido); 
			 
			 this.kanjisQueJaViraramCartas.add(kanjiEscolhido);
			 
			 return kanjiEscolhido;
		} 
	 }
	
	private void pegarTodosOsKanjisQuePodemVirarCartas()
	 {
		 this.kanjisQuePodemVirarCartasNovas = new LinkedList<KanjiTreinar>();
		 HashMap<String,LinkedList<KanjiTreinar>> categoriasEscolhidasEKanjisDelas = SingletonGuardaDadosDaPartida.getInstance().getCategoriasEscolhidasEKanjisDelas();
		 
		 Iterator<String> iteradorCategoriasEKanjis = categoriasEscolhidasEKanjisDelas.keySet().iterator();
		 while(iteradorCategoriasEKanjis.hasNext() == true)
		 {
			 String umaCategoria = iteradorCategoriasEKanjis.next();
			 LinkedList<KanjiTreinar> kanjisDaCategoria = categoriasEscolhidasEKanjisDelas.get(umaCategoria);
			 
			 for(int i = 0; i < kanjisDaCategoria.size(); i++)
			 {
				 this.kanjisQuePodemVirarCartasNovas.add(kanjisDaCategoria.get(i));
			 }
		 }
	 }
	
	public void gerarKanjiDaDica()
	{
		LinkedList<KanjiTreinar> kanjisQueAindaNaoViraramDicas = new LinkedList<KanjiTreinar>();
		 
		 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
			 
			 boolean kanjiJaVirouDica = false;
			 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
			 {
				 KanjiTreinar umKanjiQueVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
				 
				 if((umKanjiQueVirouDica.getKanji().compareTo(umKanji.getKanji()) == 0) 
						 && (umKanjiQueVirouDica.getCategoriaAssociada().compareTo(umKanji.getCategoriaAssociada()) == 0))
				 {
					 kanjiJaVirouDica = true;
				 }
			 }
			 
			 if(kanjiJaVirouDica == false)
			 {
				 kanjisQueAindaNaoViraramDicas.add(umKanji);
			 }
		 }
		 
		 
		 Random geraNumAleatorio = new Random(); 
		 int indiceKanjiDaDica = geraNumAleatorio.nextInt(kanjisQueAindaNaoViraramDicas.size());
		 
		 KanjiTreinar umKanji = kanjisQueAindaNaoViraramDicas.get(indiceKanjiDaDica);
		 this.kanjiDaDica = umKanji;
		 
		 this.alterarTextoDicaComBaseNoKanjiDaDica();
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(umKanji);
	}
	
	private void alterarTextoDicaComBaseNoKanjiDaDica()
	{
		 TextView textoDica = (TextView) findViewById(R.id.dica_kanji);
		 String hiraganaDoKanji = this.kanjiDaDica.getHiraganaDoKanji();
		 String traducaoDoKanji = this.kanjiDaDica.getTraducaoEmPortugues();
		 textoDica.setText(hiraganaDoKanji);
		 TextView textoTraducao = (TextView) findViewById(R.id.dica_kanji_traducao);
		 textoTraducao.setText("(" + traducaoDoKanji + ")");
	}
	
	private void gerarKanjiDaDicaOuIniciarNovoNivel()
	{
		 if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == this.kanjisDasCartasNaTela.size())
		 {
			//deve-se passar para o proximo nivel
		    this.realizarProcedimentoPassarParaProximoNivel();
		    this.gerarKanjiDaDica();
		 }
		 else
		 {
			 this.gerarKanjiDaDica();
		 }
	}
	 
	private void realizarProcedimentoPassarParaProximoNivel()
	{
		this.nivel = this.nivel + 1;
		if(nivel == 5)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 9)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 13)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 17)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 21)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 25)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 29)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 33)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		

		this.quantosNiveisPassaram = this.quantosNiveisPassaram + 1;
		 TextView textViewNivel = (TextView) findViewById(R.id.nivel);
		 String nivel = getResources().getString(R.string.nivel_sem_dois_pontos);
		 textViewNivel.setText(nivel + " " +String.valueOf(this.nivel));
		 
		 this.tornarCartasNaTelaClicaveisEVaziasNovamente();
	     this.escolherKanjisParaONivel();
	     
	}
	
	private void tornarCartasNaTelaClicaveisEVaziasNovamente()
	{
		 ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
		 imageViewKaruta1.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
		 findViewById(R.id.karuta1).setClickable(true);
		 imageViewKaruta1.setVisibility(View.VISIBLE);
		 this.fazerImageViewVoltarACartaNormal(imageViewKaruta1);
		 
		 ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
		 imageViewKaruta2.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
		 findViewById(R.id.karuta2).setClickable(true);
		 imageViewKaruta2.setVisibility(View.VISIBLE);
		 this.fazerImageViewVoltarACartaNormal(imageViewKaruta2);
		 
		 ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
		 imageViewKaruta3.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
		 findViewById(R.id.karuta3).setClickable(true);
		 imageViewKaruta3.setVisibility(View.VISIBLE);
		 this.fazerImageViewVoltarACartaNormal(imageViewKaruta3);
		 
		 ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 imageViewKaruta4.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
		 findViewById(R.id.karuta4).setClickable(true);
		 imageViewKaruta4.setVisibility(View.VISIBLE);
		 this.fazerImageViewVoltarACartaNormal(imageViewKaruta4);
		 
		 if(quantasCartasHaveraoNaTela < 5)
		 { 
			 TextView textoKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
			 textoKaruta5.setText("");
			 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
			 textoKaruta6.setText("");
			 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
			 textoKaruta7.setText("");
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
			 TextView textoKaruta9 = (TextView) findViewById(R.id.texto_karuta9);
			 textoKaruta9.setText("");
			 TextView textoKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
			 textoKaruta10.setText("");
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
			 
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
		 }
		 else if(this.quantasCartasHaveraoNaTela == 5)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 
			 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
			 textoKaruta6.setText("");
			 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
			 textoKaruta7.setText("");
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
			 TextView textoKaruta9 = (TextView) findViewById(R.id.texto_karuta9);
			 textoKaruta9.setText("");
			 TextView textoKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
			 textoKaruta10.setText("");
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 6)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 
			 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
			 textoKaruta7.setText("");
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
			 TextView textoKaruta9 = (TextView) findViewById(R.id.texto_karuta9);
			 textoKaruta9.setText("");
			 TextView textoKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
			 textoKaruta10.setText("");
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 7)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta7);
			 
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
			 TextView textoKaruta9 = (TextView) findViewById(R.id.texto_karuta9);
			 textoKaruta9.setText("");
			 TextView textoKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
			 textoKaruta10.setText("");
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
			 
			 
		 }
		 else if(this.quantasCartasHaveraoNaTela == 8)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta8).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 imageViewKaruta8.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta7);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta8);
			 
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 TextView textoKaruta9 = (TextView) findViewById(R.id.texto_karuta9);
			 textoKaruta9.setText("");
			 TextView textoKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
			 textoKaruta10.setText("");
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 9)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta8).setClickable(true);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta9).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 imageViewKaruta8.setVisibility(View.VISIBLE);
			 imageViewKaruta9.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta7);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta8);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta9);
			 
			 
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 TextView textoKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
			 textoKaruta10.setText("");
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 10)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta8).setClickable(true);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta9).setClickable(true);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta10).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 imageViewKaruta8.setVisibility(View.VISIBLE);
			 imageViewKaruta9.setVisibility(View.VISIBLE);
			 imageViewKaruta10.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta7);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta8);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta9);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta10);
			 
			 
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 TextView textoKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
			 textoKaruta11.setText("");
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 11)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta8).setClickable(true);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta9).setClickable(true);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta10).setClickable(true);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta11).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 imageViewKaruta8.setVisibility(View.VISIBLE);
			 imageViewKaruta9.setVisibility(View.VISIBLE);
			 imageViewKaruta10.setVisibility(View.VISIBLE);
			 imageViewKaruta11.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta7);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta8);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta9);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta10);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta11);
			 
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setVisibility(View.INVISIBLE);
			 TextView textoKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
			 textoKaruta12.setText("");
		 }
		 else
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta8).setClickable(true);
			 ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
			 imageViewKaruta9.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta9).setClickable(true);
			 ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
			 imageViewKaruta10.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta10).setClickable(true);
			 ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
			 imageViewKaruta11.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta11).setClickable(true);
			 ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
			 imageViewKaruta12.setImageResource(R.drawable.karutavaziatreinamento); //mudei a figura da carta
			 findViewById(R.id.karuta12).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 imageViewKaruta8.setVisibility(View.VISIBLE);
			 imageViewKaruta9.setVisibility(View.VISIBLE);
			 imageViewKaruta10.setVisibility(View.VISIBLE);
			 imageViewKaruta11.setVisibility(View.VISIBLE);
			 imageViewKaruta12.setVisibility(View.VISIBLE);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta5);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta6);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta7);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta8);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta9);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta10);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta11);
			 this.fazerImageViewVoltarACartaNormal(imageViewKaruta12);
		 }
	 }
	
	private LinkedList<KanjiTreinar> escolherKanjisNaoNovos(int quantosKanjis)
	{
		LinkedList<KanjiTreinar> kanjisVelhos = new LinkedList<KanjiTreinar>();
		
		for(int i= 0; i < quantosKanjis; i++)
		{	
			Random geraNumAleatorio = new Random();
			boolean kanjiVelhoEhRepetido = true;
			
			KanjiTreinar kanjiNaoNovoEscolhido = null;
			
			while(kanjiVelhoEhRepetido == true)
			{
				int posicaoNovoKanjiNosKanjisJaViraramCartas = geraNumAleatorio.nextInt(this.kanjisQueJaViraramCartas.size());
				KanjiTreinar umKanji = this.kanjisQueJaViraramCartas.get(posicaoNovoKanjiNosKanjisJaViraramCartas);
				
				boolean umKanjiJaExisteNosKanjisVelhos = false;
				for(int j = 0; j < kanjisVelhos.size(); j++)
				{
					KanjiTreinar umKanjiVelho = kanjisVelhos.get(j);
					if(umKanjiVelho.getKanji().compareTo(umKanji.getKanji()) == 0 &&
							umKanjiVelho.getCategoriaAssociada().compareTo(umKanji.getCategoriaAssociada()) == 0)
					{
						//umKanji ja existe nos velhos
						umKanjiJaExisteNosKanjisVelhos = true;
					}
					
				}
				
				//esse kanji velho nao pode pertencer aos ultimos kanjis
				for(int k = 0; k < this.ultimosKanjis.size(); k++)
				{
					KanjiTreinar umDosUltimos = this.ultimosKanjis.get(k);
					if((umDosUltimos.getKanji().compareTo(umKanji.getKanji()) == 0)
							&& (umDosUltimos.getCategoriaAssociada().compareTo(umKanji.getCategoriaAssociada()) == 0))
					{
						//o kanjivelho eh um dos ultimos! nao pode! Devemos escolher outro kanji velho, por isso vamos obrigar que outro kanji seja escolhido
						umKanjiJaExisteNosKanjisVelhos = true;
					}
				}
				
				
				if(umKanjiJaExisteNosKanjisVelhos == true)
				{
					kanjiVelhoEhRepetido = true;
				}
				else
				{
					kanjiVelhoEhRepetido = false;
					kanjiNaoNovoEscolhido = umKanji;
				}
			}
			
			kanjisVelhos.add(kanjiNaoNovoEscolhido);
			
		}
		
		return kanjisVelhos;
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.karuta1_imageview:
	    		TextView textViewKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	        	String textoKaruta1 = this.kanjisDasCartasNaTela.get(0).getKanji();
	        	String textoKanjiDaDica = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta1.compareTo(textoKanjiDaDica) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta1); //mudei a figura da carta
	        		findViewById(R.id.karuta1).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta1.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        		
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta1.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	
	    	break;
	    case R.id.karuta2_imageview:
	    		TextView textViewKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	        	String textoKaruta2 = this.kanjisDasCartasNaTela.get(1).getKanji();
	        	String textoKanjiDaDica2 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta2.compareTo(textoKanjiDaDica2) == 0)
	        	{
	        		//usuario acertou o kanji
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta2); //mudei a figura da carta
	        		findViewById(R.id.karuta2).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta2.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta2.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta3_imageview:
	    		TextView textViewKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	        	String textoKaruta3 = this.kanjisDasCartasNaTela.get(2).getKanji();
	        	String textoKanjiDaDica3 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta3.compareTo(textoKanjiDaDica3) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta3); //mudei a figura da carta
	        		findViewById(R.id.karuta3).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta3.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta3.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta4_imageview:
	    		TextView textViewKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	        	String textoKaruta4 = this.kanjisDasCartasNaTela.get(3).getKanji();
	        	String textoKanjiDaDica4 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta4.compareTo(textoKanjiDaDica4) == 0)
	        	{
	        		//usuario acertou o kanji. 
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta4); //mudei a figura da carta
	        		findViewById(R.id.karuta4).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta4.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta4.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta5_imageview:
	    		TextView textViewKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	        	String textoKaruta5 = this.kanjisDasCartasNaTela.get(4).getKanji();
	        	String textoKanjiDaDica5 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta5.compareTo(textoKanjiDaDica5) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta5); //mudei a figura da carta
	        		findViewById(R.id.karuta5).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta5.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta5.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta6_imageview:
	    		TextView textViewKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	        	String textoKaruta6 = this.kanjisDasCartasNaTela.get(5).getKanji();
	        	String textoKanjiDaDica6 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta6.compareTo(textoKanjiDaDica6) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta6); //mudei a figura da carta
	        		findViewById(R.id.karuta6).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta6.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta6.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta7_imageview:
	    		TextView textViewKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	        	String textoKaruta7 = this.kanjisDasCartasNaTela.get(6).getKanji();
	        	String textoKanjiDaDica7 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta7.compareTo(textoKanjiDaDica7) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta7); //mudei a figura da carta
	        		findViewById(R.id.karuta7).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta7.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta7.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta8_imageview:
	    		TextView textViewKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	        	String textoKaruta8 = this.kanjisDasCartasNaTela.get(7).getKanji();
	        	String textoKanjiDaDica8 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta8.compareTo(textoKanjiDaDica8) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		this.fazerMascoteFicarFelizPorUmTempo();
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta8); //mudei a figura da carta
	        		findViewById(R.id.karuta8).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta8.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		if(textViewKaruta8.getText().length() > 0)
	        		{
	        			this.realizarProcedimentoUsuarioErrouCarta();
	        		}
	        	}
	    	break;
	    case R.id.karuta9_imageview:
    		TextView textViewKaruta9 = (TextView) findViewById(R.id.texto_karuta9);
        	String textoKaruta9 = this.kanjisDasCartasNaTela.get(8).getKanji();
        	String textoKanjiDaDica9 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta9.compareTo(textoKanjiDaDica9) == 0)
        	{
        		//usuario acertou o kanji.
        		super.reproduzirSfx("acertou_carta");
        		this.fazerMascoteFicarFelizPorUmTempo();
        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		ImageView imageViewKaruta9 = (ImageView) findViewById(R.id.karuta9_imageview);
        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta9); //mudei a figura da carta
        		findViewById(R.id.karuta9).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta9.setText("");
        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
        	}
        	else
        	{
        		//errou
        		if(textViewKaruta9.getText().length() > 0)
        		{
        			this.realizarProcedimentoUsuarioErrouCarta();
        		}
        	}
    	break;
	    case R.id.karuta10_imageview:
    		TextView textViewKaruta10 = (TextView) findViewById(R.id.texto_karuta10);
        	String textoKaruta10 = this.kanjisDasCartasNaTela.get(9).getKanji();
        	String textoKanjiDaDica10 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta10.compareTo(textoKanjiDaDica10) == 0)
        	{
        		//usuario acertou o kanji.
        		super.reproduzirSfx("acertou_carta");
        		this.fazerMascoteFicarFelizPorUmTempo();
        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		ImageView imageViewKaruta10 = (ImageView) findViewById(R.id.karuta10_imageview);
        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta10); //mudei a figura da carta
        		findViewById(R.id.karuta10).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta10.setText("");
        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
        	}
        	else
        	{
        		//errou
        		if(textViewKaruta10.getText().length() > 0)
        		{
        			this.realizarProcedimentoUsuarioErrouCarta();
        		}
        	}
    	break;
	    case R.id.karuta11_imageview:
    		TextView textViewKaruta11 = (TextView) findViewById(R.id.texto_karuta11);
        	String textoKaruta11 = this.kanjisDasCartasNaTela.get(10).getKanji();
        	String textoKanjiDaDica11 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta11.compareTo(textoKanjiDaDica11) == 0)
        	{
        		//usuario acertou o kanji.
        		super.reproduzirSfx("acertou_carta");
        		this.fazerMascoteFicarFelizPorUmTempo();
        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		ImageView imageViewKaruta11 = (ImageView) findViewById(R.id.karuta11_imageview);
        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta11); //mudei a figura da carta
        		findViewById(R.id.karuta11).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta11.setText("");
        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
        	}
        	else
        	{
        		//errou
        		if(textViewKaruta11.getText().length() > 0)
        		{
        			this.realizarProcedimentoUsuarioErrouCarta();
        		}
        	}
    	break;
	    case R.id.karuta12_imageview:
    		TextView textViewKaruta12 = (TextView) findViewById(R.id.texto_karuta12);
        	String textoKaruta12 = this.kanjisDasCartasNaTela.get(11).getKanji();
        	String textoKanjiDaDica12 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta12.compareTo(textoKanjiDaDica12) == 0)
        	{
        		//usuario acertou o kanji.
        		super.reproduzirSfx("acertou_carta");
        		this.fazerMascoteFicarFelizPorUmTempo();
        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		ImageView imageViewKaruta12 = (ImageView) findViewById(R.id.karuta12_imageview);
        		this.fazerImageViewMudarParaVersoDeCarta(imageViewKaruta12); //mudei a figura da carta
        		findViewById(R.id.karuta12).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta12.setText("");
        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
        	}
        	else
        	{
        		//errou
        		if(textViewKaruta12.getText().length() > 0)
        		{
        			this.realizarProcedimentoUsuarioErrouCarta();
        		}
        	}
    	break;
	    	
		}
	}
	
	public void realizarProcedimentoUsuarioErrouCarta()
	{
		super.reproduzirSfx("errou_carta");
		
		kanjisQueUsuarioErrou.add(kanjiDaDica);
		this.vidas = this.vidas - 1;
		
		if(vidas == 3)
		{
			ImageView coracao1 = (ImageView) findViewById(R.id.coracao1);
			coracao1.setImageResource(R.drawable.heartquebrado);
		}
		else if(vidas == 2)
		{
			ImageView coracao2 = (ImageView) findViewById(R.id.coracao2);
			coracao2.setImageResource(R.drawable.heartquebrado);
		}
		else if(vidas == 1)
		{
			ImageView coracao3 = (ImageView) findViewById(R.id.coracao3);
			coracao3.setImageResource(R.drawable.heartquebrado);
		}
		else if(vidas == 0)
		{
			ImageView coracao4 = (ImageView) findViewById(R.id.coracao4);
			coracao4.setImageResource(R.drawable.heartquebrado);
			this.terminarModoTreinamento();
		}
		
		final ImageView imageViewMascote = (ImageView) findViewById(R.id.mascote);
		imageViewMascote.setImageResource(R.drawable.mascotetreinamento_zangada);
		
		//a mascote vai ficar com cara de zangada por um tempo e depois volta ao normal
		new Timer().schedule(new TimerTask() 
		 { 
			    @Override
			    public void run() 
			    {
			        //If you want to operate UI modifications, you must run ui stuff on UiThread.
			        ModoTreinamento.this.runOnUiThread(new Runnable() 
			        {
			            @Override
			            public void run() 
			            {
			            	imageViewMascote.setImageResource(R.drawable.mascotetreinamento);
			            }
			        });
			    }
			}, 1000);
		
	}
	
	
	void switchToScreen(int screenId) 
	{
		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
		    findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
		}
	}
	
	private void aumentarPontuacaoComBaseNaDificuldadeDoKanji()
	{
		int dificuldade = this.kanjiDaDica.getDificuldadeDoKanji();
		
		if(dificuldade == 1)
		{
			this.suaPontuacao = this.suaPontuacao + 1;
		}
		else if(dificuldade == 2)
		{
			this.suaPontuacao = this.suaPontuacao + 2;
		}
		else
		{
			this.suaPontuacao = this.suaPontuacao + 3;
		}
		
		TextView textoPontuacao = (TextView) findViewById(R.id.pontuacao);
    	String pontuacao = getResources().getString(R.string.pontos);
    	
		if(suaPontuacao < 100)
    	{
    		textoPontuacao.setText(pontuacao + "0" + String.valueOf(suaPontuacao));
    	}
    	else
    	{
    		textoPontuacao.setText(pontuacao + String.valueOf(suaPontuacao));
    	}
		
		//this.realizarAnimacaoAumentaPontuacao(dificuldade);
	}
	
	private void realizarAnimacaoAumentaPontuacao(int dificuldadeDoKanji)
	{
		/*final AnimationDrawable animacaoAumentaPontuacao = new AnimationDrawable(); 
		int idImagemAnimacaoAumentaPontos1 = 0;
		int idImagemAnimacaoAumentaPontos2 = 0;
		int idImagemAnimacaoAumentaPontos3 = 0;
		int idImagemAnimacaoAumentaPontos4 = 0;
		int idImagemAnimacaoAumentaPontos5 = 0;
		int idImagemAnimacaoAumentaPontos6 = 0;
		int idImagemAnimacaoAumentaPontos7 = 0;
		int idImagemAnimacaoAumentaPontos8 = 0;
		int idImagemAnimacaoAumentaPontos9 = 0;
		int idImagemAnimacaoAumentaPontos10 = 0;
		int idImagemAnimacaoAumentaPontos11 = 0;
		int idImagemAnimacaoAumentaPontos12 = 0;
		int idImagemAnimacaoAumentaPontos13 = 0;
		int idImagemAnimacaoAumentaPontos14 = 0;
		int idImagemAnimacaoAumentaPontos15 = 0;
		int idImagemAnimacaoAumentaPontos16 = 0;
		int idImagemAnimacaoAumentaPontos17 = 0;
		
		
		if(dificuldadeDoKanji == 1)
		{
			idImagemAnimacaoAumentaPontos1 = getResources().getIdentifier("animacao10_1", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos2 = getResources().getIdentifier("animacao10_2", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos3 = getResources().getIdentifier("animacao10_3", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos4 = getResources().getIdentifier("animacao10_4", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos5 = getResources().getIdentifier("animacao10_5", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos6 = getResources().getIdentifier("animacao10_6", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos7 = getResources().getIdentifier("animacao10_7", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos8 = getResources().getIdentifier("animacao10_8", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos9 = getResources().getIdentifier("animacao10_9", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos10 = getResources().getIdentifier("animacao10_10", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos11 = getResources().getIdentifier("animacao10_11", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos12 = getResources().getIdentifier("animacao10_12", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos13 = getResources().getIdentifier("animacao10_13", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos14 = getResources().getIdentifier("animacao10_14", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos15 = getResources().getIdentifier("animacao10_15", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos16 = getResources().getIdentifier("animacao10_16", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos17 = getResources().getIdentifier("animacao10_17", "drawable", getPackageName());
		}
		else if(dificuldadeDoKanji == 2)
		{
			idImagemAnimacaoAumentaPontos1 = getResources().getIdentifier("animacao20_1", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos2 = getResources().getIdentifier("animacao20_2", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos3 = getResources().getIdentifier("animacao20_3", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos4 = getResources().getIdentifier("animacao20_4", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos5 = getResources().getIdentifier("animacao20_5", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos6 = getResources().getIdentifier("animacao20_6", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos7 = getResources().getIdentifier("animacao20_7", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos8 = getResources().getIdentifier("animacao20_8", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos9 = getResources().getIdentifier("animacao20_9", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos10 = getResources().getIdentifier("animacao20_10", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos11 = getResources().getIdentifier("animacao20_11", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos12 = getResources().getIdentifier("animacao20_12", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos13 = getResources().getIdentifier("animacao20_13", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos14 = getResources().getIdentifier("animacao20_14", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos15 = getResources().getIdentifier("animacao20_15", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos16 = getResources().getIdentifier("animacao20_16", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos17 = getResources().getIdentifier("animacao20_17", "drawable", getPackageName());
		}
		else if(dificuldadeDoKanji == 3)
		{
			idImagemAnimacaoAumentaPontos1 = getResources().getIdentifier("animacao30_1", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos2 = getResources().getIdentifier("animacao30_2", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos3 = getResources().getIdentifier("animacao30_3", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos4 = getResources().getIdentifier("animacao30_4", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos5 = getResources().getIdentifier("animacao30_5", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos6 = getResources().getIdentifier("animacao30_6", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos7 = getResources().getIdentifier("animacao30_7", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos8 = getResources().getIdentifier("animacao30_8", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos9 = getResources().getIdentifier("animacao30_9", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos10 = getResources().getIdentifier("animacao30_10", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos11 = getResources().getIdentifier("animacao30_11", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos12 = getResources().getIdentifier("animacao30_12", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos13 = getResources().getIdentifier("animacao30_13", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos14 = getResources().getIdentifier("animacao30_14", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos15 = getResources().getIdentifier("animacao30_15", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos16 = getResources().getIdentifier("animacao30_16", "drawable", getPackageName());
			idImagemAnimacaoAumentaPontos17 = getResources().getIdentifier("animacao30_17", "drawable", getPackageName());
		}
		
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos1), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos2), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos3), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos4), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos5), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos6), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos7), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos8), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos9), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos10), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos11), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos12), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos13), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos14), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos15), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos16), 50);
		 animacaoAumentaPontuacao.addFrame(getResources().getDrawable(idImagemAnimacaoAumentaPontos17), 50);
		 animacaoAumentaPontuacao.setOneShot(true);
		 
		 ImageView imageViewMaisPontos = (ImageView) findViewById(R.id.maispontos);
		 imageViewMaisPontos.setImageDrawable(animacaoAumentaPontuacao);
		 
		 imageViewMaisPontos.post(new Runnable() {
			@Override
			public void run() {
				animacaoAumentaPontuacao.start();
			}
		 	});
		 
		 new Timer().schedule(new TimerTask() 
		 { 
			    @Override
			    public void run() 
			    {
			        //If you want to operate UI modifications, you must run ui stuff on UiThread.
			        ModoTreinamento.this.runOnUiThread(new Runnable() 
			        {
			            @Override
			            public void run() 
			            {
			            	TextView textoPontuacao = (TextView) findViewById(R.id.pontuacao);
			            	String pontuacao = getResources().getString(R.string.pontuacao);
			            	
			            	if(suaPontuacao < 100)
			            	{
			            		textoPontuacao.setText(pontuacao + "0" + String.valueOf(suaPontuacao));
			            	}
			            	else
			            	{
			            		textoPontuacao.setText(pontuacao + String.valueOf(suaPontuacao));
			            	}
			            }
			        });
			    }
			}, 900);*/
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
	
	private void terminarModoTreinamento()
	{
		this.mudarMusicaDeFundo(R.raw.time_to_unwind);
		
		switchToScreen(R.id.tela_fim_do_modo_treinamento);
		this.mudarFonteDeTodoOTextoDaTelaFinal();
		
		TextView quantosPontosVoceFez = (TextView) findViewById(R.id.quantosPontosVoceFez);
		String textoVoceFez = getResources().getString(R.string.voceFez);
		quantosPontosVoceFez.setText(textoVoceFez + " " + this.suaPontuacao + " ");
		
		TextView textViewNivel = (TextView) findViewById(R.id.chegouAteONivel);
		String textoNivel = getResources().getString(R.string.chegouAteQueNivel);
		textViewNivel.setText(textoNivel + " " + this.nivel);
		
		LinkedList<KanjiTreinar> kanjisErradosSemRepeticao = new LinkedList<KanjiTreinar>();
		for(int i = 0; i < this.kanjisQueUsuarioErrou.size(); i++)
		{
			KanjiTreinar umKanjiErrado = kanjisQueUsuarioErrou.get(i);
			
			boolean kanjiJaPertenceAoSemRepeticao = false;
			for(int j = 0; j < kanjisErradosSemRepeticao.size(); j++)
			{
				KanjiTreinar umKanjiErradoSemRepeticao = kanjisErradosSemRepeticao.get(j);
				
				if((umKanjiErrado.getKanji().compareTo(umKanjiErradoSemRepeticao.getKanji()) == 0)
						&& (umKanjiErrado.getCategoriaAssociada().compareTo(umKanjiErradoSemRepeticao.getCategoriaAssociada()) == 0))
				{
					kanjiJaPertenceAoSemRepeticao = true;
				}
			}
			
			if(kanjiJaPertenceAoSemRepeticao == false)
			{
				kanjisErradosSemRepeticao.add(umKanjiErrado);
			}
		}
		
		TextView textViewPalavraErrada1 = (TextView) findViewById(R.id.palavraErrada1);
		TextView textViewPalavraErrada2 = (TextView) findViewById(R.id.palavraErrada2);
		TextView textViewPalavraErrada3 = (TextView) findViewById(R.id.palavraErrada3);
		TextView textViewPalavraErrada4 = (TextView) findViewById(R.id.palavraErrada4);
		TextView textViewPalavraErrada1Furigana = (TextView) findViewById(R.id.palavraErrada1Furigana);
		TextView textViewPalavraErrada2Furigana = (TextView) findViewById(R.id.palavraErrada2Furigana);
		TextView textViewPalavraErrada3Furigana = (TextView) findViewById(R.id.palavraErrada3Furigana);
		TextView textViewPalavraErrada4Furigana = (TextView) findViewById(R.id.palavraErrada4Furigana);
		TextView textViewPalavraErrada1Traducao = (TextView) findViewById(R.id.palavraErrada1Traducao);
		TextView textViewPalavraErrada2Traducao = (TextView) findViewById(R.id.palavraErrada2Traducao);
		TextView textViewPalavraErrada3Traducao = (TextView) findViewById(R.id.palavraErrada3Traducao);
		TextView textViewPalavraErrada4Traducao = (TextView) findViewById(R.id.palavraErrada4Traducao);
		
		LinearLayout layoutPalavraErrada1 = (LinearLayout) findViewById(R.id.palavraErrada1TudoJunto);
		LinearLayout layoutPalavraErrada2 = (LinearLayout) findViewById(R.id.palavraErrada2TudoJunto);
		LinearLayout layoutPalavraErrada3 = (LinearLayout) findViewById(R.id.palavraErrada3TudoJunto);
		LinearLayout layoutPalavraErrada4 = (LinearLayout) findViewById(R.id.palavraErrada4TudoJunto);
		
		int qualTextViewIrahColocarTexto = 1;
		for(int k = 0; k < kanjisErradosSemRepeticao.size(); k++)
		{
			KanjiTreinar umKanjiTreinarSemRepeticao = kanjisErradosSemRepeticao.get(k);
			String textoParaOTextView = umKanjiTreinarSemRepeticao.getKanji();
			String textoParaOTextViewFurigana = umKanjiTreinarSemRepeticao.getHiraganaDoKanji();
			String textoParaTextViewTraducao = umKanjiTreinarSemRepeticao.getTraducaoEmPortugues(); 
			
			if(qualTextViewIrahColocarTexto == 1)
			{
				textViewPalavraErrada1.setText(textoParaOTextView);
				textViewPalavraErrada1Furigana.setText(textoParaOTextViewFurigana);
				textViewPalavraErrada1Traducao.setText("=   " + textoParaTextViewTraducao);
			}
			else if(qualTextViewIrahColocarTexto == 2)
			{
				textViewPalavraErrada2.setText(textoParaOTextView);
				textViewPalavraErrada2Furigana.setText(textoParaOTextViewFurigana);
				textViewPalavraErrada2Traducao.setText("=   " + textoParaTextViewTraducao);
			}
			else if(qualTextViewIrahColocarTexto == 3)
			{
				textViewPalavraErrada3.setText(textoParaOTextView);
				textViewPalavraErrada3Furigana.setText(textoParaOTextViewFurigana);
				textViewPalavraErrada3Traducao.setText("=   " + textoParaTextViewTraducao);
			}
			else
			{
				textViewPalavraErrada4.setText(textoParaOTextView);
				textViewPalavraErrada4Furigana.setText(textoParaOTextViewFurigana);
				textViewPalavraErrada4Traducao.setText("=   " + textoParaTextViewTraducao);
			}
			
			qualTextViewIrahColocarTexto = qualTextViewIrahColocarTexto + 1;
		}
		
		if(qualTextViewIrahColocarTexto != 5)
		{
			//alguns textviews devem ficar em branco
			RelativeLayout layoutComPalavrasErradas = (RelativeLayout) findViewById(R.id.todas_as_palavras_erradas); 
			while(qualTextViewIrahColocarTexto != 5)
			{
				if(qualTextViewIrahColocarTexto == 1)
				{
					textViewPalavraErrada1.setText("");
					textViewPalavraErrada1Furigana.setText("");
					textViewPalavraErrada1Traducao.setText("");
					layoutComPalavrasErradas.removeView(textViewPalavraErrada1);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada1Furigana);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada1Traducao);
					layoutComPalavrasErradas.removeView(layoutPalavraErrada1);
				}
				else if(qualTextViewIrahColocarTexto == 2)
				{
					textViewPalavraErrada2.setText("");
					textViewPalavraErrada2Furigana.setText("");
					textViewPalavraErrada2Traducao.setText("");
					layoutComPalavrasErradas.removeView(textViewPalavraErrada2);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada2Furigana);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada2Traducao);
					layoutComPalavrasErradas.removeView(layoutPalavraErrada2);
				}
				else if(qualTextViewIrahColocarTexto == 3)
				{
					textViewPalavraErrada3.setText("");
					textViewPalavraErrada3Furigana.setText("");
					textViewPalavraErrada3Traducao.setText("");
					layoutComPalavrasErradas.removeView(textViewPalavraErrada3);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada3Furigana);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada3Traducao);
					layoutComPalavrasErradas.removeView(layoutPalavraErrada3);
				}
				else
				{
					textViewPalavraErrada4.setText("");
					textViewPalavraErrada4Furigana.setText("");
					textViewPalavraErrada4Traducao.setText("");
					layoutComPalavrasErradas.removeView(textViewPalavraErrada4);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada4Furigana);
					layoutComPalavrasErradas.removeView(textViewPalavraErrada4Traducao);
					layoutComPalavrasErradas.removeView(layoutPalavraErrada4);
				}
				qualTextViewIrahColocarTexto = qualTextViewIrahColocarTexto + 1;
			}
		}
		
		//faltou calcular quanto dinheiro o usuário recebeu
		int creditosAdicionarAoJogador = TransformaPontosEmCredito.converterPontosEmCredito(this.suaPontuacao);
		DAOAcessaDinheiroDoJogador daoDinheiroJogador = ConcreteDAOAcessaDinheiroDoJogador.getInstance();
		daoDinheiroJogador.adicionarCredito(creditosAdicionarAoJogador, this);
		TextView textViewCreditos = (TextView) findViewById(R.id.japanPointsAdquiridos);
		textViewCreditos.setText("x" + String.valueOf(creditosAdicionarAoJogador));
		
		
	}
	
	public void voltarAoMenuPrincipal(View v)
	{
		Intent irMenuInicial =
				new Intent(ModoTreinamento.this, MainActivity.class);
		irMenuInicial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
		startActivity(irMenuInicial);
	}
	
	public void treinarNovamente(View v)
	{
		Intent comecarModoTreinamento =
				new Intent(ModoTreinamento.this, ModoTreinamento.class);
		comecarModoTreinamento.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
		startActivity(comecarModoTreinamento);
	}
	
	 /*coloca o texto verticalmente numa carta*/
	 private void colocarTextoVerticalNaCarta(TextView textViewUmaCarta, String texto)
	 {
		 //cada caractere do texto deve vir seguido de um \n
		 String textoComBarrasN = "";
		 
		 for(int i = 0; i < texto.length(); i++)
		 {
			 String umaLetra = String.valueOf(texto.charAt(i));
			 textoComBarrasN = textoComBarrasN + umaLetra + "\n";
		 }
		 
		 textViewUmaCarta.setText(textoComBarrasN);
	 }
	 
	 /*é o que eu uso para escurecer as cartas*/
	 private void fazerImageViewMudarParaVersoDeCarta(ImageView imageView)
	 {
		 //imageView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
		 imageView.setImageResource(R.drawable.karutaversotreinamento);
	 }
	 
	 private void fazerImageViewVoltarACartaNormal(ImageView imageView)
	 {
		 //imageView.setColorFilter(null);
		 //imageView.setAlpha(255);
		 imageView.setImageResource(R.drawable.karutavaziatreinamento);
	 }
	 
	 private void fazerMascoteFicarFelizPorUmTempo()
	 {
		 final ImageView imageViewMascote = (ImageView) findViewById(R.id.mascote);
			imageViewMascote.setImageResource(R.drawable.mascotetreinamento_feliz);
			
			//a mascote vai ficar com cara de feliz por um tempo e depois volta ao normal
			new Timer().schedule(new TimerTask() 
			 { 
				    @Override
				    public void run() 
				    {
				        //If you want to operate UI modifications, you must run ui stuff on UiThread.
				        ModoTreinamento.this.runOnUiThread(new Runnable() 
				        {
				            @Override
				            public void run() 
				            {
				            	imageViewMascote.setImageResource(R.drawable.mascotetreinamento);
				            }
				        });
				    }
				}, 1000);
	 }
	 
	 private void mudarFonteDosKanjis()
	 {
	 	String fontPath = "fonts/KaoriGel.ttf";
	 	 
	     // text view label
	     TextView txtkaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	     TextView txtkaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	     TextView txtkaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	     TextView txtkaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	     TextView txtkaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	     TextView txtkaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	     TextView txtkaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	     TextView txtkaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	     TextView txtkaruta9 = (TextView) findViewById(R.id.texto_karuta9);
	     TextView txtkaruta10 = (TextView) findViewById(R.id.texto_karuta10);
	     TextView txtkaruta11 = (TextView) findViewById(R.id.texto_karuta11);
	     TextView txtkaruta12 = (TextView) findViewById(R.id.texto_karuta12);
	     
	     // Loading Font Face
	     Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);

	     // Applying font
	     txtkaruta1.setTypeface(tf);
	     txtkaruta2.setTypeface(tf);
	     txtkaruta3.setTypeface(tf);
	     txtkaruta4.setTypeface(tf);
	     txtkaruta5.setTypeface(tf);
	     txtkaruta6.setTypeface(tf);
	     txtkaruta7.setTypeface(tf);
	     txtkaruta8.setTypeface(tf);
	     txtkaruta9.setTypeface(tf);
	     txtkaruta10.setTypeface(tf);
	     txtkaruta11.setTypeface(tf);
	     txtkaruta12.setTypeface(tf);
	 }
	 
	 private void mudarFonteDeTodoOTextoDaTelaFinal()
	 {
		 	String fontPath = "fonts/Wonton.ttf";
		 	// Loading Font Face
		    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);

		    // Applying font
		    TextView textoTitulo = (TextView) findViewById(R.id.tituloFimTreinamento);
		    TextView japanPointsAdquiridos = (TextView) findViewById(R.id.japanPointsAdquiridos);
		    TextView palavraErrada1 = (TextView) findViewById(R.id.palavraErrada1Traducao);
		    TextView palavraErrada2 = (TextView) findViewById(R.id.palavraErrada2Traducao);
		    TextView palavraErrada3 = (TextView) findViewById(R.id.palavraErrada3Traducao);
		    TextView palavraErrada4 = (TextView) findViewById(R.id.palavraErrada4Traducao);
		    TextView tituloPalavrasQueErrou = (TextView) findViewById(R.id.palavrasQueVoceErrou);
		    TextView quantosPontosFez = (TextView) findViewById(R.id.quantosPontosVoceFez);
		    TextView pontos = (TextView) findViewById(R.id.pontos);
		    TextView chegouAteONivel = (TextView) findViewById(R.id.chegouAteONivel);
		    
		    textoTitulo.setTypeface(tf);
		    japanPointsAdquiridos.setTypeface(tf);
		    palavraErrada1.setTypeface(tf);
		    palavraErrada2.setTypeface(tf);
		    palavraErrada3.setTypeface(tf);
		    palavraErrada4.setTypeface(tf);
		    tituloPalavrasQueErrou.setTypeface(tf);
		    quantosPontosFez.setTypeface(tf);
		    pontos.setTypeface(tf);
		    chegouAteONivel.setTypeface(tf);
		    
		    Button botaoVoltarMenuPrincipal = (Button) findViewById(R.id.botaoVoltarAoMenuPrincipal);
		    botaoVoltarMenuPrincipal.setTypeface(tf);
		    Button botaoTreinarNovamente = (Button) findViewById(R.id.botaoTreinarNovamente);
		    botaoTreinarNovamente.setTypeface(tf);
		 
	 }
	 
	 private void mudarFonte4UltimosKanjisETitulo()
	 {
		 String fontPath = "fonts/Wonton.ttf";
		 // Loading Font Face
		 Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
		 
		 TextView textoUltimoKanji1 = (TextView) findViewById(R.id.ultimokanji1);
		 TextView textoUltimoKanji2 = (TextView) findViewById(R.id.ultimokanji2);
		 TextView textoUltimoKanji3 = (TextView) findViewById(R.id.ultimokanji3);
		 TextView textoUltimoKanji4 = (TextView) findViewById(R.id.ultimokanji4);
		 
		 textoUltimoKanji1.setTypeface(tf);
		 textoUltimoKanji2.setTypeface(tf);
		 textoUltimoKanji3.setTypeface(tf);
		 textoUltimoKanji4.setTypeface(tf);
		 
		 TextView titulo = (TextView) findViewById(R.id.titulo_tela_observacao_novos_kanjis);
		 titulo.setTypeface(tf);
		 
		 TextView observacao = (TextView) findViewById(R.id.observacao);
		 observacao.setTypeface(tf);
		 
		 TextView textoNaPlaquinha = (TextView) findViewById(R.id.textoTituloTreinamento);
		 textoNaPlaquinha.setTypeface(tf);
		 
		 Button botaoOk = (Button) findViewById(R.id.botaook);
		 botaoOk.setTypeface(tf);
	 }
	 
	 private void mudarFonteTraducoesQuatroUltimosKanjis()
	 {
		 String fontPath = "fonts/Wonton.ttf";
		 // Loading Font Face
		 Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
		 
		 TextView textoUltimoKanji1Traducao = (TextView) findViewById(R.id.ultimoKanji1Traducao);
		 TextView textoUltimoKanji2Traducao = (TextView) findViewById(R.id.ultimoKanji2Traducao);
		 TextView textoUltimoKanji3Traducao = (TextView) findViewById(R.id.ultimoKanji3Traducao);
		 TextView textoUltimoKanji4Traducao = (TextView) findViewById(R.id.ultimoKanji4Traducao);
		 
		 textoUltimoKanji1Traducao.setTypeface(tf);
		 textoUltimoKanji2Traducao.setTypeface(tf);
		 textoUltimoKanji3Traducao.setTypeface(tf);
		 textoUltimoKanji4Traducao.setTypeface(tf);
	 }

}

