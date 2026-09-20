# 0001. Estrutura de documentação e uso de IA

- **Data:** 2026-08-25
- **Estado:** Aceita

## Contexto

O projeto LIG 4 está em fase inicial, sem stack tecnológica definida. Antes de qualquer decisão técnica, a equipe identificou duas necessidades:

1. A equipe usará IA de formas variadas (chat, agentes locais, IDEs assistidos), sem critérios definidos sobre modelos, provedores e confidencialidade.
2. Sem uma fonte centralizada de informação, humanos e agentes de IA podem presumir tecnologias ou inventar decisões que nunca foram tomadas.

## Decisão

Adotar uma estrutura de documentação mínima, com o [AGENTS.md](../../AGENTS.md) como ponto de entrada obrigatório para agentes de IA:

- `AGENTS.md` — índice central: mapa da documentação, regras de conduta para agentes de IA e política de uso de IA pela equipe.
- `ARCHITECTURE.md` — arquitetura do sistema, preenchido conforme as decisões forem tomadas.
- `CONTRIBUTING.md` — fluxo de contribuição.
- `docs/adr/` — registros de decisões de arquitetura, com template próprio.

A política detalhada de uso de IA (modelos aprovados, uso local vs. nuvem, confidencialidade, ferramentas homologadas) será registrada em ADR futuro.

## Consequências

- **Positivas:** agentes de IA têm instruções consistentes; decisões ficam rastreáveis; evita-se retrabalho e suposições incorretas sobre a stack.
- **Negativas / trade-offs:** custo de manutenção dos documentos; risco de desatualização se os ADRs não forem mantidos em dia.
- **Neutras:** toda decisão relevante passa a exigir um novo ADR via PR.
