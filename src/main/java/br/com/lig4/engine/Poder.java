package br.com.lig4.engine;

/** Poderes do modo Caos. Cada jogador pode usar cada poder uma vez por partida. */
public enum Poder {
    /** Remove uma peça do adversário. Gasta a vez. */
    QUEIMAR("queimar-peca"),
    /** Bloqueia uma coluna para o adversário nas próximas 3 vezes dele. Gasta a vez. */
    CONGELAR("congelar-coluna"),
    /** Dá uma segunda jogada seguida (o "Jogar 2x" do front). Não gasta a vez. */
    EXTRA("peca-extra");

    /** Id do item da loja que dá direito ao poder (o mesmo id do catálogo do front). */
    private final String itemId;

    Poder(String itemId) {
        this.itemId = itemId;
    }

    public String itemId() {
        return itemId;
    }
}