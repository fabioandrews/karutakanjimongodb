package com.karutakanji;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import bancodedados.ActivityQueEsperaAtePegarOsKanjis;
import bancodedados.AdapterListViewSalasCriadas;
import bancodedados.ArmazenaKanjisPorCategoria;
import bancodedados.BuscaSalasModoCasualComArgumentoTask;
import bancodedados.BuscaSalasModoCasualTask;
import bancodedados.CategoriaDeKanjiParaListviewSelecionavel;
import bancodedados.CriarSalaModoCasualTask;
import bancodedados.AdapterListViewIconeETexto;
import bancodedados.DadosPartidaParaOLog;
import bancodedados.FechaSalaCasualCriadaPeloUsuarioTask;
import bancodedados.EnviarDadosDaPartidaParaLogTask;
import bancodedados.KanjiTreinar;
import bancodedados.MyCustomAdapter;
import bancodedados.MyCustomAdapterSemCheckBox;
import bancodedados.PegaNomesDeTodasAsCategoriasTask;
import bancodedados.SalaModoCasual;
import bancodedados.SolicitaKanjisParaTreinoTask;
import bancodedados.ThreadExecutaBuscaSalasCasualTask;


import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.realtime.*;
//import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import lojinha.ConcreteDAOAcessaDinheiroDoJogador;
import lojinha.DAOAcessaDinheiroDoJogador;
import lojinha.TransformaPontosEmCredito;

public class ModoCasual extends ActivityDoJogoComSom implements View.OnClickListener, RealTimeMessageReceivedListener,RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener, ActivityQueEsperaAtePegarOsKanjis,ActivityMultiplayerQueEsperaAtePegarOsKanjis, OnItemSelectedListener 
{

/*
* API INTEGRATION SECTION. This section contains the code that integrates
* the game with the Google Play game services API.
*/

// Debug tag
final static boolean ENABLE_DEBUG = true;
final static String TAG = "ButtonClicker2000";

// Request codes for the UIs that we show with startActivityForResult:
final static int RC_SELECT_PLAYERS = 10000;
final static int RC_INVITATION_INBOX = 10001;
final static int RC_WAITING_ROOM = 10002;

// Room ID where the currently active game is taking place; null if we're
// not playing.
String mRoomId = null;
Room room;

private String emailUsuario; //email do usuario no google account que eh obtido assim que o usuario faz login

// Are we playing in multiplayer mode?
boolean mMultiplayer = false;

// The participants in the currently active game
ArrayList<Participant> mParticipants = null;

// My participant ID in the currently active game
String mMyId = null;

// If non-null, this is the id of the invitation we received via the
// invitation listener
String mIncomingInvitationId = null;

// Message buffer for sending messages
byte[] mMsgBuf; //1 e 2 byte eu nao uso, mas o 3 diz se o adversario acertou ou nao clicou na carta1('N','A')



private int quantasRodadasHaverao; //possiveis valores: 1,2,3 e 99(para infinito)
private int rodadaAtual;

private int suaPontuacao; //sua pontuacao no modo multiplayer
private TimerTask timerTaskDecrementaTempoRestante;
private boolean tempoEstahParado; //quando o jogador usa o item que para o tempo, ele para
private int qualImagemEstaSendoUsadaNaAnimacaoDoTempo; //int que vai de 0 ate 17 que serve para criar a animacao do tempo correndo

private LinkedList<KanjiTreinar> kanjisDasCartasNaTela; //os kanjis das cartas que aparecem na tela

private KanjiTreinar kanjiDaDica; //kanji da dica atual que mostra para ambos os jogadores
private LinkedList<KanjiTreinar> kanjisDasCartasNaTelaQueJaSeTornaramDicas; // quais kanjis ja viraram dicas? Essa lista deve se esvaziar no comeco de cada rodada   
private LinkedList<KanjiTreinar> kanjisQuePodemVirarCartas; //no comeco do jogo, eh igual a todos os kanjis das categorias escolhidas pelo usuario, mas com o passar das rodadas, vai-se tirando kanjis dessa lista 

private int pontuacaoDoAdversario; //precisaremos saber a pontuacao do adversario porque na hora de recebermos um item aleatorio, saberemos se devemos receber item do perdedor ou de quem estah ganhando 
private int quantasCartasJaSairamDoJogo; //de X em X cartas que saem, saberemos quando dar um item aleatorio p cada jogador
private LinkedList<String> itensAtuais;
private LinkedList<String> itensDoPerdedor; //essa lista e a de baixo sao preenchidas no comeco do jogo multiplayer
private LinkedList<String> itensDoGanhador;

private ImageView cartaASerTirada; //na hora que o item do trovao for usado, precisaremos armazenar algo nesse atributo so p passar p outra funcao
private boolean usouTrovaoTiraCarta; //booleano que diz se o usuario usou o item trovaotiracarta
private boolean usouReviveCarta; //booleano que diz se o usuario usou o item revivecarta
private boolean usou2x; //booleano que diz se o usuario usou o item 2x
private boolean usouNaoEspereMais; //booleano que diz se o usuario usou o item nao espere mais

private int qualCartaEstaProibida; //quando o user usa o naoesperemais e erra uma carta, ela fica proibida. Mas assim alguem acerta uma carta e a dica muda, a carta deveria voltar ao normal. Esse numero vai de 1 ate 8
								   //quando esse int for 0, nao ha cartas proibidas na tela
private LinkedList<Integer> quaisCartasEstaoDouradas; //quando o usuario usa o cartasdouradas, algumas cartas ficam douradas. Mas assim que alguem acerta uma carta e a dica muda, as cartas deveriam voltar ao normal. Os numeros das cartas vao de 1 a 8

public boolean guestTerminouDeCarregarListaDeCategorias; //booleano para resolver problema de um dos jogadores nao estar recebendo a lista de categorias
private Handler mHandler = new Handler(); //handler para o chat do final do jogo
private ArrayList<String> mensagensChat; //arraylist com as mensagens do chat do fim de jogo

private LinkedList<KanjiTreinar> palavrasAcertadas;//toda palavra/kanji que o usuario acertar entra nessa lista(vai para o log)  
private LinkedList<KanjiTreinar> palavrasErradas;//toda palavra/kanji que o usuario errar entra nessa lista(vai para o log)
private LinkedList<KanjiTreinar> palavrasJogadas; //toda palavra/kanji que sair como dica serah armazenada (vai para o log)

private LinkedList<Integer> posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas; 
//o unico jogador que estava removendo os kanjis da lista kanjisquepodemvirarcartas era quem escolhia
//os 8 kanjis, mas o outro jogador deve ter essa linkedlist atualizada tb! Soh que ela eh enorme!
//Por isso, assim que um turno passar e um jogador passar os 8 kanjis novos para o outro,
//ele tb deve enviar quais kanjis dessa linkedlist sairam!

private String emailAdversario;

private ProgressDialog loadingComecoDaPartida; //loading que comeca desde quando sao decididas as categorias do jogo ateh o primeiro kanji da dica ser escolhido
private ProgressDialog loadingSalasModoCasual;
private ProgressDialog loadingEsperarAlguemEntrarSalaModoCasual;

private boolean jogoAcabou;
private boolean jaDeixouASala; //booleano que diz se o usuario ja saiu da sala mutliplayer(usado p nao invocar leaveroom() mais de uma vez)

private boolean naCriacaoDeSalaModoCasual; //diz se o usuario esta na criacao de salas do modo casual
private boolean criouUmaSala; //diz se o usuario eh um criador de sala ou nao. Ele sera quem escolheu as categorias nesse caso
private SalaModoCasual salaAtual; //sala em que o usuario entrou ou criou

private LinkedList<SalaModoCasual> salasAbertas; //as salas abertas do lobby do modo casual vao ficar aqui. Isso serve para que a task BuscaSalasModoCasual consiga alertar quando uma mudanca acontece usando um get() desse atributo
ThreadExecutaBuscaSalasCasualTask threadFicaBuscandoSalasModoCasual; //thread que fica de x em x segundos atualizando a listagem de salas no modo casual

private AlertDialog popupPesquisarSalaPorDuracao; //quando o usuario quiser pesquisar salas por duracao da partida, esse popup aparece

//This array lists all the individual screens our game has.
final static int[] SCREENS = {
 R.id.screen_game, R.id.screen_sign_in,R.id.tela_lobby_modo_casual, R.id.tela_escolha_categoria_criar_nova_sala,
 R.id.screen_wait,R.id.decidindoQuemEscolheACategoria, R.id.tela_jogo_multiplayer,R.id.tela_fim_de_jogo};

@Override
public void onCreate(Bundle savedInstanceState) 
{
	
	enableDebugLog(ENABLE_DEBUG, TAG);
	setContentView(R.layout.activity_modo_casual);
	super.onCreate(savedInstanceState);
	// set up a click listener for everything we care about
	for (int id : CLICKABLES) 
	{
		findViewById(id).setOnClickListener(this);
	}
	
	findViewById(R.id.botao_menu_principal).setOnClickListener(this);
	this.jaDeixouASala = false;
}

/**
* Called by the base class (BaseGameActivity) when sign-in has failed. For
* example, because the user hasn't authenticated yet. We react to this by
* showing the sign-in button.
*/
@Override
public void onSignInFailed() {
Log.d(TAG, "Sign-in failed.");
switchToScreen(R.id.screen_sign_in);
}

/**
* Called by the base class (BaseGameActivity) when sign-in succeeded. We
* react by going to our main screen.
*/
@Override
public void onSignInSucceeded() {
Log.d(TAG, "Sign-in succeeded.");

// register listener so we are notified if we receive an invitation to play
// while we are in the game
Games.Invitations.registerInvitationListener(getApiClient(), this);

// if we received an invite via notification, accept it; otherwise, go to main screen
if (getInvitationId() != null) {
    acceptInviteToRoom(getInvitationId());
    return;
}

//salvarei o email do usuario para adicionar o log dele no banco de dados
AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
Account[] list = manager.getAccounts();

for(Account account: list)
{
    if(account.type.equalsIgnoreCase("com.google"))
    {
        this.emailUsuario = account.name;
        break;
    }
}
switchToMainScreen();
}

@Override
public void onClick(View v) {
Intent intent;

switch (v.getId()) {
    case R.id.button_sign_in:
        // user wants to sign in
        if (!verifyPlaceholderIdsReplaced()) {
            showAlert("Error: sample not set up correctly. Please see README.");
            return;
        }
        beginUserInitiatedSignIn();
        break;
    /*case R.id.button_sign_out:
        // user wants to sign out
        signOut();
        switchToScreen(R.id.screen_sign_in);
        break;
    case R.id.button_invite_players:
        // show list of invitable players
        intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 3);
        switchToScreen(R.id.screen_wait);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
        break;
    case R.id.button_see_invitations:
        // show list of pending invitations
        intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        switchToScreen(R.id.screen_wait);
        startActivityForResult(intent, RC_INVITATION_INBOX);
        break;*/
    case R.id.button_accept_popup_invitation:
        // user wants to accept the invitation shown on the invitation popup
        // (the one we got through the OnInvitationReceivedListener).
        acceptInviteToRoom(mIncomingInvitationId);
        mIncomingInvitationId = null;
        break;
    /*case R.id.button_quick_game:
        // user wants to play against a random opponent right now
        startQuickGame(2);
        break;*/
    /*case R.id.button_modo_casual:
    	switchToScreen(R.id.tela_lobby_modo_casual);
    	this.mostrarLobbyModoCasual();
    	break;*/
    case R.id.karuta1_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
        	String textoKaruta1 = this.kanjisDasCartasNaTela.get(0).getKanji();
        	String textoKanjiDaDica = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta1.compareTo(textoKanjiDaDica) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta1); //mudei a figura da carta
        		findViewById(R.id.karuta1).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta1.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta1");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        		
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta1)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(0)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
            		
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(1);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=1");
    		this.realizarProcedimentoReviverCarta(1,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    		
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(0);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(0);
    		}
    	}
    	
    	break;
    case R.id.karuta2_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
        	String textoKaruta2 = this.kanjisDasCartasNaTela.get(1).getKanji();
        	String textoKanjiDaDica2 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta2.compareTo(textoKanjiDaDica2) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta2); //mudei a figura da carta
        		findViewById(R.id.karuta2).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta2.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta2");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta2)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(1)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(2);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=2");
    		this.realizarProcedimentoReviverCarta(2,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(1);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(1);
    		}
    	}
    	break;
    case R.id.karuta3_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
        	String textoKaruta3 = this.kanjisDasCartasNaTela.get(2).getKanji();
        	String textoKanjiDaDica3 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta3.compareTo(textoKanjiDaDica3) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta3); //mudei a figura da carta
        		findViewById(R.id.karuta3).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta3.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta3");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta3)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(2)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(3);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=3");
    		this.realizarProcedimentoReviverCarta(3,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(2);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(2);
    		}
    	}
    	break;
    case R.id.karuta4_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
        	String textoKaruta4 = this.kanjisDasCartasNaTela.get(3).getKanji();
        	String textoKanjiDaDica4 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta4.compareTo(textoKanjiDaDica4) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta4); //mudei a figura da carta
        		findViewById(R.id.karuta4).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta4.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta4");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta4)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(3)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(4);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=4");
    		this.realizarProcedimentoReviverCarta(4,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(3);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(3);
    		}
    	}
    	break;
    case R.id.karuta5_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
        	String textoKaruta5 = this.kanjisDasCartasNaTela.get(4).getKanji();
        	String textoKanjiDaDica5 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta5.compareTo(textoKanjiDaDica5) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta5); //mudei a figura da carta
        		findViewById(R.id.karuta5).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta5.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta5");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta5)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(4)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(5);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=5");
    		this.realizarProcedimentoReviverCarta(5,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(4);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(4);
    		}
    	}
    	break;
    case R.id.karuta6_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
        	String textoKaruta6 = this.kanjisDasCartasNaTela.get(5).getKanji();
        	String textoKanjiDaDica6 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta6.compareTo(textoKanjiDaDica6) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta6); //mudei a figura da carta
        		findViewById(R.id.karuta6).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta6.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta6");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta6)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(5)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(6);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=6");
    		this.realizarProcedimentoReviverCarta(6,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(5);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(5);
    		}
    	}
    	break;
    case R.id.karuta7_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
        	String textoKaruta7 = this.kanjisDasCartasNaTela.get(6).getKanji();
        	String textoKanjiDaDica7 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta7.compareTo(textoKanjiDaDica7) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta7); //mudei a figura da carta
        		findViewById(R.id.karuta7).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta7.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta7");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta7)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(6)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(7);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=7");
    		this.realizarProcedimentoReviverCarta(7,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(6);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(6);
    		}
    	}
    	break;
    case R.id.karuta8_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
        	String textoKaruta8 = this.kanjisDasCartasNaTela.get(7).getKanji();
        	String textoKanjiDaDica8 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta8.compareTo(textoKanjiDaDica8) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		//se existir alguma carta proibida na tela, ela deve desaparecer(item naoesperemais)
        		this.fazerCartaProibidaVoltarAoNormal();
        		//se existir alguma carta dourada na tela, ela deve voltar ao normal(item cartasdouradas)
        		this.fazerCartasDouradasVoltaremAoNormal();
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
        		this.fazerImageViewFicarEscuro(imageViewKaruta8); //mudei a figura da carta
        		findViewById(R.id.karuta8).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta8.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta8");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou, mas sera q ele usou o item naoesperemais?
        		
        		if(this.usouNaoEspereMais == false)
        		{
        			//devemos executar o som que indica que ele errou uma carta
        			if(((TextView)findViewById(R.id.texto_karuta8)).getText().length() > 0)
        			{
        				super.reproduzirSfx("errou_carta");
                		
                		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(7)); //colocar no log que ele errou este kanji
                		this.realizarProcedimentoUsuarioErrouCarta();
        			}
        		}
        		else
        		{
        			this.proibirCartaNaPosicao(8);
        		}
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=8");
    		this.realizarProcedimentoReviverCarta(8,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item9);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(7);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item8);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(7);
    		}
    	}
    	break;
    case R.id.item1:
    	this.usarItemAtual("trovaotiracartaaleatoria");
    	break;
    case R.id.item2:
    	this.usarItemAtual("parartempo");
    	break;
    case R.id.item3:
    	this.usarItemAtual("misturarcartas");
    	break;
    case R.id.item4:
    	this.usarItemAtual("mudardica");
    	break;
    case R.id.item5:
    	this.usarItemAtual("doisx");
    	break;
    case R.id.item6:
    	this.usarItemAtual("naoesperemais");
    	break;
    case R.id.item7:
    	this.usarItemAtual("cartasdouradas");
    	break;
    case R.id.item8:
    	this.usarItemAtual("trovaotiracarta");
    	break;
    case R.id.item9:
    	this.usarItemAtual("revivecarta");
    	break;
    case R.id.botao_menu_principal:
    	this.voltarAoMenuInicial(null);
    	break;
}
}

/*iremos alterar a pontuacao do usuario com base na dificuldade do kanji que ele acabou de acertar.
  A sua pontuacao cresce aqui, mas a do adversario cresce somente se ele acertar alguma carta e vc recebeu
  uma mensagem dizendo que a carta X nao estah mais clicavel*/
private void aumentarPontuacaoComBaseNaDificuldadeDoKanji()
{
	int dificuldade = this.kanjiDaDica.getDificuldadeDoKanji();
	if(this.usou2x == false)
	{
		if(dificuldade == 1)
		{
			this.suaPontuacao = this.suaPontuacao + 1;
			//this.realizarAnimacaoAumentaPontuacao(1,false);
			
		}
		else if(dificuldade == 2)
		{
			this.suaPontuacao = this.suaPontuacao + 2;
			//this.realizarAnimacaoAumentaPontuacao(2,false);
		}
		else
		{
			this.suaPontuacao = this.suaPontuacao + 3;
			//this.realizarAnimacaoAumentaPontuacao(3,false);
		}
	}
	else
	{
		//usuario usou o item 2x. As animacoes sao diferentes
		if(dificuldade == 1)
		{
			this.suaPontuacao = this.suaPontuacao + 2;
			//this.realizarAnimacaoAumentaPontuacao(1,true);
		}
		else if(dificuldade == 2)
		{
			this.suaPontuacao = this.suaPontuacao + 4;
			//this.realizarAnimacaoAumentaPontuacao(2,true);
		}
		else
		{
			this.suaPontuacao = this.suaPontuacao + 6;
			//this.realizarAnimacaoAumentaPontuacao(3,true);
		}
		
		findViewById(R.id.doisxpequeno).setVisibility(View.INVISIBLE);
		//AINDA FALTA MUDAR O BOOLEANO, MAS ALTERAMOS ELE QUANDO O ADVERSARIO SABE Q O USUARIO ACERTOU COM MAIS PONTOS
	}
	
	TextView textoPontuacao = (TextView) findViewById(R.id.nome_e_pontuacao_jogador_esquerda);
	String palavraPontuacao = getResources().getString(R.string.pontuacao);
	String nomeJogador = "";
	
	if(this.emailUsuario.length() > 24)
	{
		nomeJogador = emailUsuario.substring(0, 23) + "...";
	}
	else
	{
		nomeJogador = this.emailUsuario;
	}
	
	
	if(suaPontuacao < 10)
	{
		textoPontuacao.setText(nomeJogador + ":" + "0" + String.valueOf(suaPontuacao));
	}
	else
	{
		textoPontuacao.setText(nomeJogador + ":" + palavraPontuacao + String.valueOf(suaPontuacao));
	}
}

