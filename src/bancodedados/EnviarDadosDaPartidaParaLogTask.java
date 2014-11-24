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
		String categoria = umDadosPartida.getCategoriaEmString();
		String [] categoriaSplitadaPontoEVirgula = categoria.split(";");
		List<String> categoriasPraMongo = new ArrayList<String>();
		for(int u = 0; u < categoriaSplitadaPontoEVirgula.length; u++)
		{
			String umaCategoria = categoriaSplitadaPontoEVirgula[u];
			categoriasPraMongo.add(umaCategoria);
			
		}
		String email = umDadosPartida.getEmail();
		String data = umDadosPartida.getData();
		java.lang.Integer pontuacao = umDadosPartida.getPontuacao();
		
		LinkedList<KanjiTreinar> palavrasAcertadas = umDadosPartida.getPalavrasAcertadas();
		List<BasicDBObject> palavrasAcertadasPraMongo = new ArrayList<BasicDBObject>();
		for(int y = 0; y < palavrasAcertadas.size(); y++)
		{
			KanjiTreinar umaPalavraAcertada = palavrasAcertadas.get(y);
			BasicDBObject novoObjetoKanji = new BasicDBObject();
			novoObjetoKanji.append("kanji", umaPalavraAcertada.getKanji());
			novoObjetoKanji.append("categoria", umaPalavraAcertada.getCategoriaAssociada());
			palavrasAcertadasPraMongo.add(novoObjetoKanji);
		}
		
		LinkedList<KanjiTreinar> palavrasErradas = umDadosPartida.getPalavrasErradas();
		List<BasicDBObject> palavrasErradasPraMongo = new ArrayList<BasicDBObject>();
		for(int y = 0; y < palavrasErradas.size(); y++)
		{
			KanjiTreinar umaPalavraErrada = palavrasErradas.get(y);
			BasicDBObject novoObjetoKanji = new BasicDBObject();
			novoObjetoKanji.append("kanji", umaPalavraErrada.getKanji());
			novoObjetoKanji.append("categoria", umaPalavraErrada.getCategoriaAssociada());
			palavrasErradasPraMongo.add(novoObjetoKanji);
		}
		
		LinkedList<KanjiTreinar> palavrasJogadas = umDadosPartida.getPalavrasJogadas();
		List<BasicDBObject> palavrasJogadasPraMongo = new ArrayList<BasicDBObject>();
		for(int y = 0; y < palavrasJogadas.size(); y++)
		{
			KanjiTreinar umaPalavraJogada = palavrasJogadas.get(y);
			BasicDBObject novoObjetoKanji = new BasicDBObject();
			novoObjetoKanji.append("kanji", umaPalavraJogada.getKanji());
			novoObjetoKanji.append("categoria", umaPalavraJogada.getCategoriaAssociada());
			palavrasJogadasPraMongo.add(novoObjetoKanji);
		}
		
		String jogoassociado = umDadosPartida.getJogoAssociado();
		
		String emailadversario = umDadosPartida.geteMailAdversario();
		String voceganhououperdeu = umDadosPartida.getVoceGanhouOuPerdeu();
		
		try
		{
			//"192.168.0.101"
			MongoClient mongo = new MongoClient("10.5.26.231", 27017);
			DB db = mongo.getDB("pairg_karutakanji_app");
			DBCollection collection = db.getCollection("partidas");
			BasicDBObject document = new BasicDBObject();
			
			document.append("categoria", categoriasPraMongo);
			document.append("email", email);
			document.append("data", data);
			document.append("pontuacao", pontuacao);
			document.append("palavrasacertadas", palavrasAcertadasPraMongo);
			document.append("palavraserradas", palavrasErradasPraMongo);
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
