package br.com.lig4.controller;

import br.com.lig4.dto.CorRequestDTO;
import br.com.lig4.dto.JogadorRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.service.JogadorService;
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
@RequestMapping("/jogadores")
public class JogadorController {

    private final JogadorService service;

    public JogadorController(JogadorService service) {
        this.service = service;
    }

    @GetMapping
    public List<JogadorResponseDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public JogadorResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<JogadorResponseDTO> criar(@Valid @RequestBody JogadorRequestDTO dto) {
        JogadorResponseDTO criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/jogadores/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public JogadorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody JogadorRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cor")
    public JogadorResponseDTO alterarCor(@PathVariable Long id, @Valid @RequestBody CorRequestDTO dto) {
        return service.alterarCor(id, dto);
    }
}
