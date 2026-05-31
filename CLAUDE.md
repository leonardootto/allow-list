# Agent Skills

Skills disponíveis em `.agents/skills/`. Para usar, invoque `/nome-da-skill` ou peça ao agente que a execute.

| Skill | Descrição |
|-------|-----------|
| `caveman` | Modo de comunicação ultra-comprimido (~75% menos tokens) |
| `diagnose` | Loop disciplinado para bugs difíceis e regressões de performance |
| `grill-me` | Entrevista implacável sobre um plano ou design |
| `grill-with-docs` | Grilling com atualização inline de CONTEXT.md e ADRs |
| `handoff` | Compacta a conversa atual em documento de handoff para outro agente |
| `improve-codebase-architecture` | Encontra oportunidades de aprofundamento arquitetural |
| `prototype` | Constrói protótipo descartável para validar um design |
| `setup-matt-pocock-skills` | Configura issue tracker, labels e docs de domínio para o repo |
| `tdd` | Desenvolvimento orientado a testes com loop red-green-refactor |
| `to-issues` | Quebra um plano em issues no issue tracker |
| `to-prd` | Transforma o contexto atual em um PRD e publica no issue tracker |
| `triage` | Triagem de issues através de uma máquina de estados |
| `write-a-skill` | Cria novas skills com estrutura adequada |
| `zoom-out` | Sobe um nível de abstração e mapeia módulos e callers relevantes |

Cada skill tem seu `SKILL.md` em `.agents/skills/<nome>/`. Algumas têm arquivos de suporte na mesma pasta.
