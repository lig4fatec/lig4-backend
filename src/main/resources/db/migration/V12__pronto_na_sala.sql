-- "Pronto" de cada jogador no lobby: a partida só começa quando os dois marcam.
ALTER TABLE sala ADD COLUMN pronto_dono      BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE sala ADD COLUMN pronto_convidado BOOLEAN NOT NULL DEFAULT FALSE;