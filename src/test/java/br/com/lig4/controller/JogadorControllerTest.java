package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JogadorControllerTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    @Test
    void deveCriarEBuscarJogador() {
        String email = emailUnico("ana");
        Resposta criado = enviar("POST", "/jogadores",
                "{\"nome\":\"Ana Silva\",\"email\":\"%s\"}".formatted(email));
        assertEquals(201, criado.status());
        assertTrue(criado.corpo().contains("Ana Silva"));

        Resposta busca = get("/jogadores/" + criado.id());
        assertEquals(200, busca.status());
        assertTrue(busca.corpo().contains(email));
    }

    @Test
    void naoDeveCriarJogadorComEmailDuplicado() {
        String corpo = "{\"nome\":\"Bruno Costa\",\"email\":\"%s\"}".formatted(emailUnico("bruno"));
        assertEquals(201, enviar("POST", "/jogadores", corpo).status());
        assertEquals(409, enviar("POST", "/jogadores", corpo).status());
    }

    @Test
    void naoDeveCriarJogadorComDadosInvalidos() {
        assertEquals(400, enviar("POST", "/jogadores", "{\"nome\":\"\",\"email\":\"invalido\"}").status());
    }

    @Test
    void deveRetornar404ParaJogadorInexistente() {
        assertEquals(404, get("/jogadores/999999999").status());
    }

        @Test
    void deveGuardarEAlterarACorDoJogador() {
        Resposta criado = enviar("POST", "/jogadores",
                "{\"nome\":\"Cora\",\"email\":\"%s\",\"cor\":\"#1E88E5\"}".formatted(emailUnico("cora")));
        assertEquals(201, criado.status());
        assertTrue(criado.corpo().contains("\"cor\":\"#1E88E5\""));

        Resposta alterada = enviar("PUT", "/jogadores/" + criado.id() + "/cor", "{\"cor\":\"#43A047\"}");
        assertEquals(200, alterada.status());
        assertTrue(alterada.corpo().contains("\"cor\":\"#43A047\""));
        assertTrue(get("/jogadores/" + criado.id()).corpo().contains("#43A047"));
    }

    @Test
    void naoDeveAceitarCorInvalida() {
        Resposta r = enviar("POST", "/jogadores",
                "{\"nome\":\"Davi\",\"email\":\"%s\",\"cor\":\"vermelho\"}".formatted(emailUnico("davi")));
        assertEquals(400, r.status());
    }
}
