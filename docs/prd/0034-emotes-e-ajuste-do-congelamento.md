# PRD-0034. Emotes na partida e ajuste do congelamento

- **Data:** 2026-11-23
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Duas melhorias decididas na reunião do grupo (23/11), ambas de back-end:
1. **Emotes na partida:** o front já tinha o botão, mas o back não recebia nem transmitia emotes.
2. **Ajuste do congelamento:** o Congelar Coluna bloqueava a coluna por 3 jogadas do adversário, o que era forte demais (quase um "ganha automático"). Passa a bloquear por 1.

## Requisitos

### Emotes
- Comando `EMOTE` no WebSocket (`{"tipo":"EMOTE","emote":"<id>"}`) e endpoint `POST /partidas/{id}/emotes` (`{jogadorId, emote}`).
- O servidor transmite aos dois jogadores: `{"tipo":"EMOTE","dados":{"jogadorId","emote"}}`.
- Só pode mandar quem participa da partida; o `emote` tem que ser um id da categoria `emote` da loja. Caso contrário, `EMOTE_INVALIDO` (400).
- É só visual: não custa moedas nem exige ter comprado o emote (emotes são cosméticos).

### Congelamento
- A constante `TURNOS_DE_CONGELAMENTO` passa de 3 para 1: a coluna fica bloqueada para o adversário só na próxima vez dele.
- O front deve usar o mesmo número (1).

## Decisões

- Emote não exige posse para não travar a partida com um erro; o front mostra só os emotes do jogador de qualquer forma. A validação de posse pode ser adicionada depois, se quiserem.
- 1 lance de congelamento foi a escolha do grupo, por eliminar o abuso de travar o oponente.

## Fora do escopo

- Histórico/limite de emotes por partida (pode mandar quantos quiser).
- Mudar o número de congelamento no front (tarefa do front).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Um emote enviado chega aos dois jogadores; emote inválido é recusado.
- O Congelar bloqueia a coluna por apenas 1 jogada do adversário.