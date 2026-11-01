# PRD-0012. Revanche com alternância de quem começa

- **Data:** 2026-11-01
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Permitir que os dois jogadores joguem de novo na mesma sala, como o botão de revanche do front-end, de forma justa.

## Requisitos

- `POST /salas/{pin}/revanche` com `jogadorId`, só depois que a partida terminou e só por quem está na sala.
- A sala informa em `revancheDe` quem já pediu. Quando os dois pedem, começa uma nova partida com os mesmos jogadores e a sala volta para `JOGANDO` com o novo `partidaId`.
- Quem começou a partida anterior passa a jogar em segundo, alternando a cada revanche.
- No estado da partida, a cor de cada jogador na sala é encontrada pelo id, e não pela posição.
- Depois do fim da partida, quem sai da sala a fecha (404 `PIN_NAO_ENCONTRADO` para o outro).
- Migration V6 com as colunas `revanche_dono` e `revanche_convidado`.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (73 testes).
- Com os dois pedidos, a nova partida começa pelo jogador que jogou em segundo na anterior.
- Pedir revanche com a partida em andamento é recusado com `FASE_INVALIDA`.