private void realizarAnimacaoAumentaPontuacao(int dificuldadeDoKanji, boolean eh2x)
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
	
	
	if(dificuldadeDoKanji == 1 && eh2x == false)
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
	else if(dificuldadeDoKanji == 2 && eh2x == false)
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
	else if(dificuldadeDoKanji == 3 && eh2x == false)
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
	else if(dificuldadeDoKanji == 1 && eh2x == true)
	{
		idImagemAnimacaoAumentaPontos1 = getResources().getIdentifier("doisxanimacao20_1", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos2 = getResources().getIdentifier("doisxanimacao20_2", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos3 = getResources().getIdentifier("doisxanimacao20_3", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos4 = getResources().getIdentifier("doisxanimacao20_4", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos5 = getResources().getIdentifier("doisxanimacao20_5", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos6 = getResources().getIdentifier("doisxanimacao20_6", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos7 = getResources().getIdentifier("doisxanimacao20_7", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos8 = getResources().getIdentifier("doisxanimacao20_8", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos9 = getResources().getIdentifier("doisxanimacao20_9", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos10 = getResources().getIdentifier("doisxanimacao20_10", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos11 = getResources().getIdentifier("doisxanimacao20_11", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos12 = getResources().getIdentifier("doisxanimacao20_12", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos13 = getResources().getIdentifier("doisxanimacao20_13", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos14 = getResources().getIdentifier("doisxanimacao20_14", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos15 = getResources().getIdentifier("doisxanimacao20_15", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos16 = getResources().getIdentifier("doisxanimacao20_16", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos17 = getResources().getIdentifier("doisxanimacao20_17", "drawable", getPackageName());
	}
	else if(dificuldadeDoKanji == 2 && eh2x == true)
	{
		idImagemAnimacaoAumentaPontos1 = getResources().getIdentifier("doisxanimacao40_1", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos2 = getResources().getIdentifier("doisxanimacao40_2", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos3 = getResources().getIdentifier("doisxanimacao40_3", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos4 = getResources().getIdentifier("doisxanimacao40_4", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos5 = getResources().getIdentifier("doisxanimacao40_5", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos6 = getResources().getIdentifier("doisxanimacao40_6", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos7 = getResources().getIdentifier("doisxanimacao40_7", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos8 = getResources().getIdentifier("doisxanimacao40_8", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos9 = getResources().getIdentifier("doisxanimacao40_9", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos10 = getResources().getIdentifier("doisxanimacao40_10", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos11 = getResources().getIdentifier("doisxanimacao40_11", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos12 = getResources().getIdentifier("doisxanimacao40_12", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos13 = getResources().getIdentifier("doisxanimacao40_13", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos14 = getResources().getIdentifier("doisxanimacao40_14", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos15 = getResources().getIdentifier("doisxanimacao40_15", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos16 = getResources().getIdentifier("doisxanimacao40_16", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos17 = getResources().getIdentifier("doisxanimacao40_17", "drawable", getPackageName());
	}
	else if(dificuldadeDoKanji == 3 && eh2x == true)
	{
		idImagemAnimacaoAumentaPontos1 = getResources().getIdentifier("doisxanimacao60_1", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos2 = getResources().getIdentifier("doisxanimacao60_2", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos3 = getResources().getIdentifier("doisxanimacao60_3", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos4 = getResources().getIdentifier("doisxanimacao60_4", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos5 = getResources().getIdentifier("doisxanimacao60_5", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos6 = getResources().getIdentifier("doisxanimacao60_6", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos7 = getResources().getIdentifier("doisxanimacao60_7", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos8 = getResources().getIdentifier("doisxanimacao60_8", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos9 = getResources().getIdentifier("doisxanimacao60_9", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos10 = getResources().getIdentifier("doisxanimacao60_10", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos11 = getResources().getIdentifier("doisxanimacao60_11", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos12 = getResources().getIdentifier("doisxanimacao60_12", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos13 = getResources().getIdentifier("doisxanimacao60_13", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos14 = getResources().getIdentifier("doisxanimacao60_14", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos15 = getResources().getIdentifier("doisxanimacao60_15", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos16 = getResources().getIdentifier("doisxanimacao60_16", "drawable", getPackageName());
		idImagemAnimacaoAumentaPontos17 = getResources().getIdentifier("doisxanimacao60_17", "drawable", getPackageName());
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
	 
	 if(usou2x == true)
	 {
		 super.reproduzirSfx("dois_x");
	 }
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	TextView textoPontuacao = (TextView) findViewById(R.id.pontuacao);
		            	
		            	if(suaPontuacao < 100)
		            	{
		            		textoPontuacao.setText("0" + String.valueOf(suaPontuacao));
		            	}
		            	else
		            	{
		            		textoPontuacao.setText(String.valueOf(suaPontuacao));
		            	}
		            }
		        });
		    }
		}, 900);*/
}

void startQuickGame(int variant) 
{
	if(variant < 0)
	 {
			variant = -variant;
	 }
// quick-start a game with 1 randomly selected opponent
	
	//ambos os jogadores ja sairam do lobby das salas do modo casual, entao pode matar a thread que atualizava as salas
	if(threadFicaBuscandoSalasModoCasual != null && threadFicaBuscandoSalasModoCasual.isAlive() == true)
	{
		threadFicaBuscandoSalasModoCasual.interrupt();
	}
	
	try
	{
		final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
		Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
		        MAX_OPPONENTS, 0);
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
		rtmConfigBuilder.setMessageReceivedListener(this);
		rtmConfigBuilder.setRoomStatusUpdateListener(this);
		rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);

		int variante = variant;
		rtmConfigBuilder.setVariant(variante); //somente dois usuarios com o mesmo variante podem jogar juntos no automatch. Usaremos o nivel do usuario como esse variante

		switchToScreen(R.id.screen_wait);
		keepScreenOn();
		resetGameVars();
		Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
	}
	catch(Exception e)
	{
		String excecao = e.getMessage();
		excecao = excecao + "";
	}
}

@Override
public void onActivityResult(int requestCode, int responseCode,
    Intent intent) {
super.onActivityResult(requestCode, responseCode, intent);

switch (requestCode) {
    case RC_SELECT_PLAYERS:
        // we got the result from the "select players" UI -- ready to create the room
        handleSelectPlayersResult(responseCode, intent);
        break;
    case RC_INVITATION_INBOX:
        // we got the result from the "select invitation" UI (invitation inbox). We're
        // ready to accept the selected invitation:
        handleInvitationInboxResult(responseCode, intent);
        break;
    case RC_WAITING_ROOM:
        // we got the result from the "waiting room" UI.
        if (responseCode == Activity.RESULT_OK) {
            // ready to start playing
            Log.d(TAG, "Starting game (waiting room returned OK).");
            startGame(true);
        } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
            // player indicated that they want to leave the room
            leaveRoom();
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // Dialog was cancelled (user pressed back key, for instance). In our game,
            // this means leaving the room too. In more elaborate games, this could mean
            // something else (like minimizing the waiting room UI).
            leaveRoom();
        }
        break;
}
}

// Handle the result of the "Select players UI" we launched when the user clicked the
// "Invite friends" button. We react by creating a room with those players.
private void handleSelectPlayersResult(int response, Intent data) {
if (response != Activity.RESULT_OK) {
    Log.w(TAG, "*** select players UI cancelled, " + response);
    switchToMainScreen();
    return;
}

Log.d(TAG, "Select players UI succeeded.");

// get the invitee list
final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
Log.d(TAG, "Invitee count: " + invitees.size());

// get the automatch criteria
Bundle autoMatchCriteria = null;
int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
    autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
            minAutoMatchPlayers, maxAutoMatchPlayers, 0);
    Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
}

// create the room
Log.d(TAG, "Creating room...");
RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
rtmConfigBuilder.addPlayersToInvite(invitees);
rtmConfigBuilder.setMessageReceivedListener(this);
rtmConfigBuilder.setRoomStatusUpdateListener(this);
if (autoMatchCriteria != null) {
    rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
}
switchToScreen(R.id.screen_wait);
keepScreenOn();
resetGameVars();
Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
Log.d(TAG, "Room created, waiting for it to be ready...");
}

// Handle the result of the invitation inbox UI, where the player can pick an invitation
// to accept. We react by accepting the selected invitation, if any.
private void handleInvitationInboxResult(int response, Intent data) {
if (response != Activity.RESULT_OK) {
    Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
    switchToMainScreen();
    return;
}

Log.d(TAG, "Invitation inbox UI succeeded.");
Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

// accept invitation
acceptInviteToRoom(inv.getInvitationId());
}

// Accept the given invitation.
void acceptInviteToRoom(String invId) {
// accept the invitation
Log.d(TAG, "Accepting invitation: " + invId);
RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
roomConfigBuilder.setInvitationIdToAccept(invId)
        .setMessageReceivedListener(this)
        .setRoomStatusUpdateListener(this);
switchToScreen(R.id.screen_wait);
keepScreenOn();
resetGameVars();
Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());
}

// Activity is going to the background. We have to leave the current room.
@Override
public void onStop() 
{
	Log.d(TAG, "**** got onStop");
// if we're in a room, leave it.
leaveRoomDoOnStop();

// stop trying to keep the screen on
stopKeepingScreenOn();

//switchToScreen(R.id.screen_wait);
super.onStop();
}

@Override
public void onPause()
{
	super.onPause();
	
	if(threadFicaBuscandoSalasModoCasual != null && threadFicaBuscandoSalasModoCasual.isAlive() == true)
	{
		threadFicaBuscandoSalasModoCasual.interrupt();
	}
}

// Activity just got to the foreground. We switch to the wait screen because we will now
// go through the sign-in flow (remember that, yes, every time the Activity comes back to the
// foreground we go through the sign-in flow -- but if the user is already authenticated,
// this flow simply succeeds and is imperceptible).
@Override
public void onStart() {
switchToScreen(R.id.screen_wait);
super.onStart();
}

// Handle back key to make sure we cleanly leave a game if we are in the middle of one
@Override
public boolean onKeyDown(int keyCode, KeyEvent e) {
if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
    leaveRoom();
    return true;
}
return super.onKeyDown(keyCode, e);
}

// Leave the room.
void leaveRoom() 
{
	if(this.jaDeixouASala == false)
	{
		//caso ainda exista alguma sala no bd com o email do usuario, melhr deletar
		FechaSalaCasualCriadaPeloUsuarioTask deletarSalaCasual = new FechaSalaCasualCriadaPeloUsuarioTask();
		deletarSalaCasual.execute(emailUsuario);
		
		this.jaDeixouASala = true;
		this.criouUmaSala = false;
		//Log.d(TAG, "Leaving room.");
		tempoRestante = 0;
		stopKeepingScreenOn();
		if (mRoomId != null) {
		    Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
		    mRoomId = null;
		    Intent irMenuInicial =
					new Intent(ModoCasual.this, MainActivity.class);
		    irMenuInicial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
		    startActivity(irMenuInicial);
		} else {
			 Intent irMenuInicial =
						new Intent(ModoCasual.this, MainActivity.class);
			irMenuInicial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
			 startActivity(irMenuInicial);
		}
	}
}

void leaveRoomDoOnStop() 
{
	if(this.jaDeixouASala == false)
	{
		//caso ainda exista alguma sala no bd com o email do usuario, melhr deletar
		if(this.criouUmaSala == true)
		{
			FechaSalaCasualCriadaPeloUsuarioTask deletarSalaCasual = new FechaSalaCasualCriadaPeloUsuarioTask();
			deletarSalaCasual.execute(emailUsuario);
		}
		
		this.jaDeixouASala = true;
		this.criouUmaSala = false;
		//Log.d(TAG, "Leaving room.");
		tempoRestante = 0;
		stopKeepingScreenOn();
		if (mRoomId != null) {
		    Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
		    mRoomId = null;
		} else {
		}
	}
}

// Show the waiting room UI to track the progress of other players as they enter the
// room and get connected.
void showWaitingRoom(Room room) {
// minimum number of players required for our game
// For simplicity, we require everyone to join the game before we start it
// (this is signaled by Integer.MAX_VALUE).
//final int MIN_PLAYERS = Integer.MAX_VALUE;
final int MIN_PLAYERS = 2;
Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, MIN_PLAYERS);

// show waiting room UI
startActivityForResult(i, RC_WAITING_ROOM);
}

// Called when we get an invitation to play a game. We react by showing that to the user.
@Override
public void onInvitationReceived(Invitation invitation) {
// We got an invitation to play a game! So, store it in
// mIncomingInvitationId
// and show the popup on the screen.
mIncomingInvitationId = invitation.getInvitationId();
((TextView) findViewById(R.id.incoming_invitation_text)).setText(
        invitation.getInviter().getDisplayName() + " " +
                getString(R.string.is_inviting_you));
switchToScreen(mCurScreen); // This will show the invitation popup
}

@Override
public void onInvitationRemoved(String invitationId) {
if (mIncomingInvitationId.equals(invitationId)) {
    mIncomingInvitationId = null;
    switchToScreen(mCurScreen); // This will hide the invitation popup
}
}

/*
* CALLBACKS SECTION. This section shows how we implement the several games
* API callbacks.
*/

// Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
// is connected yet).
@Override
public void onConnectedToRoom(Room room) {
Log.d(TAG, "onConnectedToRoom.");

// get room ID, participants and my ID:
mRoomId = room.getRoomId();
this.room = room;
mParticipants = room.getParticipants();
mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(getApiClient()));

// print out the list of participants (for debug purposes)
Log.d(TAG, "Room ID: " + mRoomId);
Log.d(TAG, "My ID " + mMyId);
Log.d(TAG, "<< CONNECTED TO ROOM>>");
}

// Called when we've successfully left the room (this happens a result of voluntarily leaving
// via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
@Override
public void onLeftRoom(int statusCode, String roomId) {
// we have left the room; return to main screen.
//Log.d(TAG, "onLeftRoom, code " + statusCode);
//switchToMainScreen();
}

// Called when we get disconnected from the room. We return to the main screen.
@Override
public void onDisconnectedFromRoom(Room room) {
mRoomId = null;
showGameError();
this.voltarAoMenuInicial(null);
}

// Show error message about game being cancelled and return to main screen.
void showGameError() {
	Toast t = Toast.makeText(this, getString(R.string.game_problem), Toast.LENGTH_LONG);
    t.show();
//showAlert(getString(R.string.game_problem));
}

// Called when room has been created
@Override
public void onRoomCreated(int statusCode, Room room) {
Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
if (statusCode != GamesStatusCodes.STATUS_OK) {
    Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
    showGameError();
    return;
}

// show the waiting room UI
showWaitingRoom(room);
}

// Called when room is fully connected.
@Override
public void onRoomConnected(int statusCode, Room room) {
Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
if (statusCode != GamesStatusCodes.STATUS_OK) {
    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
    showGameError();
    return;
}
updateRoom(room);
}

@Override
public void onJoinedRoom(int statusCode, Room room) {
Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
if (statusCode != GamesStatusCodes.STATUS_OK) {
    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
    showGameError();
    return;
}

// show the waiting room UI
showWaitingRoom(room);
}

// We treat most of the room update callbacks in the same way: we update our list of
// participants and update the display. In a real game we would also have to check if that
// change requires some action like removing the corresponding player avatar from the screen,
// etc.
@Override
public void onPeerDeclined(Room room, List<String> arg1) {
updateRoom(room);
}

@Override
public void onPeerInvitedToRoom(Room room, List<String> arg1) {
updateRoom(room);
}

@Override
public void onP2PDisconnected(String participant) {
}

@Override
public void onP2PConnected(String participant) {
}

@Override
public void onPeerJoined(Room room, List<String> arg1) {
updateRoom(room);
}

@Override
public void onPeerLeft(Room room, List<String> peersWhoLeft) {
updateRoom(room);
}

@Override
public void onRoomAutoMatching(Room room) {
updateRoom(room);
}

@Override
public void onRoomConnecting(Room room) {
updateRoom(room);
}

@Override
public void onPeersConnected(Room room, List<String> peers) {
updateRoom(room);
}

@Override
public void onPeersDisconnected(Room room, List<String> peers) {
updateRoom(room);
}

void updateRoom(Room room) {
if (room != null) {
    mParticipants = room.getParticipants();
}
if (mParticipants != null) {
}
}

/*
* GAME LOGIC SECTION. Methods that implement the game's rules.
*/

// Current state of the game:
int tempoRestante = -1; // how long until the game ends (seconds)
final static int GAME_DURATION = 90; // game duration, seconds.
int mScore = 0; // user's current score

// Reset game variables in preparation for a new game.
void resetGameVars() {
tempoRestante = GAME_DURATION;
mScore = 0;
}

// Start the gameplay phase of the game.
synchronized void startGame(boolean multiplayer) 
{
	mMultiplayer = multiplayer;
	
	this.enviarSeuEmailParaOAdversario();
	
	switchToScreen(R.id.tela_jogo_multiplayer);
	
	if(this.criouUmaSala == false)
	{
		//quem nao criou a sala ainda nao tem o numero de rodadas ou as categorias
		String infinitasRodadas = getResources().getString(R.string.infinitas_rodadas_sem_mais_nada);
		String rodadas = this.salaAtual.getQuantasRodadas();
		
		if(rodadas.compareTo(infinitasRodadas) == 0)
		{
			this.quantasRodadasHaverao = 99;
		}
		else
		{
			this.quantasRodadasHaverao = Integer.valueOf(rodadas);
		}
		
		//o cara que nao criou a sala ira mandar o outro escolher os kanjis para treino 
		//somente apos a task abaixo terminar seu servico na funcao procedimentoaposcarregarkanjis()
		//implementada pela classe ModoCasual
		SolicitaKanjisParaTreinoTask pegaKanjisDoBD = new SolicitaKanjisParaTreinoTask(null, this);
		pegaKanjisDoBD.execute("");
	}
	else
	{
		//eh preciso tirar do banco de dados a sala que estava aberta. Ela agora fechou
		FechaSalaCasualCriadaPeloUsuarioTask fechaSalaAberta = new FechaSalaCasualCriadaPeloUsuarioTask();
		fechaSalaAberta.execute(this.emailUsuario);
	}
	
	
	//como eh o comeco da primeira partida do jogo, vamos fazer o usuario ver uma tela de espera pelo menos ate o kanji da dica ser escolhido
	this.comecarEsperaDoUsuarioParaComecoDaPartida();
	
	//a funcao comecarJogoMultiplayer que inicia a UI do jogo no modo multiplayer so serah iniciada quando os usuarios trocarem emails
}





/*
* UI SECTION. Methods that implement the game's UI.
*/

// This array lists everything that's clickable, so we can install click
// event handlers.
final static int[] CLICKABLES = {
    R.id.button_accept_popup_invitation, R.id.button_sign_in
};

int mCurScreen = -1;

void switchToScreen(int screenId) {
// make the requested screen visible; hide all others.
for (int id : SCREENS) {
    findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
}
mCurScreen = screenId;

// should we show the invitation popup?
boolean showInvPopup;
if (mIncomingInvitationId == null) {
    // no invitation, so no popup
    showInvPopup = false;
} else if (mMultiplayer) {
    // if in multiplayer, only show invitation on main screen
    showInvPopup = (mCurScreen == R.id.tela_lobby_modo_casual);
} else {
    // single-player: show on main screen and gameplay screen
    showInvPopup = (mCurScreen == R.id.tela_lobby_modo_casual || mCurScreen == R.id.screen_game);
}
findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
}

void switchToMainScreen() {
switchToScreen(isSignedIn() ? R.id.tela_lobby_modo_casual : R.id.screen_sign_in);

Spinner spinner = (Spinner) findViewById(R.id.spinnerPesquisarSalasModoCasual);
//Create an ArrayAdapter using the string array and a default spinner layout
ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
     R.array.spinner_pesquisar_por_modo_casual, android.R.layout.simple_spinner_item);
//Specify the layout to use when the list of choices appears
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//Apply the adapter to the spinner
spinner.setAdapter(adapter);
spinner.setOnItemSelectedListener(this);

this.mostrarLobbyModoCasual();

}




/*
* MISC SECTION. Miscellaneous methods.
*/

/**
* Checks that the developer (that's you!) read the instructions. IMPORTANT:
* a method like this SHOULD NOT EXIST in your production app! It merely
* exists here to check that anyone running THIS PARTICULAR SAMPLE did what
* they were supposed to in order for the sample to work.
*/
boolean verifyPlaceholderIdsReplaced() {
final boolean CHECK_PKGNAME = true; // set to false to disable check
                                    // (not recommended!)

// Did the developer forget to change the package name?
if (CHECK_PKGNAME && getPackageName().startsWith("com.google.example.")) {
    Log.e(TAG, "*** Sample setup problem: " +
        "package name cannot be com.google.example.*. Use your own " +
        "package name.");
    return false;
}

// Did the developer forget to replace a placeholder ID?
int res_ids[] = new int[] {
        R.string.app_id
};
for (int i : res_ids) {
    if (getString(i).equalsIgnoreCase("ReplaceMe")) {
        Log.e(TAG, "*** Sample setup problem: You must replace all " +
            "placeholder IDs in the ids.xml file by your project's IDs.");
        return false;
    }
}
return true;
}

// Sets the flag to keep this screen on. It's recommended to do that during
// the
// handshake when setting up a game, because if the screen turns off, the
// game will be
// cancelled.
void keepScreenOn() {
getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
}

// Clears the flag that keeps the screen on.
void stopKeepingScreenOn() {
getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
}

/*
* COMMUNICATIONS SECTION. Methods that implement the game's network
* protocol.
*/

// Called when we receive a real-time message from the network.
// Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
// indicating
// whether it's a final or interim score. The second byte is the score.
// There is also the
// 'S' message, which indicates that the game should start.
@Override
public void onRealTimeMessageReceived(RealTimeMessage rtm) 
{
	byte[] buf = rtm.getMessageData();
	String sender = rtm.getSenderParticipantId();

	String mensagem = "";
	try {
		mensagem = new String(buf, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	/*Toast t = Toast.makeText(this, "mensagem recebida:" + mensagem, Toast.LENGTH_LONG);
    t.show();*/
    
	
	
	if(mensagem.contains("mandar dados da partida para singleton") == true)
	{
		//o adversario pediu para o jogador armazenar os dados da partida no singleton(categorias escolhidas e quantas rodadas)
		
		//como eh o comeco da primeira partida do jogo, vamos fazer o usuario ver uma tela de espera pelo menos ate o kanji da dica ser escolhido
		this.comecarEsperaDoUsuarioParaComecoDaPartida();
		
		SingletonGuardaDadosDaPartida.getInstance().setQuantasRodadasHaverao(this.quantasRodadasHaverao);
		
		ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList = this.dataAdapter.getCategoriaDeKanjiList();
		
		 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			for(int i = 0; i < categoriaDeKanjiList.size(); i++)
			{
				CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
				if(umaCategoria.isSelected() == true)
				{
					String nomeCategoria = umaCategoria.getName();
					int posicaoParenteses = nomeCategoria.indexOf("(");
					String nomeCategoriaSemParenteses = nomeCategoria.substring(0, posicaoParenteses);
					LinkedList<KanjiTreinar> kanjisDaCategoria = 
							conheceKanjisECategorias.getListaKanjisTreinar(nomeCategoriaSemParenteses);
					SingletonGuardaDadosDaPartida.getInstance().adicionarNovaCategoriaESeusKanjis(nomeCategoriaSemParenteses, kanjisDaCategoria);
					
				}
			}	
			
		//vamos mudar a tela
		switchToScreen(R.id.tela_jogo_multiplayer);
		this.comecarJogoMultiplayer();
		
		//falta avisar ao jogador que escolhe as categorias p sortear os kanjis
		String mensagemParaODono = "pode comecar a escolher os kanjis";
		
		this.mandarMensagemMultiplayer(mensagemParaODono);
		
		this.pegarTodosOsKanjisQuePodemVirarCartas(); //ele tb deve atualizar a lista com todos os kanjis q podem virar cartas. senao quando um turno passar, o jogador q n escolhe as categorias n tem como gerar 8 kanjis
		
	}
	else if(mensagem.contains("pode comecar a escolher os kanjis") == true)
	{
		//mensagem enviada do jogador que nao escolhe a categoria para o que escolhe. Eh para que o jogador que escolhe a categoria tambem escolha os kanjis
		//essa mensagem so ocorre uma vez que eh no comeco da primeira partida do jogo
		
		this.pegarTodosOsKanjisQuePodemVirarCartas();
		this.escolher8KanjisParaARodada();
	}
	else if(mensagem.contains("kanjis=") == true && mensagem.contains("item misturarcartas kanjis=") == false)
	{
		//mensagem de quem escolhe categorias p/ dizer ao adversario quais os kanjis que estao na tela dele
		//tb eh enviado as posicoes dos ultimos kanjis de kanjisquepodemvirarcartas que precisam ser removidas
		//essas duas infos sao separadas por um @
		//ex: kanjis=au|cotidiano;me|corpo...@1;2;...
		//No final desse if, eh gerado um kanji da dica tb.
		String[] kanjisEPosicoesQuePodemVirarCartasRemovidas = mensagem.split("@");
		String kanjis =kanjisEPosicoesQuePodemVirarCartasRemovidas[0].replace("kanjis=", "");
		String[] kanjisSeparadosPorPontoEVirgula = kanjis.split(";");
		
		
		//tenho de achar cada KanjiTreinar com base na mensagem mandada. Felizmente ja tenho categoria e texto do kanji
		
		if(this.kanjisDasCartasNaTela == null)
		{
			this.kanjisDasCartasNaTela = new LinkedList<KanjiTreinar>();
		}
		if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas == null)
		{
			this.kanjisDasCartasNaTelaQueJaSeTornaramDicas = new LinkedList<KanjiTreinar>();
		}
		
		this.kanjisDasCartasNaTela.clear(); //se foi de uma rodada para outra, eh bom limpar essa lista
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.clear(); //essa tb
		
		for(int i = 0; i < kanjisSeparadosPorPontoEVirgula.length; i++)
		{
			String umKanjiECategoria = kanjisSeparadosPorPontoEVirgula[i];
			String[] kanjiECategoria = umKanjiECategoria.split("\\|");
			
			if(kanjiECategoria.length > 1)
			{
				//tem um kanji e uma categoria p serem obtidos
				String kanji = kanjiECategoria[0];
				String categoria = kanjiECategoria[1];
				
				KanjiTreinar umKanjiTreinar = 
						ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
				
				this.kanjisDasCartasNaTela.add(umKanjiTreinar);
			}
		}
		
		this.mudarCartasNaTela(kanjisSeparadosPorPontoEVirgula);
		//serah que foram geradas 8 cartas na tela ou menos?
		
		
		tornarORestoDasCartasNaTelaVazias();
		
		//o jogador que nao escolheu a categoria e nem os 8 kanjis da tela eh o unico que pode criar a dica do kanji
		this.gerarKanjiDaDica();
		
		//apos gerar o kanji da dica e visto que as 8 cartas ja estao na tela, pode-se dispensar o laoding inicial que diz p o usuario que o jogo estah sendo iniciado
		if(this.rodadaAtual == 1)
		{
			this.loadingComecoDaPartida.dismiss();
		}
		
		String posicoesKanjisPodemVirarCartasASeremRemovidas = kanjisEPosicoesQuePodemVirarCartasRemovidas[1];
		String[] posicoesArray = posicoesKanjisPodemVirarCartasASeremRemovidas.split(";");
		
		for(int j = 0; j < posicoesArray.length; j++)
		{
			int umaPosicao = Integer.valueOf(posicoesArray[j]);
			this.kanjisQuePodemVirarCartas.remove(umaPosicao);
		}
		
	}
	else if(mensagem.contains("kanjiDaDica=") == true && mensagem.contains("item") == false)
	{
		//alguem acertou algum kanji(ou eh o comeco de tudo) e eh necessairo mudar a dica do kanji
		//e nao eh o item p mudar a dica atual
		//formato: kanjiDaDica=asa|Cotidiano
		
		//se existir alguma carta dourada na tela(item cartasdouradas), ela deve voltar ao normal. No entanto,
		//se ela for a carta que acabou de deixar de ser a dica, ela vira verso. Por isso, vamos fazer as cartas
		//douradas voltarem ao normal ANTES de mudar kanjidadica
		fazerCartasDouradasVoltaremAoNormal();
				
		String kanjiECategoria = mensagem.replace("kanjiDaDica=", "");
		String[] kanjiECategoriaArray = kanjiECategoria.split("\\|");
		String kanji = kanjiECategoriaArray[0];
		String categoria = kanjiECategoriaArray[1];
		
		this.kanjiDaDica = ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
		this.palavrasJogadas.add(kanjiDaDica);
		this.alterarTextoDicaComBaseNoKanjiDaDica();
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(this.kanjiDaDica);
		
		//caso essa seja a primeira rodada e ja foi gerado um kanji da dica inicial, pode-se dispensar o loading que diz que o jogo estah sendo iniciado 
		if(this.rodadaAtual == 1 && this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == 1)
		{
			this.loadingComecoDaPartida.dismiss();
		}
		
		//assim que a dica muda, se existir alguma carta proibida na tela, ela desaparece(item naoesperemais)
		fazerCartaProibidaVoltarAoNormal();
	}
	else if(mensagem.contains("naoClicavel=") == true)
	{
		//alguem acertou uma carta e por isso essa carta nao deveria ser mais clicavel p ambos os jogadores
		//tb eh avisado se o adversario usou o 2x, ou seja, ganhou o dobro dos pontos ou nao
		//ex: naoClicavel=karuta1;usou2x=false
		String[] cartaNaoClicavelEUsou2x = mensagem.split(";");		
		String karutaNaoClicavel = cartaNaoClicavelEUsou2x[0].replace("naoClicavel=", "");
		if(karutaNaoClicavel.compareTo("karuta1") == 0)
		{
			ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta1); //mudei a figura da carta
    		findViewById(R.id.karuta1).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
    		textViewKaruta1.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta2") == 0)
		{
			ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta2); //mudei a figura da carta
    		findViewById(R.id.karuta2).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
    		textViewKaruta2.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta3") == 0)
		{
			ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta3); //mudei a figura da carta
    		findViewById(R.id.karuta3).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
    		textViewKaruta3.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta4") == 0)
		{
			ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta4); //mudei a figura da carta
    		findViewById(R.id.karuta4).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
    		textViewKaruta4.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta5") == 0)
		{
			ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta5); //mudei a figura da carta
    		findViewById(R.id.karuta5).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
    		textViewKaruta5.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta6") == 0)
		{
			ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta6); //mudei a figura da carta
    		findViewById(R.id.karuta6).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
    		textViewKaruta6.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta7") == 0)
		{
			ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta7); //mudei a figura da carta
    		findViewById(R.id.karuta7).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
    		textViewKaruta7.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta8") == 0)
		{
			ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			this.fazerImageViewFicarEscuro(imageViewKaruta8); //mudei a figura da carta
    		findViewById(R.id.karuta8).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
    		textViewKaruta8.setText("");
		}
		
		//a cada X cartas que ja se foram do jogo, um item eh aleatoriamente gerado para cada um dos jogadores		
		//se o adversario usou o item 2x, ele deve ganhar o dobro dos pontos
		String usou2xEmString = cartaNaoClicavelEUsou2x[1].replace("usou2x=", "");
		boolean usou2x = Boolean.valueOf(usou2xEmString);
		
		if(this.kanjiDaDica.getDificuldadeDoKanji() == 1)
		{
			if(usou2x == false)
			{
				this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 1;
			}
			else
			{
				this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 2;
			}
		}
		else if(this.kanjiDaDica.getDificuldadeDoKanji() == 2)
		{
			if(usou2x == false)
			{
				this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 2;
			}
			else
			{
				this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 4;
			}
		}
		else
		{
			if(usou2x == false)
			{
				this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 3;
			}
			else
			{
				this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 6;
			}
		}
		
		TextView textoPontuacaoAdversario = (TextView) findViewById(R.id.nome_e_pontuacao_jogador_direita);
		String palavraPontuacao = getResources().getString(R.string.pontuacao);
		String nomeAdversario= "";
		
		if(this.emailAdversario.length() > 24)
		{
			nomeAdversario = emailAdversario.substring(0, 23) + "...";
		}
		else
		{
			nomeAdversario = this.emailAdversario;
		}
		
		
		if(pontuacaoDoAdversario < 10)
		{
			textoPontuacaoAdversario.setText(nomeAdversario + ":" + "0" + String.valueOf(pontuacaoDoAdversario));
		}
		else
		{
			textoPontuacaoAdversario.setText(nomeAdversario + ":" + String.valueOf(pontuacaoDoAdversario));
		}
		
		
		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
		this.verificarSeJogadorRecebeUmItemAleatorio();
	}
	else if(mensagem.contains("rodadaMudou") == true)
	{
		this.rodadaAtual = this.rodadaAtual + 1;
		
		String textoRodada = getResources().getString(R.string.rodada_sem_dois_pontos);
		TextView textViewRodada = (TextView) findViewById(R.id.rodada);
		textViewRodada.setText(textoRodada + " " + String.valueOf(this.rodadaAtual));
		
		this.tornarTodasAsCartasNaTelaClicaveisEVaziasNovamente();
	}
	else if(mensagem.contains("gerarMais8Cartas") == true)
	{
		/*somente quem escolhe as categorias recebe essa mensagem q significa que ele deve gerar novas 8 
		 *cartas p a rodada. A rodada acabou de mudar*/
		this.escolher8KanjisParaARodada();
	}
	else if(mensagem.contains("item trovaotiracartaaleatoria indiceCartaRemovida=") == true)
	{
		//o adversario lancou um trovaotiracartaaleatoria e o trovao deve cair em quem recebeu esta mensagem tb
		String indiceCartaRemovidaEmString = mensagem.replace("item trovaotiracartaaleatoria indiceCartaRemovida=", "");
		int indiceCartaRemovida = Integer.valueOf(indiceCartaRemovidaEmString);
		this.lancarTrovaoNaTelaNaCartaDeIndice(indiceCartaRemovida);
	}
	else if(mensagem.contains("item trovaotiracarta indiceCartaRemovida=") == true)
	{
		//o adversario lancou um trovaotiracarta e o trovao deve cair em quem recebeu esta mensagem tb
		String indiceCartaRemovidaEmString = mensagem.replace("item trovaotiracarta indiceCartaRemovida=", "");
		int indiceCartaRemovida = Integer.valueOf(indiceCartaRemovidaEmString);
		this.lancarTrovaoNaTelaNaCartaDeIndice(indiceCartaRemovida);
	}
	else if(mensagem.contains("item parartempo") == true)
	{
		this.realizarProcedimentoPararTempo();
	}
	else if(mensagem.contains("item misturarcartas kanjis=") == true)
	{
		/*os kanjis chegam assim: kanjis=au|cotidiano;kau|cotidiano...*/
		String mensagemKanjis = mensagem.replace("item misturarcartas kanjis=", "");
		String[] kanjisECategorias = mensagemKanjis.split(";");
		
		LinkedList<String> textoKanjisNovos = new LinkedList<String>();
		LinkedList<String> categoriasKanjisNovos = new LinkedList<String>();
		
		for(int i = 0; i < kanjisECategorias.length; i++)
		{
			String umKanjiECategoria = kanjisECategorias[i];
			String[] kanjiECategoriaArray = umKanjiECategoria.split("\\|");
			textoKanjisNovos.add(kanjiECategoriaArray[0]);
			categoriasKanjisNovos.add(kanjiECategoriaArray[1]);
		}
		
		this.misturarCartasRecebeuCartasOutroUsuario(textoKanjisNovos, categoriasKanjisNovos);
	}
	else if(mensagem.contains("item mudardica kanjiDaDica=") == true)
	{
		this.tirarKanjiDicaAtualDeCartasQueJaViraramDicasEPalavrasJogadas();
		
		String kanjiECategoria = mensagem.replace("item mudardica kanjiDaDica=", "");
		String[] kanjiECategoriaArray = kanjiECategoria.split("\\|");
		String kanji = kanjiECategoriaArray[0];
		String categoria = kanjiECategoriaArray[1];
		
		this.kanjiDaDica = ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
		this.palavrasJogadas.add(kanjiDaDica);
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(this.kanjiDaDica);
		this.realizarProcedimentoMudandoDicaAtual();
	}
	else if(mensagem.contains("item revivercarta numeroCarta=") == true)
	{
		//adversario reviveu uma carta
		String stringNumeroCartaRevivida = mensagem.replace("item revivercarta numeroCarta=", "");
		int numeroCartaRevivida = Integer.valueOf(stringNumeroCartaRevivida);
		this.realizarProcedimentoReviverCarta(numeroCartaRevivida,true);
	}
	else if(mensagem.contains("termineiDeCarregarListaDeCategoria;") == true)
	{
		//guest manda pro host que jah terminou de carregar lista de categorias
		this.guestTerminouDeCarregarListaDeCategorias = true;
	}
	else if(mensagem.contains("fim de jogo") == true)
	{
		//algum jogador alcancou o fim de jogo e o outro tb deve fazer o mesmo
		switchToScreen(R.id.tela_fim_de_jogo);
		this.jogoAcabou = true;
		comecarFimDeJogo();
	}
	else if(mensagem.contains("oponente falou no chat="))
	{
		String mensagemAdicionarAoChat = mensagem.replaceFirst("oponente falou no chat=", "");
		this.adicionarMensagemNoChat(mensagemAdicionarAoChat, false);
	}
	else if(mensagem.contains("email=") == true)
	{
		this.emailAdversario = mensagem.replace("email=", "");
		this.comecarJogoMultiplayer(); //so eh possivel iniciar os componentes da tela do jogo multiplayer se o email do adversario tiver sido enviado
	}

}



/*NOVO DA ACTIVITY REFERENTE A SELECIONAR CATEGORIAS */
private MyCustomAdapter dataAdapter = null;
private ProgressDialog loadingKanjisDoBd;
private static String jlptEnsinarNaFerramenta = "4";

private void solicitarPorKanjisPraTreino() {
	this.loadingKanjisDoBd = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_kanjis_remotamente), getResources().getString(R.string.por_favor_aguarde));
	  SolicitaKanjisParaTreinoTask armazenarMinhasFotos = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
	  armazenarMinhasFotos.execute("");
	 
}
  
 public void procedimentoAposCarregarKanjis() 
 {
	 if(this.naCriacaoDeSalaModoCasual == true)
	 {
		 //esta na criacao de uma sala? Entao vamos disponibilizar esses kanjis carregados numa listview
		 
		//Array list of countries
		  ArrayList<CategoriaDeKanjiParaListviewSelecionavel> listaDeCategorias = new ArrayList<CategoriaDeKanjiParaListviewSelecionavel>();
		  
		  LinkedList<String> categoriasDosKanjis = 
				  ArmazenaKanjisPorCategoria.pegarInstancia().getCategoriasDeKanjiArmazenadas(jlptEnsinarNaFerramenta);
		  
		  for(int i = 0; i < categoriasDosKanjis.size(); i++)
		  {
			  String categoriaDeKanji = categoriasDosKanjis.get(i);
			  LinkedList<KanjiTreinar> kanjisDaCategoria = ArmazenaKanjisPorCategoria.pegarInstancia().getListaKanjisTreinar(categoriaDeKanji);
			  String labelCategoriaDeKanji = categoriaDeKanji + "(" + kanjisDaCategoria.size() + getResources().getString(R.string.contagem_kanjis) + ")";
			  CategoriaDeKanjiParaListviewSelecionavel novaCategoria = new CategoriaDeKanjiParaListviewSelecionavel(labelCategoriaDeKanji, false);
			  listaDeCategorias.add(novaCategoria);
		  }
		 
		  
		  boolean possoEscolherCategorias = true;
		  
		  //create an ArrayAdaptar from the String Array
		  dataAdapter = new MyCustomAdapter(this,
		    R.layout.categoria_de_kanji_na_lista, listaDeCategorias,possoEscolherCategorias,this);
		  ListView listView = (ListView) findViewById(R.id.listaCategorias);
		  // Assign adapter to ListView
		  listView.setAdapter(dataAdapter);
		  
		  listView.setOnItemClickListener(new OnItemClickListener() {
		   public void onItemClick(AdapterView parent, View view,
		     int position, long id) 
		   {
				// When clicked, show a toast with the TextView text
				    CategoriaDeKanjiParaListviewSelecionavel categoriaDeKanji = (CategoriaDeKanjiParaListviewSelecionavel) parent.getItemAtPosition(position);
				    Toast.makeText(getApplicationContext(),
				      "Clicked on Row: " + categoriaDeKanji.getName(),
				      Toast.LENGTH_LONG).show();
				  
				 
		   }
		  });
	 }
	 else
	 {
		 //AQUI SO OCORRERAH APOS O NAO HOST PEDIR OS KANJIS DO BD NA FUNCAO startgame() e a task terminar de pegar os kanjis do bd
		 String categorias = this.salaAtual.getCategoriasJuntas();
			
			String[] stringSeparada = categorias.split(",");
			
			ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			for(int i = 0; i < stringSeparada.length; i++)
			{
				String umaCategoria = stringSeparada[i];
				LinkedList<KanjiTreinar> kanjisDaCategoria = 
						conheceKanjisECategorias.getListaKanjisTreinar(umaCategoria);
				SingletonGuardaDadosDaPartida.getInstance().adicionarNovaCategoriaESeusKanjis(umaCategoria, kanjisDaCategoria);
			}
			
			//falta avisar ao jogador que escolhe as categorias p sortear os kanjis
			String mensagemParaODono = "pode comecar a escolher os kanjis";
					
			this.mandarMensagemMultiplayer(mensagemParaODono);
					
			this.pegarTodosOsKanjisQuePodemVirarCartas(); //ele tb deve atualizar a lista com todos os kanjis q podem virar cartas. senao quando um turno passar, o jogador q n escolhe as categorias n tem como gerar 8 kanjis
	 }
  
 }
 
 public void mandarMensagemMultiplayer(String mensagem)
 {
	 byte[] mensagemEmBytes = mensagem.getBytes();
	 for (Participant p : mParticipants) 
		{
			if (p.getParticipantId().equals(mMyId))
			{
				continue;
			}
		    else
		    {
		    	Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(),null, mensagemEmBytes, mRoomId,
			            p.getParticipantId());
		    }
		}
 }
 
 public void onRadioButtonClicked(View view) 
 {
	    // O radioButton esta marcado?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radioButton1:
	            if (checked)
	                this.quantasRodadasHaverao = 1;
	            break;
	        case R.id.radioButton2:
	            if (checked)
	            	this.quantasRodadasHaverao = 2;
	            break;
	        case R.id.radioButton3:
	            if (checked)
	            	this.quantasRodadasHaverao = 3;
	            break;
	        case R.id.radioButton4:
	            if (checked)
	            	this.quantasRodadasHaverao = 99; //infinitas rodadas
	            break;
	    }
	    
	    /*String quantasRodadasHaveraoString = "quantasRodadasHaverao=" + this.quantasRodadasHaverao;
	    this.mandarMensagemMultiplayer(quantasRodadasHaveraoString);*/
}

 private void quemEscolheCategoriasClicouNoBotaoOk()
 {
	 //primeiro iremos armazenar no singleton todas as categorias escolhidas e kanjis delas
	 ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList = this.dataAdapter.getCategoriaDeKanjiList();
	
	 if(categoriaDeKanjiList.size() == 0)
	 {
		 String mensagem = getResources().getString(R.string.erroEscolherCategorias);
		 Toast t = Toast.makeText(this, mensagem, Toast.LENGTH_LONG);
		 t.show();
	 }
	 else
	 {
		 SingletonGuardaDadosDaPartida.getInstance().limparCategoriasEKanjis();
		 
		 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			for(int i = 0; i < categoriaDeKanjiList.size(); i++)
			{
				CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
				if(umaCategoria.isSelected() == true)
				{
					String nomeCategoria = umaCategoria.getName();
					int posicaoParenteses = nomeCategoria.indexOf("(");
					String nomeCategoriaSemParenteses = nomeCategoria.substring(0, posicaoParenteses);
					LinkedList<KanjiTreinar> kanjisDaCategoria = 
							conheceKanjisECategorias.getListaKanjisTreinar(nomeCategoriaSemParenteses);
					SingletonGuardaDadosDaPartida.getInstance().adicionarNovaCategoriaESeusKanjis(nomeCategoriaSemParenteses, kanjisDaCategoria);
					
				}
			}
			
		//Agora vamos armazenar quantas rodadas o jogo terah no singleton
		SingletonGuardaDadosDaPartida.getInstance().setQuantasRodadasHaverao(quantasRodadasHaverao);
		
		//Agora falta alertar ao outro jogador que ele precisa mudar tb, mas nao se preocupe que o MyCustomAdapter ja mantem as categorias selecionadas tb e o quantasRodadasHaverao foi atualizado tb
		String stringAlertarJogador = "mandar dados da partida para singleton";
		this.mandarMensagemMultiplayer(stringAlertarJogador);
		 
		 //por fim, vamos mudar a tela
		 switchToScreen(R.id.tela_jogo_multiplayer);
	 }
	 
	//como eh o comeco da primeira partida do jogo, vamos fazer o usuario ver uma tela de espera pelo menos ate o kanji da dica ser escolhido
	this.comecarEsperaDoUsuarioParaComecoDaPartida();
		
 }
 
 private void comecarJogoMultiplayer()
 {
	 mudarFonteDosKanjis();
	 /*findViewById(R.id.pontuacao).setVisibility(View.VISIBLE);
	 findViewById(R.id.rodada).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta1).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta2).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta3).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta4).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta5).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta6).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta7).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta8).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta1).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta2).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta3).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta4).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta5).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta6).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta7).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta8).setVisibility(View.VISIBLE);
	 
	 findViewById(R.id.item).setVisibility(View.VISIBLE);
	 findViewById(R.id.tempo).setVisibility(View.VISIBLE);
	 findViewById(R.id.label_item).setVisibility(View.VISIBLE);
	 findViewById(R.id.mascote).setVisibility(View.VISIBLE);
	 findViewById(R.id.dica_kanji).setVisibility(View.VISIBLE);*/
	 findViewById(R.id.parartempopequeno).setVisibility(View.INVISIBLE);
	 findViewById(R.id.doisxpequeno).setVisibility(View.INVISIBLE);
	 findViewById(R.id.naoesperemais).setVisibility(View.INVISIBLE);
	 
	 findViewById(R.id.karuta1_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta2_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta3_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta4_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta5_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta6_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta7_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta8_imageview).setOnClickListener(this);
	 
	 
	 findViewById(R.id.item1).setOnClickListener(this);
	 findViewById(R.id.item1).setClickable(false);
	 findViewById(R.id.item2).setOnClickListener(this);
	 findViewById(R.id.item2).setClickable(false);
	 findViewById(R.id.item3).setOnClickListener(this);
	 findViewById(R.id.item3).setClickable(false);
	 findViewById(R.id.item4).setOnClickListener(this);
	 findViewById(R.id.item4).setClickable(false);
	 findViewById(R.id.item4).setOnClickListener(this);
	 findViewById(R.id.item4).setClickable(false);
	 findViewById(R.id.item5).setOnClickListener(this);
	 findViewById(R.id.item5).setClickable(false);
	 findViewById(R.id.item6).setOnClickListener(this);
	 findViewById(R.id.item6).setClickable(false);
	 findViewById(R.id.item7).setOnClickListener(this);
	 findViewById(R.id.item7).setClickable(false);
	 findViewById(R.id.item8).setOnClickListener(this);
	 findViewById(R.id.item8).setClickable(false);
	 findViewById(R.id.item9).setOnClickListener(this);
	 findViewById(R.id.item9).setClickable(false);
	 
	 TextView textoPontuacao = (TextView) findViewById(R.id.nome_e_pontuacao_jogador_esquerda);
		String palavraPontuacao = getResources().getString(R.string.pontuacao);
		String nomeJogador = "";
		
		if(this.emailUsuario.length() > 24)
		{
			nomeJogador = emailUsuario.substring(0, 23) + "...";
		}
		else
		{
			nomeJogador = this.emailUsuario;
		}
		
		textoPontuacao.setText(nomeJogador + ":" + "00");
		
		TextView textoPontuacaoAdversario = (TextView) findViewById(R.id.nome_e_pontuacao_jogador_direita);
		String nomeAdversario= "";
		
		if(this.emailAdversario.length() > 24)
		{
			nomeAdversario = emailAdversario.substring(0, 23) + "...";
		}
		else
		{
			nomeAdversario = this.emailAdversario;
		}
		
		
		if(pontuacaoDoAdversario < 10)
		{
			textoPontuacaoAdversario.setText(nomeAdversario + ":" + "00");
		}
		
		
	 this.rodadaAtual = 1;
	 TextView textViewRodada = (TextView) findViewById(R.id.rodada);
	 String labelRodada = getResources().getString(R.string.rodada_sem_dois_pontos);
	 textViewRodada.setText(labelRodada + " " + "1");
	 
	 this.quantasCartasJaSairamDoJogo = 0;
	 this.suaPontuacao = 0;
	 this.pontuacaoDoAdversario = 0;
	 this.palavrasAcertadas = new LinkedList<KanjiTreinar>();
	 this.palavrasErradas = new LinkedList<KanjiTreinar>();
	 this.palavrasJogadas = new LinkedList<KanjiTreinar>();
	 
	 this.itensAtuais = new LinkedList<String>();
	 this.itensDoGanhador = new LinkedList<String>();
	 this.itensDoPerdedor = new LinkedList<String>();
	 itensDoGanhador.add("trovaotiracartaaleatoria"); //os ultimos testados
	 itensDoGanhador.add("parartempo");
	 itensDoGanhador.add("misturarcartas");
	 itensDoPerdedor.add("mudardica");
	 itensDoGanhador.add("doisx");
	 itensDoPerdedor.add("naoesperemais");
	 itensDoPerdedor.add("cartasdouradas"); //os ultimos testados
	 itensDoPerdedor.add("trovaotiracarta"); 
	 itensDoPerdedor.add("revivecarta");
	 
	 this.usouTrovaoTiraCarta = false;
	 this.usouReviveCarta = false;
	 this.usou2x = false;
	 this.usouNaoEspereMais = false;
	 
	 this.qualCartaEstaProibida = 0; //nenhuma carta esta proibida no comeco do jogo
	 this.quaisCartasEstaoDouradas = new LinkedList<Integer>(); //nenhuma carta estah dourada no comeco
	 
	 TextView textoTempo = (TextView) findViewById(R.id.tempo);
	 String stringTempo = getResources().getString(R.string.tempo_restante);
	 textoTempo.setText(stringTempo + "1:30");
	 tempoEstahParado = false;
	 this.jogoAcabou = false;
	 
	 this.qualImagemEstaSendoUsadaNaAnimacaoDoTempo = 0;
	 
	 /*this.timerTaskDecrementaTempoRestante = new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	if(tempoEstahParado == false)
		            	{
		            		passarUmSegundo();
		            	}
		            }
		        });
		    }
	 };
     new Timer().schedule(this.timerTaskDecrementaTempoRestante, 1000);*/
	 
	 final Handler h = new Handler();
     h.postDelayed(new Runnable() {
         @Override
         public void run() 
         {
             if (tempoEstahParado == false)
             {
            	 ModoCasual.this.runOnUiThread(new Runnable() 
 		        {
 		            @Override
 		            public void run() 
 		            {
 		            	if(loadingComecoDaPartida.isShowing() == false)
 		            	{
 		            		//o tempo so passa apos o loading comeco da partida passar
 		            		passarUmSegundo();
 	 		            	if(tempoRestante <= 0)
 	 		            	{
 	 		            		//o jogo deve acabar
 	 		            		
 	 		            		if(jogoAcabou == false)
 	 		            		{
 	 		            			terminarJogoEEnviarMesagemAoAdversario();
 	 		            		}
 	 		            	}
 		            	}
 		            }
 		        });
             }
             
             h.postDelayed(this, 1000); 
             
         }
     }, 1000);
	 
     final Handler h2 = new Handler();
     h2.postDelayed(new Runnable() {
         @Override
         public void run() 
         {
             if (tempoEstahParado == false)
             {
            	 ModoCasual.this.runOnUiThread(new Runnable() 
 		        {
 		            @Override
 		            public void run() 
 		            {
 		            	if(loadingComecoDaPartida.isShowing() == false)
 		            	{
 		            		//o tempo so passa apos o loading comeco da partida passar
 		            		if(jogoAcabou == false)
	 		            	{
 		            			//mudarImagemAnimacaoTempoCorrendo();
	 		            	}
 		            	}
 		            }
 		        });
             }
             
             h2.postDelayed(this, 100); 
             
         }
     }, 100);
     
     //falta iniciar a musica de fundo do jogo
     this.mudarMusicaDeFundo(R.raw.radiate);
	 
 }
 
 private void passarUmSegundo()
 {
	 this.tempoRestante = this.tempoRestante - 1;
	 String tempoParaMostrar = ""; //n irei mostrar 90s, irei mostrar 1:30,1:29,0:30...
	 if(tempoRestante < 60)
	 {
		 if(tempoRestante < 10)
		 {
			 tempoParaMostrar = "0:0" + String.valueOf(tempoRestante);
		 }
		 else
		 {
			 tempoParaMostrar = "0:" + String.valueOf(tempoRestante);
		 }
	 }
	 else
	 {
		 int segundosMenosUmMinuto = this.tempoRestante - 60;
		 if(segundosMenosUmMinuto < 10)
		 {
			 tempoParaMostrar = "1:0" + String.valueOf(segundosMenosUmMinuto);
		 }
		 else
		 {
			 tempoParaMostrar = "1:" + String.valueOf(segundosMenosUmMinuto);
		 }
	 }
	 
	 TextView textoTempo = (TextView) findViewById(R.id.tempo);
	 textoTempo.setText(tempoParaMostrar);
 }
 
 private void mudarImagemAnimacaoTempoCorrendo()
 {
	 TextView textoTempo = (TextView) findViewById(R.id.tempo);
	 this.qualImagemEstaSendoUsadaNaAnimacaoDoTempo = this.qualImagemEstaSendoUsadaNaAnimacaoDoTempo + 1;
	 if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo > 17)
	 {
		 qualImagemEstaSendoUsadaNaAnimacaoDoTempo = 0;
	 }
	 
	 if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 0)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo0);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 1)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo1);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 2)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo2);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 3)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo3);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 4)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo4);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 5)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo5);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 6)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo6);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 7)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo7);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 8)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo8);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 9)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo9);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 10)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo10);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 11)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo11);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 12)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo12);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 13)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo13);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 14)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo14);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 15)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo15);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 16)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo16);
	 }
	 else if(qualImagemEstaSendoUsadaNaAnimacaoDoTempo == 17)
	 {
		 textoTempo.setBackgroundResource(R.drawable.tempo17);
	 }
 }
 
 
 /*alem da funcao abaixo mudar cada uma das cartas na tela e decidir quais os 8 kanjis da rodada serao usados,
  * ela tb manda uma mensagem ao jogador que nao eh quem escolhe a categoria avisando quais kanjis entraram.
  * A funcao so eh executada pelo jogador que eh que escolhe as categorias*/
 private void escolher8KanjisParaARodada()
 {
	 if(this.criouUmaSala == true)
	 {
		 //quem escolhe a categoria quem vai sortear os kanjis

		 if(kanjisDasCartasNaTela == null)
		 {
			 this.kanjisDasCartasNaTela = new LinkedList<KanjiTreinar>();
		 }
		 if(kanjisDasCartasNaTelaQueJaSeTornaramDicas == null)
		 {
			 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas = new LinkedList<KanjiTreinar>();
		 }
		 if(this.posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas == null)
		 {
			 this.posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas = new LinkedList<Integer>();
		 }
		 
		 this.kanjisDasCartasNaTela.clear();
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.clear();
		 this.posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas.clear();
		 
		 for(int i = 0; i < 8; i++)
		 {
			 KanjiTreinar kanjiTreinar = this.escolherUmKanjiParaTreinar();
			 
			 if(kanjiTreinar == null)
			 {
				 //acabaram-se os kanjis que posso usar na tela
				 this.tornarORestoDasCartasNaTelaVazias();
				 break;
				 
			 }
			 else
			 { 
				 this.kanjisDasCartasNaTela.add(kanjiTreinar);
				 
				 if(i == 0)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta1_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(0);
				 }
				 else if(i == 1)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta2_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(1);
				 }
				 else if(i == 2)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta3_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(2);
				 }
				 else if(i == 3)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta4_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(3);
				 }
				 else if(i == 4)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta5_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(4);
				 }
				 else if(i == 5)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta6_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(5);
				 }
				 else if(i == 6)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta7_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(6);
				 }
				 else if(i == 7)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
					 this.colocarTextoVerticalNaCarta(texto, kanjiTreinar.getKanji());
					 findViewById(R.id.karuta8_imageview).setClickable(true);
					 this.colorirBordaDaCartaDeAcordoComCategoria(7);
				 } 
				 
			 }
		 }
		 
		 
		 //falta agora avisar ao outro jogador quais as cartas na tela
		 String kanjisString = "kanjis=";
		 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
			 if(i < this.kanjisDasCartasNaTela.size() - 1)
			 {
				 kanjisString = kanjisString + umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada() + ";";
			 }
			 else
			 {
				 kanjisString = kanjisString + umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
			 }
		 }
		 
		 //devemos enviar tambem as posicoes dos kanjis que foram removidos em kanjisQuePodemVirarCartas
		 String posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartasEmString = "";
		 
		 for(int j = 0; j < this.posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas.size(); j++)
		 {
			 int umaPosicao = this.posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas.get(j);
			 posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartasEmString =
					 posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartasEmString + String.valueOf(umaPosicao) + ";";
		 }
		 
		 String mensagemParaAdversario = kanjisString + "@" + posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartasEmString;
		 this.mandarMensagemMultiplayer(mensagemParaAdversario);
		 
	 }
 }
 
 /*pode ser que em alguma rodada, nao seja possivel criar 8 kanjis p mostrar na tela. O resto dessas 8 cartas erao cartas vazias*/
 private void tornarORestoDasCartasNaTelaVazias()
 {
	 int quantasCartasNaTela = this.kanjisDasCartasNaTela.size();
	 if(quantasCartasNaTela == 1)
	 {
		 TextView textView2 = (TextView) findViewById(R.id.texto_karuta2);
		 textView2.setText("");
		 ImageView carta2 = (ImageView) findViewById(R.id.karuta2_imageview);
		 carta2.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta2);
		 
		 TextView textView3 = (TextView) findViewById(R.id.texto_karuta3);
		 textView3.setText("");
		 ImageView carta3 = (ImageView) findViewById(R.id.karuta3_imageview);
		 carta3.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta3);
		 
		 TextView textView4 = (TextView) findViewById(R.id.texto_karuta4);
		 textView4.setText("");
		 ImageView carta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 carta4.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta4);
		 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta5);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta6);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta7);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 else if(quantasCartasNaTela == 2)
	 {
		 TextView textView3 = (TextView) findViewById(R.id.texto_karuta3);
		 textView3.setText("");
		 ImageView carta3 = (ImageView) findViewById(R.id.karuta3_imageview);
		 carta3.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta3);
		 
		 TextView textView4 = (TextView) findViewById(R.id.texto_karuta4);
		 textView4.setText("");
		 ImageView carta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 carta4.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta4);
		 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta5);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta6);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta7);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 else if(quantasCartasNaTela == 3)
	 {
		 
		 TextView textView4 = (TextView) findViewById(R.id.texto_karuta4);
		 textView4.setText("");
		 ImageView carta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 carta4.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta4);
		 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta5);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta6);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta7);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 else if(quantasCartasNaTela == 4)
	 { 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta5);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta6);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta7);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 else if(quantasCartasNaTela == 5)
	 { 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta6);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta7);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 else if(quantasCartasNaTela == 6)
	 { 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta7);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 else if(quantasCartasNaTela == 7)
	 { 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
		 this.fazerImageViewFicarEscuro(carta8);
	 }
	 
 }
 
 /*esse metodo retorna null caso todos os kanjis de kanjisQuePodemVirarCartas for usado*/
 private KanjiTreinar escolherUmKanjiParaTreinar()
 {
	 if(kanjisQuePodemVirarCartas.size() <= 0)
	 {
			return null;
	 }
	 else
	 {
		 Random geraNumAleatorio = new Random();
		 int posicaoKanjiEscolhido = geraNumAleatorio.nextInt(this.kanjisQuePodemVirarCartas.size());
		 
		 KanjiTreinar kanjiEscolhido = this.kanjisQuePodemVirarCartas.remove(posicaoKanjiEscolhido); 
		 this.posicoesUltimosKanjisQueSairamDeKanjisQuePodemVirarCartas.add(posicaoKanjiEscolhido);
		 
		 return kanjiEscolhido; 
	 }
 }
 
 /*muda todas as cartas da tela com base no atributo kanjisDasCartasNaTela. Entrada possivel: au|cotidiano,me|corpo,...*/
 private void mudarCartasNaTela(String[] kanjis)
 {
	 for(int i = 0; i < kanjis.length; i++)
	 {
		 String umKanjiECategoria = kanjis[i];
		 String[] kanjiECategoriaArray = umKanjiECategoria.split("\\|");
		 if(kanjiECategoriaArray.length > 1)
		 {
			 //tem algum kanji p ser extraido aqui. Pois ele tem o kanji e a categoria
			 String umKanji = kanjiECategoriaArray[0];
			 String umaCategoria = kanjiECategoriaArray[1];
			 
			 boolean cartaDoKanjiDeveSerX = this.cartaDoKanjiDeveSerX(umaCategoria, umKanji);
			 //se o kanji ja tiver virado dica anteriormente e nao eh a dica atual, entao a carta dele ja deveria ter saido do jogo
			 
			 if(i == 0)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
					 texto.setText(""); 
					 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
					 imageViewCarta1.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta1);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
					 imageViewCarta1.setClickable(true);
					 imageViewCarta1.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta1,i);
					 
				 }
			 }
			 else if(i == 1)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
					 texto.setText(""); 
					 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
					 imageViewCarta2.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta2);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
					 imageViewCarta2.setClickable(true);
					 imageViewCarta2.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta2,i);
				 }
			 }
			 else if(i == 2)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
					 texto.setText(""); 
					 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
					 imageViewCarta3.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta3);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
					 imageViewCarta3.setClickable(true);
					 imageViewCarta3.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta3,i);
				 }
			 }
			 else if(i == 3)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
					 texto.setText(""); 
					 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
					 imageViewCarta4.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta4);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
					 imageViewCarta4.setClickable(true);
					 imageViewCarta4.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta4,i);
				 }
			 }
			 else if(i == 4)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
					 texto.setText(""); 
					 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
					 imageViewCarta5.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta5);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
					 imageViewCarta5.setClickable(true);
					 imageViewCarta5.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta5,i);
				 }
			 }
			 else if(i == 5)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
					 texto.setText(""); 
					 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
					 imageViewCarta6.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta6);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
					 imageViewCarta6.setClickable(true);
					 imageViewCarta6.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta6,i);
				 }
			 }
			 else if(i == 6)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
					 texto.setText(""); 
					 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
					 imageViewCarta7.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta7);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
					 imageViewCarta7.setClickable(true);
					 imageViewCarta7.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta7,i);
				 }
			 }
			 else if(i == 7)
			 {
				 if(cartaDoKanjiDeveSerX == true)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
					 texto.setText(""); 
					 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
					 imageViewCarta8.setClickable(false);
					 this.fazerImageViewFicarEscuro(imageViewCarta8);
				 }
				 else
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
					 this.colocarTextoVerticalNaCarta(texto, umKanji);
					 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
					 imageViewCarta8.setClickable(true);
					 imageViewCarta8.setImageResource(R.drawable.karutavazia);
					 this.fazerCartaVoltarACorNormal(imageViewCarta8,i);
				 }
			 }
		 }
	 }
	 
 }
 
 /*caso o kanji dessa categoria e texto nao seja o kanjidaDica e ele ja tenha saido como dica, entao a carta respetiva desse kanji deveria ser uma carta X*/
 private boolean cartaDoKanjiDeveSerX(String categoriaKanji, String textoKanji)
 {
	 if(this.kanjiDaDica == null)
	 {
		 //no primeiro turno, quando nao ha dica nenhuma, nenhum kanji eh X
		 return false;
	 }
	 
	 if((this.kanjiDaDica.getKanji().compareTo(textoKanji) == 0)
			 && (this.kanjiDaDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0))
	 {
		 return false; //eh o kanji da dica
	 }
	 else
	 {
		 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
		 {
			 KanjiTreinar umKanjiJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
			 
			 if((umKanjiJaVirouDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0) 
					 && (umKanjiJaVirouDica.getKanji().compareTo(textoKanji) == 0))
			 {
				 //o kanji ja virou dica anteriormente e alguem ja o acertou e por isso deve ser um X
				 return true;
			 }
			 
		 }
		 
		 return false; //passamos por todos os kanjis que ja viraram dicas e nenhum deles era esse kanji como parametro. Entao ele ainda estah no jogo
	 }
 }
 
 /*funcao para gerar a dica que vai aparecer para ambos os usuarios e ainda enviar ao outro jogador essa dica*/
 private void gerarKanjiDaDica()
 {
	 LinkedList<KanjiTreinar> kanjisQueAindaNaoViraramDicas = new LinkedList<KanjiTreinar>();
	 
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 
		 boolean kanjiJaVirouDica = false;
		 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
		 {
			 KanjiTreinar umKanjiQueVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
			 
			 if(umKanjiQueVirouDica.getKanji().compareTo(umKanji.getKanji()) == 0)
			 {
				 kanjiJaVirouDica = true;
				 break;
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
	 this.palavrasJogadas.add(kanjiDaDica);
	 
	 String mensagem = "kanjiDaDica=" + this.kanjiDaDica.getKanji() + "|" + this.kanjiDaDica.getCategoriaAssociada();
	 this.mandarMensagemMultiplayer(mensagem);
	 
	 this.alterarTextoDicaComBaseNoKanjiDaDica();
	 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(umKanji);
 }
 
 private void alterarTextoDicaComBaseNoKanjiDaDica()
 {
	 TextView textoDica = (TextView) findViewById(R.id.dica_kanji);
	 TextView textoTraducao = (TextView) findViewById(R.id.dica_kanji_traducao);
	 String hiraganaDoKanji = this.kanjiDaDica.getHiraganaDoKanji();
	 String traducaoDoKanji = this.kanjiDaDica.getTraducaoEmPortugues();
	 textoDica.setText(hiraganaDoKanji);
	 textoTraducao.setText("(" + traducaoDoKanji + ")");
 }
 
 
 /*assim que o usuario acerta uma carta, ele avisa ao adversairo que aquela carta nao pode ser mais escolhida*/
 private void alertarAoAdversarioQueACartaNaoEhMaisClicavel(String qualCarta)
 {
	 String mensagem = "naoClicavel=" + qualCarta + ";" + "usou2x=" + this.usou2x; //ex: naoClicavel=karuta1
	 if(this.usou2x == true)
	 {
		 this.usou2x = false;
	 }
	 
	 this.mandarMensagemMultiplayer(mensagem);
 }
 
 /*funcao chamada assim que alguem acerta algum kanji na tela.*/
 private void gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo()
 {
	 if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == this.kanjisDasCartasNaTela.size())
	 {
		 //todos os kanjis da tela ja se tornaram dicas. Eh hora de mudar a rodada ou terminar o jogo
		 if(this.quantasRodadasHaverao == this.rodadaAtual || this.kanjisQuePodemVirarCartas.size() == 0)
		 {
			 //o jogo deve terminar. Ou a quantidade de rodadas foi alcancada ou nao existem mais cartas a serem geradas
			 this.terminarJogoEEnviarMesagemAoAdversario();
		 }
		 else
		 {
			 //deve-se passar para a proxima rodada
			 this.realizarProcedimentoPassarParaProximaRodada();
			 
		 }
	 }
	 else
	 {
		 this.gerarKanjiDaDica();
	 }
 }
 
 private void realizarProcedimentoPassarParaProximaRodada()
 {
	 this.rodadaAtual = this.rodadaAtual + 1;
	 TextView textViewRodada = (TextView) findViewById(R.id.rodada);
	 String textoRodada = getResources().getString(R.string.rodada_sem_dois_pontos);
	 textViewRodada.setText(textoRodada + " " + String.valueOf(this.rodadaAtual));
	 
	 this.tornarTodasAsCartasNaTelaClicaveisEVaziasNovamente();
	 
	 String mensagemRodadaMudou = "rodadaMudou";
	 this.mandarMensagemMultiplayer(mensagemRodadaMudou);
	 
	 if(this.criouUmaSala == true)
	 {
		 //jogador eh quem escolheu as categorias, entao eh ele quem sorteia os 8 kanjis da rodada
		 this.escolher8KanjisParaARodada();
	 }
	 else
	 {
		 //jogador nao eh quem escolheu as categorias, entao ele deve avisar ao adversario que eh necessario criar mais 8 cartas p a jogada
		 String mensagemGerarMais8Cartas = "gerarMais8Cartas";
		 this.mandarMensagemMultiplayer(mensagemGerarMais8Cartas);
	 }
 }
 
 /*as cartas voltam a ser clicaveis e o X no meio da carta desaparece*/
 private void tornarTodasAsCartasNaTelaClicaveisEVaziasNovamente()
 {
	 ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 imageViewKaruta1.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta1,0);
	 findViewById(R.id.karuta1).setClickable(true);
	 
	 ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 imageViewKaruta2.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta2,1);
	 findViewById(R.id.karuta2).setClickable(true);
	 
	 ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 imageViewKaruta3.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta3,2);
	 findViewById(R.id.karuta3).setClickable(true);
	 
	 ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 imageViewKaruta4.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta4,3);
	 findViewById(R.id.karuta4).setClickable(true);
	 
	 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 imageViewKaruta5.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta5,4);
	 findViewById(R.id.karuta5).setClickable(true);
	 
	 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 imageViewKaruta6.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta6,5);
	 findViewById(R.id.karuta6).setClickable(true);
	 
	 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 imageViewKaruta7.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta7,6);
	 findViewById(R.id.karuta7).setClickable(true);
	 
	 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 imageViewKaruta8.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 this.fazerCartaVoltarACorNormal(imageViewKaruta8,7);
	 findViewById(R.id.karuta8).setClickable(true);
 }
 
 private void pegarTodosOsKanjisQuePodemVirarCartas()
 {
	 this.kanjisQuePodemVirarCartas = new LinkedList<KanjiTreinar>();
	 HashMap<String,LinkedList<KanjiTreinar>> categoriasEscolhidasEKanjisDelas = SingletonGuardaDadosDaPartida.getInstance().getCategoriasEscolhidasEKanjisDelas();
	 
	 Iterator<String> iteradorCategoriasEKanjis = categoriasEscolhidasEKanjisDelas.keySet().iterator();
	 while(iteradorCategoriasEKanjis.hasNext() == true)
	 {
		 String umaCategoria = iteradorCategoriasEKanjis.next();
		 LinkedList<KanjiTreinar> kanjisDaCategoria = categoriasEscolhidasEKanjisDelas.get(umaCategoria);
		 
		 for(int i = 0; i < kanjisDaCategoria.size(); i++)
		 {
			 this.kanjisQuePodemVirarCartas.add(kanjisDaCategoria.get(i));
		 }
	 }
 }

 /*se o usuario errou, ele espera 5 segundos sem clicar em nada*/
 private void realizarProcedimentoUsuarioErrouCarta()
 {
	 String mensagemClicouCartaErrada = getResources().getString(R.string.mensagem_clicou_carta_errada);
	 Toast t = Toast.makeText(this, mensagemClicouCartaErrada, Toast.LENGTH_LONG);
	 t.show();
	 
	 ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 karuta1.setClickable(false);
	 karuta2.setClickable(false);
	 karuta3.setClickable(false);
	 karuta4.setClickable(false);
	 karuta5.setClickable(false);
	 karuta6.setClickable(false);
	 karuta7.setClickable(false);
	 karuta8.setClickable(false);
	 
	 karuta1.setAlpha(128);
	 karuta2.setAlpha(128);
	 karuta3.setAlpha(128);
	 karuta4.setAlpha(128);
	 karuta5.setAlpha(128);
	 karuta6.setAlpha(128);
	 karuta7.setAlpha(128);
	 karuta8.setAlpha(128);
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		                	terminouEsperaUsuarioErrouCarta();
		            }
		        });
		    }
		}, 5000);
 }
 
 private void terminouEsperaUsuarioErrouCarta()
 { 
	 ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 
		 if(kanjiNaoSeTornouDicaOuEhADica(umKanji) == false)
		 {
			 if(i == 0)
			 {
				 karuta1.setClickable(false);
			 }
			 else if(i == 1)
			 {
				 karuta2.setClickable(false);
			 }
			 else if(i == 2)
			 {
				 karuta3.setClickable(false);
			 }
			 else if(i == 3)
			 {
				 karuta4.setClickable(false);
			 }
			 else if(i == 4)
			 {
				 karuta5.setClickable(false);
			 }
			 else if(i == 5)
			 {
				 karuta6.setClickable(false);
			 }
			 else if(i == 6)
			 {
				 karuta7.setClickable(false);
			 }
			 else if(i == 7)
			 {
				 karuta8.setClickable(false);
			 }
		 }
		 else
		 {
			 if(i == 0)
			 {
				 karuta1.setClickable(true);
			 }
			 else if(i == 1)
			 {
				 karuta2.setClickable(true);
			 }
			 else if(i == 2)
			 {
				 karuta3.setClickable(true);
			 }
			 else if(i == 3)
			 {
				 karuta4.setClickable(true);
			 }
			 else if(i == 4)
			 {
				 karuta5.setClickable(true);
			 }
			 else if(i == 5)
			 {
				 karuta6.setClickable(true);
			 }
			 else if(i == 6)
			 {
				 karuta7.setClickable(true);
			 }
			 else if(i == 7)
			 {
				 karuta8.setClickable(true);
			 }
		 }
	 }
	 
	 karuta1.setAlpha(255);
	 karuta2.setAlpha(255);
	 karuta3.setAlpha(255);
	 karuta4.setAlpha(255);
	 karuta5.setAlpha(255);
	 karuta6.setAlpha(255);
	 karuta7.setAlpha(255);
	 karuta8.setAlpha(255);
	 
 }
 
 /*retorna true se o kanji nao se tornou dica ou se ele eh a propria dica*/
 private boolean kanjiNaoSeTornouDicaOuEhADica(KanjiTreinar kanji)
 {
	 String categoriaKanji = kanji.getCategoriaAssociada();
	 String nomeKanji = kanji.getKanji();
	 
	 if((this.kanjiDaDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0) &&
		(this.kanjiDaDica.getKanji().compareTo(nomeKanji) == 0))
	 {
		 //eh o kanji da dica
		 return true;
	 }
	 else
	 {
		 boolean jaVirouDica = false;
		 
		 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
		 {
			 KanjiTreinar kanjiJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
			 
			 if((kanjiJaVirouDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0) &&
				(kanjiJaVirouDica.getKanji().compareTo(nomeKanji) == 0))
			{
				jaVirouDica = true;
				break;
			}
		 }
		 
		 if(jaVirouDica == false)
		 {
			 return true;
		 }
		 else
		 {
			 return false;
		 }
	 }
 }
 
 private void verificarSeJogadorRecebeUmItemAleatorio()
 {
	 if(this.quantasCartasJaSairamDoJogo % 3 == 0)
	 {
		 //de 3 em 3 cartas que saem, um item aleatorio eh gerado
		 if(this.itensAtuais == null || this.itensAtuais.size() == 0)
		 {
			 //o jogador estah sem itens. Vamos dar um p ele
			 String itemAGanhar = "";
			 if(this.pontuacaoDoAdversario > this.suaPontuacao)
			 {
				 //voce estah perdendo.
				 Random geraNumeroAleatorio = new Random();
				 int indiceSeuItem = geraNumeroAleatorio.nextInt(this.itensDoPerdedor.size());
				 itemAGanhar = this.itensDoPerdedor.get(indiceSeuItem);
				 this.itensAtuais.add(itemAGanhar);
			 }
			 else
			 {
				 //voce estah ganhando ou esta empatado
				 Random geraNumeroAleatorio = new Random();
				 int indiceSeuItem = geraNumeroAleatorio.nextInt(this.itensDoGanhador.size());
				 itemAGanhar = this.itensDoGanhador.get(indiceSeuItem);
				 this.itensAtuais.add(itemAGanhar);
			 }
			 
			 this.mudarImagemItemDeAcordoComOItemAdquirido(itemAGanhar);
		 }
	 }
 }
 
 private void mudarImagemItemDeAcordoComOItemAdquirido(String itemAdquirido)
 { 
	 if(itemAdquirido.compareTo("trovaotiracartaaleatoria") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item1);
		 imagemItem.setImageResource(R.drawable.trovaotiracartaaleatoria);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("parartempo") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item2);
		 imagemItem.setImageResource(R.drawable.parartempo);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("misturarcartas") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item3);
		 imagemItem.setImageResource(R.drawable.misturarcartas);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("mudardica") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item4);
		 imagemItem.setImageResource(R.drawable.mudardica);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("doisx") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item5);
		 imagemItem.setImageResource(R.drawable.doisx);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("naoesperemais") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item6);
		 imagemItem.setImageResource(R.drawable.naoesperemais);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("cartasdouradas") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item7);
		 imagemItem.setImageResource(R.drawable.cartasdouradas);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("trovaotiracarta") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item8);
		 imagemItem.setImageResource(R.drawable.trovaotiracarta);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
	 else if(itemAdquirido.compareTo("revivecarta") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item9);
		 imagemItem.setImageResource(R.drawable.revivecarta);
		 imagemItem.setClickable(true); //agora que o usuario possui um item, a imagem eh clicavel
	 }
 }
 
 private void usarItemAtual(String nomeItemUsado)
 {
	 if(nomeItemUsado.compareTo("trovaotiracartaaleatoria") == 0)
	 {
		 if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == this.kanjisDasCartasNaTela.size())
		 {
			 //nao existe nenhum kanji que o trovao possa atacar porque soh existe aquele kanji que eh a dica
			 String mensagemTrovaoAcertaNada = getResources().getString(R.string.mensagem_trovao_carta_aleatoria_nao_acerta_nada);
			 Toast t = Toast.makeText(this, mensagemTrovaoAcertaNada , Toast.LENGTH_LONG);
			 t.show();
		 }
		 else
		 {
			 Random geraNumeroAleatorio = new Random();
			 boolean achouUmKanjiParaTirar = false;
			 int posicaoKanjiTirarEmKanjisDasCartasNaTela = -1;
			 
			 while(achouUmKanjiParaTirar == false && posicaoKanjiTirarEmKanjisDasCartasNaTela == -1)
			 {
				 int posicaoCartaAleatoria = geraNumeroAleatorio.nextInt(this.kanjisDasCartasNaTela.size());
				 KanjiTreinar kanjiDessaPosicao = this.kanjisDasCartasNaTela.get(posicaoCartaAleatoria);
				 
				 //peguei um kanji qualquer dos da tela. Mas sera que ele ja tinha virado dica antes?Se sim, nao posso escolhe-lo porque a carta dele ja ter sido tirada do jogo
				 boolean kanjiJaVirouDica = false;
				 
				 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
				 {
					 KanjiTreinar umKanjiQueJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
					 
					 if((kanjiDessaPosicao.getKanji().compareTo(umKanjiQueJaVirouDica.getKanji()) == 0) &&
					    (kanjiDessaPosicao.getCategoriaAssociada().compareTo(umKanjiQueJaVirouDica.getCategoriaAssociada()) == 0))
					 {
						 kanjiJaVirouDica = true;
						 break;
					 }
				 }
				 
				 if(kanjiJaVirouDica == false)
				 {
					 //achamos um kanji p remover
					 achouUmKanjiParaTirar = true;
					 posicaoKanjiTirarEmKanjisDasCartasNaTela = posicaoCartaAleatoria;
				 }
			 }
			 
			 this.lancarTrovaoNaTelaNaCartaDeIndice(posicaoKanjiTirarEmKanjisDasCartasNaTela);
			 
			 //falta avisar ao adversario p lancar um trovao na mesma carta
			 String mensagemLancarTrovao = "item trovaotiracartaaleatoria indiceCartaRemovida=" + posicaoKanjiTirarEmKanjisDasCartasNaTela;
			 this.mandarMensagemMultiplayer(mensagemLancarTrovao);
			 
			 ImageView imagemItem = (ImageView) findViewById(R.id.item1);
			 imagemItem.setImageResource(R.drawable.nenhumitem);
			 imagemItem.setClickable(false);
		 }
		 
	 }
	 else if(nomeItemUsado.compareTo("parartempo") == 0)
	 {
		 this.realizarProcedimentoPararTempo();
		 
		 String mensagemUsouItemPararTempo = "item parartempo";
		 this.mandarMensagemMultiplayer(mensagemUsouItemPararTempo);
		 
		 ImageView imagemItem = (ImageView) findViewById(R.id.item2);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 imagemItem.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("misturarcartas") == 0)
	 {
		 this.misturarCartasEAvisarAoAdversario();
		 ImageView imagemItem = (ImageView) findViewById(R.id.item3);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 imagemItem.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("mudardica") == 0)
	 {
		 this.usarItemMudarDica();
		 ImageView imagemItem = (ImageView) findViewById(R.id.item4);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 imagemItem.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("doisx") == 0)
	 {
		 super.reproduzirSfx("dois_x");
		 this.usou2x = true;
		 findViewById(R.id.doisxpequeno).setVisibility(View.VISIBLE);
		 ImageView imagemItem = (ImageView) findViewById(R.id.item5);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 imagemItem.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("naoesperemais") == 0)
	 {
		 this.usouNaoEspereMais = true;
		 findViewById(R.id.naoesperemais).setVisibility(View.VISIBLE);
		 ImageView imagemItem = (ImageView) findViewById(R.id.item6);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 imagemItem.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("cartasdouradas") == 0)
	 {
		 this.realizarProcedimentoFazerCartasFicaremDouradas();
		 super.reproduzirSfx("cartas_douradas");
		 ImageView imagemItem = (ImageView) findViewById(R.id.item7);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 imagemItem.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("trovaotiracarta") == 0)
	 {
		ImageView item = (ImageView) findViewById(R.id.item8);
		item.setImageResource(R.drawable.escolhaumacartatrovao);
		this.usouTrovaoTiraCarta = true;
		item.setClickable(false);
	 }
	 else if(nomeItemUsado.compareTo("revivecarta") == 0)
	 {
		 ImageView item = (ImageView) findViewById(R.id.item9);
		 item.setImageResource(R.drawable.escolhaumacartarevive);
		 this.usouReviveCarta = true;
		 
		 //falta tornar todas as cartas clicaveis novamente p o usuario escolher uma carta p reviver
		 findViewById(R.id.karuta1_imageview).setClickable(true);
		 findViewById(R.id.karuta2_imageview).setClickable(true);
		 findViewById(R.id.karuta3_imageview).setClickable(true);
		 findViewById(R.id.karuta4_imageview).setClickable(true);
		 findViewById(R.id.karuta5_imageview).setClickable(true);
		 findViewById(R.id.karuta6_imageview).setClickable(true);
		 findViewById(R.id.karuta7_imageview).setClickable(true);
		 findViewById(R.id.karuta8_imageview).setClickable(true);
		 
		 item.setClickable(false);
	 }
	 
	 //falta tirar o item da linkedlist
	 for(int i = 0; i < this.itensAtuais.size(); i++)
	 {
		 String umItem = this.itensAtuais.get(i);
		 if(umItem.compareTo(nomeItemUsado) == 0)
		 {
			 itensAtuais.remove(i);
			 break;
		 }
	 }
 }
 
 /*lanca um trovao que torna uma das cartas na tela inclicavel. O indice vai de 0 a 7*/
 private void lancarTrovaoNaTelaNaCartaDeIndice(int indiceCartaTrovoada)
 {
	 TextView textoCartaTirada = null;
	 
	 if(indiceCartaTrovoada == 0)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta1_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta1);
	 }
	 else if(indiceCartaTrovoada == 1)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta2_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta2);
	 }
	 else if(indiceCartaTrovoada == 2)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta3_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta3);
	 }
	 else if(indiceCartaTrovoada == 3)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta4_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta4);
	 }
	 else if(indiceCartaTrovoada == 4)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta5_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta5);
	 }
	 else if(indiceCartaTrovoada == 5)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta6_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta6);
	 }
	 else if(indiceCartaTrovoada == 6)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta7_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta7);
	 }
	 else if(indiceCartaTrovoada == 7)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta8_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta8);
	 }
	 
	 
	 final AnimationDrawable animacaoTrovaoAcertaCarta = new AnimationDrawable();
	 int idImagemKarutaTrovao1 = getResources().getIdentifier("karutatrovao1", "drawable", getPackageName());
	 int idImagemKarutaTrovao2 = getResources().getIdentifier("karutatrovao2", "drawable", getPackageName());
	 int idImagemKarutaTrovao3 = getResources().getIdentifier("karutatrovao3", "drawable", getPackageName());
	 int idImagemKarutaTrovao4 = getResources().getIdentifier("karutatrovao4", "drawable", getPackageName());
	 int idImagemKarutaTrovao5 = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao1), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao2), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao3), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao4), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao5), 10);
	 
	 animacaoTrovaoAcertaCarta.setOneShot(true);
	 cartaASerTirada.setImageDrawable(animacaoTrovaoAcertaCarta);
	 cartaASerTirada.post(new Runnable() {
		@Override
		public void run() {
			animacaoTrovaoAcertaCarta.start();
		}
	}); 
	 
	 super.reproduzirSfx("trovao");
	 
	cartaASerTirada.setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	textoCartaTirada.setText("");
	
	new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	fazerImageViewFicarEscuro(cartaASerTirada);
		            }
		        });
		    }
		}, 1000);
	
	KanjiTreinar kanjiRemovido = this.kanjisDasCartasNaTela.get(indiceCartaTrovoada);
	this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(kanjiRemovido);
	
	this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
	
	this.usouTrovaoTiraCarta = false;
	this.verificarSeJogadorRecebeUmItemAleatorio();
 }
 
 /*no caso de trovao carta nao aleatoria*/
 private void lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(int indiceCarta)
 {
	 this.lancarTrovaoNaTelaNaCartaDeIndice(indiceCarta);
	 
	 //falta avisar ao adversario p lancar um trovao na mesma carta
	 String mensagemLancarTrovao = "item trovaotiracarta indiceCartaRemovida=" + indiceCarta;
	 this.mandarMensagemMultiplayer(mensagemLancarTrovao);
 }
 
 private void realizarProcedimentoPararTempo()
 {
	 TextView textViewTempo = (TextView) findViewById(R.id.tempo);
	 textViewTempo.setTextColor(Color.RED);
	 findViewById(R.id.parartempopequeno).setVisibility(View.VISIBLE);
	 
	 this.tempoEstahParado = true;
	 
	 //as cartas nao vao ficar clicaveis por um tempo assim como quando um jogador erra uma carta
	 ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 karuta1.setClickable(false);
	 karuta2.setClickable(false);
	 karuta3.setClickable(false);
	 karuta4.setClickable(false);
	 karuta5.setClickable(false);
	 karuta6.setClickable(false);
	 karuta7.setClickable(false);
	 karuta8.setClickable(false);
	 
	 karuta1.setAlpha(128);
	 karuta2.setAlpha(128);
	 karuta3.setAlpha(128);
	 karuta4.setAlpha(128);
	 karuta5.setAlpha(128);
	 karuta6.setAlpha(128);
	 karuta7.setAlpha(128);
	 karuta8.setAlpha(128);
	 
	 super.reproduzirSfx("parar_tempo");
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	tempoEstahParado = false;
		            	TextView textViewTempo = (TextView) findViewById(R.id.tempo);
		           	 	textViewTempo.setTextColor(Color.BLACK);
		           	 	findViewById(R.id.parartempopequeno).setVisibility(View.INVISIBLE);
		                terminouEsperaUsuarioErrouCarta();
		            }
		        });
		    }
		}, 5000);
 }
 
 
 private void misturarCartasEAvisarAoAdversario()
 {
	 Collections.shuffle(this.kanjisDasCartasNaTela);
	 
	 final String[] kanjisEBarrasComCategoria = new String[this.kanjisDasCartasNaTela.size()]; //so preciso mudar as cartas na tela, mas a funcao recebe somente essa estrutura: au|cotidiano,me|corpo... 
	 
	 String mensagemParaOAdversario = "item misturarcartas kanjis="; //mensagem p alertar ao adversario a nova ordem dos kanjis
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 kanjisEBarrasComCategoria[i] = umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 mensagemParaOAdversario = mensagemParaOAdversario + umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 
	     mensagemParaOAdversario = mensagemParaOAdversario + ";"; //tanta faz se no item final tiver um ;, isso foi testado
	 }
	 
	 this.mandarMensagemMultiplayer(mensagemParaOAdversario);
	 
	 TextView textoKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	 textoKaruta1.setText("");
	 TextView textoKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	 textoKaruta2.setText("");
	 TextView textoKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	 textoKaruta3.setText("");
	 TextView textoKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	 textoKaruta4.setText("");
	 TextView textoKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	 textoKaruta5.setText("");
	 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	 textoKaruta6.setText("");
	 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	 textoKaruta7.setText("");
	 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	 textoKaruta8.setText("");
	 
	 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 
	 imageViewCarta1.setClickable(false);
	 imageViewCarta2.setClickable(false);
	 imageViewCarta3.setClickable(false);
	 imageViewCarta4.setClickable(false);
	 imageViewCarta5.setClickable(false);
	 imageViewCarta6.setClickable(false);
	 imageViewCarta7.setClickable(false);
	 imageViewCarta8.setClickable(false);
	 
	 final AnimationDrawable animacaoPoofCarta = new AnimationDrawable();
	 int idImagemKarutaPoof1 = getResources().getIdentifier("karutapoof1", "drawable", getPackageName());
	 int idImagemKarutaPoof2 = getResources().getIdentifier("karutapoof2", "drawable", getPackageName());
	 int idImagemKarutaPoof3 = getResources().getIdentifier("karutapoof3", "drawable", getPackageName());
	 int idImagemKarutaPoof4 = getResources().getIdentifier("karutapoof4", "drawable", getPackageName());
	 int idImagemKarutaVazia = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof1), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof2), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof3), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof4), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaVazia), 200);
	 
	 animacaoPoofCarta.setOneShot(true);
	 imageViewCarta1.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta2.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta3.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta4.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta5.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta6.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta7.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta8.setImageDrawable(animacaoPoofCarta);
	 
	 imageViewCarta1.post(new Runnable() {
		@Override
		public void run() {
			animacaoPoofCarta.start();
		}
	 	});
	 imageViewCarta2.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta3.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta4.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta5.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta6.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta7.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta8.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	mudarCartasNaTela(kanjisEBarrasComCategoria); //so irei realizar essa funcao apos a animacao ter sido realizada em cada uma das cartas
		            	
		            	if(kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		        		{
		            		//ANTES AQUI EM CIMA ESTAVA O KANJISDASCARTASDATELAQUESETORNARAMDICAS. FAZ SENTIDO? ACHO Q NAO
		        			//menos de 8 cartas, por isso algumas devem ficar vazias
		        			tornarORestoDasCartasNaTelaVazias();
		        		}
		            	
		            }
		        });
		    }
		}, 1200);
	 
 }
 
 /*funcao do usuario que recebeu a mensagem p mudar as cartas na tela.
  * As linkedlists tem o mesmo tamanho*/
 private void misturarCartasRecebeuCartasOutroUsuario(LinkedList<String> textoKanjisNovos, LinkedList<String> categoriasKanjisNovos)
 {
	 //primeiro iremos pegar os kanjis com base nas categorias e texto
	 LinkedList<KanjiTreinar> novosKanjis = new LinkedList<KanjiTreinar>();
	 
	 for(int i = 0; i < textoKanjisNovos.size(); i++)
	 {
		 String umTextoKanji = textoKanjisNovos.get(i);
		 String umaCategoria = categoriasKanjisNovos.get(i);
		 
		 for(int j = 0; j < this.kanjisDasCartasNaTela.size(); j++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(j);
			 
			 if(umKanji.getKanji().compareTo(umTextoKanji) == 0 && umKanji.getCategoriaAssociada().compareTo(umaCategoria) == 0)
			 {
				 novosKanjis.add(umKanji);
				 break;
			 }
		 }
	 }
	 
	 //agora podemos tornar a lista de kanjis das cartas na tela essa nova lista
	 this.kanjisDasCartasNaTela.clear();
	 
	 for(int k = 0; k < novosKanjis.size(); k++)
	 {
		 KanjiTreinar umKanji = novosKanjis.get(k);
		 this.kanjisDasCartasNaTela.add(umKanji);
	 }
	 
	 
	 //agora podemos comecar a mostrar a mistura das cartas na tela
	 final String[] kanjisEBarrasComCategoria = new String[this.kanjisDasCartasNaTela.size()]; //so preciso mudar as cartas na tela, mas a funcao recebe somente essa estrutura: au|cotidiano,me|corpo... 
	 
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 kanjisEBarrasComCategoria[i] = umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 
	 }
	 
	 
	 TextView textoKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	 textoKaruta1.setText("");
	 TextView textoKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	 textoKaruta2.setText("");
	 TextView textoKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	 textoKaruta3.setText("");
	 TextView textoKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	 textoKaruta4.setText("");
	 TextView textoKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	 textoKaruta5.setText("");
	 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	 textoKaruta6.setText("");
	 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	 textoKaruta7.setText("");
	 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	 textoKaruta8.setText("");
	 
	 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 
	 imageViewCarta1.setClickable(false);
	 imageViewCarta2.setClickable(false);
	 imageViewCarta3.setClickable(false);
	 imageViewCarta4.setClickable(false);
	 imageViewCarta5.setClickable(false);
	 imageViewCarta6.setClickable(false);
	 imageViewCarta7.setClickable(false);
	 imageViewCarta8.setClickable(false);
	 
	 final AnimationDrawable animacaoPoofCarta = new AnimationDrawable();
	 int idImagemKarutaPoof1 = getResources().getIdentifier("karutapoof1", "drawable", getPackageName());
	 int idImagemKarutaPoof2 = getResources().getIdentifier("karutapoof2", "drawable", getPackageName());
	 int idImagemKarutaPoof3 = getResources().getIdentifier("karutapoof3", "drawable", getPackageName());
	 int idImagemKarutaPoof4 = getResources().getIdentifier("karutapoof4", "drawable", getPackageName());
	 int idImagemKarutaVazia = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof1), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof2), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof3), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof4), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaVazia), 200);
	 
	 animacaoPoofCarta.setOneShot(true);
	 imageViewCarta1.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta2.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta3.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta4.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta5.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta6.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta7.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta8.setImageDrawable(animacaoPoofCarta);
	 
	 imageViewCarta1.post(new Runnable() {
		@Override
		public void run() {
			animacaoPoofCarta.start();
		}
	 	});
	 imageViewCarta2.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta3.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta4.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta5.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta6.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta7.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta8.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	mudarCartasNaTela(kanjisEBarrasComCategoria); //so irei realizar essa funcao apos a animacao ter sido realizada em cada uma das cartas
		            	if(kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		        		{
		            		//ANTES AQUI EM CIMA ESTAVA O KANJISDASCARTASDATELAQUESETORNARAMDICAS. FAZ SENTIDO? ACHO Q NAO
		        			//menos de 8 cartas, por isso algumas devem ficar vazias
		        			tornarORestoDasCartasNaTelaVazias();
		        		}
		            	
		            }
		        });
		    }
		}, 1200);
 }
 
 /*caso uma nova dica possa ser criada, a dica atual mudarah. */
 private void usarItemMudarDica()
 {
	 if(kanjisDasCartasNaTela.size() == kanjisDasCartasNaTelaQueJaSeTornaramDicas.size())
	 {
		 //sinto muito, mas nao ha dicas diferentes que possam ser geradas. A ultima dica ja esta presente
		 String mensagemErroMudarDica = getResources().getString(R.string.erro_mudar_dica);
		 Toast.makeText(this, mensagemErroMudarDica, Toast.LENGTH_SHORT).show();
	 }
	 else
	 { 
		 //acharemos uma nova dica que seja diferente da anterior
		 //primeiro, precisamos remover o kanji da dica da linkedlist chamada kanjisDasCartasNaTelaQueJaSeTornaramDicas
		 
		 this.tirarKanjiDicaAtualDeCartasQueJaViraramDicasEPalavrasJogadas();
		 
		 //agora vamos gerar um novo kanji da dica diferente da anterior
		 
		 LinkedList<KanjiTreinar> kanjisQueAindaNaoViraramDicas = new LinkedList<KanjiTreinar>();
		 
		 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
			 
			 boolean kanjiJaVirouDica = false;
			 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
			 {
				 KanjiTreinar umKanjiQueVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
				 
				 if(umKanjiQueVirouDica.getKanji().compareTo(umKanji.getKanji()) == 0)
				 {
					 kanjiJaVirouDica = true;
					 break;
				 }
			 }
			 
			 if(kanjiJaVirouDica == false 
					 && (this.ehMesmoKanji(umKanji, kanjiDaDica) == false))
			 {
				 //agora tb n queremos que esse kanji seja a antiga dica!!!!
				 kanjisQueAindaNaoViraramDicas.add(umKanji);
			 }
		 }
		 
		 Random geraNumAleatorio = new Random();
		 int tamanhoKanjisQueNaoViraramDicas = kanjisQueAindaNaoViraramDicas.size();
		 
		 int indiceKanjiDaDica = geraNumAleatorio.nextInt(tamanhoKanjisQueNaoViraramDicas);
		 
		 KanjiTreinar umKanji = kanjisQueAindaNaoViraramDicas.get(indiceKanjiDaDica);
		 this.kanjiDaDica = umKanji;
		 this.palavrasJogadas.add(kanjiDaDica);
		 
		 String mensagem = "item mudardica kanjiDaDica=" + this.kanjiDaDica.getKanji() + "|" + this.kanjiDaDica.getCategoriaAssociada();
		 this.mandarMensagemMultiplayer(mensagem);
		 
		 //this.alterarTextoDicaComBaseNoKanjiDaDica(); //vamos realizar um poof antes de mudar esse texto
		 
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(umKanji);
		 
		 this.realizarProcedimentoMudandoDicaAtual();
			 
	 }
 }
 
 private boolean ehMesmoKanji(KanjiTreinar k1, KanjiTreinar k2)
 {
	 if((k1.getKanji().compareTo(k2.getKanji()) == 0) && (k1.getCategoriaAssociada().compareTo(k2.getCategoriaAssociada()) == 0))
	 {
		 return true;
	 }
	 else
	 {
		 return false;
	 }
 }
 
 private void tirarKanjiDicaAtualDeCartasQueJaViraramDicasEPalavrasJogadas()
 {
	//primeiro, precisamos remover o kanji da dica da linkedlist chamada kanjisDasCartasNaTelaQueJaSeTornaramDicas
	 for(int g = 0; g < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); g++)
	 {
		 KanjiTreinar umKanjiJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(g);
		 
		 if((umKanjiJaVirouDica.getKanji().compareTo(kanjiDaDica.getKanji()) == 0) 
			 && (umKanjiJaVirouDica.getCategoriaAssociada().compareTo(kanjiDaDica.getCategoriaAssociada()) == 0))
		 {
			 //achamos o kanji da dica! Falta remove-lo
			 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.remove(g);
			 break;	 
		 } 
	 }
	 
	 //agora precisamos remove-lo das palavras jogadas tb
	 
	 for(int h = 0; h < this.palavrasJogadas.size(); h++)
	 {
		 KanjiTreinar umKanjiJaJogado = this.palavrasJogadas.get(h);
		 
		 if((umKanjiJaJogado.getKanji().compareTo(kanjiDaDica.getKanji()) == 0) 
			 && (umKanjiJaJogado.getCategoriaAssociada().compareTo(kanjiDaDica.getCategoriaAssociada()) == 0))
		 {
			 //achamos o kanji da dica! Falta remove-lo
			 this.palavrasJogadas.remove(h);
			 break;	 
		 } 
	 }
 }
 
 //a dica atual ficarah com "..." por alguns segundos ate mudar
 private void realizarProcedimentoMudandoDicaAtual()
 {
	 TextView textoDicaKanji = (TextView) findViewById(R.id.dica_kanji);
	 TextView textoDicaKanjiTraducao = (TextView) findViewById(R.id.dica_kanji_traducao);
	 String mensagemDicaMudando = getResources().getString(R.string.palavra_esta_mudando);
	 textoDicaKanji.setText(mensagemDicaMudando);
	 textoDicaKanjiTraducao.setText("");
	 
	 super.reproduzirSfx("mudar_dica");
	 
	 //durante essa espera, as cartas n devem ser clicaveis
	 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 
	 imageViewCarta1.setClickable(false);
	 imageViewCarta2.setClickable(false);
	 imageViewCarta3.setClickable(false);
	 imageViewCarta4.setClickable(false);
	 imageViewCarta5.setClickable(false);
	 imageViewCarta6.setClickable(false);
	 imageViewCarta7.setClickable(false);
	 imageViewCarta8.setClickable(false);
	 
	 final String[] kanjisEBarrasComCategoria = new String[this.kanjisDasCartasNaTela.size()]; //so preciso disso p tornar as cartas clicaveis novamente e somente aquelas que deveriam ser clicaveis
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 kanjisEBarrasComCategoria[i] = umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 
	 }
	 
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	mudarCartasNaTela(kanjisEBarrasComCategoria); //so irei realizar essa funcao apos a animacao ter sido realizada
		            	if(kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		        		{
		        			//menos de 8 cartas, por isso algumas devem ficar vazias
		        			tornarORestoDasCartasNaTelaVazias();
		        		}
		            	alterarTextoDicaComBaseNoKanjiDaDica();
		            }
		        });
		    }
		}, 3000);
 }
 
 /*o usuario usou o item para reviver uma carta. O numero da carta vai de 1 ate 8*/
 private void realizarProcedimentoReviverCarta(int numeroCarta, boolean ehUsuarioQueRecebeuMensagem)
 {
	 KanjiTreinar kanjiRevivido = this.kanjisDasCartasNaTela.get(numeroCarta - 1);
	 
	 //temos de tirar ele das cartas que ja viraram dicas
	 //mas o usuario pode dar uma de engracadinho e tentar reviver uma carta que ta viva!
	 //o usuario n pode reviver carta ja viva ou a da dicaatual
	 boolean cartaFoiRemovidaDaLinkedListTornaramDicas = false;
	 String textoCartaRevivida = "";
	 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
	 {
		 KanjiTreinar umKanjiQueJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
		 
		 if(this.ehMesmoKanji(umKanjiQueJaVirouDica, kanjiRevivido) == true &&
				 this.ehMesmoKanji(this.kanjiDaDica, kanjiRevivido) == false)
		 {
			 //achamos o kanji que era p reviver! Ele n pode ser o kanji da dica
			 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.remove(i);
			 textoCartaRevivida = umKanjiQueJaVirouDica.getKanji();
			 cartaFoiRemovidaDaLinkedListTornaramDicas = true;
			 break;
		 }
	 }
	 
	 if(cartaFoiRemovidaDaLinkedListTornaramDicas == false)
	 {
		 //dizer ao usuario que essa carta nao pode ser revivida. O usuario q recebeu a mensagem da carta ser revivida n deve receber esse informativo
		 if(ehUsuarioQueRecebeuMensagem == false)
		 {
			 String mensagemErroReviverCarta = getResources().getString(R.string.erro_reviver_carta);
			 TextView textViewFalaMascote = (TextView) findViewById(R.id.dica_kanji);
			 String fraseAtualMascote = String.valueOf(textViewFalaMascote.getText());
			 mascoteFalaFrasePor4Segundos(mensagemErroReviverCarta, fraseAtualMascote);
		 } 
	 }
	 else
	 {
		 //Falta mudar a figura da carta para nao ser mais a imagem de carta removida
		 ImageView imageViewCartaRevivida;
		 TextView textViewCartaRevivida;
		 int posicaoDaCartaRevivida = 0;
		 if(numeroCarta == 1)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta1_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta1);
			 posicaoDaCartaRevivida = 0;
		 }
		 else if(numeroCarta == 2)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta2_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta2);
			 posicaoDaCartaRevivida = 1;
		 }
		 else if(numeroCarta == 3)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta3_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta3);
			 posicaoDaCartaRevivida = 2;
		 }
		 else if(numeroCarta == 4)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta4_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta4);
			 posicaoDaCartaRevivida = 3;
		 }
		 else if(numeroCarta == 5)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta5_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta5);
			 posicaoDaCartaRevivida = 4;
		 }
		 else if(numeroCarta == 6)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta6_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta6);
			 posicaoDaCartaRevivida = 5;
		 }
		 else if(numeroCarta == 7)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta7_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta7);
			 posicaoDaCartaRevivida = 6;
		 }
		 else
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta8_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta8);
			 posicaoDaCartaRevivida = 7;
		 }
		
		 //falta realizar a animacao de carta sendo revivida
		 this.realizarAnimacaoCartaRevivida(imageViewCartaRevivida, textViewCartaRevivida, textoCartaRevivida,posicaoDaCartaRevivida);
	 } 
 }
 
 private void realizarAnimacaoCartaRevivida(ImageView imageViewCartaRevivida, TextView textViewCartaRevivida, String textoCartaRevivida, int posicaoDaCarta)
 {
	 final AnimationDrawable animacaoReviveCarta = new AnimationDrawable();
	 final TextView textViewCartaRevividaFinal = textViewCartaRevivida;
	 final ImageView imageViewCartaRevividaFinal = imageViewCartaRevivida;
	 final String textoCartaRevividaFinal = textoCartaRevivida; 
	 
	 int idImagemKarutaRevive1 = getResources().getIdentifier("karutarevive1", "drawable", getPackageName());
	 int idImagemKarutaRevive2 = getResources().getIdentifier("karutarevive2", "drawable", getPackageName());
	 int idImagemKarutaRevive3 = getResources().getIdentifier("karutarevive3", "drawable", getPackageName());
	 int idImagemKarutaVazia = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaRevive1), 200);
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaRevive2), 200);
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaRevive3), 200);
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaVazia), 200);
	 
	 animacaoReviveCarta.setOneShot(true);
	 imageViewCartaRevivida.setImageDrawable(animacaoReviveCarta);
	 
	 super.reproduzirSfx("reviver_carta");
	 
	 this.fazerCartaVoltarACorNormal(imageViewCartaRevividaFinal,posicaoDaCarta);
	 
	 imageViewCartaRevivida.post(new Runnable() {
		@Override
		public void run() {
			animacaoReviveCarta.start();
		}
	 	});
	 final int posicaoCartaFinal = posicaoDaCarta;
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		       		  //FALTA TORNAR A CARTA CLICAVEL E AS OUTRAS NAO. posso fazer isso com a funcao abaixo
		       		  terminouEsperaUsuarioErrouCarta();
		       		  fazerCartaVoltarACorNormal(imageViewCartaRevividaFinal,posicaoCartaFinal);
		       		  colocarTextoVerticalNaCarta(textViewCartaRevividaFinal, textoCartaRevividaFinal);
		            }
		        });
		    }
		}, 1000);
 }
 
 /*faz carta nessa posicao ficar proibida. Chamada quando o usuario erra uma carta e usou o item naoesperemais Posicao vai de 1 a 8*/
 private void proibirCartaNaPosicao(int posicao)
 {
	 this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(posicao - 1));
	 this.qualCartaEstaProibida = posicao;
	 ImageView imageViewCartaErrada;
	 TextView textViewCartaErrada;
	 
	 if(posicao == 1)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta1_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta1);
	 }
	 else if(posicao == 2)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta2_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta2);
	 }
	 else if(posicao == 3)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta3_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta3);
	 }
	 else if(posicao == 4)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta4_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta4);
	 }
	 else if(posicao == 5)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta5_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta5);
	 }
	 else if(posicao == 6)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta6_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta6);
	 }
	 else if(posicao == 7)
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta7_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta7);
	 }
	 else 
	 {
		 imageViewCartaErrada = (ImageView) findViewById(R.id.karuta8_imageview);
		 textViewCartaErrada = (TextView) findViewById(R.id.texto_karuta8);
	 }
	 
	 imageViewCartaErrada.setImageResource(R.drawable.karutavaziaproibida);
	 imageViewCartaErrada.setClickable(false);
	 imageViewCartaErrada.setAlpha(128);
	 textViewCartaErrada.setVisibility(View.INVISIBLE);
	 
	 findViewById(R.id.naoesperemais).setVisibility(View.INVISIBLE);
	 this.usouNaoEspereMais = false;
	 
	 super.reproduzirSfx("nao_espere_mais");
 }
 
 private void fazerCartaProibidaVoltarAoNormal()
 {
	 if(this.qualCartaEstaProibida != 0)
	 {
		 //existe uma carta proibida
		 ImageView imageViewCartaProibida;
		 TextView textViewCartaProibida;
		 if(this.qualCartaEstaProibida == 1)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta1_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta1);
		 }
		 else if(this.qualCartaEstaProibida == 2)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta2_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta2);
		 }
		 else if(this.qualCartaEstaProibida == 3)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta3_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta3);
		 }
		 else if(this.qualCartaEstaProibida == 4)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta4_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta4);
		 }
		 else if(this.qualCartaEstaProibida == 5)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta5_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta5);
		 }
		 else if(this.qualCartaEstaProibida == 6)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta6_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta6);
		 }
		 else if(this.qualCartaEstaProibida == 7)
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta7_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta7);
		 }
		 else
		 {
			 imageViewCartaProibida = (ImageView) findViewById(R.id.karuta8_imageview);
			 textViewCartaProibida = (TextView) findViewById(R.id.texto_karuta8);
		 }
		 
		 imageViewCartaProibida.setClickable(true);
		 imageViewCartaProibida.setAlpha(255);
		 this.colorirBordaDaCartaDeAcordoComCategoria(this.qualCartaEstaProibida - 1);
		 
		 textViewCartaProibida.setVisibility(View.VISIBLE);
		 this.qualCartaEstaProibida = 0;
	 }
 }
 
 private void fazerCartasDouradasVoltaremAoNormal()
 {
	 //quando alguem usa cartas douradas e o adversario eh quem acerta a carta, as douradas viram normais,
	 //mas uma das douradas era a da dica antiga, certo? Ela n deveria virar normal, ela deveria virar o verso.
	 //Por isso, essa funcao eh chamada antes de a carta com a dica atual mudar e se achar a carta da dica, deve fazer ela virar verso
	 if(this.quaisCartasEstaoDouradas.size() > 0)
	 {
		 for(int i = 0; i < this.quaisCartasEstaoDouradas.size(); i++)
		 {
			 int posicaoUmaCartaDourada = this.quaisCartasEstaoDouradas.get(i);
			 
			 ImageView imageViewCartaVoltaAoNormal;
			 
			 if(posicaoUmaCartaDourada == 1)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta1_imageview);
			 }
			 else if(posicaoUmaCartaDourada == 2)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta2_imageview);
			 }
			 else if(posicaoUmaCartaDourada == 3)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta3_imageview);
			 }
			 else if(posicaoUmaCartaDourada == 4)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta4_imageview);
			 }
			 else if(posicaoUmaCartaDourada == 5)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta5_imageview);
			 }
			 else if(posicaoUmaCartaDourada == 6)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta6_imageview);
			 }
			 else if(posicaoUmaCartaDourada == 7)
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta7_imageview);
			 }
			 else
			 {
				 imageViewCartaVoltaAoNormal = (ImageView) findViewById(R.id.karuta8_imageview);
			 }
			 
			 KanjiTreinar kanjiDaCartaDourada = this.kanjisDasCartasNaTela.get(posicaoUmaCartaDourada - 1);
			 if((this.kanjiDaDica != null) &&
					 (kanjiDaCartaDourada.getKanji().compareTo(this.kanjiDaDica.getKanji()) == 0) && 
					 (kanjiDaCartaDourada.getCategoriaAssociada().compareTo(this.kanjiDaDica.getCategoriaAssociada()) == 0))
			 {
				 //era p a carta ficar verso, nao normal
				 imageViewCartaVoltaAoNormal.setImageResource(R.drawable.karutaverso);
			 }
			 else
			 {
				 this.colorirBordaDaCartaDeAcordoComCategoria(posicaoUmaCartaDourada - 1);
			 }
		 }
		 this.quaisCartasEstaoDouradas.clear();
	 }
 }
 
 //o usuario usou o item das cartas douradas. 4 cartas ou menos ficarao com cor diferente
 private void realizarProcedimentoFazerCartasFicaremDouradas()
 {
	 //primeiro, vamos pegar as posicoes de todas cartas cartas que ainda estao no jogo e a dica
	 //essas posicoes sao referentes a linkedlist kanjisdascartasnatela
	 LinkedList<KanjiTreinar> kanjisNaoSairamDoJogo = new LinkedList<KanjiTreinar>();
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanjiCartaNaTela = this.kanjisDasCartasNaTela.get(i);
		 
		 if(this.ehMesmoKanji(umKanjiCartaNaTela,this.kanjiDaDica) == true)
		 {
			 //eh o kanji da dica
			 kanjisNaoSairamDoJogo.add(umKanjiCartaNaTela);
		 }
		 else
		 {
			 boolean cartaJaSaiuDoJogo = false;
			 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
			 {
				 KanjiTreinar umKanjiCartaJaVirouDica = 
						 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
				 if(this.ehMesmoKanji(umKanjiCartaJaVirouDica, umKanjiCartaNaTela) == true)
				 {
					 cartaJaSaiuDoJogo = true;
					 break;
				 }
			 }
	         
			 if(cartaJaSaiuDoJogo == false)
			 {
				 kanjisNaoSairamDoJogo.add(umKanjiCartaNaTela);
			 }
		 }
	 }
	 
	 //agora dependendo de quantas posicoes foram encontradas, faremos algumas cartas serem douradas
	 int quantasCartasDouradasDevemSerCriadas = 0;
	 if(kanjisNaoSairamDoJogo.size() == 1)
	 {
		 quantasCartasDouradasDevemSerCriadas = 1;
	 }
	 else if(kanjisNaoSairamDoJogo.size() == 2)
	 {
		 quantasCartasDouradasDevemSerCriadas = 2;
	 }
	 else if(kanjisNaoSairamDoJogo.size() == 3)
	 {
		 quantasCartasDouradasDevemSerCriadas = 3;
	 }
	 else if(kanjisNaoSairamDoJogo.size() >= 4)
	 {
		 quantasCartasDouradasDevemSerCriadas = 4;
	 }
	 else
	 {
		 //nao vai acontecer essa situacao, mas para previnir...
		 quantasCartasDouradasDevemSerCriadas = 0;
	 }
	 
	 for(int k = 0; k < quantasCartasDouradasDevemSerCriadas; k++)
	 {
		 Random geraNumAleatorio = new Random();
		 int posicaoKanji = geraNumAleatorio.nextInt(kanjisNaoSairamDoJogo.size());
		 KanjiTreinar kanjiCartaVaiVirarDourada = kanjisNaoSairamDoJogo.remove(posicaoKanji);
		 
		 //agora vamos achar essa carta dourada e pinta-la!
		 for(int l = 0; l < this.kanjisDasCartasNaTela.size(); l++)
		 {
			 KanjiTreinar umKanjiCartaNaTela = this.kanjisDasCartasNaTela.get(l);
			 
			 if(this.ehMesmoKanji(umKanjiCartaNaTela,kanjiCartaVaiVirarDourada) == true)
			 {
				 //achamos a carta que vai ser dourada
				 ImageView imageViewCartaDourada;
				 if(l == 0)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta1_imageview);
					 this.quaisCartasEstaoDouradas.add(1);
				 }
				 else if(l == 1)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta2_imageview);
					 this.quaisCartasEstaoDouradas.add(2);
				 }
				 else if(l == 2)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta3_imageview);
					 this.quaisCartasEstaoDouradas.add(3);
				 }
				 else if(l == 3)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta4_imageview);
					 this.quaisCartasEstaoDouradas.add(4);
				 }
				 else if(l == 4)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta5_imageview);
					 this.quaisCartasEstaoDouradas.add(5);
				 }
				 else if(l == 5)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta6_imageview);
					 this.quaisCartasEstaoDouradas.add(6);
				 }
				 else if(l == 6)
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta7_imageview);
					 this.quaisCartasEstaoDouradas.add(7);
				 }
				 else
				 {
					 imageViewCartaDourada = (ImageView) findViewById(R.id.karuta8_imageview);
					 this.quaisCartasEstaoDouradas.add(8);
				 }
				 
				 imageViewCartaDourada.setImageResource(R.drawable.karutavaziadourada);
				 break;
			 }
		 } 
	 }
 }
 
 /*a mascote fala a frase por 4 segundos e depois volta a fala anterior*/
 private void mascoteFalaFrasePor4Segundos(String fraseFalarPor4Segundos, String fraseAnterior)
 {
	 final TextView textViewFalaMascote = (TextView) findViewById(R.id.dica_kanji);
	 textViewFalaMascote.setText(fraseFalarPor4Segundos);
	 
	 final String fraseAnteriorFinal = fraseAnterior;
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        ModoCasual.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	textViewFalaMascote.setText(fraseAnteriorFinal);
		            }
		        });
		    }
		}, 4000);
 }
 
 @Override
 public boolean jogadorEhHost() {
 	if(this.criouUmaSala == true)
 	{
 		return true;
 	}
 	else
 	{
 		return false;
 	}
 	
 }

 public boolean oGuestTerminouDeCarregarListaDeCategorias() {
 	return guestTerminouDeCarregarListaDeCategorias;
 }
 
 private void terminarJogoEEnviarMesagemAoAdversario()
 {
	 this.jogoAcabou = true;
	 
	 try
	 {
		 this.mandarMensagemMultiplayer("fim de jogo");
		 switchToScreen(R.id.tela_fim_de_jogo);
		 comecarFimDeJogo();
	 }
	 catch(IllegalStateException E)
	 {
		 voltarAoMenuInicial(null);
	 }
 }
 
 private void comecarFimDeJogo()
 {
	 findViewById(R.id.textoPontuacaoAdversario).setVisibility(View.VISIBLE);
	 findViewById(R.id.textoSuaPontuacao).setVisibility(View.VISIBLE);
	 findViewById(R.id.tituloTelaFimDeJogo).setVisibility(View.VISIBLE);
	 findViewById(R.id.mensagens_chat).setVisibility(View.VISIBLE);
	 findViewById(R.id.sendBtn).setVisibility(View.VISIBLE);
	 findViewById(R.id.botao_menu_principal).setVisibility(View.VISIBLE);
	 findViewById(R.id.botao_revanche).setVisibility(View.VISIBLE);
	 findViewById(R.id.chatET).setVisibility(View.VISIBLE);
	 
	 TextView textoPontuacaoAdversario = (TextView) findViewById(R.id.textoPontuacaoAdversario);
	 String pontuacaoDoAdversario = getResources().getString(R.string.pontuacaoDoAdversario);
	 textoPontuacaoAdversario.setText(pontuacaoDoAdversario + String.valueOf(this.pontuacaoDoAdversario));
	 
	 TextView textoSuaPontuacao = (TextView) findViewById(R.id.textoSuaPontuacao);
	 String stringSuaPontuacao = getResources().getString(R.string.suaPontuacao);
	 textoSuaPontuacao.setText(stringSuaPontuacao + String.valueOf(this.suaPontuacao));
	 
	 this.mensagensChat = new ArrayList<String>();
	 
	 this.enviarDadosDaPartidaParaOLogDoUsuarioNoBancoDeDados();
	 
	 //falta adicionar o dinheiro que o usuario ganhou na partida
	 
	 int creditosAdicionarAoJogador = TransformaPontosEmCredito.converterPontosEmCredito(this.suaPontuacao);
	 DAOAcessaDinheiroDoJogador daoDinheiroJogador = ConcreteDAOAcessaDinheiroDoJogador.getInstance();
	 daoDinheiroJogador.adicionarCredito(creditosAdicionarAoJogador, this);
	 String textoGanhouCreditoNaPartida = getResources().getString(R.string.texto_ganhou) + " " + 
	 											 creditosAdicionarAoJogador + " " + getResources().getString(R.string.moeda_do_jogo) + " " + 
	 											 getResources().getString(R.string.texto_na_partida); 
	 Toast.makeText(this, textoGanhouCreditoNaPartida, Toast.LENGTH_SHORT).show();
	 
	 this.mudarMusicaDeFundo(R.raw.time_to_unwind);
 }
 

 private void setListAdapter() { //arraylist<string>
     ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, this.mensagensChat);
     ListView listViewChat = (ListView) findViewById(R.id.mensagens_chat);
     listViewChat.setAdapter(adapter);
   }

 /**
  * 
  * @param mensagem
  * @param adicionarNomeDoRemetente precisa complementar a mensagem com o nome do remetente ou nao...
  * @return a mensagem adicionada no chat.
  */
 private String adicionarMensagemNoChat(String mensagem, boolean adicionarNomeDoRemetente)
 {
 	String mensagemAdicionarNoChat = mensagem;
 	if(adicionarNomeDoRemetente == true)
 	{
 		//append na mensagem o nome do remetente
 		//String emailUsuario = this.emailUsuario.substring(0, 11);
 		mensagemAdicionarNoChat = this.emailUsuario + ":" + mensagem;
 	}
 	
 	this.mensagensChat.add(mensagemAdicionarNoChat);
 	setListAdapter();
 	return mensagemAdicionarNoChat;
 }

 private void avisarAoOponenteQueDigitouMensagem(String mensagemAdicionarNoChat)
 {
 	//mandar mensagem para oponente...
 	this.mandarMensagemMultiplayer("oponente falou no chat=" + mensagemAdicionarNoChat);
 }

 private void enviarSeuEmailParaOAdversario()
 {
	 this.mandarMensagemMultiplayer("email=" + this.emailUsuario);
 }
 
 private void enviarDadosDaPartidaParaOLogDoUsuarioNoBancoDeDados()
 {
	 //enviaremos as informacoes da partida num log que escreveremos para o usuário e salvaremos num servidor remoto
	 DadosPartidaParaOLog dadosPartida = new DadosPartidaParaOLog();
	 HashMap<String,LinkedList<KanjiTreinar>> categoriasEKanjis = SingletonGuardaDadosDaPartida.getInstance().getCategoriasEscolhidasEKanjisDelas();
	 Iterator<String> iteradorCategorias = categoriasEKanjis.keySet().iterator();
	 LinkedList<String> categorias = new LinkedList<String>();
	 while(iteradorCategorias.hasNext() == true)
	 {
		 categorias.add(iteradorCategorias.next());
	 }
	 
	 dadosPartida.setCategoria(categorias);
	 
	 Calendar c = Calendar.getInstance();
	 SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
	 String formattedDate = df.format(c.getTime());
	 dadosPartida.setData(formattedDate);
	 
	 dadosPartida.setEmail(this.emailUsuario);
	 dadosPartida.setJogoAssociado("karuta kanji");
	 
	 dadosPartida.setPalavrasAcertadas(this.palavrasAcertadas);
	 dadosPartida.setPalavrasErradas(this.palavrasErradas);
	 dadosPartida.setPalavrasJogadas(this.palavrasJogadas);
	 dadosPartida.setPontuacao(suaPontuacao);
	 
	 if(this.suaPontuacao > this.pontuacaoDoAdversario)
	 {
		 String ganhou = getResources().getString(R.string.ganhou);
		 dadosPartida.setVoceGanhouOuPerdeu(ganhou);
	 }
	 else if(this.suaPontuacao < this.pontuacaoDoAdversario)
	 {
		 String perdeu = getResources().getString(R.string.perdeu);
		 dadosPartida.setVoceGanhouOuPerdeu(perdeu);
	 }
	 else
	 {
		 String empatou = getResources().getString(R.string.empatou);
		 dadosPartida.setVoceGanhouOuPerdeu(empatou); 
	 }
	 
	 dadosPartida.seteMailAdversario(this.emailAdversario);
	 
	 EnviarDadosDaPartidaParaLogTask armazenaNoLog = new EnviarDadosDaPartidaParaLogTask();
	 armazenaNoLog.execute(dadosPartida);
 }
 
 public void voltarAoMenuInicial(View v)
 {
		try
		{
			/*this.leaveRoom();
			 Intent irMenuInicial =
						new Intent(TelaInicialMultiplayer.this, MainActivity.class);
			irMenuInicial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
			 startActivity(irMenuInicial);*/
			this.leaveRoom();
		}
		catch(Exception e)
		{
			String mensagemerro = e.getMessage();
			mensagemerro = mensagemerro + "";
		}
	
 }
 
 public void mandarMensagemChat(View v)
 {
	 EditText textfieldMensagemDigitada = (EditText) findViewById(R.id.chatET);
 	String mensagemDigitada = textfieldMensagemDigitada.getText().toString();
 	textfieldMensagemDigitada.setText("");
 	String mensagemAdicionadaAoChat = this.adicionarMensagemNoChat(mensagemDigitada, true);
 	this.avisarAoOponenteQueDigitouMensagem(mensagemAdicionadaAoChat);
 }
 
 /*foi percebido um bug onde no comeco da primeira rodada de qualquer partida o usuario ve todas as cartas iguais e a dica do kanji nao eh a correta.
  * Irei fazer o usuario esperar um pouco antes de comecar a partida p o bug n acontecer. A espera acaba quando ambos os jogadores ja tem a dica do kanji atualizada*/
 private void comecarEsperaDoUsuarioParaComecoDaPartida()
 {
	 this.loadingComecoDaPartida = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.iniciandoJogo), getResources().getString(R.string.por_favor_aguarde));
 }
 
 /*é o que eu uso para escurecer as cartas*/
 private void fazerImageViewFicarEscuro(ImageView imageView)
 {
	 //imageView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
	 imageView.setImageResource(R.drawable.karutaverso);
 }
 
 /*o imageview eh o imageview da carta e o qualcarta vai de 0 ate 7 e indica a posicao da carta*/
 private void fazerCartaVoltarACorNormal(ImageView imageView, int qualCarta)
 {
	 //imageView.setColorFilter(null);
	 //imageView.setAlpha(255);
	 this.colorirBordaDaCartaDeAcordoComCategoria(qualCarta);
	 //segunda tentativa caso o acima nao funcionar: imageView.setImageResource(R.drawable.karutavazia); mas tem de colorir carta segundo categoria
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

@Override
public void procedimentoConexaoFalhou() {
	// TODO Auto-generated method stub
	
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
}

/*indiceDaCarta vai de 0 ate 7*/
private void colorirBordaDaCartaDeAcordoComCategoria(int indiceDaCarta)
{
	ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
    ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
    ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
    ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
    ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
    ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
    ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
    ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
    
    
    	KanjiTreinar umKanjiCartaNaTela = this.kanjisDasCartasNaTela.get(indiceDaCarta);
    	ImageView cartaASerMudada;
    	
    	if(indiceDaCarta == 0)
    	{
    		cartaASerMudada = karuta1;
    	}
    	else if(indiceDaCarta == 1)
    	{
    		cartaASerMudada = karuta2;
    	}
    	else if(indiceDaCarta == 2)
    	{
    		cartaASerMudada = karuta3;
    	}
    	else if(indiceDaCarta == 3)
    	{
    		cartaASerMudada = karuta4;
    	}
    	else if(indiceDaCarta == 4)
    	{
    		cartaASerMudada = karuta5;
    	}
    	else if(indiceDaCarta == 5)
    	{
    		cartaASerMudada = karuta6;
    	}
    	else if(indiceDaCarta == 6)
    	{
    		cartaASerMudada = karuta7;
    	}
    	else
    	{
    		cartaASerMudada = karuta8;
    	}
    	
    	String categoria = umKanjiCartaNaTela.getCategoriaAssociada();
    	
    	if(categoria.compareTo("Cotidiano") == 0)
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziaamarelo);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziaazulescuro);
    	}
    	else if(categoria.compareTo("Lugar") == 0)
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziaazul);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziaazulescuro);
    	}
    	else if(categoria.compareTo("Natureza") == 0)
    	{
    		cartaASerMudada.setImageResource(R.drawable.karutavaziaverde);
    	}
    	else if(categoria.compareTo("Verbos") == 0)
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziacinza);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziamarrom);
    	}
    	else if(categoria.compareTo("Adjetivos") == 0)
    	{
    		cartaASerMudada.setImageResource(R.drawable.karutavaziamarrom);
    	}
    	else if(categoria.compareTo("Tempo") == 0)
    	{
    		cartaASerMudada.setImageResource(R.drawable.karutavazialaranja);
    	}
    	else if(categoria.compareTo("Supermercado") == 0)
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziaroxo);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziarosa);
    	}
    	else if(categoria.compareTo("Lazer") == 0)
    	{
    		cartaASerMudada.setImageResource(R.drawable.karutavaziarosa);
    	}
    	else if(categoria.compareTo("Educação") == 0)
    	{
    		cartaASerMudada.setImageResource(R.drawable.karutavaziaazulescuro);
    	}
    	else if(categoria.compareTo("Trabalho") == 0)
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziaverdeclaro);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziaverde);
    	}
    	else if(categoria.compareTo("Geografia") == 0)
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziaverdeescuro);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziaverde);
    	}
    	else
    	{
    		//cartaASerMudada.setImageResource(R.drawable.karutavaziavermelho);
    		cartaASerMudada.setImageResource(R.drawable.karutavaziarosa);
    	}
}


