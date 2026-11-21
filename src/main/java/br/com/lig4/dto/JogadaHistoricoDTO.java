package br.com.lig4.dto;

import br.com.lig4.domain.JogadaRegistro;
import br.com.lig4.domain.TipoJogada;

import java.time.Instant;

public record JogadaHistoricoDTO(
        int numero,
        Long jogadorId,
        /** PECA, QUEIMAR, CONGELAR ou EXTRA. */
        TipoJogada tipo,
        int coluna,
        int linha,
        Instant criadoEm
) {
    public static JogadaHistoricoDTO from(JogadaRegistro j) {
        return new JogadaHistoricoDTO(j.numero, j.jogador.id, j.tipo, j.coluna, j.linha, j.criadoEm);
    }
}