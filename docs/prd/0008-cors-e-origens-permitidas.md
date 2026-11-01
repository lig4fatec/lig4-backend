# PRD-0008. CORS e origens permitidas do front-end

- **Data:** 2026-11-01
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Permitir que o front-end, hospedado em outro endereço, chame o REST e abra o WebSocket, bloqueando origens desconhecidas.

## Requisitos

- Propriedade `lig4.origens-permitidas` com a lista de origens, separadas por vírgula, com curinga de subdomínio.
- A mesma lista vale para o CORS do REST e para as origens do WebSocket.
- Padrão de desenvolvimento: `localhost` e `127.0.0.1` nas portas 3000, 5173, 5500 e 8000.
- No deploy, a lista é trocada pela variável de ambiente `LIG4_ORIGENS_PERMITIDAS`, sem alterar o código.
- Origem fora da lista é recusada (403).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (44 testes).
- Uma requisição de origem permitida recebe `Access-Control-Allow-Origin`.
- Uma origem desconhecida é bloqueada.