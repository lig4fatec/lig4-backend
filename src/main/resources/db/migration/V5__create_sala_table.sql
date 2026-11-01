CREATE TABLE sala (
    id             BIGSERIAL PRIMARY KEY,
    pin            VARCHAR(6) NOT NULL UNIQUE,
    serie          SMALLINT NOT NULL DEFAULT 1,
    fase           VARCHAR(20) NOT NULL,
    dono_id        BIGINT NOT NULL REFERENCES jogador(id),
    cor_dono       VARCHAR(20) NOT NULL,
    convidado_id   BIGINT REFERENCES jogador(id),
    cor_convidado  VARCHAR(20),
    partida_id     BIGINT REFERENCES partida(id) ON DELETE SET NULL,
    criado_em      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_sala_serie CHECK (serie IN (1, 3, 5)),
    CONSTRAINT chk_sala_convidado_diferente CHECK (convidado_id IS NULL OR convidado_id <> dono_id)
);

CREATE INDEX idx_sala_fase ON sala(fase);