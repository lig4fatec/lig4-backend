package br.com.lig4.websocket;

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

/** Desistência e proposta de empate pelo WebSocket da partida. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartidaAcoesWebSocketTest {

    @LocalServerPort
    int porta;

    static class Cliente extends TextWebSocketHandler {
        final BlockingQueue<String> recebidas = new LinkedBlockingQueue<>();

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            recebidas.add(message.getPayload());
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
    void desistenciaChegaParaOsDoisJogadores() throws Exception {
        long j1 = novoJogador("WsDesistA");
        long j2 = novoJogador("WsDesistB");
        long partida = novaPartida(j1, j2);
        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");
        c2.esperar("\"conectados\":[" + j1 + "," + j2 + "]");

        s1.sendMessage(new TextMessage("{\"tipo\":\"DESISTIR\"}"));

        String para1 = c1.esperar("\"motivoFim\":\"DESISTENCIA\"");
        String para2 = c2.esperar("\"motivoFim\":\"DESISTENCIA\"");
        assertTrue(para1.contains("\"tipo\":\"ESTADO\""), para1);
        assertTrue(para2.contains("\"vencedorId\":" + j2), para2);
        s1.close();
        s2.close();
    }

    @Test
    void propostaDeEmpateEAceiteChegamParaOsDois() throws Exception {
        long j1 = novoJogador("WsEmpateA");
        long j2 = novoJogador("WsEmpateB");
        long partida = novaPartida(j1, j2);
        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");
        c2.esperar("\"conectados\":[" + j1 + "," + j2 + "]");

        s1.sendMessage(new TextMessage("{\"tipo\":\"PROPOR_EMPATE\"}"));
        c2.esperar("\"empatePropostoPor\":" + j1);
        c1.esperar("\"empatePropostoPor\":" + j1);

        s2.sendMessage(new TextMessage("{\"tipo\":\"ACEITAR_EMPATE\"}"));

        String para1 = c1.esperar("\"motivoFim\":\"EMPATE_ACORDADO\"");
        c2.esperar("\"motivoFim\":\"EMPATE_ACORDADO\"");
        assertTrue(para1.contains("\"resultado\":\"EMPATE\""), para1);
        s1.close();
        s2.close();
    }

    @Test
    void acaoInvalidaRetornaErroSoParaQuemPediu() throws Exception {
        long j1 = novoJogador("WsErroA");
        long j2 = novoJogador("WsErroB");
        long partida = novaPartida(j1, j2);
        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        WebSocketSession s1 = conectar(partida, j1, c1);
        WebSocketSession s2 = conectar(partida, j2, c2);
        c1.esperar("\"conectados\":[" + j1 + "," + j2 + "]");

        s2.sendMessage(new TextMessage("{\"tipo\":\"ACEITAR_EMPATE\"}"));

        String erro = c2.esperar("\"tipo\":\"ERRO\"");
        assertTrue(erro.contains("\"codigo\":\"SEM_PROPOSTA_DE_EMPATE\""), erro);
        s1.close();
        s2.close();
    }
}