private void mostrarLobbyModoCasual() 
{
	this.criouUmaSala = false;
	this.naCriacaoDeSalaModoCasual = false;
	
	this.loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
	//threadFicaBuscandoSalasModoCasual = new ThreadExecutaBuscaSalasCasualTask(this, loadingSalasModoCasual);
	//threadFicaBuscandoSalasModoCasual.start();
	//BuscaSalasModoCasualTask procurarSalas = new BuscaSalasModoCasualTask(this.loadingSalasModoCasual, this);
	  //procurarSalas.execute("");
}

public LinkedList<SalaModoCasual> getSalasAbertas()
{	
	return this.salasAbertas;
}

 public void mostrarListaComSalasAposCarregar(LinkedList<SalaModoCasual> salasModoCasual, boolean mostrarAlerta) 
 {
	 if(this.salasAbertas != null && mostrarAlerta == true)
	 {
		 //alertar ao usuario que novas salas foram criadas!
		 
		 Animation animacaoPiscar = AnimationUtils.loadAnimation(this, R.anim.anim_piscar_alerta_salas);
		 final TextView textoAlertaNovasSalas = (TextView) findViewById(R.id.alerta_salas_novas);
		 textoAlertaNovasSalas.setVisibility(View.VISIBLE);
		 textoAlertaNovasSalas.startAnimation(animacaoPiscar); 
		 
		//FALTA CRIAR UMA THREAD P DEPOIS DE 2 SEGUNDOS ELA TORNAR A VIEW INVISIVEL NOVAMENTE
		 new Timer().schedule(new TimerTask() 
		 { 
			    @Override
			    public void run() 
			    {
			        //If you want to operate UI modifications, you must run ui stuff on UiThread.
			        ModoCasual.this.runOnUiThread(new Runnable() 
			        {
			            @Override
			            public void run() 
			            {
			            	textoAlertaNovasSalas.setVisibility(View.INVISIBLE);
			            }
			        });
			    }
			}, 3000);
	 }
	 
	 
	 /*AdapterListViewSalasCriadas adapterSalasAtivas = new AdapterListViewSalasCriadas
				(this, R.layout.item_lista_sala, salasCarregadasModoCasual, this);


		 // Assign adapter to ListView
		 ListView listViewSalas = (ListView) findViewById(R.id.lista_salas_abertas);
		 //Parcelable state = listViewSalas.onSaveInstanceState(); //pegar o estado da listview para quando ela atualizar o user nao voltar p o comeco da lista
		 
		 
		 listViewSalas.setAdapter(adapterSalasAtivas); 
		 
		 
		 //listViewSalas.onRestoreInstanceState(state); //voltar ao estado antes do novo adapter*/
	 
	 
	 ListView listViewSalas = (ListView) findViewById(R.id.lista_salas_abertas);
		int indiceObjetoDeCima = listViewSalas.getFirstVisiblePosition();
		int visiblePercent = getVisiblePercent(listViewSalas.getChildAt(0)); //pega o primeiro item VISIVEL da listview
		if(visiblePercent != 100)
		{
			//se o primeiro item visivel da listview nao estah 100% visivel, o primeiro item visivel deveria ser o proximo
			//antes tava dando bug por exemplo: O item mais acima atualmente visivel era soh 50% visivel.
			indiceObjetoDeCima = indiceObjetoDeCima + 1;
		}
		
		int novoIndiceObjetoDeCima = 0; 
		
		if(this.salasAbertas != null)
		{ 
			SalaModoCasual salaVistaPorUltimo = salasAbertas.get(indiceObjetoDeCima);
			//Toast.makeText(getApplicationContext(), "indiceObjetoDeCima=" + indiceObjetoDeCima, Toast.LENGTH_SHORT).show();
			this.salasAbertas = salasModoCasual;
			
			
			for(int y = 0; y < salasAbertas.size(); y++)
			{
				SalaModoCasual umaSalaAberta = salasAbertas.get(y);
				if(umaSalaAberta.getId_sala() == salaVistaPorUltimo.getId_sala())
				{
					novoIndiceObjetoDeCima = y;
					//Toast.makeText(getApplicationContext(), "novoIndiceObjetoDeCima=" + novoIndiceObjetoDeCima, Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
		else
		{
			this.salasAbertas = salasModoCasual;
		}
		
		
		//comecar adapter para listview
		final ArrayList<SalaModoCasual> salasCarregadasModoCasual = new ArrayList<SalaModoCasual>();
		for(int i = 0; i < salasAbertas.size(); i++)
		 {
			 salasCarregadasModoCasual.add(salasAbertas.get(i));
		 }  
	
		 
		AdapterListViewSalasCriadas adapterSalasAtivas = new AdapterListViewSalasCriadas
				(this, R.layout.item_lista_sala, salasCarregadasModoCasual, this);

		 // Assign adapter to ListView
		 //Parcelable state = listViewSalas.onSaveInstanceState();//pegar estado atual da listView
		 listViewSalas.setAdapter(adapterSalasAtivas); 
		 if(indiceObjetoDeCima != 0)
		 {
			 //se o usuario nao estava no comeco da lista. Caso ele estivesse no comeco, era melhor mostrar a nova sala
			 listViewSalas.setSelection(novoIndiceObjetoDeCima); 
		 }	 
		 
		 /*listViewSalas.setOnItemClickListener(new OnItemClickListener() {
			 public void onItemClick(AdapterView parent, View view,
					 int position, long id) 
			 {
				 salaAtual = salasCarregadasModoCasual.get(position);
				 int id_sala = salaAtual.getId_sala();
				 startQuickGame(id_sala);
				 
				 
			 }
		 });*/
		 
		 //em seguida, setar o conteudo do spinner de filtragem
		 Spinner spinnerFiltragem = (Spinner) findViewById(R.id.spinnerPesquisarSalasModoCasual);
		 String labelFiltroNenhum = getResources().getString(R.string.nada);
		 String labelFiltroCategoria = getResources().getString(R.string.categorias);
		 String labelFiltroRanking = getResources().getString(R.string.dan);
		 String labelFiltroUsername = getResources().getString(R.string.email);
		 List<String> filtrosDeSala = new ArrayList<String>();
		 filtrosDeSala.add(labelFiltroNenhum);
		 filtrosDeSala.add(labelFiltroCategoria);
		 filtrosDeSala.add(labelFiltroRanking);
		 filtrosDeSala.add(labelFiltroUsername);
		 
		 Spinner spinner = (Spinner) findViewById(R.id.spinnerPesquisarSalasModoCasual);
		 SpinnerAdapter adapterDoSpinner = spinner.getAdapter();
		 if(adapterDoSpinner == null)
		 {
			//Create an ArrayAdapter using the string array and a default spinner layout
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				     R.array.spinner_pesquisar_por_modo_casual, android.R.layout.simple_spinner_item);
				//Specify the layout to use when the list of choices appears
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				//Apply the adapter to the spinner
				spinner.setAdapter(adapter); 
				spinner.setOnItemSelectedListener(this);
		 }
 }
 
 /*quando o usuario clica no iconezinho da porta, ele deve entrar numa sala*/
 public void entrarNaSala(SalaModoCasual salaVaiEntrar)
 {
	 salaAtual = salaVaiEntrar;
	 int id_sala = salaAtual.getId_sala();
	 if(id_sala < 0)
	 {
			id_sala = -id_sala;
			salaAtual.setId_sala(id_sala);
	 }
	 startQuickGame(id_sala);
 }
 
 
 /*quando o usuario clica no iconezinho "C" em uma das salas, ele deve ver as categorias daquela sala*/
 public void abrirPopupMostrarCategoriasDeUmaSala(String[] categorias)
 {
	 final Dialog dialog = new Dialog(ModoCasual.this);
	 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
	  // Include dialog.xml file
	 dialog.setContentView(R.layout.popup_categorias_de_uma_sala_casual_da_lista);
	 
	 
	 for(int i = 0; i < categorias.length; i++)
	 {
		 ImageView imageViewQuadradoBase; //quadrado que fica por baixo dos icones das categorias
		 ImageView imageViewQuadradoCategoria;
		 if(i == 0)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado1base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado1categoria);
		 }
		 else if(i == 1)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado2base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado2categoria);
		 }
		 else if(i == 2)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado3base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado3categoria);
		 }
		 else if(i == 3)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado4base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado4categoria);
		 }
		 else if(i == 4)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado5base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado5categoria);
		 }
		 else if(i == 5)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado6base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado6categoria);
		 }
		 else if(i == 6)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado7base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado7categoria);
		 }
		 else if(i == 7)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado8base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado8categoria);
		 }
		 else if(i == 8)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado9base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado9categoria);
		 }
		 else if(i == 9)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado10base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado10categoria);
		 }
		 else if(i == 10)
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado11base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado11categoria);
		 }
		 else
		 {
			 imageViewQuadradoBase = (ImageView) dialog.findViewById(R.id.quadrado12base);
			 imageViewQuadradoCategoria = (ImageView) dialog.findViewById(R.id.quadrado12categoria);
		 }
		 
		 //vamos tornar o quadrado de fundo e o icone da categoriavisiveis
		 imageViewQuadradoBase.setVisibility(View.VISIBLE);
		 imageViewQuadradoCategoria.setVisibility(View.VISIBLE);
		 
		 //agora falta mudar o icone de acordo com a categoria
		 String umaCategoria = categorias[i];
		 int imageResourceIconeCategoria = RetornaIconeDaCategoriaParaTelasDeEscolha.retornarIdIconeDaCategoria(umaCategoria); 
		 //funcao acima eh so para pegar o icone da categoria com base no nome dela,tipo R.id.icone_cotidiano
		 
		 imageViewQuadradoCategoria.setImageResource(imageResourceIconeCategoria); 
	 }
	 
	 
	 dialog.show();
 }
 
 public static int getVisiblePercent(View v) {
     if (v != null && v.isShown()) {
         Rect r = new Rect();
         v.getGlobalVisibleRect(r);
         double sVisible = r.width() * r.height();
         double sTotal = v.getWidth() * v.getHeight();
         return (int) (100 * sVisible / sTotal);
     } else {
         return -1;
     }
 }
 
 public void abrirTelaCriarNovaSala(View v)
 {
	this.naCriacaoDeSalaModoCasual = true;
	switchToScreen(R.id.tela_escolha_categoria_criar_nova_sala);
	this.solicitarPorKanjisPraTreino();
 }
 

