package com.karutakanji;

public class RetornaNomeCategoriaEscritaEmKanji 
{
	public static String retornarNomeCategoriaEscritaEmKanji(String categoria)
	{
		if(categoria.compareTo("Adjetivos") == 0)
		{
			return "形容詞";
		}
		else if(categoria.compareTo("Cotidiano") == 0)
		{
			return "日常";
		}
		else if(categoria.compareTo("Educação") == 0)
		{
			return "教育";
		}
		else if(categoria.compareTo("Geografia") == 0)
		{
			return "地理学";
		}
		else if(categoria.compareTo("Lazer") == 0)
		{
			return "余暇";
		}
		else if(categoria.compareTo("Lugar") == 0)
		{
			return "場所";
		}
		else if(categoria.compareTo("Natureza") == 0)
		{
			return "自然";
		}
		else if(categoria.compareTo("Saúde") == 0)
		{
			return "健康";
		}
		else if(categoria.compareTo("Supermercado") == 0)
		{
			return "スーパー";
		}
		else if(categoria.compareTo("Tempo") == 0)
		{
			return "時間";
		}
		else if(categoria.compareTo("Trabalho") == 0)
		{
			return "仕事";
		}
		else if(categoria.compareTo("Verbo") == 0 || categoria.compareTo("Verbos") == 0)
		{
			return "動詞";
		}
		else
		{
			return "";
		}
	}

}
