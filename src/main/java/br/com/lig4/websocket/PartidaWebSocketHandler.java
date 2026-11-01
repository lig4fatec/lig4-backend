package br.com.lig4.websocket;

import br.com.lig4.dto.JogadaRequestDTO;
import br.com.lig4.dto.JogadaResponseDTO;
import br.com.lig4.engine.JogadaInvalidaException;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.PartidaInvalidaException;
import br.com.lig4.exception.PartidaNotFoundException;
import br.com.lig4.service.JogadaService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comunicação em tempo real de uma partida. Cada partida é uma "sala":
 * as jogadas aceitas são transmitidas aos dois jogadores conectados.
 */
@Component
public class PartidaWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PartidaWebSocketHandler.class);
    private static final String ATTR_PARTIDA = "partidaId";
    private static final String ATTR_JOGADOR = "jogadorId";

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Comando(String tipo, Integer coluna) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Mensagem(String tipo, Object dados, String mensagem) {
    }

    private final JogadaService jogadaService;
    private final ObjectMapper mapper;
    private final Map<Long, Set<WebSocketSession>> salas = new ConcurrentHashMap<>();

    public PartidaWebSocketHandler(JogadaService jogadaService, ObjectMapper mapper) {
        this.jogadaService = jogadaService;
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long partidaId;
        Long jogadorId;
        try {
            partidaId = partidaIdDe(session);
            jogadorId = jogadorIdDe(session);
            jogadaService.validarParticipante(partidaId, jogadorId);
        } catch (RuntimeException e) {
            enviar(session, new Mensagem("ERRO", null, mensagemDe(e)));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        session.getAttributes().put(ATTR_PARTIDA, partidaId);
        session.getAttributes().put(ATTR_JOGADOR, jogadorId);
        salas.computeIfAbsent(partidaId, k -> ConcurrentHashMap.newKeySet()).add(session);

        enviar(session, new Mensagem("ESTADO", jogadaService.estado(partidaId), null));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long partidaId = (Long) session.getAttributes().get(ATTR_PARTIDA);
        Long jogadorId = (Long) session.getAttributes().get(ATTR_JOGADOR);
        if (partidaId == null || jogadorId == null) {
            return;
        }

        try {
            Comando comando = lerComando(message.getPayload());
            switch (comando.tipo()) {
                case "JOGAR" -> {
                    if (comando.coluna() == null) {
                        throw new PartidaInvalidaException("Informe a coluna (0 a 6).");
                    }
                    JogadaResponseDTO resultado = jogadaService.jogar(partidaId,
                            new JogadaRequestDTO(jogadorId, comando.coluna()));
                    transmitir(partidaId, new Mensagem("JOGADA", resultado, null));
                }
                case "ESTADO" -> enviar(session,
                        new Mensagem("ESTADO", jogadaService.estado(partidaId), null));
                default -> throw new PartidaInvalidaException(
                        "Tipo de mensagem desconhecido: " + comando.tipo());
            }
        } catch (RuntimeException e) {
            enviar(session, new Mensagem("ERRO", null, mensagemDe(e)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long partidaId = (Long) session.getAttributes().get(ATTR_PARTIDA);
        if (partidaId == null) {
            return;
        }
        Set<WebSocketSession> sala = salas.get(partidaId);
        if (sala != null) {
            sala.remove(session);
            if (sala.isEmpty()) {
                salas.remove(partidaId);
            }
        }
    }

    private Comando lerComando(String json) {
        Comando comando;
        try {
            comando = mapper.readValue(json, Comando.class);
        } catch (RuntimeException e) {
            comando = null;
        }
        if (comando == null || comando.tipo() == null) {
            throw new PartidaInvalidaException(
                    "Mensagem inválida. Envie JSON no formato {\"tipo\":\"JOGAR\",\"coluna\":3}.");
        }
        return comando;
    }

    private void transmitir(Long partidaId, Mensagem mensagem) {
        Set<WebSocketSession> sala = salas.get(partidaId);
        if (sala == null) {
            return;
        }
        for (WebSocketSession destino : sala) {
            enviar(destino, mensagem);
        }
    }

    private void enviar(WebSocketSession session, Mensagem mensagem) {
        if (!session.isOpen()) {
            return;
        }
        try {
            String json = mapper.writeValueAsString(mensagem);
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Falha ao enviar mensagem WebSocket: {}", e.getMessage());
        }
    }

    private String mensagemDe(RuntimeException e) {
        if (e instanceof JogadaInvalidaException
                || e instanceof PartidaInvalidaException
                || e instanceof PartidaNotFoundException
                || e instanceof JogadorNotFoundException) {
            return e.getMessage();
        }
        log.error("Erro inesperado no WebSocket", e);
        return "Erro interno ao processar a mensagem.";
    }

    private Long partidaIdDe(WebSocketSession session) {
        String caminho = session.getUri() == null ? "" : session.getUri().getPath();
        String ultimo = caminho.substring(caminho.lastIndexOf('/') + 1);
        try {
            return Long.valueOf(ultimo);
        } catch (NumberFormatException e) {
            throw new PartidaInvalidaException("Use a URL /ws/partidas/{partidaId}?jogadorId={id}");
        }
    }

    private Long jogadorIdDe(WebSocketSession session) {
        String valor = UriComponentsBuilder.fromUri(session.getUri()).build()
                .getQueryParams().getFirst("jogadorId");
        if (valor == null) {
            throw new PartidaInvalidaException("Informe o jogadorId na URL: ?jogadorId=1");
        }
        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException e) {
            throw new PartidaInvalidaException("jogadorId inválido: " + valor);
        }
    }
}
