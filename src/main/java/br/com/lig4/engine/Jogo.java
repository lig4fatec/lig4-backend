package br.com.lig4.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras do Lig4: turnos, validação de jogadas, vitória e empate, mais os poderes do modo Caos.
 * Não conhece banco de dados nem rede.
 */
public class Jogo {

    private static final int[][] DIRECOES = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
    private static final int SEQUENCIA = 4;
    /** Quantas vezes do adversário uma coluna congelada fica bloqueada. */
    public static final int TURNOS_DE_CONGELAMENTO = 3;

    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final List<Jogada> historico = new ArrayList<>();
    private Disco turno;
    private StatusJogo status = StatusJogo.EM_ANDAMENTO;
    private Disco vencedor;
    private List<Posicao> sequenciaVencedora = List.of();

    // poderes do Caos
    private final Map<Disco, Set<Poder>> poderesUsados = new EnumMap<>(Disco.class);
    private final List<Congelamento> congelamentos = new ArrayList<>();
    private boolean pecaExtraAtiva;
    /** Peças que o jogador da vez já soltou neste turno (0 ou 1, ou 2 com a Peça Extra). */
    private int pecasNoTurno;

    public Jogo(Disco primeiro) {
        this.turno = primeiro;
        for (Disco disco : Disco.values()) {
            poderesUsados.put(disco, EnumSet.noneOf(Poder.class));
        }
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
        if (congelada(quem, coluna)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.COLUNA_CONGELADA,
                    "A coluna " + coluna + " está congelada para você.");
        }
        if (tabuleiro.colunaCheia(coluna)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.COLUNA_CHEIA,
                    "A coluna " + coluna + " está cheia.");
        }

        int linha = tabuleiro.soltar(coluna, quem);
        pecasNoTurno++;
        Jogada jogada = new Jogada(quem, coluna, linha);
        historico.add(jogada);

        List<Posicao> sequencia = buscarSequencia(linha, coluna, quem);
        if (!sequencia.isEmpty()) {
            status = StatusJogo.VENCEDOR;
            vencedor = quem;
            sequenciaVencedora = sequencia;
        } else if (tabuleiro.cheio()) {
            status = StatusJogo.EMPATE;
        } else if (pecaExtraAtiva && pecasNoTurno == 1 && !colunasJogaveis(quem).isEmpty()) {
            // Peça Extra: a vez continua para a segunda peça
            return jogada;
        } else {
            encerrarTurno(quem);
        }
        return jogada;
    }

    /**
     * Queimar Peça: remove uma peça do adversário e gasta a vez. As peças de cima não descem
     * (a gravidade vem em outro PRD), então a casa fica vazia e a próxima peça da coluna cai nela.
     */
    public void queimar(Disco quem, int coluna, int linha) {
        exigirInicioDeTurno(quem, Poder.QUEIMAR);
        if (!tabuleiro.dentro(linha, coluna) || tabuleiro.get(linha, coluna) != quem.oposto()) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.ALVO_INVALIDO,
                    "Escolha uma peça do adversário.");
        }
        tabuleiro.remover(linha, coluna);
        poderesUsados.get(quem).add(Poder.QUEIMAR);
        encerrarTurno(quem);
    }

    /** Congelar Coluna: o adversário não joga nela nas próximas 3 vezes dele. Gasta a vez. */
    public void congelar(Disco quem, int coluna) {
        exigirInicioDeTurno(quem, Poder.CONGELAR);
        if (!tabuleiro.colunaValida(coluna)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.COLUNA_INVALIDA,
                    "Coluna inválida: " + coluna + ". Use de 0 a " + (Tabuleiro.COLUNAS - 1) + ".");
        }
        if (tabuleiro.colunaCheia(coluna)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.COLUNA_CHEIA,
                    "A coluna " + coluna + " está cheia.");
        }
        congelamentos.add(new Congelamento(coluna, quem.oposto(), TURNOS_DE_CONGELAMENTO));
        poderesUsados.get(quem).add(Poder.CONGELAR);
        encerrarTurno(quem);
    }

    /** Peça Extra: o jogador solta duas peças seguidas nesta vez. Não gasta a vez; a jogada vem em seguida. */
    public void pecaExtra(Disco quem) {
        exigirInicioDeTurno(quem, Poder.EXTRA);
        if (tabuleiro.espacosLivres() < 2) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.PECA_EXTRA_SEM_ESPACO,
                    "Não há espaço para duas peças.");
        }
        poderesUsados.get(quem).add(Poder.EXTRA);
        pecaExtraAtiva = true;
    }

    /** Poder só vale na sua vez, uma vez por partida e antes de soltar peça neste turno. */
    private void exigirInicioDeTurno(Disco quem, Poder poder) {
        if (status != StatusJogo.EM_ANDAMENTO) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "A partida já terminou.");
        }
        if (quem != turno) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.FORA_DO_TURNO,
                    "Não é a vez de " + quem + ".");
        }
        if (poderesUsados.get(quem).contains(poder)) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.PODER_JA_USADO,
                    "Você já usou este poder nesta partida.");
        }
        if (pecasNoTurno > 0 || pecaExtraAtiva) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.PODER_FORA_DE_HORA,
                    "Poderes só podem ser usados no começo da sua vez.");
        }
    }

    /**
     * Passa a vez. Conta uma vez do jogador que terminou para o congelamento dele. Quem só tem colunas
     * congeladas para jogar perde a vez (e essa vez também conta para o congelamento).
     */
    private void encerrarTurno(Disco quem) {
        pecaExtraAtiva = false;
        pecasNoTurno = 0;
        contarVez(quem);
        Disco proximo = quem.oposto();
        for (int i = 0; i < 8 && colunasJogaveis(proximo).isEmpty(); i++) {
            contarVez(proximo);
            proximo = proximo.oposto();
        }
        turno = proximo;
    }

    private void contarVez(Disco disco) {
        List<Congelamento> atualizados = new ArrayList<>();
        for (Congelamento c : congelamentos) {
            if (c.afetado() != disco) {
                atualizados.add(c);
            } else if (c.turnosRestantes() > 1) {
                atualizados.add(new Congelamento(c.coluna(), c.afetado(), c.turnosRestantes() - 1));
            }
        }
        congelamentos.clear();
        congelamentos.addAll(atualizados);
    }

    private boolean congelada(Disco disco, int coluna) {
        return congelamentos.stream().anyMatch(c -> c.afetado() == disco && c.coluna() == coluna);
    }

    private List<Integer> colunasJogaveis(Disco disco) {
        List<Integer> jogaveis = new ArrayList<>();
        for (int c = 0; c < Tabuleiro.COLUNAS; c++) {
            if (!tabuleiro.colunaCheia(c) && !congelada(disco, c)) {
                jogaveis.add(c);
            }
        }
        return jogaveis;
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

    /** Poderes que o jogador já usou nesta partida. */
    public Set<Poder> getPoderesUsados(Disco disco) {
        return Collections.unmodifiableSet(poderesUsados.get(disco));
    }

    /** Colunas congeladas agora, com quantas vezes do jogador bloqueado ainda faltam. */
    public List<Congelamento> getCongelamentos() {
        return Collections.unmodifiableList(congelamentos);
    }

    /** Verdadeiro entre usar a Peça Extra e soltar a segunda peça. */
    public boolean isPecaExtraAtiva() {
        return pecaExtraAtiva;
    }

    public List<Jogada> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }
}