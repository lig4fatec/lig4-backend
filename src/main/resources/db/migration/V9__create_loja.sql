ALTER TABLE jogador ADD COLUMN avatar_equipado    VARCHAR(40) NOT NULL DEFAULT 'avatar-classico';
ALTER TABLE jogador ADD COLUMN tabuleiro_equipado VARCHAR(40) NOT NULL DEFAULT 'board-minimalista';
ALTER TABLE jogador ADD COLUMN emote_equipado     VARCHAR(40) NOT NULL DEFAULT 'emote-saudacao';

-- Itens pagos que o jogador já comprou (os itens grátis são de todos e não ficam aqui)
CREATE TABLE jogador_item (
    id          BIGSERIAL PRIMARY KEY,
    jogador_id  BIGINT NOT NULL REFERENCES jogador(id) ON DELETE CASCADE,
    item_id     VARCHAR(40) NOT NULL,
    comprado_em TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_jogador_item UNIQUE (jogador_id, item_id)
);