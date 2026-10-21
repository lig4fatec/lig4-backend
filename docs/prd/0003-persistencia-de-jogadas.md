# PRD-0003. Persistência e registro das jogadas

- **Data:** 2026-10-21
- **Estado:** Proposta

## Objetivo

Registrar cada jogada de uma partida no banco e ligar o motor de regras ao back-end, para que o front-end possa jogar, consultar o histórico e receber o resultado da partida (WBS 1.6.1.2, 1.6.2.1 e 1.6.2.2).

## Requisitos

- Tabela `jogada` (migration V3) ligada a `partida` e `jogador`, com número da jogada, coluna e linha, e restrições de integridade (coluna de 0 a 6, linha de 0 a 5, número único por partida).
- Endpoint `POST /partidas/{id}/jogadas` que valida e registra a jogada usando o motor de regras (`br.com.lig4.engine`) e devolve a posição da peça, o tabuleiro, o resultado e o próximo jogador.
- Endpoint `GET /partidas/{id}/jogadas` com o histórico de jogadas da partida.
- Ao ocorrer vitória ou empate, a partida é finalizada automaticamente, com vencedor e data de término registrados.
- Duas jogadas simultâneas na mesma partida não podem se misturar (bloqueio da partida durante a jogada).
- Respostas de erro: 409 para jogada fora do turno ou partida encerrada; 400 para coluna inválida ou cheia, jogador que não participa da partida e dados inválidos; 404 para partida inexistente.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas, incluindo os testes de jogadas.
- Uma partida jogada até a vitória vertical é finalizada com o vencedor correto e rejeita novas jogadas com 409.
- O histórico devolve as jogadas em ordem de número.