public void quemEscolheCategoriasClicouNoBotaoOkCriacaoDeSala(View v)
{
	 //primeiro iremos armazenar no singleton todas as categorias escolhidas e kanjis delas
	 ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList = this.dataAdapter.getCategoriaDeKanjiList();
	
	 if(categoriaDeKanjiList.size() == 0)
	 {
		 String mensagem = getResources().getString(R.string.erroEscolherCategorias);
		 Toast t = Toast.makeText(this, mensagem, Toast.LENGTH_LONG);
		 t.show();
	 }
	 else
	 {
		 
		 CriarSalaModoCasualTask armazenaNoBdNovaSala = new CriarSalaModoCasualTask(this);
		 SalaModoCasual novaSala = new SalaModoCasual();
		 
		 novaSala.setDanDoCriador("dan 1");
		 novaSala.setEmailDoCriador(emailUsuario);
		 
		 if(this.quantasRodadasHaverao > 3)
		 {
			 //o jogador escolheu infinitas rodadas
			 novaSala.setQuantasRodadas(getResources().getString(R.string.infinitas_rodadas_sem_mais_nada));
		 }
		 else
		 {
			 novaSala.setQuantasRodadas(String.valueOf(this.quantasRodadasHaverao)); 
		 }
		 
		 String categoriasSeparadasPorVirgula = "";
		 for(int i = 0; i < categoriaDeKanjiList.size(); i++)
		 {
			 CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
				if(umaCategoria.isSelected() == true)
				{
					String nomeCategoria = umaCategoria.getName();
					int posicaoParenteses = nomeCategoria.indexOf("(");
					String nomeCategoriaSemParenteses = nomeCategoria.substring(0, posicaoParenteses);
					
					
						categoriasSeparadasPorVirgula = 
								categoriasSeparadasPorVirgula + nomeCategoriaSemParenteses + ",";
				}
		 }
		 
		 
		 categoriasSeparadasPorVirgula = categoriasSeparadasPorVirgula.substring(0, categoriasSeparadasPorVirgula.length() - 1); //falta tirar a ultima virgula
		 
		 novaSala.setCategoriasJuntas(categoriasSeparadasPorVirgula);
		 
		 armazenaNoBdNovaSala.execute(novaSala);
		 this.salaAtual = novaSala;
		 
		 SingletonGuardaDadosDaPartida.getInstance().limparCategoriasEKanjis();
		 
		 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			for(int i = 0; i < categoriaDeKanjiList.size(); i++)
			{
				CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
				if(umaCategoria.isSelected() == true)
				{
					String nomeCategoria = umaCategoria.getName();
					int posicaoParenteses = nomeCategoria.indexOf("(");
					String nomeCategoriaSemParenteses = nomeCategoria.substring(0, posicaoParenteses);
					LinkedList<KanjiTreinar> kanjisDaCategoria = 
							conheceKanjisECategorias.getListaKanjisTreinar(nomeCategoriaSemParenteses);
					SingletonGuardaDadosDaPartida.getInstance().adicionarNovaCategoriaESeusKanjis(nomeCategoriaSemParenteses, kanjisDaCategoria);
					
				}
			}
			
		//Agora vamos armazenar quantas rodadas o jogo terah no singleton
		SingletonGuardaDadosDaPartida.getInstance().setQuantasRodadasHaverao(quantasRodadasHaverao);
		
	 }
}


