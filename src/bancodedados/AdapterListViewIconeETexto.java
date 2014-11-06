package bancodedados;

import com.karutakanji.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*classe para fazer uma listview com icones a esquerda e texto a direita*/
public class AdapterListViewIconeETexto extends ArrayAdapter<String>{
private final Activity context;
private final String[] web;
private final Integer[] imageId;
private final Typeface typeFaceFonteTexto;
private final boolean iconesDevemEstarMeioTransparentesNoComeco;
private final boolean textoDeveEstarMeioTransparenteNoComeco;
private int layoutUsadoParaTextoEImagem; //possivel valor = R.layout.list_item_icone_e_texto
private boolean corDoTextoDeveSerMudada;

public AdapterListViewIconeETexto(Activity context,String[] web, Integer[] imageId, Typeface typeFaceFonteTexto, boolean iconesDevemEstarMeioTransparentesNoComeco, boolean textoDeveSerTransparente, boolean mudarCorDoTexto) 
{
	super(context, R.layout.list_item_icone_e_texto, web);
	this.context = context;
	this.web = web;
	this.imageId = imageId;
	this.typeFaceFonteTexto = typeFaceFonteTexto;
	this.iconesDevemEstarMeioTransparentesNoComeco = iconesDevemEstarMeioTransparentesNoComeco;
	this.textoDeveEstarMeioTransparenteNoComeco = textoDeveSerTransparente;
	this.layoutUsadoParaTextoEImagem = R.layout.list_item_icone_e_texto;
	this.corDoTextoDeveSerMudada = mudarCorDoTexto;
}
@Override
public View getView(int position, View view, ViewGroup parent) 
{
		//se for so o icone e o texto, as imagens estao em imageviews normais
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(layoutUsadoParaTextoEImagem, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
		
		//mudar fonte do texto
		txtTitle.setTypeface(typeFaceFonteTexto);
		if(this.corDoTextoDeveSerMudada == true)
		{
			txtTitle.setTextColor(Color.parseColor("#6b0674"));
		}
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
		String stringASetColocadaNoTextView = web[position];
		
		Spannable span = new SpannableString(stringASetColocadaNoTextView);
		if(stringASetColocadaNoTextView.contains("(") == true)
		{
			//tudo que esta dentro do parenteses deveria ficar menor
			int indiceParentesesQueAbre = stringASetColocadaNoTextView.indexOf("(");
			int indiceParentesesQueFecha = stringASetColocadaNoTextView.indexOf(")");
			span.setSpan(new RelativeSizeSpan(0.7f), indiceParentesesQueAbre, indiceParentesesQueFecha + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		txtTitle.setText(span);
		
		//txtTitle.setCompoundDrawablesWithIntrinsicBounds(imageId[position], 0, 0, 0);
		imageView.setImageResource(imageId[position]);
		
		if(this.iconesDevemEstarMeioTransparentesNoComeco == true)
		{
			imageView.setAlpha(70);
		}
		
		if(this.textoDeveEstarMeioTransparenteNoComeco == true)
		{
			int alpha = 70;
			txtTitle.setAlpha(alpha);
			if(corDoTextoDeveSerMudada == true)
			{
				txtTitle.setTextColor(Color.argb(alpha, 107, 6, 116)); //cor dos textos para roxo em rgb fica assim
			}
			else
			{
				txtTitle.setTextColor(Color.argb(alpha, 0, 0, 0));
			}
		}
		
		return rowView;
	
}
public int getLayoutUsadoParaTextoEImagem() {
	return layoutUsadoParaTextoEImagem;
}
public void setLayoutUsadoParaTextoEImagem(int layoutUsadoParaTextoEImagem) {
	this.layoutUsadoParaTextoEImagem = layoutUsadoParaTextoEImagem;
}

public Drawable scaleDrawable(Drawable drawable, int width, int 
height) 
{ 
      int wi = drawable.getIntrinsicWidth(); 
      int hi = drawable.getIntrinsicHeight(); 
      int dimDiff = Math.abs(wi - width) - Math.abs(hi - height); 
      float scale = (dimDiff > 0) ? width/(float)wi : height/ 
(float)hi; 
      Rect bounds = new Rect(0, 0, (int)(scale*wi), (int)(scale*hi)); 
      drawable.setBounds(bounds); 
      return drawable; 
} 



}
