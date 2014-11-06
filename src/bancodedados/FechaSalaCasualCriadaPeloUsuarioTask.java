package bancodedados;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import android.os.AsyncTask;
import android.util.Log;

/*em algum momento, seja pela desistencia do usuario ou porque a partida comecou, serah necessario tirar a sala criada anteriormente pelo usuario do BD*/
public class FechaSalaCasualCriadaPeloUsuarioTask extends AsyncTask<String, String, String>
{
	@Override
	protected String doInBackground(String... emailUsuario) 
	{
		String email_do_criador = emailUsuario[0];
		
		String url_select = "http://server.karutakanji.pairg.dimap.ufrn.br/app/fechasalacasualcriadapelousuario.php";//android nao aceita localhost, tem de ser seu IP
		//String url_select = "http://192.168.0.110/amit/inserirpartidanolog.php";//android nao aceita localhost, tem de ser seu IP
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		try
		{
			MongoClient mongo = new MongoClient("192.168.0.109", 27017);
			DB db = mongo.getDB("pairg_karutakanji_app");
			DBCollection collection = db.getCollection("salasmodocasual");
			BasicDBObject searchByEmailCriador = new BasicDBObject("emaildocriador", email_do_criador);
			DBCursor cursor = collection.find(searchByEmailCriador);
			
			while(cursor.hasNext())
			{
				BasicDBObject objetoDB = (BasicDBObject) cursor.next();
				String salaaberta = (String) objetoDB.get("salaaberta");
				String emailCriador = (String) objetoDB.get("emaildocriador");
				String danCriador = (String) objetoDB.get("dandocriador");
				String rodadas = (String) objetoDB.get("quantasrodadas");
				List<String> categoriasList = (List<String>) objetoDB.get("categorias");
				
				if(salaaberta.compareTo("sim") == 0)
				{
					//encontramos a sala aberta criada agora pouco
					BasicDBObject novoObjetoDB = new BasicDBObject();
					novoObjetoDB.put("salaaberta","não");
					novoObjetoDB.append("emaildocriador", emailCriador);
					novoObjetoDB.append("dandocriador", danCriador);
					novoObjetoDB.append("quantasrodadas", rodadas);
					novoObjetoDB.append("categorias", categoriasList);
					collection.update(objetoDB,novoObjetoDB);
					
				}
			}
		}
		catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        }
		
		 
		return "";
	}
}
