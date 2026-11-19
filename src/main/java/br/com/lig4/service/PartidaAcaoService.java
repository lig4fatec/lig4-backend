package br.com.lig4.service;

import br.com.lig4.domain.FaseSala;
import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.MotivoFim;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.engine.JogadaInvalidaException;
import br.com.lig4.exception.AcaoPartidaInvalidaException;
import br.com.lig4.exception.CodigoErro;
import br.com.lig4.exception.PartidaInvalidaException;
import br.com.lig4.exception.PartidaNotFoundException;
import br.com.lig4.repository.PartidaRepository;
import br.com.lig4.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Desistência e proposta de empate: as formas de terminar a partida sem ligar quatro peças. */
@Service
public class PartidaAcaoService {

    private final PartidaRepository partidaRepository;
    private final SalaRepository salaRepository;
    private final RecompensaService recompensas;
    private final JogadaService jogadaService;

    public PartidaAcaoService(PartidaRepository partidaRepository,
                              SalaRepository salaRepository,
                              RecompensaService recompensas,
                              JogadaService jogadaService) {
        this.partidaRepository = partidaRepository;
        this.salaRepository = salaRepository;
        this.recompensas = recompensas;
        this.jogadaService = jogadaService;
    }

    /** Quem desiste perde: o adversário vence e cada um recebe as moedas de vitória e de derrota do modo. */
    @Transactional
    public PartidaEstadoDTO desistir(Long partidaId, Long jogadorId) {
        Partida partida = emAndamento(partidaId, jogadorId);
        partida.vencedor = ehJogadorUm(partida, jogadorId) ? partida.jogadorDois : partida.jogadorUm;
        encerrar(partida, MotivoFim.DESISTENCIA);
        return jogadaService.estado(partidaId);
    }

    /** Cada jogador pode propor empate uma vez por partida, e só uma proposta fica aberta por vez. */
    @Transactional
    public PartidaEstadoDTO proporEmpate(Long partidaId, Long jogadorId) {
        Partida partida = emAndamento(partidaId, jogadorId);
        if (partida.empatePendenteDe != null) {
            throw new AcaoPartidaInvalidaException(CodigoErro.EMPATE_PENDENTE,
                    "Já existe uma proposta de empate aguardando resposta.");
        }
        boolean um = ehJogadorUm(partida, jogadorId);
        if (um ? partida.empateUsadoUm : partida.empateUsadoDois) {
            throw new AcaoPartidaInvalidaException(CodigoErro.EMPATE_JA_PROPOSTO,
                    "Você já usou a proposta de empate desta partida.");
        }
        if (um) {
            partida.empateUsadoUm = true;
            partida.empatePendenteDe = partida.jogadorUm;
        } else {
            partida.empateUsadoDois = true;
            partida.empatePendenteDe = partida.jogadorDois;
        }
        return jogadaService.estado(partidaId);
    }

    /** O adversário de quem propôs aceita: a partida termina empatada e os dois recebem as moedas de empate. */
    @Transactional
    public PartidaEstadoDTO aceitarEmpate(Long partidaId, Long jogadorId) {
        Partida partida = emAndamento(partidaId, jogadorId);
        exigirPropostaDoAdversario(partida, jogadorId);
        partida.vencedor = null;
        encerrar(partida, MotivoFim.EMPATE_ACORDADO);
        return jogadaService.estado(partidaId);
    }

    /** O adversário de quem propôs recusa: a partida continua e quem propôs não pode propor de novo. */
    @Transactional
    public PartidaEstadoDTO recusarEmpate(Long partidaId, Long jogadorId) {
        Partida partida = emAndamento(partidaId, jogadorId);
        exigirPropostaDoAdversario(partida, jogadorId);
        partida.empatePendenteDe = null;
        return jogadaService.estado(partidaId);
    }

    private void exigirPropostaDoAdversario(Partida partida, Long jogadorId) {
        if (partida.empatePendenteDe == null || partida.empatePendenteDe.id.equals(jogadorId)) {
            throw new AcaoPartidaInvalidaException(CodigoErro.SEM_PROPOSTA_DE_EMPATE,
                    "O adversário não propôs empate.");
        }
    }

    /** Trava a partida e confere que ela está em andamento e que o jogador participa dela. */
    private Partida emAndamento(Long partidaId, Long jogadorId) {
        Partida partida = partidaRepository.buscarComLock(partidaId)
                .orElseThrow(() -> new PartidaNotFoundException(partidaId));
        if (!jogadorId.equals(partida.jogadorUm.id) && !jogadorId.equals(partida.jogadorDois.id)) {
            throw new PartidaInvalidaException("O jogador " + jogadorId + " não participa desta partida");
        }
        if (partida.status == StatusPartida.FINALIZADA) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "A partida já terminou.");
        }
        return partida;
    }

    private boolean ehJogadorUm(Partida partida, Long jogadorId) {
        return jogadorId.equals(partida.jogadorUm.id);
    }


    private void encerrar(Partida partida, MotivoFim motivo) {
        partida.status = StatusPartida.FINALIZADA;
        partida.motivoFim = motivo;
        partida.finalizadoEm = Instant.now();
        partida.empatePendenteDe = null;
        recompensas.conceder(partida);
        salaRepository.findByPartidaId(partida.id).ifPresent(sala -> sala.fase = FaseSala.ENCERRADA);
    }
}
