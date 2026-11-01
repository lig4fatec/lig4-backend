package br.com.lig4.dto;

import br.com.lig4.domain.Jogador;

import java.time.Instant;

public record JogadorResponseDTO(
        Long id,
        String nome,
        String email,
        Instant criadoEm,
        String cor
) {
    public static JogadorResponseDTO from(Jogador jogador) {
        return new JogadorResponseDTO(jogador.id, jogador.nome, jogador.email, jogador.criadoEm, jogador.cor);
    }
}