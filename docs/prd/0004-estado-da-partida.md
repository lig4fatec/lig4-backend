# PRD-0004. Consulta do estado da partida

- **Data:** 2026-10-21
- **Estado:** Proposta

## Objetivo

Permitir que o front-end consulte a qualquer momento o estado atual de uma partida (tabuleiro, de quem é a vez e resultado), para desenhar a tela ao entrar na partida e, futuramente, ressincronizar um jogador que reconectou (WBS 1.3.1.2 e 1.5.3.2).

## Requisitos

- Endpoint `GET /partidas/{id}/estado` que devolve o status da partida, o resultado das regras, o vencedor, o próximo jogador, o total de jogadas e o tabuleiro atual.
- O estado é reconstruído a partir das jogadas gravadas, usando o motor de regras.
- Partida inexistente responde 404.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas, incluindo os testes do estado.
- Uma partida nova mostra o jogador 1 como próximo e 0 jogadas.
- Depois de uma jogada, o estado mostra 1 jogada, o jogador 2 como próximo e a peça no tabuleiro.