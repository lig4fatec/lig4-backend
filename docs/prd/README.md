# Documentos de Requisitos de Produto (PRDs)

PRDs registram os requisitos do produto e são a fonte dos identificadores usados nas mensagens de commit (ver [ADR 0002](../adr/0002-padronizacao-de-commits-com-rastreio-por-prd.md)).

## Convenções

- Um arquivo por requisito, nomeado `PRD-XXXX-titulo-curto.md` (ex.: `PRD-0001-autenticacao-de-usuarios.md`).
- Numeração sequencial em quatro dígitos, começando em `0001`, única por repositório.
- Novos PRDs seguem o [template](template.md) e entram via PR, como os ADRs.

## Fluxo

1. Escreva o PRD descrevendo objetivo, requisitos e critérios de aceite.
2. Obtenha aprovação da equipe via PR.
3. Referencie o número (`PRD-XXXX`) nos branches, issues, PRs e commits relacionados ao requisito.

## Índice

| PRD | Título | Estado |
|---|---|---|
| [0001](0001-estrutura-de-documentacao-e-padronizacao-de-commits.md) | Estrutura de documentação e padronização de commits | Implementado |
| [0002](0002-migracao-para-spring-boot-e-motor-de-regras.md) | Migração para Spring Boot e motor de regras do jogo | Implementado |
| [0003](0003-persistencia-de-jogadas.md) | Persistência e registro das jogadas | Implementado |
| [0004](0004-estado-da-partida.md) | Consulta do estado da partida | Implementado |
| [0005](0005-websocket-da-partida.md) | Comunicação em tempo real da partida via WebSocket | Implementado |
| [0006](0006-cor-da-peca-do-jogador.md) | Cor da peça escolhida no perfil do jogador | Implementado |
| [0007](0007-codigo-de-erro-nas-respostas.md) | Código de erro nas respostas da API | Implementado |
| [0008](0008-cors-e-origens-permitidas.md) | CORS e origens permitidas do front-end | Implementado |
| [0009](0009-salas-por-pin.md) | Salas por PIN: criar, entrar e consultar | Implementado |
| [0010](0010-lobby-da-sala.md) | Lobby da sala: trocar cor, iniciar e sair | Implementado |
| [0011](0011-partida-da-sala.md) | Partida ligada à sala | Implementado |
| [0012](0012-revanche-com-alternancia.md) | Revanche com alternância de quem começa | Implementado |
| [0014](0014-login-essencial.md) | Login essencial: cadastro e entrada com senha | Implementado |
| [0015](0015-perfil-estatisticas-e-moedas.md) | Perfil, estatísticas e moedas do jogador | Implementado |
| [0016](0016-historico-de-partidas.md) | Histórico de partidas do jogador | Implementado |
| [0017](0017-loja.md) | Loja: catálogo, compra e itens equipados | Implementado |
| [0018](0018-placar-e-fim-da-serie.md) | Placar e fim da série (melhor de 3 e 5) | Implementado |
| [0013](0013-publicacao-do-back-end.md) | Publicação do back-end | Implementado |
| [0019](0019-presenca-e-logs-do-websocket.md) | Presença dos jogadores na partida e logs do WebSocket | Implementado |