package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorsTest extends ApiTestSupport {

    record Preflight(int status, String origemLiberada) {
    }

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private Preflight preflight(String origem) {
        return RestClient.create("http://localhost:" + porta)
                .method(HttpMethod.OPTIONS)
                .uri("/jogadores")
                .header("Origin", origem)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .exchange((req, res) -> new Preflight(res.getStatusCode().value(),
                        res.getHeaders().getFirst("Access-Control-Allow-Origin")));
    }

    @Test
    void origemPermitidaRecebeOsCabecalhosDeCors() {
        Preflight r = preflight("http://localhost:5500");
        assertEquals(200, r.status());
        assertEquals("http://localhost:5500", r.origemLiberada());
    }

    @Test
    void origemDesconhecidaEBloqueada() {
        Preflight r = preflight("http://site-desconhecido.com");
        assertEquals(403, r.status());
        assertNull(r.origemLiberada());
    }

    @Test
    void respostaNormalTambemTrazOCabecalhoParaOrigemPermitida() {
        String origem = RestClient.create("http://localhost:" + porta)
                .get().uri("/jogadores/999999999")
                .header("Origin", "http://localhost:5173")
                .exchange((req, res) -> res.getHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("http://localhost:5173", origem);
    }
}
