-- Relógio por jogador, como no xadrez: cada um tem um tempo total e só corre o de quem está na vez.
ALTER TABLE partida ADD COLUMN tempo_um_ms         BIGINT NOT NULL DEFAULT 300000;
ALTER TABLE partida ADD COLUMN tempo_dois_ms       BIGINT NOT NULL DEFAULT 300000;
ALTER TABLE partida ADD COLUMN relogio_de_id       BIGINT REFERENCES jogador(id);
ALTER TABLE partida ADD COLUMN relogio_marcado_em  TIMESTAMP;
ALTER TABLE partida ADD COLUMN prazo_em            TIMESTAMP;

CREATE INDEX idx_partida_prazo ON partida(prazo_em) WHERE prazo_em IS NOT NULL;