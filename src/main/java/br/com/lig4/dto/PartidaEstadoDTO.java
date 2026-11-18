package br.com.lig4.dto;

import br.com.lig4.domain.ModoJogo;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.engine.StatusJogo;

/**
 * Situação da partida. Quando ela nasceu de uma sala, vem também o PIN, e a cor de cada jogador
 * passa a ser a escolhida na sala. Sem sala, o pin é nulo e a cor é a do perfil.
 */
public record PartidaEstadoDTO(
        Long partidaId,
        JogadorResumoDTO jogadorUm,
        JogadorResumoDTO jogadorDois,
        StatusPartida statusPartida,
        StatusJogo resultado,
        Long vencedorId,
        Long proximoJogadorId,
        int totalJogadas,
        String[][] tabuleiro,
        String pin,
        ModoJogo modo
) {
}