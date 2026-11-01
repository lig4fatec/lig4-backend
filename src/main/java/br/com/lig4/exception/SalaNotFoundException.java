package br.com.lig4.exception;

public class SalaNotFoundException extends RuntimeException {

    public SalaNotFoundException(String pin) {
        super("Sala não encontrada com o PIN: " + pin);
    }
}