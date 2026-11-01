package br.com.lig4.controller;

import br.com.lig4.dto.CadastroRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.dto.LoginRequestDTO;
import br.com.lig4.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<JogadorResponseDTO> cadastrar(@Valid @RequestBody CadastroRequestDTO dto) {
        JogadorResponseDTO criado = service.cadastrar(dto);
        return ResponseEntity.created(URI.create("/jogadores/" + criado.id())).body(criado);
    }

    @PostMapping("/login")
    public JogadorResponseDTO entrar(@Valid @RequestBody LoginRequestDTO dto) {
        return service.entrar(dto);
    }
}