# PRD-0020. Modo de jogo (Clássico e Caos), fim da série e moedas por modo

- **Data:** 2026-11-18
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

A sala passa a ter um modo (Clássico ou Caos) no lugar da série, e as moedas do fim da partida dependem do modo.

## Requisitos

- `POST /salas` recebe `modo` (`CLASSICO` ou `CAOS`, em qualquer caixa e com ou sem acento). Vazio vale `CLASSICO`. Outro valor devolve `400 MODO_INVALIDO`.
- A série e o placar saem da sala e do estado da partida (`serie`, `placar`, `serieEncerrada` e `campeaoId`). Um `serie` enviado por um front antigo é ignorado.
- A sala e o estado da partida devolvem `modo`. A partida guarda o modo da sala, e a revanche mantém o modo.
- `POST /partidas` (sem sala) aceita `modo` e usa `CLASSICO` por padrão. A resposta traz `modo`.
- Moedas no fim da partida, creditadas uma única vez:

| Modo | Vitória | Empate | Derrota |
|---|---|---|---|
| Clássico | +15 | +5 | +2 |
| Caos | +30 | +10 | +5 |

- As cores continuam únicas por sala (`COR_JA_ESCOLHIDA`).
- Migration V11: remove `serie`, `vitorias_dono` e `vitorias_convidado` da sala e cria `modo` na sala e na partida (padrão `CLASSICO`, com restrição aos dois valores).

## Fora do escopo

- Poderes do Caos (PRD futuro).
- Relógio, desistência, proposta de empate e "pronto" no lobby (PRDs futuros).
- Os valores do Clássico (15, 5 e 2) são uma proposta a confirmar com o grupo. Para mudar, basta alterar as constantes de `RecompensaService`.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Sala criada sem modo vem como `CLASSICO`, e com `CAOS` vem como `CAOS`.
- Vitória no Caos dá +30 e +5 ao perdedor. No Clássico, +15 e +2.
- A revanche mantém o modo.
- O PRD-0018 passa a constar como substituído por este.