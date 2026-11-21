# PRD-0028. Histórico por modo de jogo

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Alinhar o histórico ao front: ele filtra por `todos`/`classico`/`caos` e mostra o modo de cada partida. O back devolvia `modo` fixo em `"online"` e filtrava por `online`/`diario`, que o front não usa.

## Requisitos

- `GET /jogadores/{id}/historico?filtro=` aceita `todos`, `classico` e `caos`. Qualquer outro valor devolve `400 FILTRO_INVALIDO` (inclusive `online` e `diario`, que saíram).
- Cada partida do histórico traz `modo` com o modo real do jogo: `CLASSICO` ou `CAOS`.
- `filtro=classico` e `filtro=caos` trazem só as partidas daquele modo; `todos` traz todas.
- O resto do histórico não muda (vitória/derrota/empate, adversário, contagem de jogadas, ordem da mais recente para a mais antiga).

## Decisões

- O `modo` sai em maiúsculas (`CLASSICO`/`CAOS`), como os outros enums da API (o front converte para as chaves dele, `classico`/`caos`, do mesmo jeito que já vai converter `VITORIA` para "win").
- O desafio diário continua não existindo no back; o filtro `diario` agora é inválido em vez de devolver lista vazia.

## Fora do escopo

- Adaptador do front (mapear `CLASSICO`→`classico`, `VITORIA`→`win` etc.).
- Desafio diário.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- O histórico traz o modo certo e filtra por `classico`/`caos`.