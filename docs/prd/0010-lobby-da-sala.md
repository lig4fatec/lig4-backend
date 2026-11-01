# PRD-0010. Lobby da sala: trocar cor, iniciar e sair

- **Data:** 2026-11-01
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Completar o fluxo da sala de espera do front-end (T08): trocar a cor, iniciar a partida e sair da sala.

## Requisitos

- `PUT /salas/{pin}/cor` troca a cor de quem está na sala, sem repetir a do outro jogador, apenas enquanto a sala aguarda o início.
- `POST /salas/{pin}/iniciar` só vale para o dono, com o convidado na sala. Cria a partida (dono como jogador 1, que começa), devolve `partidaId` e muda a fase para `JOGANDO`.
- `POST /salas/{pin}/sair` responde 204. No lobby, o convidado libera a vaga e, se o dono sair, a sala deixa de existir. Com a partida iniciada, a sala passa a `ENCERRADA`.
- O front acompanha a sala consultando `GET /salas/{pin}` a cada 2 segundos.
- Códigos de erro novos: `SEM_PERMISSAO` (403), `SEM_CONVIDADO` (409) e `FASE_INVALIDA` (409).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (65 testes).
- O dono inicia a partida e `GET /partidas/{partidaId}/estado` responde.
- Ações fora de hora ou de quem não está na sala são recusadas com o código esperado.