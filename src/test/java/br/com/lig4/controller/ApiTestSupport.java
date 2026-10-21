package br.com.lig4.controller;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Cliente HTTP simples para os testes (sem dependências além do Spring). */
abstract class ApiTestSupport {

    record Resposta(int status, String corpo) {
        long id() {
            Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(corpo);
            if (!m.find()) {
                throw new AssertionError("Resposta sem id: " + corpo);
            }
            return Long.parseLong(m.group(1));
        }
    }

    protected abstract int porta();

    private RestClient cliente() {
        return RestClient.create("http://localhost:" + porta());
    }

    protected Resposta get(String caminho) {
        return cliente().get().uri(caminho).exchange((req, res) ->
                new Resposta(res.getStatusCode().value(), new String(res.getBody().readAllBytes())));
    }

    protected Resposta enviar(String metodo, String caminho, String json) {
        var spec = switch (metodo) {
            case "POST" -> cliente().post();
            case "PUT" -> cliente().put();
            default -> throw new IllegalArgumentException(metodo);
        };
        return spec.uri(caminho).contentType(MediaType.APPLICATION_JSON).body(json).exchange((req, res) ->
                new Resposta(res.getStatusCode().value(), new String(res.getBody().readAllBytes())));
    }

    /** E-mail único por execução, para o teste poder rodar várias vezes no mesmo banco. */
    protected String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID().toString().substring(0, 8) + "@lig4.com";
    }

    protected long criarJogador(String nome) {
        Resposta r = enviar("POST", "/jogadores",
                "{\"nome\":\"%s\",\"email\":\"%s\"}".formatted(nome, emailUnico(nome.toLowerCase())));
        if (r.status() != 201) {
            throw new AssertionError("Falha ao criar jogador: " + r);
        }
        return r.id();
    }
}
