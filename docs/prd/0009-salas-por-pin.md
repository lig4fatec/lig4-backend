# PRD-0009. Salas por PIN: criar, entrar e consultar

- **Data:** 2026-11-01
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Permitir que um jogador crie uma sala com um PIN de 6 dígitos e outro jogador entre nela, como o fluxo de telas do front-end (T05 a T08).

## Requisitos

- `POST /salas` cria a sala com `jogadorId`, `serie` (1, 3 ou 5; padrão 1) e `cor`, e devolve o PIN.
- `POST /salas/entrar` coloca o convidado na sala pelo `pin`, com `jogadorId` e `cor`.
- `GET /salas/{pin}` devolve a situação da sala: PIN, série, fase, dono, convidado e partida.
- As cores válidas são `blue`, `red`, `purple`, `green`, `orange`, `yellow` e `brown`, e os dois jogadores não podem repetir a cor.
- Quem já está na sala e entra de novo recebe a situação atual (volta à sala).
- Códigos de erro: `PIN_INVALIDO`, `PIN_NAO_ENCONTRADO`, `SALA_CHEIA`, `COR_JA_ESCOLHIDA`, `SERIE_INVALIDA`, `COR_INVALIDA` e `JOGADOR_NAO_ENCONTRADO`.
- Tabela `sala` criada pela migration V5.
- Troca de cor, início da partida e saída da sala ficam para o PRD-0010.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (54 testes).
- É possível criar uma sala, entrar com outro jogador e consultar a sala.
- Entrar com a mesma cor do dono, ou numa sala cheia, é recusado com o código esperado.