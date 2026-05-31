Status: ready-for-agent

# Apresentação Reveal.js + `update_presentation.py`

## What to build

Apresentação `presentation/index.html` em Reveal.js (dark theme, português) que conta a história completa: problema → arquitetura → metodologia 70/30 → benchmark local → k6 com rede → SLA → Kubernetes → comparação com alternativas → pros/cons → conclusão.

A apresentação usa um bloco `// RESULTS_START ... // RESULTS_END` no `<script>` com `const RESULTS = {...}` contendo valores placeholder. O script `scripts/update_presentation.py` lê `results/benchmark-results.json` e `results/summary.json` e substitui esse bloco via regex com os dados reais.

Charts via Chart.js: comparação de memória (fastutil vs HashSet), percentis do benchmark (ns), percentis do k6 (ms) com linha de SLA em 20ms, e gauge de p99 vs SLA. Métricas textuais (p99, RPS, error rate) são populadas dinamicamente a partir do `const RESULTS`.

A nota sobre metodologia 70/30 deve aparecer explicitamente no slide de metodologia para antecipar questionamentos.

A tabela comparativa cobre: Static In-Memory (esta solução), Redis Cache, PostgreSQL, Hazelcast, Unleash/LaunchDarkly — com colunas p99 estimado, throughput, frequência de atualização, complexidade operacional e dependências externas.

## Acceptance criteria

- [ ] `presentation/index.html` abre no browser sem servidor HTTP (file://) e exibe todos os slides
- [ ] Slides com charts renderizam corretamente via Chart.js CDN
- [ ] Valores de `const RESULTS` com dados placeholder são visíveis nos slides antes de rodar o update script
- [ ] `python3 scripts/update_presentation.py` atualiza o bloco `RESULTS` com dados reais de `results/benchmark-results.json` e `results/summary.json`
- [ ] Após o update, p99 do k6 aparece no slide de SLA com indicador visual ✅/❌ baseado no threshold de 20ms
- [ ] Slide de metodologia contém nota explicando o padrão 70% hits / 30% misses e o motivo da escolha
- [ ] Tabela comparativa com as 5 arquiteturas está presente e legível

## Blocked by

- #03 — estrutura de `results/benchmark-results.json` precisa estar definida
- #04 — estrutura de `results/summary.json` precisa estar definida
