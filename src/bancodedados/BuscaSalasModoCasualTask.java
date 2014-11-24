package bancodedados;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.karutakanji.ModoCasual;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class BuscaSalasModoCasualTask extends AsyncTask<String, String, Void>
{
	private InputStream inputStream = null;
	private String result = ""; 
	private ProgressDialog loadingDaTelaEmEspera;//eh o dialog da tela em espera pelo resultado do web service
	private ModoCasual activityQueEsperaAtePegarAsSalas;
	private DBCursor cursor;
	private LinkedList<SalaModoCasual> salasModoCasual;
	
	public BuscaSalasModoCasualTask(ProgressDialog loadingDaTela, ModoCasual activityQueEsperaAteRequestTerminar)
	{
		this.loadingDaTelaEmEspera = loadingDaTela;
		this.activityQueEsperaAtePegarAsSalas = activityQueEsperaAteRequestTerminar;
	}

	@Override
	protected Void doInBackground(String... arg0) 
	{
		//LCC: http://10.9.99.239/amit/pegarjlptjson.php
				//sala de aula: http://10.5.26.127/amit/pegarjlptjson.php

		       //String url_select = "http://app.karutakanji.pairg.dimap.ufrn.br/pegarjlptjson.php";//android nao aceita localhost, tem de ser seu IP
				String url_select = "http://server.karutakanji.pairg.dimap.ufrn.br/app/pegarsalasmodocasualjson.php";
				//String url_select = "http://192.168.0.110/amit/pegarjlptjson.php";
		       
				ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

		        try 
		        {
		        	//"192.168.0.101"
		        	MongoClient mongo = new MongoClient("10.5.26.231", 27017);
					DB db = mongo.getDB("pairg_karutakanji_app");
					DBCollection collection = db.getCollection("salasmodocasual");
					DBObject searchBySalaAberta = new BasicDBObject("salaaberta", "sim");
					this.cursor = collection.find(searchBySalaAberta);
					
					this.salasModoCasual = new LinkedList<SalaModoCasual>();
		            while(cursor.hasNext() == true) 
		            {
		            	DBObject objetoDB = cursor.next();
		            	ObjectId idSalaMongo = (ObjectId) objetoDB.get( "_id" );
		            	int id_sala = idSalaMongo._inc();
						if(id_sala < 0)
						{
							id_sala = -id_sala;
						}
						
						String categorias_juntas = "";
						List<String> categoriasList = (List<String>) objetoDB.get("categorias");
						for(int i = 0; i < categoriasList.size(); i++)
						{
							categorias_juntas = categorias_juntas + categoriasList.get(i);
							
							if(i != categoriasList.size() - 1)
							{
								categorias_juntas = categorias_juntas + ",";
							}
						}
						
		         
		                String email_do_criador = (String) objetoDB.get("emaildocriador");
		                String dan_do_criador = (String) objetoDB.get("dandocriador");
		                String quantas_rodadas = (String) objetoDB.get("quantasrodadas");
		                
		                SalaModoCasual novaSalaModoCasual = new SalaModoCasual();
		                novaSalaModoCasual.setId_sala(id_sala);
		                novaSalaModoCasual.setCategoriasJuntas(categorias_juntas);
		                novaSalaModoCasual.setDanDoCriador(dan_do_criador);
		                novaSalaModoCasual.setEmailDoCriador(email_do_criador);
		                novaSalaModoCasual.setQuantasRodadas(quantas_rodadas);
		                
		                salasModoCasual.add(novaSalaModoCasual);

		            } // End Loop
					
		        } catch (IllegalStateException e3) {
		            Log.e("IllegalStateException", e3.toString());
		            e3.printStackTrace();
		        } catch (IOException e4) {
		            Log.e("IOException", e4.toString());
		            e4.printStackTrace();
		        }
		        
				return null;
	}
	
	 protected void onPostExecute(Void v) {
	        //parse JSON data
	        try {
	            //vamos reverter a ordem das salas, para coloca-las do mais recente pro menos
	            LinkedList<SalaModoCasual> salasModoCasualRevert = new LinkedList<SalaModoCasual>();
	            Iterator<SalaModoCasual> iteradorReverso = salasModoCasual.descendingIterator();
	            while(iteradorReverso.hasNext() == true)
	            {
	            	SalaModoCasual umaSala = iteradorReverso.next();
	            	salasModoCasualRevert.add(umaSala);
	            }
	            
	            if(activityQueEsperaAtePegarAsSalas.getSalasAbertas() == null)
	            {
	            	//nao ha listas de salas anteriores para comparar
	            	this.activityQueEsperaAtePegarAsSalas.mostrarListaComSalasAposCarregar(salasModoCasualRevert,false);
		            this.loadingDaTelaEmEspera.dismiss();
	            }
	            else
	            {
	            	//sera que a GUI precisa mesmo ser atualizada? E a lista de salas obtidas eh a mesma da apresentada?
	            	LinkedList<SalaModoCasual> salasMostradasNaGui = this.activityQueEsperaAtePegarAsSalas.getSalasAbertas();
	            	if(salasMostradasNaGui.size() == salasModoCasualRevert.size())
	            	{
	            		//mesmo tamanho, mas serah que tem os mesmos elementos?
	            		for(int j = 0; j < salasModoCasualRevert.size(); j++)
	            		{
	            			int idUmaSalaModoCasual = salasModoCasualRevert.get(j).getId_sala();
	            			int idSalaJaExistiaNaGui = salasMostradasNaGui.get(j).getId_sala();
	            			
	            			if(idSalaJaExistiaNaGui != idUmaSalaModoCasual)
	            			{
	            				//tem de atualizar a gui. As salas deveriam estar ate na mesma ordem
	            				this.activityQueEsperaAtePegarAsSalas.mostrarListaComSalasAposCarregar(salasModoCasualRevert,true);
	    			            this.loadingDaTelaEmEspera.dismiss();
	    			            break;
	            			}
	            		}
	            	}
	            	else
	            	{
	            		//as salas obtidas sao diferentes das mostradas na gui. Vamos ter de atualizar a GUI
	            		if(salasMostradasNaGui.size() < salasModoCasualRevert.size())
        				{
        					//alertar novas salas surgiram
        					this.activityQueEsperaAtePegarAsSalas.mostrarListaComSalasAposCarregar(salasModoCasualRevert,true);
        				}
        				else
        				{
        					//algumas salas sairam, entao n precisa alert
        					this.activityQueEsperaAtePegarAsSalas.mostrarListaComSalasAposCarregar(salasModoCasualRevert,false);
        				}
			            this.loadingDaTelaEmEspera.dismiss();
	            	}
	            }
	           
	        } catch (Exception e) {
	            Log.e("JSONException", "Error: " + e.toString());
	            this.loadingDaTelaEmEspera.dismiss();
	        }

	    }
}
