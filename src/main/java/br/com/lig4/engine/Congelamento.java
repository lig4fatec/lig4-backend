package br.com.lig4.engine;

/** Coluna bloqueada para um jogador. {@code turnosRestantes} conta as vezes dele que ainda faltam. */
public record Congelamento(int coluna, Disco afetado, int turnosRestantes) {
}