public void iniciarAberturaDeSalaCasualComId(int id_sala)
{
	this.salaAtual.setId_sala(id_sala);
	int id_sala_criada = id_sala;
	startQuickGame(id_sala_criada);
	this.criouUmaSala = true;
}

public void recarregarSalas(View v)
{
	this.loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
	  BuscaSalasModoCasualTask procurarSalas = new BuscaSalasModoCasualTask(this.loadingSalasModoCasual, this);
	  procurarSalas.execute("");
}

@Override
public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
{
	String itemEscolhido = arg0.getItemAtPosition(arg2).toString();
	String categorias = getResources().getString(R.string.categorias);
	String duracao_da_partida = getResources().getString(R.string.duracao_da_partida);
	String dan = getResources().getString(R.string.dan);
	String email = getResources().getString(R.string.email);
	
	if(itemEscolhido.compareTo(categorias) == 0)
	{
		if(threadFicaBuscandoSalasModoCasual != null && threadFicaBuscandoSalasModoCasual.isAlive() == true)
		{
			threadFicaBuscandoSalasModoCasual.interrupt();
		}
		
		this.loadingSalasModoCasual = 
				ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_todas_as_categorias), getResources().getString(R.string.por_favor_aguarde));
		 
		PegaNomesDeTodasAsCategoriasTask pegaCategorias = new PegaNomesDeTodasAsCategoriasTask(this.loadingSalasModoCasual,this);
		pegaCategorias.execute("");
	}
	else if (itemEscolhido.compareTo(duracao_da_partida) == 0)
	{
		if(threadFicaBuscandoSalasModoCasual != null && threadFicaBuscandoSalasModoCasual.isAlive() == true)
		{
			threadFicaBuscandoSalasModoCasual.interrupt();
		}
		this.mostrarPopupPesquisarPorDuracao();
	}
	else if (itemEscolhido.compareTo(dan) == 0)
	{
		if(threadFicaBuscandoSalasModoCasual != null && threadFicaBuscandoSalasModoCasual.isAlive() == true)
		{
			threadFicaBuscandoSalasModoCasual.interrupt();
		}
		this.mostrarPopupPesquisarPorDan();
	}
	else if (itemEscolhido.compareTo(email) == 0)
	{
		if(threadFicaBuscandoSalasModoCasual != null && threadFicaBuscandoSalasModoCasual.isAlive() == true)
		{
			threadFicaBuscandoSalasModoCasual.interrupt();
		}
		this.mostrarPopupPesquisarPorEmail();
	}
	else
	{
		//usuario escolheu nenhum
		this.threadFicaBuscandoSalasModoCasual = new ThreadExecutaBuscaSalasCasualTask(this, this.loadingSalasModoCasual);
		this.threadFicaBuscandoSalasModoCasual.start();
	}
}

