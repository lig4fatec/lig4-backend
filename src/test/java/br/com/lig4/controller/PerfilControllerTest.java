package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerfilControllerTest extends ApiTestSupport {

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

    private long novaPartida(long um, long dois) {
        return enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois)).id();
    }

    private Resposta jogar(long partida, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private void esperarPerfil(long jogador, long vitorias, long derrotas, int moedas) {
        Resposta r = get("/jogadores/" + jogador + "/perfil");
        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"vitorias\":" + vitorias), r.corpo());
        assertTrue(r.corpo().contains("\"derrotas\":" + derrotas), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":" + moedas), r.corpo());
    }

    @Test
    void perfilNovoTemValoresIniciais() {
        String email = emailUnico("novo");
        Resposta cadastro = enviar("POST", "/auth/cadastro",
                "{\"email\":\"%s\",\"senha\":\"segredo123\"}".formatted(email));

        Resposta perfil = get("/jogadores/" + cadastro.id() + "/perfil");

        assertEquals(200, perfil.status(), perfil.corpo());
        assertTrue(perfil.corpo().contains("\"nick\":\"" + email.substring(0, email.indexOf('@')) + "\""));
        assertTrue(perfil.corpo().contains("\"cor\":\"blue\""));
        esperarPerfil(cadastro.id(), 0, 0, 500);
    }

    @Test
    void deveAtualizarOPerfil() {
        long id = criarJogador("Perfil");

        Resposta r = enviar("PUT", "/jogadores/" + id + "/perfil",
                "{\"nick\":\"Rainha\",\"nomeCompleto\":\"Ana Souza\",\"bio\":\"Gosto de Lig4\",\"cor\":\"purple\"}");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"nick\":\"Rainha\""));
        assertTrue(r.corpo().contains("\"nomeCompleto\":\"Ana Souza\""));
        assertTrue(r.corpo().contains("\"bio\":\"Gosto de Lig4\""));
        assertTrue(r.corpo().contains("\"cor\":\"purple\""));
        // o nick é o nome mostrado nas partidas
        assertTrue(get("/jogadores/" + id).corpo().contains("\"nome\":\"Rainha\""));
    }

    @Test
    void deveRecusarNickInvalido() {
        long id = criarJogador("Nick");
        String vinteEUm = "a".repeat(21);

        esperar(enviar("PUT", "/jogadores/" + id + "/perfil",
                "{\"nick\":\"\",\"cor\":\"blue\"}"), 400, "NICK_INVALIDO");
        esperar(enviar("PUT", "/jogadores/" + id + "/perfil",
                "{\"nick\":\"%s\",\"cor\":\"blue\"}".formatted(vinteEUm)), 400, "NICK_INVALIDO");
    }

    @Test
    void deveRecusarCorInvalida() {
        long id = criarJogador("Cor");

        esperar(enviar("PUT", "/jogadores/" + id + "/perfil",
                "{\"nick\":\"Ok\",\"cor\":\"#FF0000\"}"), 400, "COR_INVALIDA");
    }

    @Test
    void perfilInexistenteDevolve404() {
        esperar(get("/jogadores/999999999/perfil"), 404, "JOGADOR_NAO_ENCONTRADO");
    }

    @Test
    void vitoriaEDerrotaMudamEstatisticasEMoedas() {
        long j1 = criarJogador("Vence");
        long j2 = criarJogador("Perde");
        long partida = novaPartida(j1, j2);

        assertEquals(200, enviar("PUT", "/partidas/" + partida, "{\"vencedorId\":%d}".formatted(j1)).status());

        esperarPerfil(j1, 1, 0, 550);
        esperarPerfil(j2, 0, 1, 510);
    }

    @Test
    void empateDaMoedasSemVitoriaNemDerrota() {
        long j1 = criarJogador("EmpateA");
        long j2 = criarJogador("EmpateB");
        long partida = novaPartida(j1, j2);

        assertEquals(200, enviar("PUT", "/partidas/" + partida, "{}").status());

        esperarPerfil(j1, 0, 0, 510);
        esperarPerfil(j2, 0, 0, 510);
    }

    @Test
    void partidaJogadaAteOFimTambemCreditaAsMoedas() {
        long j1 = criarJogador("Jogada1");
        long j2 = criarJogador("Jogada2");
        long partida = novaPartida(j1, j2);

        jogar(partida, j1, 0);
        jogar(partida, j2, 1);
        jogar(partida, j1, 0);
        jogar(partida, j2, 1);
        jogar(partida, j1, 0);
        jogar(partida, j2, 1);
        assertEquals(201, jogar(partida, j1, 0).status());

        esperarPerfil(j1, 1, 0, 550);
        esperarPerfil(j2, 0, 1, 510);
        // jogada depois do fim é recusada e não credita de novo
        assertEquals(409, jogar(partida, j2, 3).status());
        esperarPerfil(j1, 1, 0, 550);
    }
}
