-- Como a partida terminou, e a proposta de empate (cada jogador pode propor uma vez por partida).
ALTER TABLE partida ADD COLUMN motivo_fim            VARCHAR(20);
ALTER TABLE partida ADD COLUMN empate_pendente_de_id BIGINT REFERENCES jogador(id);
ALTER TABLE partida ADD COLUMN empate_usado_um       BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE partida ADD COLUMN empate_usado_dois     BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE partida ADD CONSTRAINT chk_partida_motivo_fim
    CHECK (motivo_fim IS NULL OR motivo_fim IN ('VITORIA', 'TABULEIRO_CHEIO', 'DESISTENCIA', 'EMPATE_ACORDADO', 'TEMPO'));