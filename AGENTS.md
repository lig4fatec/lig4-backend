# AGENTS.md

Ponto de entrada para **agentes de IA** (e para humanos que queiram entender como a IA deve ser usada neste projeto).

## Sobre o projeto

Backend do projeto LIG 4. O repositório está em fase inicial: **ainda não há stack tecnológica definida**. Não presuma linguagens, frameworks, bibliotecas ou ferramentas — se algo não estiver documentado, trate como indefinido e sinalize.

## Como um agente de IA deve trabalhar neste repositório

1. Leia este arquivo primeiro.
2. Consulte o [mapa da documentação](#mapa-da-documentação) e leia os documentos indicados para a tarefa em questão.
3. Verifique os ADRs existentes em [`docs/adr/`](docs/adr/) antes de propor qualquer mudança estrutural ou tecnológica.
4. **Não invente decisões.** Se uma informação necessária não existe nos documentos, aponte a lacuna e sugira registrar uma nova ADR.
5. Escreva documentação e código em português, salvo convenções técnicas consolidadas (nomes de arquivos, termos consagrados etc.).
6. Toda decisão relevante (tecnologia, padrão, processo) deve ser registrada como ADR seguindo o [template](docs/adr/template.md).

## Mapa da documentação

| Documento | Conteúdo | Quando ler |
|---|---|---|
| [README.md](README.md) | Visão geral do projeto | Sempre |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Arquitetura do sistema e decisões técnicas | Antes de propor mudanças estruturais |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Fluxo de contribuição (branches, commits, PRs) | Antes de criar branches, commits ou PRs |
| [docs/adr/README.md](docs/adr/README.md) | Processo de registro de decisões (ADRs) | Antes de registrar ou contestar uma decisão |
| [docs/adr/](docs/adr/) | Registros de decisões de arquitetura | Para entender o porquê das escolhas existentes |
| [docs/prd/README.md](docs/prd/README.md) | Requisitos de produto (PRDs), fonte dos identificadores de commit | Antes de criar requisitos, branches, commits ou PRs |
| [docs/prd/](docs/prd/) | Documentos de requisitos de produto | Para entender o que deve ser construído e rastrear mudanças |

## Política de uso de IA pela equipe

> **Status: em definição.** As regras abaixo serão formalizadas em ADR próprio.

A equipe usará IA de formas variadas (chat, agentes locais, IDEs assistidos). Para evitar inconsistências, os seguintes pontos precisam estar definidos e documentados:

- [ ] Modelos/provedores aprovados para uso com código deste projeto
- [ ] Política de uso local vs. nuvem (quando cada um é exigido)
- [ ] Confidencialidade: o que pode e o que não pode ser enviado a serviços externos
- [ ] Ferramentas homologadas (IDEs, CLIs, extensões)
- [ ] Expectativa de revisão humana sobre saídas geradas por IA

Enquanto esses itens estiverem indefinidos:

- **Não envie dados sensíveis** (credenciais, dados de clientes, documentos restritos) a serviços de IA externos.
- Trate saídas de IA como **rascunho**: sempre revise antes de commitar.

## Manutenção deste arquivo

Este arquivo é a fonte da verdade sobre "onde está a informação". Ao adicionar ou mover documentação, atualize o mapa acima na mesma mudança.