private void mostrarPopupPesquisarPorEmail()
{
	final Dialog dialog = new Dialog(ModoCasual.this);
	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	
    // Include dialog.xml file
    dialog.setContentView(R.layout.popup_escolha_email);
    
    //mudar fonte do titulo informe um email
    this.mudarFonteTituloETextoEBotaoOkInformeUmEmailPesquisaSalaModoCasual(dialog);
    
    final ModoCasual telaModoCasual = this;
    Button botaoOk = (Button) dialog.findViewById(R.id.confirmar_pesquisa_email);
    botaoOk.setOnClickListener(new Button.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  EditText editTextInformeEmailPesquisarSalasModoCasual = (EditText) dialog.findViewById(R.id.edit_text_informe_email_pesquisar_salas_modo_casual);
			  String email = editTextInformeEmailPesquisarSalasModoCasual.getText().toString();
				loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
				BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
						new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
				taskBuscaSalasModoCasual.execute("email",email);
	      }	  
	  });
    
    dialog.show();
}

private void mostrarPopupPesquisarPorDuracao()
{
	String umaRodada = getResources().getString(R.string.uma_rodada);
	String duasRodadas = getResources().getString(R.string.duas_rodadas);
	String tresRodadas = getResources().getString(R.string.tres_rodadas);
	String infinitasRodadas = getResources().getString(R.string.infinitas_rodadas);
	final String[] arrayDuracao = {umaRodada,duasRodadas,tresRodadas,infinitasRodadas};
	
	Integer[] imageId = new Integer[4];
	imageId[0] = R.drawable.mascote_feliz_pequena;
	imageId[1] = R.drawable.macote_normal_pequena;
	imageId[2] = R.drawable.mascote_assustada_pequena;
	imageId[3] = R.drawable.mascote_zangada_pequena;

	
    // arraylist to keep the selected items
   
	final Dialog dialog = new Dialog(ModoCasual.this);
	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	
	
    // Include dialog.xml file
    dialog.setContentView(R.layout.popup_escolha_duracao);
	
	//definindo fontes para os textos dessa tela...
	this.mudarFonteTituloInformeADuracaoPesquisaSalaModoCasual(dialog);
	Typeface typeFaceFonteTextoListViewIconeETexto = this.escolherFonteDoTextoListViewIconeETexto();
	
	final ModoCasual telaModoCasual = this;
	
	AdapterListViewIconeETexto adapter = new AdapterListViewIconeETexto(ModoCasual.this, arrayDuracao, imageId,typeFaceFonteTextoListViewIconeETexto,false,false,true);
	    ListView list=(ListView)dialog.findViewById(R.id.listaDuracaoPesquisaSalas);
	    
	        list.setAdapter(adapter);
	        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	                @Override
	                public void onItemClick(AdapterView<?> parent, View view,
	                                        int position, long id) 
	                {
	                	switch(position)
	                    {
	                        case 0:
	                                // Your code when first option seletced
	                        	dialog.dismiss();
	                        	loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
	                    		BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
	                    				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
	                    		taskBuscaSalasModoCasual.execute("duracao",String.valueOf(1));
	                                break;
	                        case 1:
	                                // Your code when 2nd  option seletced
	                        	dialog.dismiss();
	                        	loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
	                    		BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual2 =
	                    				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
	                    		taskBuscaSalasModoCasual2.execute("duracao",String.valueOf(2));
	                                break;   
	                        case 2:
	                               // Your code when 3rd option seletced
	                        	dialog.dismiss();
	                        	loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
	                    		BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual3 =
	                    				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
	                    		taskBuscaSalasModoCasual3.execute("duracao",String.valueOf(3));
	                                break;
	                        case 3:
	                                 // Your code when 4th  option seletced
	                        	dialog.dismiss();
	                        	String infinitasRodadas = getResources().getString(R.string.infinitas_rodadas_sem_mais_nada);
	                        	loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
	                    		BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual4 =
	                    				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
	                    		taskBuscaSalasModoCasual4.execute("duracao",infinitasRodadas);
	                                break;
	                    }
	                }
	            });


	  /*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	  lp.copyFrom(dialog.getWindow().getAttributes());
	  lp.width = 400;
	  lp.height = 400;*/
	  dialog.show();
	  //dialog.getWindow().setAttributes(lp);
	
}

