package bancodedados;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.karutakanji.ActivityMultiplayerQueEsperaAtePegarOsKanjis;
import com.karutakanji.DadosPartidasAnteriores;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import android.os.AsyncTask;
import android.util.Log;

public class PegarDadosUltimasPartidasTask extends AsyncTask<String, String, Void>  
{
	private InputStream inputStream;
	private String result;
	private DadosPartidasAnteriores telaDadosPartidasAnteriores;
	private DBCursor cursor;
	private LinkedList<DadosPartidaParaOLog> dadosPartidasParaLog;
	
	public PegarDadosUltimasPartidasTask(DadosPartidasAnteriores telaDadosPartidasAnteriores)
	{
		this.telaDadosPartidasAnteriores = telaDadosPartidasAnteriores;
	}
	
	@Override
	protected Void doInBackground(String... emailJogador) 
	{

	        try 
	        {
	            // Set up HTTP post

	            // HttpClient is more then less deprecated. Need to change to URLConnection
	            String emailjogador = emailJogador[0];
	            //"192.168.0.101"
	            MongoClient mongo = new MongoClient("10.5.26.231", 27017);
				DB db = mongo.getDB("pairg_karutakanji_app");
				DBCollection collection = db.getCollection("partidas");
				DBObject searchByEMail = new BasicDBObject("email", emailjogador);
				this.cursor = collection.find(searchByEMail);
				
				this.dadosPartidasParaLog = new LinkedList<DadosPartidaParaOLog>();
		        //parse JSON data
				
		while(cursor.hasNext()) 
		{
			DBObject objetoDB = cursor.next();
			
			String email = (String) objetoDB.get("email");
			String data = (String) objetoDB.get("data");
			BasicDBList categoria = (BasicDBList) objetoDB.get("categoria");
			java.lang.Integer pontuacao = (java.lang.Integer) objetoDB.get("pontuacao");
			BasicDBList palavrasAcertadasArray = (BasicDBList) objetoDB.get("palavrasacertadas");
			BasicDBList palavrasErradasArray = (BasicDBList) objetoDB.get("palavraserradas");
			BasicDBList palavrasJogadasArray = (BasicDBList) objetoDB.get("palavrasjogadas");
			String jogoassociado = (String) objetoDB.get("jogoassociado");
			String emailadversario = (String) objetoDB.get("emailadversario");
			String voceganhououperdeu = (String) objetoDB.get("voceganhououperdeu");
			
			DadosPartidaParaOLog dadosLog = new DadosPartidaParaOLog();
			LinkedList<String> categoriasTreinadas = this.extrairListaArray(categoria);
        	dadosLog.setCategoria(categoriasTreinadas);
        	dadosLog.setData(data);
        	dadosLog.setEmail(email);
        	dadosLog.seteMailAdversario(emailadversario);
        	dadosLog.setJogoAssociado(jogoassociado);
        	dadosLog.setPontuacao(pontuacao);
        	dadosLog.setVoceGanhouOuPerdeu(voceganhououperdeu);
        	
        	LinkedList<KanjiTreinar> palavrasAcertadas = extrairKanjisTreinarArray(palavrasAcertadasArray);
        	LinkedList<KanjiTreinar> palavrasErradas = extrairKanjisTreinarArray(palavrasErradasArray);
        	LinkedList<KanjiTreinar> palavrasJogadas = extrairKanjisTreinarArray(palavrasJogadasArray);
        	dadosLog.setPalavrasAcertadas(palavrasAcertadas);
        	dadosLog.setPalavrasErradas(palavrasErradas);
        	dadosLog.setPalavrasJogadas(palavrasJogadas);
        	
        	dadosPartidasParaLog.add(dadosLog);
		}
				
	        } 
	        catch (Exception e1) 
	        {
	        	Log.e("exceção mongo:", e1.toString());
	            e1.printStackTrace();
	        }
			return null;
	   } 
		
	protected void onPostExecute(Void v) 
	{	
		this.telaDadosPartidasAnteriores.atualizarListViewComAsUltimasPartidas(dadosPartidasParaLog);      
	}

	/*pega a string do bd e transforma em montes de kanjis como era antes de enviar ao bd. Ex: au|verbos;kau|verbos...*/
	private LinkedList<KanjiTreinar> extrairKanjisTreinar(String kanjisTreinarEmString)
	{
		if(kanjisTreinarEmString.contains(";") == false)
		{
			//nao ha kanjis nessa string, vamos retornar linkedlist vazia
			return new LinkedList<KanjiTreinar>();
		}
		else
		{
			LinkedList<KanjiTreinar> kanjisTreinar = new LinkedList<KanjiTreinar>();
			String[] kanjisECategoriasComBarra = kanjisTreinarEmString.split(";");
			for(int i = 0; i < kanjisECategoriasComBarra.length; i++)
			{
				String umKanjiECategoria = kanjisECategoriasComBarra[i];
				String[] kanjiECategoria = umKanjiECategoria.split("\\|");
				String kanji = kanjiECategoria[0];
				String categoria = kanjiECategoria[1];
				
				KanjiTreinar kanjiTreinar = ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
				kanjisTreinar.add(kanjiTreinar);
			}
			
			return kanjisTreinar;
		}
	}
	
	/*pega a string do bd e transforma em montes de kanjis como era antes de enviar ao bd. Ex: au|verbos;kau|verbos...*/
	private LinkedList<KanjiTreinar> extrairKanjisTreinarArray(BasicDBList kanjisECategoriasComBarra)
	{
		LinkedList<KanjiTreinar> kanjisTreinar = new LinkedList<KanjiTreinar>();
		
		for(int i = 0; i < kanjisECategoriasComBarra.size(); i++)
		{
			DBObject umKanjiECategoria = (DBObject)kanjisECategoriasComBarra.get(i);
			String kanji = (String) umKanjiECategoria.get("kanji");
			String categoria = (String) umKanjiECategoria.get("categoria");
			
			KanjiTreinar kanjiTreinar = ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
			kanjisTreinar.add(kanjiTreinar);
		}
		
		return kanjisTreinar;
		
	}
	
	private LinkedList<String> extrairListaArray(BasicDBList listaEmDBList)
	{
		LinkedList<String> listaDoArray = new LinkedList<String>();
		
		for(int i = 0; i < listaEmDBList.size(); i++)
		{
			String umKanjiECategoria = (String)listaEmDBList.get(i);
			listaDoArray.add(umKanjiECategoria);
		}
		
		return listaDoArray;
		
	}

}
