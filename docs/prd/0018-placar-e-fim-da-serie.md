# PRD-0018. Placar e fim da série (melhor de 3 e 5)

- **Data:** 2026-11-01
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender as séries de 3 e 5 partidas escolhidas ao criar a sala, com placar e campeão, como o botão de revanche do front-end já prevê.

## Requisitos

- A série é "melhor de N": vence quem chegar primeiro à maioria das vitórias (série 1 → 1, série 3 → 2, série 5 → 3).
- A sala guarda as vitórias do dono e do convidado (migration V10). Vitória soma no placar quando a partida termina; empate não pontua.
- `GET /salas/{pin}` e as respostas da sala trazem `placar` (vitórias por id de jogador), `serieEncerrada` e `campeaoId`.
- O estado da partida (REST e WebSocket) traz `placar` e `serieEncerrada` quando a partida nasceu de uma sala.
- A próxima partida da série segue sendo pedida por `POST /salas/{pin}/revanche`, com a alternância de quem começa. Quando a série já está decidida, a revanche abre uma série nova com o placar zerado.

## Fora do escopo

- Série decidida por abandono: uma partida abandonada continua sem ser finalizada e sem pontuar.
- A regra "sempre 3 partidas", rejeitada em favor da maioria de vitórias.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (110 testes, ou 111 se o PRD-0013 já foi aplicado).
- Numa série de 3, o dono vence, o convidado vence e o dono vence de novo: o placar é 2 x 1, a série fica encerrada e o dono é o campeão.