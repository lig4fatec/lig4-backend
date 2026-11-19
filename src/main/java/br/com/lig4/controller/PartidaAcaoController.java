package br.com.lig4.controller;

import br.com.lig4.dto.PartidaAcaoRequestDTO;
import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.service.PartidaAcaoService;
import br.com.lig4.websocket.PartidaWebSocketHandler;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desistência e proposta de empate por HTTP. Durante a partida o front usa os comandos do WebSocket;
 * estes endpoints fazem o mesmo e avisam quem estiver conectado.
 */
@RestController
@RequestMapping("/partidas/{partidaId}")
public class PartidaAcaoController {

    private final PartidaAcaoService service;
    private final PartidaWebSocketHandler webSocket;

    public PartidaAcaoController(PartidaAcaoService service, PartidaWebSocketHandler webSocket) {
        this.service = service;
        this.webSocket = webSocket;
    }

    @PostMapping("/desistir")
    public PartidaEstadoDTO desistir(@PathVariable Long partidaId, @Valid @RequestBody PartidaAcaoRequestDTO dto) {
        return avisar(partidaId, service.desistir(partidaId, dto.jogadorId()));
    }

    @PostMapping("/empate")
    public PartidaEstadoDTO proporEmpate(@PathVariable Long partidaId,
                                         @Valid @RequestBody PartidaAcaoRequestDTO dto) {
        return avisar(partidaId, service.proporEmpate(partidaId, dto.jogadorId()));
    }

    @PostMapping("/empate/aceitar")
    public PartidaEstadoDTO aceitarEmpate(@PathVariable Long partidaId,
                                          @Valid @RequestBody PartidaAcaoRequestDTO dto) {
        return avisar(partidaId, service.aceitarEmpate(partidaId, dto.jogadorId()));
    }

    @PostMapping("/empate/recusar")
    public PartidaEstadoDTO recusarEmpate(@PathVariable Long partidaId,
                                          @Valid @RequestBody PartidaAcaoRequestDTO dto) {
        return avisar(partidaId, service.recusarEmpate(partidaId, dto.jogadorId()));
    }

    private PartidaEstadoDTO avisar(Long partidaId, PartidaEstadoDTO estado) {
        webSocket.transmitirEstado(partidaId, estado);
        return estado;
    }
}
