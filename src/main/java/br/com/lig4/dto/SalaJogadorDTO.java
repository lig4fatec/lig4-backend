package br.com.lig4.dto;

public record SalaJogadorDTO(
        Long jogadorId,
        String nome,
        String cor,
        /** Id do avatar equipado, para o front mostrar o avatar do jogador na sala. */
        String avatar
) {
}
