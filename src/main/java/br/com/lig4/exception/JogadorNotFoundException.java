package br.com.lig4.exception;

public class JogadorNotFoundException extends RuntimeException {

    public JogadorNotFoundException(Long id) {
        super("Jogador não encontrado com id: " + id);
    }
}
