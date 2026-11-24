package br.com.lig4.controller;

import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.dto.TrocarEmailRequestDTO;
import br.com.lig4.dto.TrocarSenhaRequestDTO;
import br.com.lig4.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Alteração de credenciais da conta: trocar a senha e trocar o e-mail (as duas confirmam a senha). */
@RestController
@RequestMapping("/jogadores/{id}")
public class ContaController {

    private final AuthService service;

    public ContaController(AuthService service) {
        this.service = service;
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> trocarSenha(@PathVariable Long id,
                                            @Valid @RequestBody TrocarSenhaRequestDTO dto) {
        service.trocarSenha(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email")
    public JogadorResponseDTO trocarEmail(@PathVariable Long id,
                                          @Valid @RequestBody TrocarEmailRequestDTO dto) {
        return service.trocarEmail(id, dto);
    }
}