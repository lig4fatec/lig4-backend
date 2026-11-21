package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.Partida;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Relógio da partida, como no xadrez: cada jogador tem um tempo total e só corre o de quem está na vez.
 * Quando o tempo de alguém chega a zero, ele perde (a partida termina por TEMPO).
 *
 * <p>O tempo de cada jogador é guardado em milissegundos. O relógio de quem está na vez corre desde
 * {@code relogioMarcadoEm}, então o tempo restante dele agora é o guardado menos o que passou desde então.
 * Partidas antigas, criadas antes do relógio, não têm relógio ({@code relogioDe} vazio) e nunca expiram.
 */
@Component
public class RelogioPartida {

    private final long tempoTotalMs;

    public RelogioPartida(@Value("${lig4.relogio.tempo-por-jogador-segundos:300}") long segundosPorJogador) {
        if (segundosPorJogador < 1) {
            throw new IllegalArgumentException("lig4.relogio.tempo-por-jogador-segundos deve ser de pelo menos 1");
        }
        this.tempoTotalMs = segundosPorJogador * 1000;
    }

    /** Começa o relógio de uma partida nova: os dois com o tempo total, correndo o de quem joga primeiro. */
    public void iniciar(Partida partida, Instant agora) {
        partida.tempoUmMs = tempoTotalMs;
        partida.tempoDoisMs = tempoTotalMs;
        partida.relogioDe = partida.jogadorUm;
        partida.relogioMarcadoEm = agora;
        partida.prazoEm = agora.plusMillis(tempoTotalMs);
    }

    /** Se a partida tem relógio rodando. */
    public boolean ativo(Partida partida) {
        return partida.relogioDe != null;
    }

    /** Tempo que o jogador ainda tem agora, em milissegundos (nunca negativo). */
    public long restante(Partida partida, Long jogadorId, Instant agora) {
        long base = jogadorId.equals(partida.jogadorUm.id) ? partida.tempoUmMs : partida.tempoDoisMs;
        if (partida.relogioDe != null && partida.relogioDe.id.equals(jogadorId)
                && partida.relogioMarcadoEm != null) {
            base -= Math.max(0, Duration.between(partida.relogioMarcadoEm, agora).toMillis());
        }
        return Math.max(base, 0);
    }

    /**
     * Debita o tempo de quem jogou e passa o relógio ao próximo jogador. Sem próximo (a partida terminou),
     * o relógio para. Não faz nada em partida sem relógio.
     */
    public void passar(Partida partida, Jogador quemJogou, Jogador proximo, Instant agora) {
        if (partida.relogioDe == null) {
            return;
        }
        guardar(partida, quemJogou.id, restante(partida, quemJogou.id, agora));
        if (proximo == null) {
            parar(partida);
            return;
        }
        partida.relogioDe = proximo;
        partida.relogioMarcadoEm = agora;
        partida.prazoEm = agora.plusMillis(restante(partida, proximo.id, agora));
    }

    /** Para o relógio (fim da partida), debitando o que o jogador da vez já gastou. */
    public void parar(Partida partida, Instant agora) {
        if (partida.relogioDe == null) {
            return;
        }
        guardar(partida, partida.relogioDe.id, restante(partida, partida.relogioDe.id, agora));
        parar(partida);
    }

    private void parar(Partida partida) {
        partida.relogioDe = null;
        partida.relogioMarcadoEm = null;
        partida.prazoEm = null;
    }

    private void guardar(Partida partida, Long jogadorId, long restanteMs) {
        if (jogadorId.equals(partida.jogadorUm.id)) {
            partida.tempoUmMs = restanteMs;
        } else {
            partida.tempoDoisMs = restanteMs;
        }
    }
}
