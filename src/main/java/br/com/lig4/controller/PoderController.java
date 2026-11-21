package br.com.lig4.controller;

import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.dto.PoderRequestDTO;
import br.com.lig4.service.PoderService;
import br.com.lig4.websocket.PartidaWebSocketHandler;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Poderes do modo Caos por HTTP. Faz o mesmo que o comando PODER do WebSocket e avisa quem estiver conectado. */
@RestController
@RequestMapping("/partidas/{partidaId}/poderes")
public class PoderController {

    private final PoderService service;
    private final PartidaWebSocketHandler webSocket;

    public PoderController(PoderService service, PartidaWebSocketHandler webSocket) {
        this.service = service;
        this.webSocket = webSocket;
    }

    @PostMapping
    public PartidaEstadoDTO usar(@PathVariable Long partidaId, @Valid @RequestBody PoderRequestDTO dto) {
        PartidaEstadoDTO estado = service.usar(partidaId, dto);
        webSocket.transmitirEstado(partidaId, estado);
        return estado;
    }
}
