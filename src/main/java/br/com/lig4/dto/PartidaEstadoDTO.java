package br.com.lig4.dto;

import br.com.lig4.domain.ModoJogo;
import br.com.lig4.domain.MotivoFim;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.engine.StatusJogo;

import java.util.List;

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
        ModoJogo modo,
        /** Como a partida terminou; nulo enquanto ela está em andamento. */
        MotivoFim motivoFim,
        /** Quem propôs um empate que ainda não foi respondido (nulo se não há proposta). */
        Long empatePropostoPor,
        /** Ids dos jogadores que já usaram a proposta de empate desta partida. */
        List<Long> empatesUsados,
        /** De quem é o relógio que está correndo (o jogador da vez); nulo se a partida terminou. */
        Long relogioDe,
        /** Tempo que o jogador 1 ainda tem, em milissegundos. */
        long tempoJogadorUmMs,
        /** Tempo que o jogador 2 ainda tem, em milissegundos. */
        long tempoJogadorDoisMs,
        /** Poderes já usados na partida, de cada jogador. */
        List<PoderDTO> poderesUsados,
        /** Poderes que cada jogador comprou e ainda pode usar (vazio fora do Caos e depois do fim). */
        List<PoderDTO> poderesDisponiveis,
        /** Colunas congeladas agora. */
        List<CongelamentoDTO> congelamentos,
        /** Verdadeiro quando o jogador da vez usou a Peça Extra e ainda vai soltar duas peças. */
        boolean pecaExtraAtiva
) {
}