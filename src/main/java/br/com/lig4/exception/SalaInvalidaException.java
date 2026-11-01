package br.com.lig4.exception;

public class SalaInvalidaException extends RuntimeException {

    private final CodigoErro codigo;

    public SalaInvalidaException(CodigoErro codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public CodigoErro codigo() {
        return codigo;
    }
}
