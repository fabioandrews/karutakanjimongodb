package bancodedados;

/**
 * interface de Activity que precisa esperar a task de pegar os kanjis terminar seu serviço
 * @author FábioPhillip
 *
 */
public interface ActivityQueEsperaAtePegarOsKanjis {
	public void procedimentoAposCarregarKanjis();
	public void procedimentoConexaoFalhou(); //executar procedimento caso a conexao com o BD tenha falhado
}
