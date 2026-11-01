package br.com.lig4.controller;

import br.com.lig4.dto.HistoricoResponseDTO;
import br.com.lig4.service.HistoricoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jogadores/{id}/historico")
public class HistoricoController {

    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    @GetMapping
    public HistoricoResponseDTO listar(@PathVariable Long id,
                                       @RequestParam(defaultValue = "todos") String filtro) {
        return service.listar(id, filtro);
    }
}
