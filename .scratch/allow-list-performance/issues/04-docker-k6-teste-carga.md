Status: ready-for-agent

# Docker + k6 teste de carga com rede

## What to build

Infraestrutura completa para medir o p99 end-to-end com overhead de rede real: `Dockerfile` para a aplicação, `docker-compose.yml` com os serviços `app` e `k6` na mesma Docker bridge network, e script k6 `k6/load-test.js`.

O container k6 só inicia após o healthcheck do `app` passar (`/actuator/health`). O k6 usa `handleSummary` para gravar `results/summary.json` com as métricas agregadas que o script de apresentação (issue #5) vai consumir.

Perfil de carga: ramp-up 0→100 VUs em 30s, sustentado por 2min, ramp-down em 15s. Padrão 70% hits (PVs 1–500.000) / 30% misses (PVs 1.000.001–1.500.000) na lista `merchants`. Thresholds automáticos: `p(99) < 20`, `error_rate < 0.001` — o Docker Compose falha se não forem atingidos.

## Acceptance criteria

- [ ] `docker-compose build` constrói a imagem da aplicação sem erros
- [ ] `docker-compose up --abort-on-container-exit` inicia app, aguarda healthcheck e executa k6
- [ ] k6 não inicia antes de `/actuator/health` retornar `UP`
- [ ] `results/summary.json` é gravado ao final do teste com as chaves: `metrics.http_req_duration.values` (incluindo `p(75)`, `p(99)`), `metrics.http_reqs.values`, `metrics.http_req_failed.values`
- [ ] Threshold `p(99) < 20ms` passa (exit code 0 no container k6)
- [ ] Threshold `error_rate < 0.001` passa
- [ ] Docker Compose retorna exit code não-zero se qualquer threshold falhar

## Blocked by

- #02 — dados de teste precisam estar em `src/main/resources/lists/` antes do `docker-compose build`