private void mostrarPopupPesquisarPorDan()
{
	final Dialog dialog = new Dialog(ModoCasual.this);
	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	
    // Include dialog.xml file
    dialog.setContentView(R.layout.popup_escolha_dan);
    
    ImageView inicianteImageView = (ImageView) dialog.findViewById(R.id.iniciante);
    ImageView intermediarioImageView = (ImageView) dialog.findViewById(R.id.intermediario);
    ImageView dan1ImageView = (ImageView) dialog.findViewById(R.id.dan1);
    ImageView dan2ImageView = (ImageView) dialog.findViewById(R.id.dan2);
    ImageView dan3ImageView = (ImageView) dialog.findViewById(R.id.dan3);
    ImageView dan4ImageView = (ImageView) dialog.findViewById(R.id.dan4);
    ImageView dan5ImageView = (ImageView) dialog.findViewById(R.id.dan5);
    ImageView dan6ImageView = (ImageView) dialog.findViewById(R.id.dan6);
    ImageView dan7ImageView = (ImageView) dialog.findViewById(R.id.dan7);
    ImageView dan8ImageView = (ImageView) dialog.findViewById(R.id.dan8);
    ImageView dan9ImageView = (ImageView) dialog.findViewById(R.id.dan9);
    ImageView dan10ImageView = (ImageView) dialog.findViewById(R.id.dan10);
    
    inicianteImageView.setClickable(true);
    intermediarioImageView.setClickable(true);
    dan1ImageView.setClickable(true);
    dan2ImageView.setClickable(true);
    dan3ImageView.setClickable(true);
    dan4ImageView.setClickable(true);
    dan5ImageView.setClickable(true);
    dan6ImageView.setClickable(true);
    dan7ImageView.setClickable(true);
    dan8ImageView.setClickable(true);
    dan9ImageView.setClickable(true);
    dan10ImageView.setClickable(true);
    
    final String iniciante = getResources().getString(R.string.iniciante);
	final String intermediario = getResources().getString(R.string.intermediario);
	final String dan1 = getResources().getString(R.string.dan1);
	final String dan2 = getResources().getString(R.string.dan2);
	final String dan3 = getResources().getString(R.string.dan3);
	final String dan4 = getResources().getString(R.string.dan4);
	final String dan5 = getResources().getString(R.string.dan5);
	final String dan6 = getResources().getString(R.string.dan6);
	final String dan7 = getResources().getString(R.string.dan7);
	final String dan8 = getResources().getString(R.string.dan8);
	final String dan9 = getResources().getString(R.string.dan9);
	final String dan10 = getResources().getString(R.string.dan10);
    
	this.mudarFonteTituloInformeUmDanPesquisaSalaModoCasual(dialog);
	
    final ModoCasual telaModoCasual = this;
    
    inicianteImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
      			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
      				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
      		taskBuscaSalasModoCasual.execute("dan",iniciante);
	      }	  
	  });
    intermediarioImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
    			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
    				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
    		taskBuscaSalasModoCasual.execute("dan",intermediario);
	      }	  
	  });
    dan1ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
  			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
  				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
  		taskBuscaSalasModoCasual.execute("dan",dan1);
	      }	  
	  });
    dan2ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan2);
	      }	  
	  });
    dan3ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan3);
	      }	  
	  });
    dan4ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan4);
	      }	  
	  });
    dan5ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan5);
	      }	  
	  });
    dan6ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan6);
	      }	  
	  });
    dan7ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan7);
	      }	  
	  });
    dan8ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan8);
	      }	  
	  });
    dan9ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan9);
	      }	  
	  });
    dan10ImageView.setOnClickListener(new ImageView.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
			BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual =
				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
		taskBuscaSalasModoCasual.execute("dan",dan10);
	      }	  
	  });
    
    dialog.show();
    
    
}

