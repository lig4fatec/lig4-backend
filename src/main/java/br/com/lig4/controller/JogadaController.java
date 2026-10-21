package br.com.lig4.controller;

import br.com.lig4.dto.JogadaHistoricoDTO;
import br.com.lig4.dto.JogadaRequestDTO;
import br.com.lig4.dto.JogadaResponseDTO;
import br.com.lig4.service.JogadaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/partidas/{partidaId}/jogadas")
public class JogadaController {

    private final JogadaService service;

    public JogadaController(JogadaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JogadaResponseDTO> jogar(@PathVariable Long partidaId,
                                                   @Valid @RequestBody JogadaRequestDTO dto) {
        return ResponseEntity.status(201).body(service.jogar(partidaId, dto));
    }

    @GetMapping
    public List<JogadaHistoricoDTO> historico(@PathVariable Long partidaId) {
        return service.historico(partidaId);
    }
} 
