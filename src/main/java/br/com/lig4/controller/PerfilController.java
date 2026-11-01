package br.com.lig4.controller;

import br.com.lig4.dto.PerfilRequestDTO;
import br.com.lig4.dto.PerfilResponseDTO;
import br.com.lig4.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jogadores/{id}/perfil")
public class PerfilController {

    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public PerfilResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping
    public PerfilResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequestDTO dto) {
        return service.atualizar(id, dto);
    }
}
