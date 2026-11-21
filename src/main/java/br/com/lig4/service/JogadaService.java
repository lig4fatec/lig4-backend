package br.com.lig4.service;

import br.com.lig4.domain.FaseSala;
import br.com.lig4.domain.JogadaRegistro;
import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.ModoJogo;
import br.com.lig4.domain.MotivoFim;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.Sala;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.domain.TipoJogada;
import br.com.lig4.dto.CongelamentoDTO;
import br.com.lig4.dto.JogadaHistoricoDTO;
import br.com.lig4.dto.JogadaRequestDTO;
import br.com.lig4.dto.JogadaResponseDTO;
import br.com.lig4.dto.JogadorResumoDTO;
import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.dto.PoderDTO;
import br.com.lig4.engine.Disco;
import br.com.lig4.engine.Jogada;
import br.com.lig4.engine.JogadaInvalidaException;
import br.com.lig4.engine.Jogo;
import br.com.lig4.engine.Poder;
import br.com.lig4.engine.StatusJogo;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.PartidaInvalidaException;
import br.com.lig4.exception.PartidaNotFoundException;
import br.com.lig4.repository.JogadaRegistroRepository;
import br.com.lig4.repository.ItemJogadorRepository;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import br.com.lig4.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JogadaService {

    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;
    private final JogadaRegistroRepository jogadaRepository;
    private final SalaRepository salaRepository;
    private final RecompensaService recompensas;
    private final RelogioPartida relogio;
    private final ItemJogadorRepository itensRepository;

    public JogadaService(PartidaRepository partidaRepository,
                         JogadorRepository jogadorRepository,
                         JogadaRegistroRepository jogadaRepository,
                         SalaRepository salaRepository,
                         RecompensaService recompensas,
                         RelogioPartida relogio,
                         ItemJogadorRepository itensRepository) {
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
        this.jogadaRepository = jogadaRepository;
        this.salaRepository = salaRepository;
        this.recompensas = recompensas;
        this.relogio = relogio;
        this.itensRepository = itensRepository;
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
        Jogo jogo = reproduzir(partida, anteriores);

        Jogada jogada = jogo.jogar(disco, dto.coluna());

        // Quem estourou o tempo não joga mais: o agendador termina a partida por TEMPO em até 1 segundo.
        Instant agora = Instant.now();
        if (relogio.ativo(partida) && relogio.restante(partida, dto.jogadorId(), agora) <= 0) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "O seu tempo acabou.");
        }

        Jogador jogador = jogadorRepository.findById(dto.jogadorId())
                .orElseThrow(() -> new JogadorNotFoundException(dto.jogadorId()));

        JogadaRegistro registro = new JogadaRegistro();
        registro.partida = partida;
        registro.jogador = jogador;
        registro.numero = anteriores.size() + 1;
        registro.coluna = jogada.coluna();
        registro.linha = jogada.linha();
        jogadaRepository.save(registro);

        // jogar depois de receber uma proposta de empate é recusá-la
        if (partida.empatePendenteDe != null && !partida.empatePendenteDe.id.equals(jogador.id)) {
            partida.empatePendenteDe = null;
        }

        Long vencedorId = null;
        Long proximoJogadorId = null;
        if (jogo.getStatus() == StatusJogo.EM_ANDAMENTO) {
            proximoJogadorId = jogadorDe(partida, jogo.getTurno());
            Jogador proximo = proximoJogadorId.equals(partida.jogadorUm.id) ? partida.jogadorUm : partida.jogadorDois;
            relogio.passar(partida, jogador, proximo, agora);
        } else {
            relogio.passar(partida, jogador, null, agora);
            partida.status = StatusPartida.FINALIZADA;
            partida.finalizadoEm = Instant.now();
            partida.empatePendenteDe = null;
            if (jogo.getStatus() == StatusJogo.VENCEDOR) {
                partida.vencedor = jogador;
                partida.motivoFim = MotivoFim.VITORIA;
                vencedorId = jogador.id;
            } else {
                partida.motivoFim = MotivoFim.TABULEIRO_CHEIO;
            }
            // moedas de vitória, derrota ou empate (a partida só termina uma vez)
            recompensas.conceder(partida);
            // a sala de origem também termina
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
                jogo.getTabuleiro().comoMatriz(),
                partida.relogioDe == null ? null : partida.relogioDe.id,
                relogio.restante(partida, partida.jogadorUm.id, agora),
                relogio.restante(partida, partida.jogadorDois.id, agora),
                congelamentosDe(partida, jogo),
                jogo.isPecaExtraAtiva());
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

        Instant agora = Instant.now();
        List<JogadaRegistro> jogadas = jogadaRepository.findByPartidaIdOrderByNumeroAsc(partidaId);
        Jogo jogo = reproduzir(partida, jogadas);
        int totalJogadas = (int) jogadas.stream().filter(j -> j.tipo == TipoJogada.PECA).count();

        StatusJogo resultado = jogo.getStatus();
        Long vencedorId = resultado == StatusJogo.VENCEDOR
                ? jogadorDe(partida, jogo.getVencedor())
                : null;
        Long proximoJogadorId = resultado == StatusJogo.EM_ANDAMENTO
                ? jogadorDe(partida, jogo.getTurno())
                : null;

        // Desistência, empate combinado e tempo terminam a partida sem a última jogada: vale o que foi gravado nela.
        if (partida.status == StatusPartida.FINALIZADA) {
            vencedorId = partida.vencedor == null ? null : partida.vencedor.id;
            resultado = partida.vencedor == null ? StatusJogo.EMPATE : StatusJogo.VENCEDOR;
            proximoJogadorId = null;
        }

        List<Long> empatesUsados = new ArrayList<>();
        if (partida.empateUsadoUm) {
            empatesUsados.add(partida.jogadorUm.id);
        }
        if (partida.empateUsadoDois) {
            empatesUsados.add(partida.jogadorDois.id);
        }
        Long empatePropostoPor = partida.status == StatusPartida.EM_ANDAMENTO && partida.empatePendenteDe != null
                ? partida.empatePendenteDe.id
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
                resultado,
                vencedorId,
                proximoJogadorId,
                totalJogadas,
                jogo.getTabuleiro().comoMatriz(),
                sala == null ? null : sala.pin,
                partida.modo,
                partida.motivoFim,
                empatePropostoPor,
                empatesUsados,
                partida.relogioDe == null ? null : partida.relogioDe.id,
                relogio.restante(partida, partida.jogadorUm.id, agora),
                relogio.restante(partida, partida.jogadorDois.id, agora),
                poderesUsadosDe(partida, jogo),
                poderesDisponiveisDe(partida, jogo),
                congelamentosDe(partida, jogo),
                jogo.isPecaExtraAtiva());
    }

    @Transactional(readOnly = true)
    public void validarParticipante(Long partidaId, Long jogadorId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new PartidaNotFoundException(partidaId));
        discoDe(partida, jogadorId);
    }

    /** Refaz a partida do começo, jogada por jogada (peças e poderes), para chegar à situação atual. */
    Jogo reproduzir(Partida partida, List<JogadaRegistro> registros) {
        Jogo jogo = new Jogo(Disco.VERMELHO);
        for (JogadaRegistro r : registros) {
            Disco disco = discoDe(partida, r.jogador.id);
            switch (r.tipo) {
                case PECA -> jogo.jogar(disco, r.coluna);
                case QUEIMAR -> jogo.queimar(disco, r.coluna, r.linha);
                case CONGELAR -> jogo.congelar(disco, r.coluna);
                case EXTRA -> jogo.pecaExtra(disco);
            }
        }
        return jogo;
    }

    private List<PoderDTO> poderesUsadosDe(Partida partida, Jogo jogo) {
        List<PoderDTO> usados = new ArrayList<>();
        for (Disco disco : Disco.values()) {
            Long jogadorId = jogadorDe(partida, disco);
            for (Poder poder : jogo.getPoderesUsados(disco)) {
                usados.add(new PoderDTO(jogadorId, poder));
            }
        }
        return usados;
    }

    /** Poderes comprados e ainda não usados. Só existem no Caos, com a partida em andamento. */
    private List<PoderDTO> poderesDisponiveisDe(Partida partida, Jogo jogo) {
        List<PoderDTO> disponiveis = new ArrayList<>();
        if (partida.modo != ModoJogo.CAOS || partida.status != StatusPartida.EM_ANDAMENTO) {
            return disponiveis;
        }
        for (Disco disco : Disco.values()) {
            Long jogadorId = jogadorDe(partida, disco);
            Set<String> comprados = itensRepository.findByJogadorId(jogadorId).stream()
                    .map(item -> item.itemId)
                    .collect(Collectors.toSet());
            for (Poder poder : Poder.values()) {
                if (comprados.contains(poder.itemId()) && !jogo.getPoderesUsados(disco).contains(poder)) {
                    disponiveis.add(new PoderDTO(jogadorId, poder));
                }
            }
        }
        return disponiveis;
    }

    List<CongelamentoDTO> congelamentosDe(Partida partida, Jogo jogo) {
        return jogo.getCongelamentos().stream()
                .map(c -> new CongelamentoDTO(c.coluna(), jogadorDe(partida, c.afetado()), c.turnosRestantes()))
                .toList();
    }

    Disco discoDe(Partida partida, Long jogadorId) {
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