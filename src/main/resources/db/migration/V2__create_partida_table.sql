CREATE TABLE partida (
    id               BIGSERIAL PRIMARY KEY,
    jogador_um_id    BIGINT NOT NULL REFERENCES jogador(id),
    jogador_dois_id  BIGINT NOT NULL REFERENCES jogador(id),
    vencedor_id      BIGINT REFERENCES jogador(id),
    status           VARCHAR(20) NOT NULL,
    criado_em        TIMESTAMP NOT NULL DEFAULT now(),
    finalizado_em    TIMESTAMP,

    CONSTRAINT chk_jogadores_diferentes CHECK (jogador_um_id <> jogador_dois_id)
);

CREATE INDEX idx_partida_status ON partida(status);
