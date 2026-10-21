package br.com.lig4.engine;

/** Resultado de uma jogada aceita: onde a peça caiu e quem jogou. */
public record Jogada(Disco disco, int coluna, int linha) {
    public Posicao posicao() {
        return new Posicao(linha, coluna);
    }
}
