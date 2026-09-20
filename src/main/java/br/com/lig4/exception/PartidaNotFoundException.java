package br.com.lig4.exception;

public class PartidaNotFoundException extends RuntimeException {

    public PartidaNotFoundException(Long id) {
        super("Partida não encontrada com id: " + id);
    }
}
