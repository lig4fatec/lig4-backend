# PRD-0035. Trocar senha e e-mail

- **Data:** 2026-11-23
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

A tela de perfil do front tem os botões "Alterar e-mail" e "Alterar senha" (marcados como "Em breve"), mas o back não tinha endpoint para isso: a senha só era definida no cadastro e o e-mail só mudava por um endpoint genérico (`PUT /jogadores/{id}`). Este PRD adiciona a troca de senha e de e-mail da conta.

## Requisitos

- `PUT /jogadores/{id}/senha` com `{senhaAtual, senhaNova}`: confere a `senhaAtual` (BCrypt) e grava a nova (6 a 72 caracteres). Senha atual errada devolve `401 CREDENCIAIS_INVALIDAS`. Sucesso: `204`.
- `PUT /jogadores/{id}/email` com `{email, senha}`: confere a `senha`, valida o e-mail e grava, desde que não esteja em uso por outro jogador (`409 EMAIL_JA_CADASTRADO`). Senha errada: `401`. Sucesso: o jogador atualizado.
- As duas operações **confirmam a senha** antes de gravar (boa prática de segurança).

## Decisões

- Ficam em `/jogadores/{id}/senha` e `/jogadores/{id}/email`, no mesmo padrão de `/jogadores/{id}/perfil` e `/jogadores/{id}/cor`.
- A lógica fica no `AuthService` (que já tem o `PasswordEncoder`).
- Senha errada usa o mesmo código do login (`CREDENCIAIS_INVALIDAS`), para não distinguir os casos.
- Erro de validação em qualquer campo que comece com "senha" vira `SENHA_INVALIDA`.

## Fora do escopo

- "Esqueci minha senha" / recuperação por e-mail (precisaria de envio de e-mail).
- Token: segue sem autenticação; qualquer cliente pode chamar com um `jogadorId`, como no resto da API.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Trocar a senha com a senha certa deixa logar com a nova e não com a antiga.
- Trocar o e-mail com a senha certa deixa logar com o novo; e-mail já usado é recusado.