# PRD-0026. Gravidade depois do Queimar

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Completar o poder Queimar Peça (PRD-0025): depois que uma peça é queimada, as peças de cima descem para preencher o buraco, como a gravidade faria. Essa queda pode alinhar quatro peças e terminar a partida.

## Requisitos

- Ao queimar a peça em `(coluna, linha)`, as peças acima dela na mesma coluna descem uma casa (a coluna fica compacta, sem buracos).
- Depois da queda, o motor confere o tabuleiro todo. Se alguma cor tiver um 4 em linha, a partida termina com `motivoFim` `VITORIA`, as moedas são pagas, a sala encerra e os dois jogadores recebem o `ESTADO`.
- **Regra "física pura":** vence a cor do 4 que se formar, mesmo que seja a do adversário de quem queimou (queimar mal pode entregar a vitória ao adversário). Se as duas cores formarem um 4 ao mesmo tempo, vence o adversário de quem queimou.
- Se a queda não formar nenhum 4, o Queimar só passa a vez, como antes.
- A regra vale também no replay: como o estado da partida é refeito jogada por jogada, a gravidade é reaplicada a cada evento de Queimar. Não há migration.

## Decisões

- A vitória por queda usa o vencedor que o motor aponta (pode ser o adversário de quem usou o poder), diferente da jogada normal, em que só quem solta a peça pode vencer.
- O empate de "tabuleiro cheio" não acontece no Queimar, porque queimar sempre libera uma casa.

## Fora do escopo

- Animação da queda (cabe ao front, a partir do `tabuleiro` do estado).
- Gravidade para outros poderes (só o Queimar tira peças do tabuleiro).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Depois do Queimar, as peças de cima descem.
- Um 4 em linha formado pela queda termina a partida com a cor certa (física pura).