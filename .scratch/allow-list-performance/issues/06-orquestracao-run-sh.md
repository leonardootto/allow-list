Status: ready-for-agent

# Orquestração `run.sh`

## What to build

Script `run.sh` que executa o fluxo completo de ponta a ponta em uma única invocação: benchmark local → geração de dados → build Gradle → Docker build → k6 com Docker → atualização da apresentação.

O script deve verificar pré-requisitos (`python3`, `kotlin`, `docker`, `docker compose`, `java`) e falhar com mensagem clara se algum estiver ausente. Cada etapa deve ser claramente sinalizada no stdout com numeração e tempo decorrido. O script deve parar em qualquer erro (`set -euo pipefail`).

Ao final, exibir um resumo com os números principais (p99 benchmark, p99 k6, RPS, error rate) e instruir o usuário a abrir `presentation/index.html`.

## Acceptance criteria

- [ ] `./run.sh` executa todas as 6 etapas em sequência sem intervenção manual
- [ ] Script falha com mensagem útil se `python3`, `kotlin`, `docker` ou `java` não estiverem no PATH
- [ ] Cada etapa exibe início, conclusão e tempo decorrido
- [ ] Falha em qualquer etapa interrompe o script imediatamente (exit code não-zero)
- [ ] Ao final, exibe resumo com p99 benchmark, p99 k6, RPS e error rate
- [ ] Mensagem final instrui o usuário a abrir `presentation/index.html`
- [ ] `chmod +x run.sh` está documentado ou o script já vem com permissão de execução

## Blocked by

- #05 — todos os componentes precisam existir antes de serem orquestrados
