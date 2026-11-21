# PRD-0025. Poderes do modo Caos

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender a decisão de 12 a 18/11 com o front-end: no modo Caos o jogador usa os poderes que comprou na loja (PRD-0023): Queimar Peça, Congelar Coluna e Peça Extra ("Jogar 2x").

## Requisitos

- Só no modo Caos, só com o poder comprado, cada poder uma vez por jogador por partida, só na vez do jogador e antes de soltar peça nessa vez.
- **Queimar Peça:** remove uma peça do adversário (coluna e linha, com linha 0 na base) e gasta a vez. Não há peça para queimar no primeiro lance. Sem gravidade: a casa fica vazia e a próxima peça da coluna cai nela.
- **Congelar Coluna:** o adversário não joga na coluna escolhida nas próximas 3 vezes dele, e a vez passa a ele. Não vale em coluna cheia. Quem só tem colunas congeladas perde a vez.
- **Peça Extra:** o jogador solta duas peças seguidas. Não gasta a vez. Não vale com menos de 2 casas livres.
- O poder é gravado como evento na tabela `jogada` (coluna `tipo`: `PECA`, `QUEIMAR`, `CONGELAR`, `EXTRA`), e a partida continua sendo refeita jogada por jogada pelo motor.
- Endpoint `POST /partidas/{id}/poderes` e comando `PODER` no WebSocket; os dois jogadores recebem o `ESTADO`.
- O estado traz `poderesUsados`, `poderesDisponiveis`, `congelamentos` e `pecaExtraAtiva`. A resposta de cada jogada traz `congelamentos` e `pecaExtraAtiva`.
- Códigos novos: `PODER_INVALIDO` (400); `MODO_SEM_PODERES`, `PODER_NAO_POSSUIDO`, `PODER_JA_USADO`, `PODER_FORA_DE_HORA`, `ALVO_INVALIDO`, `COLUNA_CONGELADA`, `PECA_EXTRA_SEM_ESPACO` (409).
- `totalJogadas` e o histórico do perfil contam só as peças.
- Migration V15: coluna `tipo` em `jogada` (as jogadas antigas viram `PECA`).

## Decisões

- Queimar e Congelar gastam a vez; a Peça Extra não.
- Usar poder depois de uma proposta de empate do adversário vale como recusá-la.

## Fora do escopo

- Gravidade depois de queimar (peças de cima descerem): PRD-0026.
- Preço diferente por poder, poderes em modo Clássico e poderes por tempo limitado.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Cada poder funciona pela API e pelo WebSocket e respeita as regras acima.
- Partidas antigas continuam funcionando (jogadas sem `tipo` viram `PECA`).