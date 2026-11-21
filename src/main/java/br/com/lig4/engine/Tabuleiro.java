package br.com.lig4.engine;

/**
 * Grade 6 linhas x 7 colunas. Linha 0 é a base. Classe Java pura (sem Quarkus),
 * por isso é simples de testar e de reutilizar no WebSocket.
 */
public class Tabuleiro {

    public static final int LINHAS = 6;
    public static final int COLUNAS = 7;

    private final Disco[][] grade = new Disco[LINHAS][COLUNAS];

    public Disco get(int linha, int coluna) {
        return grade[linha][coluna];
    }

    public boolean dentro(int linha, int coluna) {
        return linha >= 0 && linha < LINHAS && coluna >= 0 && coluna < COLUNAS;
    }

    public boolean colunaValida(int coluna) {
        return coluna >= 0 && coluna < COLUNAS;
    }

    public boolean colunaCheia(int coluna) {
        return grade[LINHAS - 1][coluna] != null;
    }

    /** Solta o disco na coluna e devolve a linha em que ele parou. */
    public int soltar(int coluna, Disco disco) {
        for (int linha = 0; linha < LINHAS; linha++) {
            if (grade[linha][coluna] == null) {
                grade[linha][coluna] = disco;
                return linha;
            }
        }
        throw new IllegalStateException("Coluna cheia: " + coluna);
    }

    public boolean cheio() {
        for (int c = 0; c < COLUNAS; c++) {
            if (!colunaCheia(c)) {
                return false;
            }
        }
        return true;
    }

    /** Tira a peça de uma posição (poder Queimar). */
    public void remover(int linha, int coluna) {
        grade[linha][coluna] = null;
    }

    /**
     * Faz as peças da coluna caírem para preencher os buracos (gravidade depois do Queimar):
     * junta as peças que sobraram na base, na mesma ordem, e deixa o resto da coluna vazio.
     */
    public void compactarColuna(int coluna) {
        Disco[] pilha = new Disco[LINHAS];
        int n = 0;
        for (int linha = 0; linha < LINHAS; linha++) {
            if (grade[linha][coluna] != null) {
                pilha[n++] = grade[linha][coluna];
            }
        }
        for (int linha = 0; linha < LINHAS; linha++) {
            grade[linha][coluna] = linha < n ? pilha[linha] : null;
        }
    }

    /** Casas vazias que ainda dá para alcançar: as das colunas que não estão cheias. */
    public int espacosLivres() {
        int livres = 0;
        for (int c = 0; c < COLUNAS; c++) {
            if (colunaCheia(c)) {
                continue;
            }
            for (int l = 0; l < LINHAS; l++) {
                if (grade[l][c] == null) {
                    livres++;
                }
            }
        }
        return livres;
    }

    /** Cópia da grade como texto por linha (de cima para baixo), útil para enviar ao front-end. */
    public String[][] comoMatriz() {
        String[][] m = new String[LINHAS][COLUNAS];
        for (int l = 0; l < LINHAS; l++) {
            for (int c = 0; c < COLUNAS; c++) {
                Disco d = grade[LINHAS - 1 - l][c];
                m[l][c] = d == null ? null : d.name();
            }
        }
        return m;
    }
}