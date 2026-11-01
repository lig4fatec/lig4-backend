# PRD-0016. Histórico de partidas do jogador

- **Data:** 2026-11-01
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender a tela de histórico do front-end, listando as partidas já terminadas de um jogador.

## Requisitos

- `GET /jogadores/{id}/historico?filtro=todos|online|diario` devolve `{"partidas":[...]}` com `id`, `data` (fim da partida), `adversario` (nick), `resultado` (`VITORIA`, `DERROTA` ou `EMPATE`), `jogadas` (total da partida) e `modo`.
- Só entram partidas finalizadas, das 50 mais recentes, da mais nova para a mais antiga.
- Toda partida do back-end é `online`. O filtro `diario` devolve lista vazia, porque o desafio diário não existe no back-end.
- Filtro desconhecido devolve 400 `FILTRO_INVALIDO`. Jogador inexistente devolve 404 `JOGADOR_NAO_ENCONTRADO`.

## Fora do escopo

- Paginação além das 50 partidas mais recentes e o detalhe de uma partida (já existe em `GET /partidas/{id}/jogadas`).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (96 testes, ou 97 se o PRD-0013 já foi aplicado).
- Após uma vitória, o vencedor vê `VITORIA` e o perdedor vê `DERROTA`, cada um com o nick do adversário.