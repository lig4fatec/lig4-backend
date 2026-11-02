package br.com.lig4.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Rota simples para a hospedagem (e para quem quiser) saber que o servidor está de pé. */
@RestController
public class SaudeController {

    @GetMapping("/saude")
    public Map<String, String> saude() {
        return Map.of("status", "ok");
    }
}
