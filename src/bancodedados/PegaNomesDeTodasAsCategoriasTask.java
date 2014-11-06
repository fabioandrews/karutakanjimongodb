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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.karutakanji.ModoCasual;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class PegaNomesDeTodasAsCategoriasTask extends AsyncTask<String, String, Void> {
	
	private InputStream inputStream = null;
	private String result = ""; 
	private ProgressDialog loadingDaTelaEmEspera;//eh o dialog da tela em espera pelo resultado do web service
	private ModoCasual modoCasual;
	
	public PegaNomesDeTodasAsCategoriasTask(ProgressDialog loadingDaTela, ModoCasual modoCasual)
	{
		this.loadingDaTelaEmEspera = loadingDaTela;
		this.modoCasual = modoCasual;
	}
	
	@Override
    protected Void doInBackground(String... params) {
		//LCC: http://10.9.99.239/amit/pegarjlptjson.php
		//sala de aula: http://10.5.26.127/amit/pegarjlptjson.php

       //String url_select = "http://app.karutakanji.pairg.dimap.ufrn.br/pegarjlptjson.php";//android nao aceita localhost, tem de ser seu IP
		String url_select = "http://server.karutakanji.pairg.dimap.ufrn.br/app/pegarnomestodasascategorias.php";
		//String url_select = "http://192.168.0.110/amit/pegarjlptjson.php";
       
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

        try {
            // Set up HTTP post

            // HttpClient is more then less deprecated. Need to change to URLConnection
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url_select);
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // Read content & Log
            inputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e1) {
            Log.e("UnsupportedEncodingException", e1.toString());
            e1.printStackTrace();
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        }
        // Convert response to string using String Builder
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
            	if(line.startsWith("<meta") == false)//pula linha de metadados
            	{
            		 sBuilder.append(line + "\n");
            	}
               
            }

            inputStream.close();
            result = sBuilder.toString();

        } catch (Exception e) {
            Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
        }
        
        /*if(this.activityQueEsperaAtePegarOsKanjis instanceof ActivityMultiplayerQueEsperaAtePegarOsKanjis)
        {
        	//se trata da activity do multiplayer
        	ActivityMultiplayerQueEsperaAtePegarOsKanjis activityMultiplayer = (ActivityMultiplayerQueEsperaAtePegarOsKanjis) this.activityQueEsperaAtePegarOsKanjis;
        	if(activityMultiplayer.jogadorEhHost() == true)
        	{
        		 while(activityMultiplayer.oGuestTerminouDeCarregarListaDeCategorias() == false)
        	        {
        	        	//espera...
        	        	try {
        					Thread.sleep(1000);
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        	        }
        	}
        }*/
       
        
		return null;
    } // protected Void doInBackground(String... params)
	
	 protected void onPostExecute(Void v) 
	 {
	        //parse JSON data
		 LinkedList<String> categorias = new LinkedList<String>();
	        try {
	            JSONArray jArray = new JSONArray(result); 
	            for(int i=0; i < jArray.length(); i++) {

	                JSONObject jObject = jArray.getJSONObject(i);
	                
	                String categoria = jObject.getString("categoria");
	                categorias.add(categoria);
	                

	            } // End Loop
	           
	        } catch (JSONException e) {
	            Log.e("JSONException", "Error: " + e.toString());
	        }
	       
	        
	        if(this.loadingDaTelaEmEspera != null)
	        {
	        	//esse loading eh null quando a activity estiver chamando essa task apenas para carregar kanjis
	        	//sem ter nenhum loading para espera
	        	this.loadingDaTelaEmEspera.dismiss();
	        }
	        
	        this.modoCasual.mostrarPopupPesquisarPorCategorias(categorias);
	        
	    } // protected void onPostExecute(Void v)
} //class MyAsyncTask extends AsyncTask<String, String, Void>


