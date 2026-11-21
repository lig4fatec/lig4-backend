package br.com.lig4.engine;

/** Poderes do modo Caos. Cada jogador pode usar cada poder uma vez por partida. */
public enum Poder {
    /** Remove uma peça do adversário. Gasta a vez. */
    QUEIMAR,
    /** Bloqueia uma coluna para o adversário nas próximas 3 vezes dele. Gasta a vez. */
    CONGELAR,
    /** Dá uma segunda jogada seguida (o "Jogar 2x" do front). Não gasta a vez. */
    EXTRA;

    /** Id do item da loja que dá direito ao poder. */
    public String itemId() {
        return "poder-" + name().toLowerCase();
    }
}
