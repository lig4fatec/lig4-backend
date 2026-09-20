package br.com.lig4.dto;

import br.com.lig4.domain.Partida;
import br.com.lig4.domain.StatusPartida;

import java.time.Instant;

public record PartidaResponseDTO(
        Long id,
        JogadorResumoDTO jogadorUm,
        JogadorResumoDTO jogadorDois,
        JogadorResumoDTO vencedor,
        StatusPartida status,
        Instant criadoEm,
        Instant finalizadoEm
) {
    public static PartidaResponseDTO from(Partida partida) {
        return new PartidaResponseDTO(
                partida.id,
                JogadorResumoDTO.from(partida.jogadorUm),
                JogadorResumoDTO.from(partida.jogadorDois),
                partida.vencedor != null ? JogadorResumoDTO.from(partida.vencedor) : null,
                partida.status,
                partida.criadoEm,
                partida.finalizadoEm
        );
    }
}
