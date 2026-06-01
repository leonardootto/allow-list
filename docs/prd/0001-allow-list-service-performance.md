# PRD: Allow-List Service com Validação de Performance

## Problem Statement

O time desenvolveu uma solução de allow-list estática para controle de acesso por PV (Ponto de Venda), mas os testes de performance realizados localmente foram questionados pela liderança técnica como números inflados — sem overhead de rede, o ambiente local não representa fielmente o comportamento em produção. É preciso demonstrar a viabilidade da solução com dados coletados em condições realistas, incluindo latência de rede, e apresentar esses dados de forma convincente para validar a adoção.

## Solution

Construir do zero o Allow-List Service em Spring Boot + Kotlin com armazenamento in-memory via `LongOpenHashSet` (fastutil), acompanhado de uma suite de validação em dois níveis:

1. **Benchmark local (CPU-only)**: script `.main.kts` que mede throughput e latência do algoritmo puro sem stack HTTP, gerando dados que demonstram a eficiência do lookup e o consumo de memória em comparação com `HashSet<Long>` padrão do Java.

2. **Teste de carga com rede real**: k6 rodando em container Docker separado, comunicando com a aplicação via Docker bridge network, medindo p99 de latência end-to-end sob 100 VUs com padrão 70% hit / 30% miss.

Os resultados dos dois testes são automaticamente injetados em uma apresentação Reveal.js que compara a solução com quatro arquiteturas alternativas (Redis, PostgreSQL, Hazelcast, Unleash), serve como argumento definitivo para a liderança e documenta a decisão arquitetural.

## User Stories

1. Como desenvolvedor, quero consultar se um PV está em uma allow-list específica via HTTP, para que outros serviços possam usar o allow-list sem acoplamento direto.
2. Como desenvolvedor, quero receber `{"allowed": true}` ou `{"allowed": false}` na resposta, para que o consumidor não precise interpretar status codes para determinar o resultado da consulta.
3. Como desenvolvedor, quero receber `404 Not Found` quando consulto uma allow-list inexistente, para que erros de configuração sejam detectáveis explicitamente.
4. Como desenvolvedor, quero que a aplicação recuse tráfego até todas as allow-lists estarem carregadas, para que nenhum request seja atendido com estado parcial.
5. Como desenvolvedor, quero adicionar uma nova allow-list apenas colocando um arquivo JSON no classpath, para que o processo de onboarding de uma lista nova seja simples e sem código.
6. Como tech lead, quero ver o p99 de latência com overhead de rede real abaixo de 20ms, para que o SLA de produção seja comprovadamente atendido.
7. Como tech lead, quero ver a taxa de erro abaixo de 0,1% sob 100 usuários virtuais simultâneos, para que a estabilidade sob carga seja demonstrada.
8. Como tech lead, quero ver o consumo de memória real do `LongOpenHashSet` comparado ao `HashSet<Long>` padrão, para que a escolha do fastutil seja justificada com dados concretos.
9. Como tech lead, quero ver uma comparação da solução com Redis, PostgreSQL, Hazelcast e Unleash, para que a decisão arquitetural esteja documentada com trade-offs explícitos.
10. Como tech lead, quero ver os resultados de benchmark local (sem rede) ao lado dos resultados com Docker, para que o delta introduzido pela rede seja visível e o argumento de que os números locais eram inflados seja respondido diretamente.
11. Como engenheiro de SRE, quero que a aplicação exponha `/actuator/health` como readiness probe, para que o Kubernetes saiba quando o pod está pronto para receber tráfego.
12. Como engenheiro de SRE, quero que cada pod Kubernetes carregue sua própria cópia das listas, para que o scale-out horizontal não introduza dependências de sincronização.
13. Como engenheiro de SRE, quero que a aplicação não tenha dependências externas em runtime, para que falhas de infraestrutura (Redis down, DB indisponível) não afetem o serviço.
14. Como desenvolvedor, quero rodar `./run.sh` e obter a apresentação atualizada automaticamente, para que o fluxo de gerar evidência seja reproduzível sem intervenção manual.
15. Como desenvolvedor, quero que o script de benchmark escreva `results/benchmark-results.json` e o k6 escreva `results/summary.json`, para que `update_presentation.py` possa injetar os dados reais na apresentação sem edição manual do HTML.

## Implementation Decisions

### Endpoint

- `GET /allow-list/{listName}?pv={Long}` retorna `200 {"allowed": true|false}` ou `404` se a lista não existir.
- O nome da allow-list no path corresponde exatamente ao nome do arquivo JSON no classpath (sem extensão).

### Armazenamento in-memory

- Cada allow-list é indexada como um `LongOpenHashSet` (fastutil), que armazena primitivos `long` sem boxing.
- Todas as listas são carregadas na inicialização da aplicação (Carregamento Eager). A aplicação bloqueia e só aceita tráfego após todas as listas estarem prontas — ver ADR 0003.
- Justificativa da escolha do fastutil: ver ADR 0001.

