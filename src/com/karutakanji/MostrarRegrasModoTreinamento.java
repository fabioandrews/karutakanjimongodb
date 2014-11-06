package com.karutakanji;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MostrarRegrasModoTreinamento extends ActivityDoJogoComSom implements View.OnClickListener 
{
	private boolean mostrarRegrasTreinamento; //pegarei da memoria do android o valor desse booleano e aqui eu modificarei
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mostrar_regras_modo_treinamento);
		
		ImageView imageViewCheckbox = (ImageView) findViewById(R.id.checkbox_nao_mostrar_novamente);
		imageViewCheckbox.setClickable(true);
		imageViewCheckbox.setOnClickListener(this);
		
		if(this.mostrarRegrasTreinamento == true)
		{
			mostrarRegrasTreinamento = false;
			imageViewCheckbox.setImageResource(R.drawable.checkbox_marcada_regras_treinamento);
		}
		else
		{
			mostrarRegrasTreinamento = true;
			imageViewCheckbox.setImageResource(R.drawable.checkbox_desmarcada_regras_treinamento);
		}
		
		ArmazenaMostrarRegrasTreinamento armazenaMostrarRegras = 
							ArmazenaMostrarRegrasTreinamento.getInstance();
		this.mostrarRegrasTreinamento = 
				armazenaMostrarRegras.getMostrarRegrasDoTreinamento(this);
		
		this.mudarFonteTodosOsTextosDessaTela();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mostrar_regras_modo_treinamento, menu);
		return true;
	}
	
	public void escolherCategoriasTreinamento(View v)
	{
		//antes disso, armazenar o valor da checkbox
		ArmazenaMostrarRegrasTreinamento armazenaMostrarRegras = 
				ArmazenaMostrarRegrasTreinamento.getInstance();
		armazenaMostrarRegras.alterarMostrarRegrasDoTreinamento(this, this.mostrarRegrasTreinamento);
		
		Intent criaTelaModoTreinamento =
				new Intent(this, EscolherCategoriasModoTreinamento.class);
		startActivity(criaTelaModoTreinamento);
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
			case R.id.checkbox_nao_mostrar_novamente:
				ImageView imageViewCheckbox = (ImageView) findViewById(R.id.checkbox_nao_mostrar_novamente);
				if(this.mostrarRegrasTreinamento == true)
				{
					mostrarRegrasTreinamento = false;
					imageViewCheckbox.setImageResource(R.drawable.checkbox_marcada_regras_treinamento);
				}
				else
				{
					mostrarRegrasTreinamento = true;
					imageViewCheckbox.setImageResource(R.drawable.checkbox_desmarcada_regras_treinamento);
				}
			break;
		}
		
	}
	
	private void mudarFonteTodosOsTextosDessaTela()
	{
		String fontPath = "fonts/Wonton.ttf";
		// Loading Font Face
	    Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);

	    // Applying font
	    TextView textoTitulo = (TextView) findViewById(R.id.textoTituloTreinamento);
	    TextView instrucoes = (TextView) findViewById(R.id.instrucoes);
	    TextView descricaoTreinamento = (TextView) findViewById(R.id.descricao_modo_treinamento);
	    TextView textoNaoMostrarNovamente = (TextView) findViewById(R.id.texto_nao_mostrar_novamente);
	    
	    textoTitulo.setTypeface(tf);
	    instrucoes.setTypeface(tf);
	    descricaoTreinamento.setTypeface(tf);
	    textoNaoMostrarNovamente.setTypeface(tf);
	    
	    Button botaoOk = (Button) findViewById(R.id.botao_proximo);
		botaoOk.setTypeface(tf);
	    
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}

}
