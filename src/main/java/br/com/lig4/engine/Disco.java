package br.com.lig4.engine;

/** Peça de cada jogador. Cada jogador joga com uma cor. */
public enum Disco {
    VERMELHO,
    AMARELO;

    public Disco oposto() {
        return this == VERMELHO ? AMARELO : VERMELHO;
    }
}