### Dados das allow-lists

- Arquivos JSON embarcados no JAR em `classpath:lists/*.json`, com formato `{"pvs": [Long, ...]}`.
- O nome do arquivo determina o nome da allow-list.
- Atualização requer redeploy — ver ADR 0002.
- Para os testes de performance, os dados são gerados por `scripts/generate_test_data.py`: 20 listas de 500k PVs cada, com PVs sequenciais (1–500.000 para `merchants`, 500.001–1.000.000 para `merchants-2`, etc.).

### Benchmark local (CPU-only)

- Script `benchmark/allowlist-benchmark.main.kts` (Kotlin Script), sem Spring, sem HTTP.
- Gera 1.000.000 PVs aleatórios em memória para o `LongOpenHashSet` e 1.000.000 PVs para um `HashSet<Long>` equivalente (para comparação de memória).
- Padrão de acesso: 70% hits / 30% misses — mesmo padrão do k6 para garantir comparabilidade.
- 1.000.000 ops de warmup (descartadas) + 5.000.000 ops medidas em lotes de 1.000.
- Saída: `results/benchmark-results.json` com `p50_us`, `p75_us`, `p90_us`, `p95_us`, `p99_us`, `max_us`, `throughput_ops_sec`, `memory_mb`, `memory_hashset_mb`.

### Teste de carga com rede (k6 + Docker)

- Docker Compose: container `app` (Spring Boot) + container `k6`, na mesma Docker bridge network.
- k6 só inicia após o healthcheck do `app` passar (`/actuator/health`).
- Perfil de carga: ramp-up 0→100 VUs em 30s, sustentado 100 VUs por 2min, ramp-down em 15s.
- Padrão 70% hits / 30% misses: hits usam PVs 1–500.000 (presentes na lista `merchants`), misses usam PVs 1.000.001–1.500.000 (ausentes).
- Threshold de sucesso: `p(99) < 20ms`, `error_rate < 0.001`.
- `handleSummary` no k6 escreve `results/summary.json` com métricas agregadas.

### Apresentação

- `presentation/index.html` — Reveal.js, português, dark theme.
- Bloco `// RESULTS_START ... // RESULTS_END` no `<script>` contém `const RESULTS = {...}` com valores placeholder.
- `scripts/update_presentation.py` lê `results/benchmark-results.json` + `results/summary.json` e substitui o bloco via regex.
- Slides: problema → arquitetura → metodologia 70/30 → benchmark → k6 → SLA → Kubernetes → comparação com 4 alternativas → pros/cons → conclusão.

### Orquestração

- `run.sh`: executa em sequência benchmark → gera dados → build Gradle → Docker build → k6 → update apresentação.

## Testing Decisions

**O que faz um bom teste aqui**: testa comportamento externo observável (status HTTP, corpo da resposta, ausência de erros sob carga), não detalhes de implementação (como os dados estão estruturados internamente, qual classe do fastutil é usada).

**Seam primário — HTTP endpoint**: testes de integração que sobem o contexto Spring completo com listas de teste pequenas no classpath de teste. Cobrem:
- Hit: PV presente retorna `200 {"allowed": true}`
- Miss: PV ausente retorna `200 {"allowed": false}`
- Lista inexistente retorna `404`
- Parâmetro `pv` inválido retorna `400`

**Seam secundário — `AllowListService`**: testes unitários diretos ao serviço (sem HTTP) para casos que o seam HTTP tornaria verbosos. Cobrem os mesmos casos de hit/miss/lista inexistente em isolamento.

**Seam de performance — k6 + Docker**: threshold automático no k6 (`p(99) < 20`, `error_rate < 0.001`). O teste falha o Docker Compose se os thresholds não forem atingidos.

## Out of Scope

- Atualização de allow-lists em runtime sem redeploy.
- Autenticação ou autorização no endpoint.
- Suporte a outros tipos de identificador além de `Long`.
- Persistência ou auditoria de consultas.
- Dashboard em tempo real de acessos por allow-list.
- API para listar os PVs de uma allow-list.
- Suporte a múltiplos formatos de dados além de JSON.

## Further Notes

- Três ADRs documentam as decisões chave: `docs/adr/0001` (fastutil), `docs/adr/0002` (JSON no classpath), `docs/adr/0003` (Carregamento Eager).
- Escala esperada: ~2M PVs totais distribuídos em até 20 allow-lists de 500k entradas.
- SLA de 20ms p99 é o benchmark da indústria para serviços internos de lookup in-memory via HTTP.
- A nota metodológica 70/30 deve aparecer explicitamente na apresentação para antecipar questionamentos sobre o padrão de acesso usado nos testes.
