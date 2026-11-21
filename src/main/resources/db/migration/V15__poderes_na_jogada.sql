-- Cada linha de jogada passa a ser um evento: soltar peça ou usar um poder do modo Caos.
-- Nos poderes, coluna e linha guardam o alvo (Queimar: coluna e linha; Congelar: coluna; Peça Extra: 0 e 0).
ALTER TABLE jogada ADD COLUMN tipo VARCHAR(10) NOT NULL DEFAULT 'PECA';
ALTER TABLE jogada ADD CONSTRAINT chk_jogada_tipo CHECK (tipo IN ('PECA', 'QUEIMAR', 'CONGELAR', 'EXTRA'));