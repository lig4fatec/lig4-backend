package br.com.lig4.controller;

import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.service.JogadaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/partidas/{partidaId}/estado")
public class EstadoController {

    private final JogadaService service;

    public EstadoController(JogadaService service) {
        this.service = service;
    }

    @GetMapping
    public PartidaEstadoDTO estado(@PathVariable Long partidaId) {
        return service.estado(partidaId);
    }
}