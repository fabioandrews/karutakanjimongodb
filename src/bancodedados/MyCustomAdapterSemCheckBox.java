package bancodedados;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.karutakanji.R;
import com.karutakanji.ModoCasual;

public class MyCustomAdapterSemCheckBox extends ArrayAdapter<String> {
	  
	  private ArrayList<String> elementosDaLista;
	  private Context contexto;
	  
	  public MyCustomAdapterSemCheckBox(Context context, int textViewResourceId,
	    ArrayList<String> salaList) {
	   super(context, textViewResourceId, salaList);
	   this.elementosDaLista = new ArrayList<String>();
	   this.elementosDaLista.addAll(salaList);
	   this.contexto = context;
	  }
	  
	  
	  
	  public ArrayList<String> getElementosDaLista() {
		return elementosDaLista;
	}



	private class ViewHolder {
	   CheckBox name;
	  }
	  
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	  
	   ViewHolder holder = null;
	   Log.v("ConvertView", String.valueOf(position));
	  
	   if (convertView == null) {
	   LayoutInflater vi = (LayoutInflater)contexto.getSystemService(
	     Context.LAYOUT_INFLATER_SERVICE);
	   convertView = vi.inflate(R.layout.categoria_de_kanji_na_lista, null);
	  
	   
	    holder.name.setOnClickListener( new View.OnClickListener() { 
	     public void onClick(View v) 
	     { 
	    	 
	   	      /*Toast.makeText(contexto.getApplicationContext(),
	   	       "Clicked on Checkbox: " + cb.getText() +
	   	       " is " + cb.isChecked(),
	   	       Toast.LENGTH_LONG).show();*/
	    	
	     } 
	    }); 
	   }
	   else {
	    holder = (ViewHolder) convertView.getTag();
	   }
	  
	   String nomeElemento = elementosDaLista.get(position);
	   holder.name.setText(nomeElemento);
	   holder.name.setTag(nomeElemento);
	  
	   return convertView;
	  
	  }



	public void setElementosDaLista(ArrayList<String> elementosDaLista) {
		this.elementosDaLista = elementosDaLista;
	}
	  
	 }