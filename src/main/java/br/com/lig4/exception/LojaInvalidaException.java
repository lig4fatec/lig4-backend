package br.com.lig4.exception;

public class LojaInvalidaException extends RuntimeException {

    private final CodigoErro codigo;

    public LojaInvalidaException(CodigoErro codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public CodigoErro codigo() {
        return codigo;
    }
}
