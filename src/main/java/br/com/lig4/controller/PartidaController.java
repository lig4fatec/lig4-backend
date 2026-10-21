package br.com.lig4.controller;

import br.com.lig4.dto.PartidaFinalizarDTO;
import br.com.lig4.dto.PartidaRequestDTO;
import br.com.lig4.dto.PartidaResponseDTO;
import br.com.lig4.service.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/partidas")
public class PartidaController {

    private final PartidaService service;

    public PartidaController(PartidaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PartidaResponseDTO> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public PartidaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PartidaResponseDTO> criar(@Valid @RequestBody PartidaRequestDTO dto) {
        PartidaResponseDTO criada = service.criar(dto);
        return ResponseEntity.created(URI.create("/partidas/" + criada.id())).body(criada);
    }

    /**
     * Encerra a partida, registrando o vencedor (ou empate, se vencedorId vier nulo).
     * A lógica de quem venceu fica no motor de regras (pacote engine);
     * este endpoint só registra o resultado final.
     */
    @PutMapping("/{id}")
    public PartidaResponseDTO finalizar(@PathVariable Long id,
                                        @RequestBody(required = false) PartidaFinalizarDTO dto) {
        return service.finalizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
