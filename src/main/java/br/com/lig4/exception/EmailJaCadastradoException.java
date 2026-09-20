package br.com.lig4.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Já existe um jogador cadastrado com o email: " + email);
    }
}
