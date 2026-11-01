package br.com.lig4.exception;

public record ErroResponse(String codigo, String mensagem) {

    public static ErroResponse de(CodigoErro codigo, String mensagem) {
        return new ErroResponse(codigo.name(), mensagem);
    }
}
