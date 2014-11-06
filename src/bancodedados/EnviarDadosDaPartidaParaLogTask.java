package bancodedados;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.karutakanji.MainActivity;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import android.os.AsyncTask;
import android.util.Log;

public class EnviarDadosDaPartidaParaLogTask extends AsyncTask<DadosPartidaParaOLog, String, String> 
{
	
	@Override
	protected String doInBackground(DadosPartidaParaOLog... dadosPartida) 
	{
		DadosPartidaParaOLog umDadosPartida = dadosPartida[0];
		String categoria = umDadosPartida.getCategoria();
		String email = umDadosPartida.getEmail();
		String data = umDadosPartida.getData();
		java.lang.Integer pontuacao = umDadosPartida.getPontuacao();
		
		LinkedList<KanjiTreinar> palavrasAcertadas = umDadosPartida.getPalavrasAcertadas();
		String palavrasacertadas = 
				this.transformarLinkedListKanjisEmString(palavrasAcertadas);
		
		LinkedList<KanjiTreinar> palavrasErradas = umDadosPartida.getPalavrasErradas();
		String palavraserradas = 
				this.transformarLinkedListKanjisEmString(palavrasErradas);
		
		LinkedList<KanjiTreinar> palavrasJogadas = umDadosPartida.getPalavrasJogadas();
		List<String> palavrasJogadasPraMongo = new ArrayList<String>();
		for(int y = 0; y < palavrasJogadas.size(); y++)
		{
			KanjiTreinar umaPalavraJogada = palavrasJogadas.get(y);
			String kanjiEmString = umaPalavraJogada.getKanji() + "|" + umaPalavraJogada.getCategoriaAssociada();
			palavrasJogadasPraMongo.add(kanjiEmString);
		}
		String jogoassociado = umDadosPartida.getJogoAssociado();
		
		String emailadversario = umDadosPartida.geteMailAdversario();
		String voceganhououperdeu = umDadosPartida.getVoceGanhouOuPerdeu();
		
		try
		{
			MongoClient mongo = new MongoClient("192.168.0.109", 27017);
			DB db = mongo.getDB("pairg_karutakanji_app");
			DBCollection collection = db.getCollection("partidas");
			BasicDBObject document = new BasicDBObject();
			
			document.append("categoria", categoria);
			document.append("email", email);
			document.append("data", data);
			document.append("pontuacao", pontuacao);
			document.append("palavrasacertadas", palavrasacertadas);
			document.append("palavraserradas", palavraserradas);
			document.append("palavrasjogadas", palavrasJogadasPraMongo);
			document.append("jogoassociado", jogoassociado);
			document.append("emailadversario", emailadversario);
			document.append("voceganhououperdeu", voceganhououperdeu);
			collection.insert(document);
			
        } catch (Exception e4) {
            Log.e("excecao no envio de dados da partida", e4.toString());
            e4.printStackTrace();
        }
		
		 
		return "";
	}
	
	/*o banco de dados nao pode armazenar linkedlist, por isso transformaremos uma linkedlist de kanjis em string*/
	private String transformarLinkedListKanjisEmString(LinkedList<KanjiTreinar> listaKanjis)
	{
		String listaEmString = "";
		for(int i = 0; i < listaKanjis.size(); i++)
		{
			KanjiTreinar umaPalavra = listaKanjis.get(i);
			listaEmString = listaEmString + umaPalavra.getKanji() + "|" + umaPalavra.getCategoriaAssociada() + ";";
		}
		
		return listaEmString;
	}
	

}
