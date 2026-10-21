# PRD-0006. Cor da peça escolhida no perfil do jogador

- **Data:** 2026-10-21
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Permitir que cada jogador escolha, antes da partida, a cor da sua peça, guardando a escolha no perfil para que o front-end e o oponente a exibam.

## Requisitos

- Campo opcional `cor` no jogador, no formato `#RRGGBB` (migration V4).
- Cor informada na criação e na atualização do jogador, e alterável em `PUT /jogadores/{id}/cor`.
- Cor devolvida nas respostas de jogador, de partida e no estado da partida (inclusive no `ESTADO` do WebSocket), com os dois jogadores identificados.
- Cor inválida responde 400. Jogador sem cor devolve `null`, e o front-end aplica uma cor padrão.
- A cor é apenas visual e não influencia as regras do jogo.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (34 testes).
- É possível criar um jogador com cor, alterá-la e consultá-la.
- O estado da partida traz `jogadorUm` e `jogadorDois` com id, nome e cor.