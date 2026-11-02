# PRD-0007. Código de erro nas respostas da API

- **Data:** 2026-11-01
- **Estado:** Aceito
- **Desenvolvedor** Thamires

## Objetivo

Permitir que o front-end identifique cada erro por um código estável, sem depender do texto da mensagem, alinhado aos `error.code` já usados pelos serviços do front.

## Requisitos

- Toda resposta de erro REST tem o formato `{"codigo": "...", "mensagem": "..."}`.
- O `ERRO` do WebSocket passa a incluir o campo `codigo`.
- Códigos: `JOGADOR_NAO_ENCONTRADO`, `PARTIDA_NAO_ENCONTRADA`, `EMAIL_JA_CADASTRADO`, `PARTIDA_INVALIDA`, `DADOS_INVALIDOS`, `EMAIL_INVALIDO`, `COR_INVALIDA`, `REQUISICAO_INVALIDA`, `JOGO_ENCERRADO`, `FORA_DO_TURNO`, `COLUNA_INVALIDA`, `COLUNA_CHEIA` e `ERRO_INTERNO`.
- Os status HTTP existentes não mudam, e o campo `mensagem` continua presente.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (41 testes).
- Erros de jogador, partida, validação, corpo malformado e jogada inválida devolvem o código esperado.