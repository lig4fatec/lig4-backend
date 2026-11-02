package br.com.lig4.websocket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartidaWebSocketTest {

    @LocalServerPort
    int porta;

    /** Guarda as mensagens recebidas para o teste conferir. */
    static class Cliente extends TextWebSocketHandler {
        final BlockingQueue<String> recebidas = new LinkedBlockingQueue<>();

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            recebidas.add(message.getPayload());
        }

        /** A próxima mensagem, qualquer que seja o tipo. */
        String proxima() throws InterruptedException {
            String mensagem = recebidas.poll(5, TimeUnit.SECONDS);
            assertNotNull(mensagem, "Nenhuma mensagem recebida em 5 segundos");
            return mensagem;
        }

        /** Consome mensagens até achar uma que contenha o trecho; falha se não chegar em 5 segundos. */
        String esperar(String trecho) throws InterruptedException {
            long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (true) {
                long restante = Math.max(limite - System.nanoTime(), 0);
                String mensagem = recebidas.poll(restante, TimeUnit.NANOSECONDS);
                assertNotNull(mensagem, "Não chegou nenhuma mensagem com " + trecho + " em 5 segundos");
                if (mensagem.contains(trecho)) {
                    return mensagem;
                }
            }
        }
    }

    private long postar(String caminho, String json) {
        String corpo = RestClient.create("http://localhost:" + porta)
                .post().uri(caminho).contentType(MediaType.APPLICATION_JSON).body(json)
                .retrieve().body(String.class);
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(corpo);
        if (!m.find()) {
            throw new AssertionError("Resposta sem id: " + corpo);
        }
        return Long.parseLong(m.group(1));
    }

    private long novoJogador(String nome) {
        String email = nome.toLowerCase() + "." + UUID.randomUUID().toString().substring(0, 8) + "@lig4.com";
        return postar("/jogadores", "{\"nome\":\"%s\",\"email\":\"%s\"}".formatted(nome, email));
    }

    private long novaPartida(long um, long dois) {
        return postar("/partidas", "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois));
    }

    private WebSocketSession conectar(long partida, long jogador, Cliente cliente) throws Exception {
        String url = "ws://localhost:%d/ws/partidas/%d?jogadorId=%d".formatted(porta, partida, jogador);
        return new StandardWebSocketClient().execute(cliente, url).get(5, TimeUnit.SECONDS);
    }

    @Test
    void aoConectarRecebeOEstadoAtual() throws Exception {
        long j1 = novoJogador("Vera");
        long j2 = novoJogador("Wilson");
        long partida = novaPartida(j1, j2);

        Cliente c1 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);

        String mensagem = c1.esperar("\"tipo\":\"ESTADO\"");
        assertTrue(mensagem.contains("\"proximoJogadorId\":" + j1));
        s1.close();
    }

    @Test
    void jogadaChegaParaOsDoisJogadores() throws Exception {
        long j1 = novoJogador("Xuxa");
        long j2 = novoJogador("Yuri");
        long partida = novaPartida(j1, j2);

        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"tipo\":\"ESTADO\""); // estado inicial: garante que a conexão já foi registrada
        c2.esperar("\"tipo\":\"ESTADO\"");

        s1.sendMessage(new TextMessage("{\"tipo\":\"JOGAR\",\"coluna\":3}"));

        String para1 = c1.esperar("\"tipo\":\"JOGADA\"");
        String para2 = c2.esperar("\"tipo\":\"JOGADA\"");
        assertTrue(para1.contains("\"tipo\":\"JOGADA\""));
        assertTrue(para2.contains("\"proximoJogadorId\":" + j2));

        s1.close();
        s2.close();
    }

    @Test
    void jogadaForaDoTurnoRetornaErroSoParaQuemErrou() throws Exception {
        long j1 = novoJogador("Zeca");
        long j2 = novoJogador("Amanda");
        long partida = novaPartida(j1, j2);

        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"tipo\":\"ESTADO\"");
        c2.esperar("\"tipo\":\"ESTADO\"");

        s2.sendMessage(new TextMessage("{\"tipo\":\"JOGAR\",\"coluna\":0}"));

        c2.esperar("\"tipo\":\"ERRO\"");
        // o outro jogador pode receber avisos de presença, mas nunca o erro
        String outra;
        while ((outra = c1.recebidas.poll(1, TimeUnit.SECONDS)) != null) {
            assertFalse(outra.contains("\"tipo\":\"ERRO\""), outra);
        }

        s1.close();
        s2.close();
    }

    @Test
    void jogadorQueNaoParticipaERecusado() throws Exception {
        long j1 = novoJogador("Bia");
        long j2 = novoJogador("Caio");
        long intruso = novoJogador("Duda");
        long partida = novaPartida(j1, j2);

        Cliente c = new Cliente();
        conectar(partida, intruso, c);

        String mensagem = c.proxima();
        assertTrue(mensagem.contains("\"tipo\":\"ERRO\""));
        assertTrue(mensagem.contains("\"codigo\":\"PARTIDA_INVALIDA\""));
    }

    @Test
    void quemEntraDepoisAvisaOPrimeiro() throws Exception {
        long j1 = novoJogador("Eva");
        long j2 = novoJogador("Fabio");
        long partida = novaPartida(j1, j2);

        Cliente c1 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        c1.esperar("\"conectados\":[" + j1 + "]");

        Cliente c2 = new Cliente();
        WebSocketSession s2 = conectar(partida, j2, c2);

        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");
        c2.esperar("\"conectados\":[" + j1 + "," + j2 + "]");
        s1.close();
        s2.close();
    }

    @Test
    void quemSaiAvisaOAdversario() throws Exception {
        long j1 = novoJogador("Gina");
        long j2 = novoJogador("Hugo");
        long partida = novaPartida(j1, j2);

        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");

        s2.close();

        c1.esperar("\"conectados\":[" + j1 + "]");
        s1.close();
    }

    @Test
    void quemVoltaRecebeOEstadoEOAdversarioVeQueVoltou() throws Exception {
        long j1 = novoJogador("Iris");
        long j2 = novoJogador("Jonas");
        long partida = novaPartida(j1, j2);

        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");
        s2.sendMessage(new TextMessage("{\"tipo\":\"ESTADO\"}"));
        s2.close();
        c1.esperar("\"conectados\":[" + j1 + "]");

        Cliente volta = new Cliente();
        WebSocketSession s2Nova = conectar(partida, j2, volta);

        String estado = volta.esperar("\"tipo\":\"ESTADO\"");
        assertTrue(estado.contains("\"proximoJogadorId\":" + j1), estado);
        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");
        s1.close();
        s2Nova.close();
    }
}