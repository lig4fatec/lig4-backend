-- Alinha o catálogo da loja aos ids do front-end. Os itens equipados padrão mudam de id,
-- então atualiza o valor padrão das colunas e os jogadores que ainda estão com o padrão antigo
-- (senão ficariam apontando para um item que não existe mais no catálogo).
ALTER TABLE jogador ALTER COLUMN avatar_equipado    SET DEFAULT 'avatar-padrao';
ALTER TABLE jogador ALTER COLUMN tabuleiro_equipado SET DEFAULT 'board-classico';
ALTER TABLE jogador ALTER COLUMN emote_equipado     SET DEFAULT 'emote-risadinha';

UPDATE jogador SET avatar_equipado    = 'avatar-padrao'   WHERE avatar_equipado    = 'avatar-classico';
UPDATE jogador SET tabuleiro_equipado = 'board-classico'  WHERE tabuleiro_equipado = 'board-minimalista';
UPDATE jogador SET emote_equipado     = 'emote-risadinha' WHERE emote_equipado     = 'emote-saudacao';

-- Remove itens comprados cujos ids saíram do catálogo (as partidas de teste do deploy, se houver).
-- Os ids novos passam a valer a partir daqui; nada é reembolsado porque a loja ainda é de demonstração.
DELETE FROM jogador_item
WHERE item_id NOT IN (
    'avatar-samurai', 'avatar-robo', 'avatar-rei', 'avatar-rainha', 'avatar-mago',
    'board-espacial', 'board-floresta', 'board-medieval', 'board-praia', 'board-inverno',
    'emote-aplauso', 'emote-choro', 'emote-surpresa', 'emote-bravo', 'emote-cool', 'emote-coracao', 'emote-confuso',
    'congelar-coluna', 'queimar-peca', 'peca-extra'
);