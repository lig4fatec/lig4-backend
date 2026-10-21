package br.com.lig4.engine;

/** Lançada quando uma jogada viola as regras. O motivo permite montar a mensagem de erro da API. */
public class JogadaInvalidaException extends RuntimeException {

    public enum Motivo {
        JOGO_ENCERRADO,
        FORA_DO_TURNO,
        COLUNA_INVALIDA,
        COLUNA_CHEIA
    }

    private final Motivo motivo;

    public JogadaInvalidaException(Motivo motivo, String mensagem) {
        super(mensagem);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
