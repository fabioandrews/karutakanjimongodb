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
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.karutakanji.ModoCasual;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CriarSalaModoCasualTask extends AsyncTask<SalaModoCasual, String, String>
{
	private ModoCasual telaModoCasual;
	private int id_sala_em_int; //toda sala criada tem um id, mas ele eh criado pelo BD e nao pelo user
	
	public CriarSalaModoCasualTask(ModoCasual telaModoCasual)
	{
		this.telaModoCasual = telaModoCasual;
	}
	@Override
	protected String doInBackground(SalaModoCasual... salaModoCasual) 
	{
		SalaModoCasual umaSalaModoCasual = salaModoCasual[0];
		String emailCriador = umaSalaModoCasual.getEmailDoCriador();
		String danCriador = umaSalaModoCasual.getDanDoCriador();
		String categoriasselecionadas = umaSalaModoCasual.getCategoriasJuntas();
		String rodadas = umaSalaModoCasual.getQuantasRodadas();
		String sala_aberta = "sim";
		
		
		String url_criarSala = "http://server.karutakanji.pairg.dimap.ufrn.br/app/criarsalamodocasual.php";//android nao aceita localhost, tem de ser seu IP
		String url_pegarIdSalaCriada = "http://server.karutakanji.pairg.dimap.ufrn.br/app/pegaridsalacasualcriadapelousuario.php";
		String url_inserirCategoriasSalaCriada = "http://server.karutakanji.pairg.dimap.ufrn.br/app/criarsalamodocasualcategorias.php";
		
		//String url_select = "http://192.168.0.110/amit/inserirpartidanolog.php";//android nao aceita localhost, tem de ser seu IP
		
		try
		{
			//primeiro vamos criar a sala sem as categorias
			//"192.168.0.101"
			MongoClient mongo = new MongoClient("10.5.26.231", 27017);
			DB db = mongo.getDB("pairg_karutakanji_app");
			DBCollection collection = db.getCollection("salasmodocasual");
			BasicDBObject document = new BasicDBObject();
            document.append("emaildocriador", emailCriador);
            document.append("dandocriador", danCriador);
            document.append("quantasrodadas", rodadas);
            document.append("salaaberta", sala_aberta);
            String[] categorias = categoriasselecionadas.split(",");
            List<String> categoriasList = new ArrayList<String>();
            for(int i = 0; i < categorias.length; i++)
            {
            	categoriasList.add(categorias[i]);
            }
            document.append("categorias", categoriasList);
			collection.insert(document);
            
            //agora vamos pegar o id da sala que acabamos de criar
			DBObject searchByEmailCriador = new BasicDBObject("emaildocriador", emailCriador);
			DBCursor cursor = collection.find(searchByEmailCriador);
			while(cursor.hasNext())
			{
				DBObject objetoDB = cursor.next();
				String salaaberta = (String) objetoDB.get("salaaberta");
				
				if(salaaberta.compareTo("sim") == 0)
				{
					//encontramos a sala aberta criada agora pouco
					ObjectId idSalaMongo = (ObjectId) objetoDB.get( "_id" );
					this.id_sala_em_int = idSalaMongo._inc();
					if(this.id_sala_em_int < 0)
					{
						this.id_sala_em_int = -this.id_sala_em_int;
					}
					break;
				}
			}
		}
		catch(Exception e)
		{
			Toast t = Toast.makeText(telaModoCasual, e.getMessage(), Toast.LENGTH_LONG);
	        t.show();
			e.printStackTrace();
		}
		
		 
		return "";
	}
	
	@Override
	protected void onPostExecute(String s) 
	{
		this.telaModoCasual.iniciarAberturaDeSalaCasualComId(id_sala_em_int);
	}
	
	private void obterIdSalaDoResultadoDoBd(InputStream inputStream) throws Exception
	{
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
            String result = sBuilder.toString();
            
            JSONArray jArray = new JSONArray(result);  
            JSONObject jObject = jArray.getJSONObject(0);
            
            String id_sala = jObject.getString("id_sala");
            this.id_sala_em_int = Integer.valueOf(id_sala);

        } catch (Exception e) 
        {
        	String eString = e.toString();
            Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
            throw e;
        }
	}
}
