package br.com.lig4.controller;

import br.com.lig4.dto.LojaCompraRequestDTO;
import br.com.lig4.dto.LojaEquiparRequestDTO;
import br.com.lig4.dto.LojaResponseDTO;
import br.com.lig4.service.LojaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jogadores/{id}/loja")
public class LojaController {

    private final LojaService service;

    public LojaController(LojaService service) {
        this.service = service;
    }

    @GetMapping
    public LojaResponseDTO ver(@PathVariable Long id) {
        return service.ver(id);
    }

    @PostMapping("/comprar")
    public LojaResponseDTO comprar(@PathVariable Long id, @Valid @RequestBody LojaCompraRequestDTO dto) {
        return service.comprar(id, dto);
    }

    @PutMapping("/equipar")
    public LojaResponseDTO equipar(@PathVariable Long id, @Valid @RequestBody LojaEquiparRequestDTO dto) {
        return service.equipar(id, dto);
    }
}
