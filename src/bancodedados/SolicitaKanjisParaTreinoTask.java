package bancodedados;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.karutakanji.ActivityMultiplayerQueEsperaAtePegarOsKanjis;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Essa classe vai solicitar a um servidor php por listas de kanjis para treino no jogo
 * 
 *
 */
public class SolicitaKanjisParaTreinoTask extends AsyncTask<String, String, Void> {
	
	private InputStream inputStream = null;
	private String result = ""; 
	private ProgressDialog loadingDaTelaEmEspera;//eh o dialog da tela em espera pelo resultado do web service
	private ActivityQueEsperaAtePegarOsKanjis activityQueEsperaAtePegarOsKanjis;
	private DBCursor cursor; //cursor do banco obtido no final de doInBackground
	private long quantosNaCollection;
	
	public SolicitaKanjisParaTreinoTask(ProgressDialog loadingDaTela, ActivityQueEsperaAtePegarOsKanjis activityQueEsperaAteRequestTerminar)
	{
		this.loadingDaTelaEmEspera = loadingDaTela;
		this.activityQueEsperaAtePegarOsKanjis = activityQueEsperaAteRequestTerminar;
	}
	
	@Override
    protected Void doInBackground(String... params) {
		try
		{
			//era 192.168.0.109
			//era 10.5.23.145 na ufrn
			//"192.168.0.101"
			MongoClient mongo = new MongoClient("10.5.26.231", 27017);
			DB db = mongo.getDB("pairg_karutakanji_app");
			DBCollection collection = db.getCollection("jlpt");
			this.cursor = collection.find();
			
			//parse JSON data
			while(cursor.hasNext()) 
			{
				DBObject objetoDB = cursor.next();
				
				java.lang.Integer jlptAssociadoInteger = (java.lang.Integer) objetoDB.get("jlpt");
				String jlptAssociado = String.valueOf(jlptAssociadoInteger);
                String categoriaAssociada = (String) objetoDB.get("categoria");
                String kanji = (String) objetoDB.get("kanji");
                String traducaoEmPortugues = (String) objetoDB.get("traducao");
                String hiraganaDoKanji = (String) objetoDB.get("hiragana");
                
                java.lang.Integer dificuldadeDoKanjiInteger = (java.lang.Integer) objetoDB.get("dificuldade");
                String dificuldadeDoKanji = String.valueOf(dificuldadeDoKanjiInteger);
                
                int dificuldadeDoKanjiEmNumero; 
                try
                {
                	dificuldadeDoKanjiEmNumero = Integer.valueOf(dificuldadeDoKanji);
                }
                catch(NumberFormatException exc)
                {
                	dificuldadeDoKanjiEmNumero = 1;
                }
                
                KanjiTreinar novoKanjiTreinar = new KanjiTreinar(jlptAssociado, categoriaAssociada, kanji, 
                		traducaoEmPortugues, hiraganaDoKanji, dificuldadeDoKanjiEmNumero);
                
                //e, por fim, armazenar esses kanjis na lista de ArmazenaKanjisPorCategoria
                ArmazenaKanjisPorCategoria.pegarInstancia().adicionarKanjiACategoria(categoriaAssociada, novoKanjiTreinar);
				
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return null;
    } // protected Void doInBackground(String... params)
	
	 protected void onPostExecute(Void v) {
	           
	        this.activityQueEsperaAtePegarOsKanjis.procedimentoAposCarregarKanjis();
	        
	        if(this.loadingDaTelaEmEspera != null)
	        {
	        	//esse loading eh null quando a activity estiver chamando essa task apenas para carregar kanjis
	        	//sem ter nenhum loading para espera
	        	this.loadingDaTelaEmEspera.dismiss();
	        }
	        
	    } // protected void onPostExecute(Void v)
} //class MyAsyncTask extends AsyncTask<String, String, Void>

