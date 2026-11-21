package br.com.lig4.service;

import br.com.lig4.repository.PartidaRepository;
import br.com.lig4.websocket.PartidaWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** A cada segundo, termina por tempo as partidas em que o jogador da vez ficou sem relógio. */
@Component
public class RelogioAgendador {

    private static final Logger log = LoggerFactory.getLogger(RelogioAgendador.class);

    private final PartidaRepository partidaRepository;
    private final PartidaAcaoService acaoService;
    private final PartidaWebSocketHandler webSocket;

    public RelogioAgendador(PartidaRepository partidaRepository,
                            PartidaAcaoService acaoService,
                            PartidaWebSocketHandler webSocket) {
        this.partidaRepository = partidaRepository;
        this.acaoService = acaoService;
        this.webSocket = webSocket;
    }

    @Scheduled(fixedDelay = 1000)
    public void verificar() {
        for (Long partidaId : partidaRepository.comTempoEsgotado(Instant.now())) {
            try {
                acaoService.encerrarPorTempo(partidaId).ifPresent(estado -> {
                    log.info("Tempo esgotado: partida={} vencedor={}", partidaId, estado.vencedorId());
                    webSocket.transmitirEstado(partidaId, estado);
                });
            } catch (RuntimeException e) {
                log.warn("Falha ao encerrar por tempo a partida {}: {}", partidaId, e.getMessage());
            }
        }
    }
}
