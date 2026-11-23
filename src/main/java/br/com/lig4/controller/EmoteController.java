package br.com.lig4.controller;

import br.com.lig4.dto.EmoteDTO;
import br.com.lig4.dto.EmoteRequestDTO;
import br.com.lig4.service.EmoteService;
import br.com.lig4.websocket.PartidaWebSocketHandler;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Emotes da partida por HTTP. Faz o mesmo que o comando EMOTE do WebSocket e avisa quem está conectado. */
@RestController
@RequestMapping("/partidas/{partidaId}/emotes")
public class EmoteController {

    private final EmoteService service;
    private final PartidaWebSocketHandler webSocket;

    public EmoteController(EmoteService service, PartidaWebSocketHandler webSocket) {
        this.service = service;
        this.webSocket = webSocket;
    }

    @PostMapping
    public EmoteDTO enviar(@PathVariable Long partidaId, @Valid @RequestBody EmoteRequestDTO dto) {
        EmoteDTO emote = service.validar(partidaId, dto.jogadorId(), dto.emote());
        webSocket.transmitirEmote(partidaId, emote);
        return emote;
    }
}