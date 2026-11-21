package br.com.lig4.dto;

import br.com.lig4.domain.StatusPartida;
import br.com.lig4.engine.StatusJogo;

public record JogadaResponseDTO(
        int numero,
        Long jogadorId,
        int coluna,
        int linha,
        StatusPartida statusPartida,
        StatusJogo resultado,
        Long vencedorId,
        Long proximoJogadorId,
        String[][] tabuleiro,
        /** De quem é o relógio que está correndo agora (o próximo a jogar); nulo se a partida terminou. */
        Long relogioDe,
        long tempoJogadorUmMs,
        long tempoJogadorDoisMs
) {
}