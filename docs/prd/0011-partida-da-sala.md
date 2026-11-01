# PRD-0011. Partida ligada à sala

- **Data:** 2026-11-01
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Fazer a partida iniciada numa sala mostrar ao front-end as informações da sala (PIN, série e cores) e avisar quando terminar.

## Requisitos

- O estado da partida (REST e mensagem `ESTADO` do WebSocket) passa a trazer `pin` e `serie`. Sem sala, os dois vêm nulos.
- Em partidas de sala, `jogadorUm.cor` e `jogadorDois.cor` são as cores escolhidas na sala. O dono é o jogador 1 e começa.
- Quando a partida termina, por vitória ou empate, a sala passa para `ENCERRADA`.
- O front conecta ao WebSocket em `/ws/partidas/{partidaId}?jogadorId={id}`, usando o `partidaId` devolvido pela sala.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (68 testes).
- O estado de uma partida de sala traz o PIN, a série e as cores da sala.
- Ao final da partida, `GET /salas/{pin}` devolve a fase `ENCERRADA`.