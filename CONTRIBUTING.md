# CONTRIBUTING.md

Diretrizes para contribuir com o projeto LIG 4.

> **Status:** documento em construção. O repositório está em fase inicial e vários pontos abaixo ainda serão decididos via ADR. Itens indefinidos estão marcados com **[a definir]**.

## Antes de contribuir

1. Leia o [README.md](README.md) para entender o projeto.
2. Consulte [ARCHITECTURE.md](ARCHITECTURE.md) para conhecer a estrutura do sistema.
3. Verifique os [ADRs existentes](docs/adr/) — eles explicam as decisões já tomadas.

## Fluxo de contribuição

### 1. Issues

- Toda mudança deve partir de uma issue (bug, feature, dúvida, proposta de decisão).
- Propostas de decisão de arquitetura/tecnologia devem indicar que resultarão em um novo [ADR](docs/adr/README.md).

### 2. Branches

- Crie branches a partir de `main`.
- Convenção de nomenclatura: **[a definir]** (sugestão: `feat/nome-curto`, `fix/nome-curto`, `docs/nome-curto`).

### 3. Commits

- Toda mudança deve estar rastreada a um PRD ([docs/prd/](docs/prd/)).
- Formato da mensagem: `PRD-XXXX: descrição curta do que foi feito` (ex.: `PRD-0003: adiciona endpoint de login`).
- Commits de merge, revert e fixup/squash são isentos do formato.
- Detalhes da decisão: [ADR 0002](docs/adr/0002-padronizacao-de-commits-com-rastreio-por-prd.md).
- A validação é feita pelo hook `githooks/commit-msg`. Após clonar o repositório, ative-o uma vez:

  ```sh
  git config core.hooksPath githooks
  ```

### 4. Pull Requests

- PRs devem referenciar a issue correspondente.
- Descreva **o quê** mudou e **por quê**; detalhes de decisão devem estar no ADR correspondente.
- Revisão: pelo menos uma aprovação **[a definir]**.

## Uso de IA na contribuição

Consulte a seção ["Política de uso de IA"](AGENTS.md#política-de-uso-de-ia-pela-equipe) do [AGENTS.md](AGENTS.md).

Resumo enquanto a política não estiver formalizada:

- Saídas geradas por IA são rascunhos: **revise tudo antes de commitar**.
- Não envie dados sensíveis a serviços de IA externos.

## Documentação

- Mudanças estruturais ou tecnológicas exigem um novo ADR ([template aqui](docs/adr/template.md)).
- Ao adicionar ou mover documentação, atualize o mapa em [AGENTS.md](AGENTS.md).
