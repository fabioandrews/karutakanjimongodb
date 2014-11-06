package bancodedados;

import java.util.ArrayList;
import java.util.LinkedList;

import com.karutakanji.LojinhaMaceteKanjiActivity;
import com.karutakanji.R;
import com.karutakanji.ModoCasual;
import com.karutakanji.VerMaceteKanjiActivity;

import lojinha.ConcreteDAOAcessaComprasMaceteKanji;
import lojinha.ConcreteDAOAcessaDinheiroDoJogador;
import lojinha.DAOAcessaComprasMaceteKanji;
import lojinha.DAOAcessaDinheiroDoJogador;
import lojinha.MaceteKanjiParaListviewSelecionavel;
import android.R.array;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdapterListViewSalasCriadas extends ArrayAdapter<SalaModoCasual> 
{
	private ArrayList<SalaModoCasual> arrayListSalasAbertas;
	private Context contextoAplicacao;
	private ModoCasual telaDoModoCasual;
	
	public AdapterListViewSalasCriadas(Context contextoAplicacao, int textViewResourceId,
		    ArrayList<SalaModoCasual> salasAbertas, ModoCasual telaDoModoCasual) 
	{
		super(contextoAplicacao, textViewResourceId, salasAbertas);
		this.arrayListSalasAbertas = new ArrayList<SalaModoCasual>();
		this.arrayListSalasAbertas.addAll(salasAbertas);
		this.contextoAplicacao = contextoAplicacao;
		this.telaDoModoCasual = telaDoModoCasual;
	}

	public ArrayList<SalaModoCasual> getArrayListSalasAbertas() {
		return arrayListSalasAbertas;
	}

	public void setArrayListSalasAbertas(
			ArrayList<SalaModoCasual> arrayListSalasAbertas) {
		this.arrayListSalasAbertas = arrayListSalasAbertas;
	}
	
	
	private class ViewHolderSalasCriadas {
		   TextView nomeDeUsuario;
		   ImageView nivelDoUsuario;
		   /*ImageView categoria1;
		   ImageView categoria2;
		   ImageView categoria3;
		   ImageView categoria4;
		   ImageView categoria5;
		   ImageView categoria6;
		   ImageView categoria7;
		   ImageView categoria8;
		   ImageView categoria9;
		   ImageView categoria10;*/
		   ImageView imageViewsCategorias;
		   ImageView imageViewPortaEntrarSala;
		   TextView textViewQuantasCategorias;
		  }
	
	
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	  
	   ViewHolderSalasCriadas holder = null;
	   Log.v("ConvertView", String.valueOf(position));
	  
	   if (convertView == null) {
		   
	   LayoutInflater vi = (LayoutInflater)contextoAplicacao.getSystemService(
	     Context.LAYOUT_INFLATER_SERVICE);
	   convertView = vi.inflate(R.layout.item_lista_sala, null);
	   
	   LinearLayout layoutDeUmaLinhaDoBuscarSalas = (LinearLayout) convertView.findViewById(R.id.uma_linha_buscar_salas);
	   TextView textoUsername = (TextView) convertView.findViewById(R.id.username);
	   ImageView imagemTituloDoJogador = (ImageView) convertView.findViewById(R.id.titulo_do_jogador);
	   if((position & 1) != 0)
	   {
		   layoutDeUmaLinhaDoBuscarSalas.setBackgroundResource(R.drawable.red_header);
		   textoUsername.setTextColor(Color.parseColor("#FFFFFF"));
		   //imagemTituloDoJogador.setTextColor(Color.parseColor("#FFFFFF"));
	   }
	   else
	   {
		   layoutDeUmaLinhaDoBuscarSalas.setBackgroundResource(R.drawable.white_header);
		   textoUsername.setTextColor(Color.parseColor("#000000"));
		   //imagemTituloDoJogador.setTextColor(Color.parseColor("#000000"));
	   }
	  
	   holder = new ViewHolderSalasCriadas();
	   holder.nomeDeUsuario = (TextView) convertView.findViewById(R.id.username);
	   holder.nivelDoUsuario = (ImageView) convertView.findViewById(R.id.titulo_do_jogador);
	   
	  
	   
	   convertView.setTag(holder);
	   final SalaModoCasual salaEscolhidaPraJogar = arrayListSalasAbertas.get(position);
	   holder.nomeDeUsuario.setText(salaEscolhidaPraJogar.getEmailDoCriador());
	   //holder.nivelDoUsuario.setText(salaEscolhidaPraJogar.getDanDoCriador());
	   this.setarIconeNivelDoJogador(holder, salaEscolhidaPraJogar.getDanDoCriador());
	   String categoriasSalasModoCasualSeparadasPorVirgula = salaEscolhidaPraJogar.getCategoriasJuntas();
	   
	   final String[] arrayCategorias;
	   if(categoriasSalasModoCasualSeparadasPorVirgula.contains(",") == true)
	   {
		   arrayCategorias = categoriasSalasModoCasualSeparadasPorVirgula.split(",");  
	   }
	   else
	   {
		   //so tem uma categoria
		   arrayCategorias = new String[1];
		   arrayCategorias[0] = categoriasSalasModoCasualSeparadasPorVirgula;
	   }
	   
	   
	   
	   holder.imageViewsCategorias = (ImageView) convertView.findViewById(R.id.campo_categorias);
	   holder.imageViewsCategorias.setImageResource(R.drawable.icone_abre_popup_categorias_sala_casual);
	   holder.imageViewPortaEntrarSala = (ImageView) convertView.findViewById(R.id.icone_entrar_sala_casual);
	   holder.imageViewPortaEntrarSala.setImageResource(R.drawable.icone_entrar_sala_casual);
	   
	   
	   holder.imageViewPortaEntrarSala.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            telaDoModoCasual.entrarNaSala(salaEscolhidaPraJogar);
	        }
	    });
	   
	   holder.imageViewsCategorias.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            telaDoModoCasual.abrirPopupMostrarCategoriasDeUmaSala(arrayCategorias);
	        }
	    });
	   
	   holder.textViewQuantasCategorias = (TextView) convertView.findViewById(R.id.quantas_categorias_tem_a_sala);
	   holder.textViewQuantasCategorias.setText(String.valueOf(arrayCategorias.length));
	   
	   /*LinearLayout linearLayoutAtualParaAdicionar = null;
	   for(int i = 0; i < categoriasTreinadasNaSala.size(); i++)
	   {
		   if(i == 0 || (i % 4) == 0)//4 categorias por linha
		   {
			   //adicionar novo LinearLayout no com uma linha nova para ícones de categorias PROGRAMATICAMENTE
			   LinearLayout novoLinearLayoutCategorias = new LinearLayout(contextoAplicacao);
			   RelativeLayout.LayoutParams parametrosNovoLinearLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			   parametrosNovoLinearLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
			   if(linearLayoutAtualParaAdicionar == null)
			   {
				   parametrosNovoLinearLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			   }
			   else
			   {
				   int idAntigoLinearLayoutAdicionarCategorias = linearLayoutAtualParaAdicionar.getId();
				   parametrosNovoLinearLayout.addRule(RelativeLayout.BELOW, idAntigoLinearLayoutAdicionarCategorias);
			   }
			   novoLinearLayoutCategorias.setLayoutParams(parametrosNovoLinearLayout);
			   novoLinearLayoutCategorias.setId(123456789 + i);
			   RelativeLayout layoutIconesCategorias = (RelativeLayout) convertView.findViewById(R.id.campo_categorias);
			   layoutIconesCategorias.addView(novoLinearLayoutCategorias);
			   linearLayoutAtualParaAdicionar = novoLinearLayoutCategorias;
			 //terminou de adicionar novo LinearLayout no com uma linha nova para ícones de categorias PROGRAMATICAMENTE
		   }
		   String nomeUmaCategoriaTreinada = categoriasTreinadasNaSala.get(i);
		   int idImagemCategoria = AssociaCategoriaComIcone.pegarIdImagemDaCategoria(contextoAplicacao, nomeUmaCategoriaTreinada);
		   if(idImagemCategoria != -1)
		   {
			   ImageView umImageViewCategoria = new ImageView(contextoAplicacao);
			   LinearLayout.LayoutParams parametrosNovaImageView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			   umImageViewCategoria.setLayoutParams(parametrosNovaImageView);
			   umImageViewCategoria.setImageResource(idImagemCategoria);
			   umImageViewCategoria.setPadding(1, 0, 1, 0);
			   linearLayoutAtualParaAdicionar.addView(umImageViewCategoria);
		   }
	   }*/
	   
	   
	    /*convertView.setOnClickListener( new View.OnClickListener() { 
	     public void onClick(View v) {
	    	 
	     
	      
	     
	    	  
	     } 
	    }); */
	   }
	   else 
	   {
		   //ANDREWS ADICIONOU
	        holder = (ViewHolderSalasCriadas) convertView.getTag();
	        LinearLayout layoutDeUmaLinhaDoBuscarSalas = (LinearLayout) convertView.findViewById(R.id.uma_linha_buscar_salas);
	 	   TextView textoUsername = (TextView) convertView.findViewById(R.id.username);
	 	   ImageView imagemTituloDoJogador = (ImageView) convertView.findViewById(R.id.titulo_do_jogador);
	 	   if((position & 1) != 0)
	 	   {
	 		   layoutDeUmaLinhaDoBuscarSalas.setBackgroundResource(R.drawable.red_header);
	 		   textoUsername.setTextColor(Color.parseColor("#FFFFFF"));
	 		   //textoTituloDoJogador.setTextColor(Color.parseColor("#FFFFFF"));
	 	   }
	 	   else
	 	   {
	 		   layoutDeUmaLinhaDoBuscarSalas.setBackgroundResource(R.drawable.white_header);
	 		   textoUsername.setTextColor(Color.parseColor("#000000"));
	 		   //textoTituloDoJogador.setTextColor(Color.parseColor("#000000"));
	 	   }
	 	  
	 	   //holder = new ViewHolderSalasCriadas();
	 	   holder.nomeDeUsuario = (TextView) convertView.findViewById(R.id.username);
	 	   holder.nivelDoUsuario = (ImageView) convertView.findViewById(R.id.titulo_do_jogador);
	 	   
	 	  
	 	   
	 	   //convertView.setTag(holder);
	 	   final SalaModoCasual salaEscolhidaPraJogar = arrayListSalasAbertas.get(position);
	 	   holder.nomeDeUsuario.setText(salaEscolhidaPraJogar.getEmailDoCriador());
	 	   this.setarIconeNivelDoJogador(holder, salaEscolhidaPraJogar.getDanDoCriador());
	 	   String categoriasSalasModoCasualSeparadasPorVirgula = salaEscolhidaPraJogar.getCategoriasJuntas();
	 	   
	 	   final String[] arrayCategorias;
	 	   if(categoriasSalasModoCasualSeparadasPorVirgula.contains(",") == true)
	 	   {
	 		   arrayCategorias = categoriasSalasModoCasualSeparadasPorVirgula.split(",");  
	 	   }
	 	   else
	 	   {
	 		   //so tem uma categoria
	 		   arrayCategorias = new String[1];
	 		   arrayCategorias[0] = categoriasSalasModoCasualSeparadasPorVirgula;
	 	   }
	 	   
	 	   
	 	   LinkedList<String> categoriasTreinadasNaSala = new LinkedList<String>();
	 	   for(int h = 0; h < arrayCategorias.length; h++)
	 	   {
	 		   String umaCategoria = arrayCategorias[h];
	 		   categoriasTreinadasNaSala.add(umaCategoria);
	 	   }
	 	   
	 	  holder.imageViewsCategorias = (ImageView) convertView.findViewById(R.id.campo_categorias);
		  holder.imageViewsCategorias.setImageResource(R.drawable.icone_abre_popup_categorias_sala_casual);
		  holder.imageViewPortaEntrarSala = (ImageView) convertView.findViewById(R.id.icone_entrar_sala_casual);
		  holder.imageViewPortaEntrarSala.setImageResource(R.drawable.icone_entrar_sala_casual);
	 	  
		  holder.imageViewPortaEntrarSala.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
		            telaDoModoCasual.entrarNaSala(salaEscolhidaPraJogar);
		        }
		    });
		   
		   holder.imageViewsCategorias.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
		            telaDoModoCasual.abrirPopupMostrarCategoriasDeUmaSala(arrayCategorias);
		        }
		    });
		   
		   holder.textViewQuantasCategorias = (TextView) convertView.findViewById(R.id.quantas_categorias_tem_a_sala);
		   holder.textViewQuantasCategorias.setText(String.valueOf(arrayCategorias.length));
	 	   /*holder.imageViewsCategorias = new LinkedList<ImageView>();
	 	   LinearLayout linearLayoutAtualParaAdicionar = null;
	 	   RelativeLayout layoutIconesCategorias = (RelativeLayout) convertView.findViewById(R.id.campo_categorias);
		   layoutIconesCategorias.removeAllViewsInLayout();
	 	   
	 	   for(int i = 0; i < categoriasTreinadasNaSala.size(); i++)
	 	   {
	 		   if(i == 0 || (i % 4) == 0)//4 categorias por linha
	 		   {
	 			   //adicionar novo LinearLayout no com uma linha nova para ícones de categorias PROGRAMATICAMENTE
	 			   LinearLayout novoLinearLayoutCategorias = new LinearLayout(contextoAplicacao);
	 			   RelativeLayout.LayoutParams parametrosNovoLinearLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	 			   parametrosNovoLinearLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
	 			   if(linearLayoutAtualParaAdicionar == null)
	 			   {
	 				   parametrosNovoLinearLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	 			   }
	 			   else
	 			   {
	 				   int idAntigoLinearLayoutAdicionarCategorias = linearLayoutAtualParaAdicionar.getId();
	 				   parametrosNovoLinearLayout.addRule(RelativeLayout.BELOW, idAntigoLinearLayoutAdicionarCategorias);
	 			   }
	 			   novoLinearLayoutCategorias.setLayoutParams(parametrosNovoLinearLayout);
	 			   novoLinearLayoutCategorias.setId(123456789 + i);
	 			   //RelativeLayout layoutIconesCategorias = (RelativeLayout) convertView.findViewById(R.id.campo_categorias);
	 			   
	 			   layoutIconesCategorias.addView(novoLinearLayoutCategorias);
	 			   linearLayoutAtualParaAdicionar = novoLinearLayoutCategorias;
	 			 //terminou de adicionar novo LinearLayout no com uma linha nova para ícones de categorias PROGRAMATICAMENTE
	 		   }
	 		   String nomeUmaCategoriaTreinada = categoriasTreinadasNaSala.get(i);
	 		   int idImagemCategoria = AssociaCategoriaComIcone.pegarIdImagemDaCategoria(contextoAplicacao, nomeUmaCategoriaTreinada);
	 		   if(idImagemCategoria != -1)
	 		   {
	 			   ImageView umImageViewCategoria = new ImageView(contextoAplicacao);
	 			   LinearLayout.LayoutParams parametrosNovaImageView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	 			   umImageViewCategoria.setLayoutParams(parametrosNovaImageView);
	 			   umImageViewCategoria.setImageResource(idImagemCategoria);
	 			   umImageViewCategoria.setPadding(1, 0, 1, 0);
	 			   linearLayoutAtualParaAdicionar.addView(umImageViewCategoria);
	 		   }
	 	   }*/
	 	   
	 	    /*convertView.setOnClickListener( new View.OnClickListener() { 
	 	     public void onClick(View v) {
	 	    	 
	 	     
	 	      
	 	     
	 	    	  
	 	     } 
	 	    }); */
	    }
	   
	  
	   
	  
	   return convertView;
	  
	  }
	  
	  private void setarIconeNivelDoJogador(ViewHolderSalasCriadas holder, String nivelDoJogador)
	  {
		  //vai ter varios ifs aqui de acordo com o nivel para mudar a figurinha do nivel do jogador
		  holder.nivelDoUsuario.setImageResource(R.drawable.icone_dan_1);
	  }
	  
	  
	  

}
