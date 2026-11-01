package br.com.lig4.exception;

public class FiltroInvalidoException extends RuntimeException {

    public FiltroInvalidoException(String filtro) {
        super("Filtro inválido: " + filtro + ". Use todos, online ou diario");
    }
}