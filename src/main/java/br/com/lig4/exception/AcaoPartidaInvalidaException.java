package br.com.lig4.exception;

public class AcaoPartidaInvalidaException extends RuntimeException {

    private final CodigoErro codigo;

    public AcaoPartidaInvalidaException(CodigoErro codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public CodigoErro codigo() {
        return codigo;
    }
}