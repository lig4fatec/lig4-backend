package br.com.lig4.dto;

import br.com.lig4.domain.Jogador;

public record PerfilResponseDTO(
        Long id,
        String email,
        String nick,
        String nomeCompleto,
        String bio,
        String cor,
        long vitorias,
        long derrotas,
        int moedas
) {
    private static final String COR_PADRAO = "blue";

    public static PerfilResponseDTO from(Jogador jogador, long vitorias, long derrotas) {
        return new PerfilResponseDTO(
                jogador.id,
                jogador.email,
                jogador.nome,
                jogador.nomeCompleto == null ? "" : jogador.nomeCompleto,
                jogador.bio == null ? "" : jogador.bio,
                jogador.cor == null ? COR_PADRAO : jogador.cor,
                vitorias,
                derrotas,
                jogador.moedas);
    }
}
