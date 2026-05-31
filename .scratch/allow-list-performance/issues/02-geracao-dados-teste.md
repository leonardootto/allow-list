Status: ready-for-agent

# Geração de dados de teste (500k PVs × 20 listas)

## What to build

Script Python `scripts/generate_test_data.py` que gera 20 arquivos JSON no formato `{"pvs": [...]}` em `src/main/resources/lists/`, cada um com 500.000 PVs sequenciais e não sobrepostos.

Convenção de ranges: `merchants` usa PVs 1–500.000, `merchants-2` usa 500.001–1.000.000, e assim por diante. Essa convenção é necessária para que o teste k6 (issue #4) saiba quais PVs são hits e quais são misses na lista `merchants`.

## Acceptance criteria

- [ ] `python3 scripts/generate_test_data.py` gera 20 arquivos JSON em `src/main/resources/lists/`
- [ ] Cada arquivo contém exatamente 500.000 PVs
- [ ] PVs do arquivo `merchants.json` estão no range 1–500.000
- [ ] Ranges não se sobrepõem entre arquivos
- [ ] Script é idempotente (rodar duas vezes produz o mesmo resultado)
- [ ] Script reporta progresso e tempo de execução no stdout

## Blocked by

- #01 — estrutura de projeto e `src/main/resources/lists/` precisam existir
