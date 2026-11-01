package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest extends ApiTestSupport {

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

    private String corpo(String email, String senha) {
        return "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha);
    }

    @Test
    void deveCadastrarSemDevolverASenha() {
        String email = emailUnico("cadastro");
        Resposta r = enviar("POST", "/auth/cadastro", corpo(email, "segredo123"));

        assertEquals(201, r.status(), r.corpo());
        assertTrue(r.corpo().contains(email));
        assertFalse(r.corpo().contains("segredo123"), r.corpo());
        assertFalse(r.corpo().toLowerCase().contains("senha"), r.corpo());
    }

    @Test
    void deveEntrarComASenhaCerta() {
        String email = emailUnico("entrar");
        Resposta cadastro = enviar("POST", "/auth/cadastro", corpo(email, "segredo123"));

        Resposta login = enviar("POST", "/auth/login", corpo(email, "segredo123"));

        assertEquals(200, login.status(), login.corpo());
        assertEquals(cadastro.id(), login.id());
    }

    @Test
    void deveIgnorarMaiusculasNoEmail() {
        String email = emailUnico("Maria");
        assertEquals(201, enviar("POST", "/auth/cadastro", corpo(email, "segredo123")).status());

        assertEquals(200, enviar("POST", "/auth/login", corpo(email.toLowerCase(), "segredo123")).status());
    }

    @Test
    void naoDeveEntrarComSenhaErrada() {
        String email = emailUnico("errada");
        enviar("POST", "/auth/cadastro", corpo(email, "segredo123"));

        esperar(enviar("POST", "/auth/login", corpo(email, "outra-senha")), 401, "CREDENCIAIS_INVALIDAS");
    }

    @Test
    void naoDeveEntrarComEmailInexistente() {
        esperar(enviar("POST", "/auth/login", corpo(emailUnico("fantasma"), "segredo123")),
                401, "CREDENCIAIS_INVALIDAS");
    }

    @Test
    void naoDeveCadastrarEmailRepetido() {
        String email = emailUnico("repetido");
        assertEquals(201, enviar("POST", "/auth/cadastro", corpo(email, "segredo123")).status());

        esperar(enviar("POST", "/auth/cadastro", corpo(email, "segredo123")), 409, "EMAIL_JA_CADASTRADO");
    }

    @Test
    void deveRecusarSenhaCurtaEEmailInvalido() {
        esperar(enviar("POST", "/auth/cadastro", corpo(emailUnico("curta"), "12345")), 400, "SENHA_INVALIDA");
        esperar(enviar("POST", "/auth/cadastro", corpo("isso-nao-e-email", "segredo123")), 400, "EMAIL_INVALIDO");
    }

    @Test
    void jogadorCriadoSemSenhaNaoConsegueEntrar() {
        String email = emailUnico("semsenha");
        enviar("POST", "/jogadores", "{\"nome\":\"Sem Senha\",\"email\":\"%s\"}".formatted(email));

        esperar(enviar("POST", "/auth/login", corpo(email, "qualquer1")), 401, "CREDENCIAIS_INVALIDAS");
    }
}
