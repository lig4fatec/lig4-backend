package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Troca de senha e de e-mail da conta. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContaControllerTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    /** Cadastra um jogador com e-mail e senha conhecidos e devolve o id. */
    private long cadastrar(String email, String senha) {
        Resposta r = enviar("POST", "/auth/cadastro",
                "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha));
        assertEquals(201, r.status(), r.corpo());
        return r.id();
    }

    private Resposta login(String email, String senha) {
        return enviar("POST", "/auth/login", "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha));
    }

    private Resposta trocarSenha(long id, String atual, String nova) {
        return enviar("PUT", "/jogadores/" + id + "/senha",
                "{\"senhaAtual\":\"%s\",\"senhaNova\":\"%s\"}".formatted(atual, nova));
    }

    private Resposta trocarEmail(long id, String email, String senha) {
        return enviar("PUT", "/jogadores/" + id + "/email",
                "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha));
    }

    @Test
    void trocaSenhaEPassaALogarComANova() {
        String email = emailUnico("trocasenha");
        long id = cadastrar(email, "segredo123");

        assertEquals(204, trocarSenha(id, "segredo123", "novaSenha456").status());

        assertEquals(200, login(email, "novaSenha456").status());          // a nova funciona
        esperar(login(email, "segredo123"), 401, "CREDENCIAIS_INVALIDAS"); // a antiga não
    }

    @Test
    void naoTrocaSenhaComASenhaAtualErrada() {
        String email = emailUnico("senhaerrada");
        long id = cadastrar(email, "segredo123");

        esperar(trocarSenha(id, "errada", "novaSenha456"), 401, "CREDENCIAIS_INVALIDAS");
        assertEquals(200, login(email, "segredo123").status()); // continua a mesma
    }

    @Test
    void recusaSenhaNovaCurta() {
        long id = cadastrar(emailUnico("curta"), "segredo123");

        esperar(trocarSenha(id, "segredo123", "123"), 400, "SENHA_INVALIDA");
    }

    @Test
    void trocaEmailEPassaALogarComONovo() {
        String antigo = emailUnico("antigo");
        String novo = emailUnico("novo");
        long id = cadastrar(antigo, "segredo123");

        Resposta r = trocarEmail(id, novo, "segredo123");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains(novo), r.corpo());
        assertEquals(200, login(novo, "segredo123").status());
    }

    @Test
    void naoTrocaParaUmEmailJaUsado() {
        String emailA = emailUnico("contaA");
        String emailB = emailUnico("contaB");
        long a = cadastrar(emailA, "segredo123");
        cadastrar(emailB, "segredo123");

        esperar(trocarEmail(a, emailB, "segredo123"), 409, "EMAIL_JA_CADASTRADO");
    }

    @Test
    void naoTrocaEmailComASenhaErrada() {
        String email = emailUnico("emailsenha");
        long id = cadastrar(email, "segredo123");

        esperar(trocarEmail(id, emailUnico("destino"), "errada"), 401, "CREDENCIAIS_INVALIDAS");
    }
}
