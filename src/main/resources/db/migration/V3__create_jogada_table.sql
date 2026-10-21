CREATE TABLE jogada (
    id          BIGSERIAL PRIMARY KEY,
    partida_id  BIGINT NOT NULL REFERENCES partida(id) ON DELETE CASCADE,
    jogador_id  BIGINT NOT NULL REFERENCES jogador(id),
    numero      INTEGER NOT NULL,
    coluna      SMALLINT NOT NULL,
    linha       SMALLINT NOT NULL,
    criado_em   TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_jogada_coluna CHECK (coluna BETWEEN 0 AND 6),
    CONSTRAINT chk_jogada_linha  CHECK (linha BETWEEN 0 AND 5),
    CONSTRAINT uq_jogada_partida_numero UNIQUE (partida_id, numero)
);

CREATE INDEX idx_jogada_partida ON jogada(partida_id);