# 0002. Padronização de commits com rastreio por PRD

- **Data:** 2026-08-25
- **Estado:** Aceita

## Contexto

O projeto LIG 4 é composto por dois repositórios (`lig4-backend` e `lig4-frontend`) e ainda não tem stack tecnológica definida ([ADR 0001](0001-estrutura-de-documentacao-e-uso-de-ia.md)). A equipe precisa de rastreabilidade entre o que foi pedido (requisitos) e o que foi efetivamente alterado, para controle e revisão de PRs.

Alternativas consideradas:

- **Conventional Commits** (`feat:`, `fix:` etc.): padroniza o tipo da mudança, mas não conecta o commit a um requisito.
- **Ferramenta externa de gestão** (Jira, Azure DevOps etc.): exigiria adotar uma ferramenta além do próprio repositório.
- **Ferramentas de validação de ecossistema** (ex.: commitlint + husky): exigem Node.js, antecipando uma decisão de stack ainda não tomada.

## Decisão

Adotar PRDs (Product Requirements Documents) versionados nos próprios repositórios como fonte dos identificadores de rastreio:

- PRDs ficam em [`docs/prd/`](../prd/), um arquivo por requisito, nomeados `PRD-XXXX-titulo-curto.md`, com numeração sequencial em quatro dígitos a partir de `0001`, única por repositório.
- Toda mensagem de commit deve começar com o identificador do PRD atendido:
  - Formato: `PRD-XXXX: descrição curta do que foi feito`
  - Exemplo: `PRD-0003: adiciona endpoint de login`.
- A validação é feita por hook nativo do Git ([`githooks/commit-msg`](../../githooks/commit-msg)), ativado por desenvolvedor com `git config core.hooksPath githooks`. Commits de merge, revert e fixup/squash são isentos.

## Consequências

- **Positivas:** rastreio direto requisito → commit sem depender de ferramenta externa; padrão independente de linguagem/stack; validação automática na máquina de quem committa.
- **Negativas / trade-offs:** ativação do hook é manual após cada clone; exige criar o PRD antes de commitar, o que adiciona cerimônia para mudanças pequenas; numeração por repositório pode duplicar identificadores para requisitos cross-repo.
- **Neutras:** quando a stack for definida, o hook poderá ser substituído por ferramentas do ecossistema sem mudança na convenção de mensagens.
