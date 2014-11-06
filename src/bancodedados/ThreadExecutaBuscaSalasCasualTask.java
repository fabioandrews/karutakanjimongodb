package bancodedados;

import android.app.ProgressDialog;

import com.karutakanji.ModoCasual;

/*essa thread vai ficar chamando o buscasalasmodocasualtask varias vezes*/
public class ThreadExecutaBuscaSalasCasualTask extends Thread
{
	private ModoCasual telaModoCasual;
	private ProgressDialog loadingBuscandoSalas;
	
	public ThreadExecutaBuscaSalasCasualTask(ModoCasual telaCasual, ProgressDialog loadingSalas)
	{
		this.telaModoCasual = telaCasual;
		loadingBuscandoSalas = loadingSalas;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try 
			{
				BuscaSalasModoCasualTask buscaSalasTask = new BuscaSalasModoCasualTask(loadingBuscandoSalas, telaModoCasual);
				buscaSalasTask.execute("");
				Thread.sleep(5000);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return;
			}
		}
	}
	
	


}
