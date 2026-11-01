-- Hash BCrypt (60 caracteres). Nulo para jogadores criados sem senha (POST /jogadores).
ALTER TABLE jogador ADD COLUMN senha_hash VARCHAR(100);