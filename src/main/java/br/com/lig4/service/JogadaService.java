package br.com.lig4.service;

import br.com.lig4.domain.FaseSala;
import br.com.lig4.domain.JogadaRegistro;
import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.Sala;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.dto.JogadaHistoricoDTO;
import br.com.lig4.dto.JogadaRequestDTO;
import br.com.lig4.dto.JogadaResponseDTO;
import br.com.lig4.dto.JogadorResumoDTO;
import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.engine.Disco;
import br.com.lig4.engine.Jogada;
import br.com.lig4.engine.JogadaInvalidaException;
import br.com.lig4.engine.Jogo;
import br.com.lig4.engine.StatusJogo;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.PartidaInvalidaException;
import br.com.lig4.exception.PartidaNotFoundException;
import br.com.lig4.repository.JogadaRegistroRepository;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import br.com.lig4.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class JogadaService {

    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;
    private final JogadaRegistroRepository jogadaRepository;
    private final SalaRepository salaRepository;

    public JogadaService(PartidaRepository partidaRepository,
                         JogadorRepository jogadorRepository,
                         JogadaRegistroRepository jogadaRepository,
                         SalaRepository salaRepository) {
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
        this.jogadaRepository = jogadaRepository;
        this.salaRepository = salaRepository;
    }

    @Transactional
    public JogadaResponseDTO jogar(Long partidaId, JogadaRequestDTO dto) {
        Partida partida = partidaRepository.buscarComLock(partidaId)
                .orElseThrow(() -> new PartidaNotFoundException(partidaId));

        if (partida.status == StatusPartida.FINALIZADA) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "A partida já terminou.");
        }

        Disco disco = discoDe(partida, dto.jogadorId());

        List<JogadaRegistro> anteriores = jogadaRepository.findByPartidaIdOrderByNumeroAsc(partidaId);
        Jogo jogo = new Jogo(Disco.VERMELHO);
        for (JogadaRegistro anterior : anteriores) {
            jogo.jogar(discoDe(partida, anterior.jogador.id), anterior.coluna);
        }

        Jogada jogada = jogo.jogar(disco, dto.coluna());

        Jogador jogador = jogadorRepository.findById(dto.jogadorId())
                .orElseThrow(() -> new JogadorNotFoundException(dto.jogadorId()));

        JogadaRegistro registro = new JogadaRegistro();
        registro.partida = partida;
        registro.jogador = jogador;
        registro.numero = anteriores.size() + 1;
        registro.coluna = jogada.coluna();
        registro.linha = jogada.linha();
        jogadaRepository.save(registro);

        Long vencedorId = null;
        Long proximoJogadorId = null;
        if (jogo.getStatus() == StatusJogo.EM_ANDAMENTO) {
            proximoJogadorId = jogadorDe(partida, jogo.getTurno());
        } else {
            partida.status = StatusPartida.FINALIZADA;
            partida.finalizadoEm = Instant.now();
            if (jogo.getStatus() == StatusJogo.VENCEDOR) {
                partida.vencedor = jogador;
                vencedorId = jogador.id;
            }
            // a sala de origem também termina, e o front percebe ao consultá-la
            salaRepository.findByPartidaId(partidaId).ifPresent(sala -> sala.fase = FaseSala.ENCERRADA);
        }

        return new JogadaResponseDTO(
                registro.numero,
                jogador.id,
                registro.coluna,
                registro.linha,
                partida.status,
                jogo.getStatus(),
                vencedorId,
                proximoJogadorId,
                jogo.getTabuleiro().comoMatriz());
    }

    @Transactional(readOnly = true)
    public List<JogadaHistoricoDTO> historico(Long partidaId) {
        if (!partidaRepository.existsById(partidaId)) {
            throw new PartidaNotFoundException(partidaId);
        }
        return jogadaRepository.findByPartidaIdOrderByNumeroAsc(partidaId).stream()
                .map(JogadaHistoricoDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PartidaEstadoDTO estado(Long partidaId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new PartidaNotFoundException(partidaId));

        List<JogadaRegistro> jogadas = jogadaRepository.findByPartidaIdOrderByNumeroAsc(partidaId);
        Jogo jogo = new Jogo(Disco.VERMELHO);
        for (JogadaRegistro j : jogadas) {
            jogo.jogar(discoDe(partida, j.jogador.id), j.coluna);
        }

        Long vencedorId = jogo.getStatus() == StatusJogo.VENCEDOR
                ? jogadorDe(partida, jogo.getVencedor())
                : null;
        Long proximoJogadorId = jogo.getStatus() == StatusJogo.EM_ANDAMENTO
                ? jogadorDe(partida, jogo.getTurno())
                : null;

        // Na sala, a cor de cada jogador é a escolhida lá. Quem começa alterna a cada revanche,
        // então a cor é procurada pelo id do jogador, e não pela posição.
        Sala sala = salaRepository.findByPartidaId(partidaId).orElse(null);
        JogadorResumoDTO jogadorUm = resumoDe(partida.jogadorUm, sala);
        JogadorResumoDTO jogadorDois = resumoDe(partida.jogadorDois, sala);

        return new PartidaEstadoDTO(
                partida.id,
                jogadorUm,
                jogadorDois,
                partida.status,
                jogo.getStatus(),
                vencedorId,
                proximoJogadorId,
                jogadas.size(),
                jogo.getTabuleiro().comoMatriz(),
                sala == null ? null : sala.pin,
                sala == null ? null : sala.serie);
    }

    @Transactional(readOnly = true)
    public void validarParticipante(Long partidaId, Long jogadorId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new PartidaNotFoundException(partidaId));
        discoDe(partida, jogadorId);
    }

    private Disco discoDe(Partida partida, Long jogadorId) {
        if (jogadorId.equals(partida.jogadorUm.id)) {
            return Disco.VERMELHO;
        }
        if (jogadorId.equals(partida.jogadorDois.id)) {
            return Disco.AMARELO;
        }
        throw new PartidaInvalidaException("O jogador " + jogadorId + " não participa desta partida");
    }

    private Long jogadorDe(Partida partida, Disco disco) {
        return disco == Disco.VERMELHO ? partida.jogadorUm.id : partida.jogadorDois.id;
    }

    private JogadorResumoDTO resumoDe(Jogador jogador, Sala sala) {
    if (sala == null) {
        return JogadorResumoDTO.from(jogador);
    }
    String cor = jogador.id.equals(sala.dono.id) ? sala.corDono : sala.corConvidado;
    return new JogadorResumoDTO(jogador.id, jogador.nome, cor);
    }
}