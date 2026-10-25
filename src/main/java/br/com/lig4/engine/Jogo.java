package br.com.lig4.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Regras do Lig4: turnos, validação de jogadas, vitória e empate.
 * Não conhece banco de dados nem rede.
 */
public class Jogo {

    private static final int[][] DIRECOES = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
    private static final int SEQUENCIA = 4;

    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final List<Jogada> historico = new ArrayList<>();
    private Disco turno;
    private StatusJogo status = StatusJogo.EM_ANDAMENTO;
    private Disco vencedor;
    private List<Posicao> sequenciaVencedora = List.of();

    public Jogo(Disco primeiro) {
        this.turno = primeiro;
    }

    public Jogada jogar(Disco quem, int coluna) {
        if (status != StatusJogo.EM_ANDAMENTO) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "A partida já terminou.");
        }
        if (quem != turno) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.FORA_DO_TURNO,
                    "Não é a vez de " + quem + ".");
        }
        if (!tabuleiro.colunaValida(coluna)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.COLUNA_INVALIDA,
                    "Coluna inválida: " + coluna + ". Use de 0 a " + (Tabuleiro.COLUNAS - 1) + ".");
        }
        if (tabuleiro.colunaCheia(coluna)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.COLUNA_CHEIA,
                    "A coluna " + coluna + " está cheia.");
        }

        int linha = tabuleiro.soltar(coluna, quem);
        Jogada jogada = new Jogada(quem, coluna, linha);
        historico.add(jogada);

        List<Posicao> sequencia = buscarSequencia(linha, coluna, quem);
        if (!sequencia.isEmpty()) {
            status = StatusJogo.VENCEDOR;
            vencedor = quem;
            sequenciaVencedora = sequencia;
        } else if (tabuleiro.cheio()) {
            status = StatusJogo.EMPATE;
        } else {
            turno = quem.oposto();
        }
        return jogada;
    }

    /** Procura 4 ou mais discos iguais passando pela posição jogada, nas 4 direções. */
    private List<Posicao> buscarSequencia(int linha, int coluna, Disco disco) {
        for (int[] d : DIRECOES) {
            List<Posicao> linhaEncontrada = new ArrayList<>();
            linhaEncontrada.add(new Posicao(linha, coluna));
            contar(linha, coluna, d[0], d[1], disco, linhaEncontrada);
            contar(linha, coluna, -d[0], -d[1], disco, linhaEncontrada);
            if (linhaEncontrada.size() >= SEQUENCIA) {
                return Collections.unmodifiableList(linhaEncontrada);
            }
        }
        return List.of();
    }

    private void contar(int linha, int coluna, int dl, int dc, Disco disco, List<Posicao> acumulado) {
        int l = linha + dl;
        int c = coluna + dc;
        while (tabuleiro.dentro(l, c) && tabuleiro.get(l, c) == disco) {
            acumulado.add(new Posicao(l, c));
            l += dl;
            c += dc;
        }
    }

    public Disco getTurno() {
        return turno;
    }

    public StatusJogo getStatus() {
        return status;
    }

    /** Null enquanto não há vencedor (andamento ou empate). */
    public Disco getVencedor() {
        return vencedor;
    }

    public List<Posicao> getSequenciaVencedora() {
        return sequenciaVencedora;
    }

    public List<Jogada> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }
}
