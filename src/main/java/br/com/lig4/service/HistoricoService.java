package br.com.lig4.service;

import br.com.lig4.domain.ModoJogo;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.ResultadoPartida;
import br.com.lig4.dto.HistoricoPartidaDTO;
import br.com.lig4.dto.HistoricoResponseDTO;
import br.com.lig4.exception.FiltroInvalidoException;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.repository.JogadaRegistroRepository;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class HistoricoService {

    private static final String FILTRO_TODOS = "todos";

    private final JogadorRepository jogadores;
    private final PartidaRepository partidas;
    private final JogadaRegistroRepository jogadas;

    public HistoricoService(JogadorRepository jogadores,
                            PartidaRepository partidas,
                            JogadaRegistroRepository jogadas) {
        this.jogadores = jogadores;
        this.partidas = partidas;
        this.jogadas = jogadas;
    }

    @Transactional(readOnly = true)
    public HistoricoResponseDTO listar(Long jogadorId, String filtro) {
        if (!jogadores.existsById(jogadorId)) {
            throw new JogadorNotFoundException(jogadorId);
        }
        // Filtro: "todos" ou o modo do jogo ("classico"/"caos"). O front manda esses valores.
        String filtroNormalizado = filtro == null || filtro.isBlank()
                ? FILTRO_TODOS
                : filtro.trim().toLowerCase(Locale.ROOT);
        ModoJogo modoFiltro = null;
        if (!filtroNormalizado.equals(FILTRO_TODOS)) {
            modoFiltro = ModoJogo.de(filtroNormalizado);
            if (modoFiltro == null) {
                throw new FiltroInvalidoException(filtro);
            }
        }

        List<Partida> finalizadas = partidas.finalizadasDoJogador(jogadorId);
        if (modoFiltro != null) {
            ModoJogo alvo = modoFiltro;
            finalizadas = finalizadas.stream().filter(partida -> partida.modo == alvo).toList();
        }
        Map<Long, Integer> totalDeJogadas = contarJogadas(finalizadas);

        List<HistoricoPartidaDTO> itens = finalizadas.stream()
                .map(partida -> new HistoricoPartidaDTO(
                        partida.id,
                        partida.finalizadoEm,
                        adversarioDe(partida, jogadorId),
                        resultadoDe(partida, jogadorId),
                        totalDeJogadas.getOrDefault(partida.id, 0),
                        partida.modo))
                .toList();
        return new HistoricoResponseDTO(itens);
    }

    private Map<Long, Integer> contarJogadas(List<Partida> finalizadas) {
        Map<Long, Integer> total = new HashMap<>();
        if (finalizadas.isEmpty()) {
            return total;
        }
        List<Long> ids = finalizadas.stream().map(partida -> partida.id).toList();
        for (Object[] linha : jogadas.contarPorPartida(ids)) {
            total.put((Long) linha[0], ((Long) linha[1]).intValue());
        }
        return total;
    }

    private String adversarioDe(Partida partida, Long jogadorId) {
        return partida.jogadorUm.id.equals(jogadorId) ? partida.jogadorDois.nome : partida.jogadorUm.nome;
    }

    private ResultadoPartida resultadoDe(Partida partida, Long jogadorId) {
        if (partida.vencedor == null) {
            return ResultadoPartida.EMPATE;
        }
        return partida.vencedor.id.equals(jogadorId) ? ResultadoPartida.VITORIA : ResultadoPartida.DERROTA;
    }
}