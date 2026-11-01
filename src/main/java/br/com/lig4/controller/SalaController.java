package br.com.lig4.controller;

import br.com.lig4.dto.SalaCriarRequestDTO;
import br.com.lig4.dto.SalaEntrarRequestDTO;
import br.com.lig4.dto.SalaResponseDTO;
import br.com.lig4.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService service;

    public SalaController(SalaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SalaResponseDTO> criar(@Valid @RequestBody SalaCriarRequestDTO dto) {
        SalaResponseDTO criada = service.criar(dto);
        return ResponseEntity.created(URI.create("/salas/" + criada.pin())).body(criada);
    }

    @PostMapping("/entrar")
    public SalaResponseDTO entrar(@Valid @RequestBody SalaEntrarRequestDTO dto) {
        return service.entrar(dto);
    }

    @GetMapping("/{pin}")
    public SalaResponseDTO consultar(@PathVariable String pin) {
        return service.consultar(pin);
    }
}