//o metodo abaixo so eh chamado pela task PegarNomesDeTodasAsCategoriasTask
public void mostrarPopupPesquisarPorCategorias(final LinkedList<String> categorias)
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

	
    // arraylist to keep the selected items
   
	final Dialog dialog = new Dialog(ModoCasual.this);
	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	//vou tirar a linha que divide o titulo e o conteudo do dialog
	/*int divierId = dialog.getContext().getResources()
            .getIdentifier("android:id/titleDivider", null, null);
	View divider = dialog.findViewById(divierId);
	divider.setBackgroundColor(Color.WHITE);*/
	
	
    // Include dialog.xml file
    dialog.setContentView(R.layout.popup_escolha_categorias);
    
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
	
	//definindo fontes para os textos dessa tela...
	this.mudarFonteTituloEBotaoOkInformeAsCategoriasPesquisaSalaModoCasual(dialog);
	Typeface typeFaceFonteTextoListViewIconeETexto = this.escolherFonteDoTextoListViewIconeETexto();
	
	AdapterListViewIconeETexto adapter = new AdapterListViewIconeETexto(ModoCasual.this, arrayCategorias, imageId,typeFaceFonteTextoListViewIconeETexto,true,true,true);
	    ListView list=(ListView)dialog.findViewById(R.id.listaCategoriasPesquisaSalas1);
	    
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
	                		textView.setTextColor(Color.argb(alpha, 107, 6, 116));
	                    }
	                    else
	                    {
	                    	categoriaEstahSelecionada[position] = false;
	                    	ImageView imageView = (ImageView) view.findViewById(R.id.img);
	                    	imageView.setAlpha(70);
	                    	TextView textView = (TextView) view.findViewById(R.id.txt);
	                    	int alpha = 70;
	                		textView.setTextColor(Color.argb(alpha, 107, 6, 116));
	                    }
	                }
	            });
	        
	        
	        AdapterListViewIconeETexto adapter2 = new AdapterListViewIconeETexto(ModoCasual.this, arrayCategorias2, imageId2,typeFaceFonteTextoListViewIconeETexto,true,true,true);
		    ListView list2=(ListView)dialog.findViewById(R.id.listaCategoriasPesquisaSalas2);
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
		                		textView.setTextColor(Color.argb(alpha, 107, 6, 116));
		                    }
		                    else
		                    {
		                    	categoriaEstahSelecionada2[position] = false;
		                    	ImageView imageView = (ImageView) view.findViewById(R.id.img);
		                    	imageView.setAlpha(70);
		                    	TextView textView = (TextView) view.findViewById(R.id.txt);
		                    	int alpha = 70;
		                		textView.setTextColor(Color.argb(alpha, 107, 6, 116));
		                    }
		                }
		            });

    //this.popupPesquisarSalaPorCategoria = builder.create();//AlertDialog dialog; create like this outside onClick
	  dialog.show();
	        
	//falta definir a ação para o botão ok desse popup das categorias
	  Button botaoOk = (Button) dialog.findViewById(R.id.confirmar_escolha_categorias);
	  final ModoCasual telaModoCasual = this;
	  botaoOk.setOnClickListener(new Button.OnClickListener() 
	  {
		  public void onClick(View v) 
	      {
			  dialog.dismiss();
			  loadingSalasModoCasual = ProgressDialog.show(ModoCasual.this, getResources().getString(R.string.carregando_salas), getResources().getString(R.string.por_favor_aguarde));
		    	BuscaSalasModoCasualComArgumentoTask taskBuscaSalasModoCasual12 =
		    				new BuscaSalasModoCasualComArgumentoTask(loadingSalasModoCasual, telaModoCasual);
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
		    	}
		    	taskBuscaSalasModoCasual12.execute("categorias",categoriasSeparadasPorVirgula);    
		    	
		    	//A STRING SCIMA ESTAH NORMAL COM AS CATEGORIAS. POR ALGUM MOTIVO O LISTVIEW NAO ESTAH SENDO ATUALIZADO COM O RESULTADO DA BUSCA
	      }
	  });
}


private Typeface escolherFonteDoTextoListViewIconeETexto()
{
	String fontPath = "fonts/Wonton.ttf";
    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
    return tf;
}

/*o Dialog eh passado para procurar o textview nele*/
private void mudarFonteTituloEBotaoOkInformeAsCategoriasPesquisaSalaModoCasual(Dialog dialog)
{
	String fontPath = "fonts/Wonton.ttf";
    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
    TextView titulo = (TextView) dialog.findViewById(R.id.tituloEscolhaAsCategoriasPesquisarSalasModoCasual);
    titulo.setTypeface(tf);
    
    Button botaoOk = (Button) dialog.findViewById(R.id.confirmar_escolha_categorias);
    botaoOk.setTypeface(tf);
    
}

private void mudarFonteTituloInformeADuracaoPesquisaSalaModoCasual(Dialog dialog)
{
	String fontPath = "fonts/Wonton.ttf";
    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
    TextView titulo = (TextView) dialog.findViewById(R.id.tituloEscolhaDuracaoPesquisaSalasModoCasual);
    titulo.setTypeface(tf);
}

private void mudarFonteTituloETextoEBotaoOkInformeUmEmailPesquisaSalaModoCasual(Dialog dialog)
{
	String fontPath = "fonts/Wonton.ttf";
    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
    TextView titulo = (TextView) dialog.findViewById(R.id.tituloEscolhaEmailPesquisaSalasModoCasual);
    titulo.setTypeface(tf);
    
    TextView texto = (TextView) dialog.findViewById(R.id.label_email_pesquisa_sala_modo_casual);
    texto.setTypeface(tf);
    
    Button botaoOk = (Button) dialog.findViewById(R.id.confirmar_pesquisa_email);
    botaoOk.setTypeface(tf);
}

private void mudarFonteTituloInformeUmDanPesquisaSalaModoCasual(Dialog dialog)
{
	String fontPath = "fonts/Wonton.ttf";
    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
    TextView titulo = (TextView) dialog.findViewById(R.id.titulo_popup_escolha_dan);
    titulo.setTypeface(tf);
}

@Override
public void onNothingSelected(AdapterView<?> arg0) 
{
	// TODO Auto-generated method stub
	
}
}

  
 
