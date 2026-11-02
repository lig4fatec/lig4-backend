# PRD-0019. Presença dos jogadores na partida e logs do WebSocket

- **Data:** 2026-11-01
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Avisar cada jogador quando o adversário desconecta ou volta durante a partida, e deixar o servidor com registros úteis para diagnosticar problemas.

## Requisitos

- Quando alguém conecta ou desconecta, o servidor envia aos conectados da partida `{"tipo":"PRESENCA","dados":{"conectados":[ids]}}`, com os ids sem repetição e em ordem crescente.
- Quem conecta continua recebendo o `ESTADO` atual (reconexão), seguido da presença.
- Uma pessoa com mais de uma conexão aparece uma vez e só sai da lista quando fecha todas.
- O servidor registra em log as conexões, desconexões, jogadas aceitas e mensagens recusadas, com a partida e o jogador.
- O front deve ignorar tipos de mensagem que não conhece.

## Fora do escopo

- Encerrar a partida ou dar a vitória a quem ficou quando o adversário não volta. A partida abandonada continua sem ser finalizada.
- Presença no lobby (a sala segue por consulta a cada 2 segundos).
- Tratamento geral de erro interno no REST.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (113 testes, ou 114 se o PRD-0013 já foi aplicado).
- Quem entra depois avisa o primeiro, e quem sai avisa o adversário.
- Quem volta recebe o estado atual e o adversário vê o id reaparecer na lista.