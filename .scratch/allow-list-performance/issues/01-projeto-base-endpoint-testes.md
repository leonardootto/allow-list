Status: ready-for-agent

# Projeto base: endpoint + testes

## What to build

Criar o projeto Spring Boot + Kotlin do zero com a funcionalidade central: endpoint `GET /allow-list/{listName}?pv={Long}` que consulta se um PV está em uma allow-list carregada em memória via `LongOpenHashSet` (fastutil).

A aplicação deve bloquear o startup até todas as allow-lists estarem indexadas (Carregamento Eager), expor `/actuator/health` como readiness probe, e responder `{"allowed": true|false}` para listas existentes ou `404` para listas inexistentes.

Arquivos JSON de teste pequenos (dezenas de PVs) devem ser incluídos no classpath de teste para permitir que a suite rode sem depender dos dados de produção.

Decisões arquiteturais já tomadas estão em `docs/adr/0001`, `docs/adr/0002` e `docs/adr/0003`.

## Acceptance criteria

- [ ] `GET /allow-list/{listName}?pv={pv}` retorna `200 {"allowed": true}` para PV presente na lista
- [ ] `GET /allow-list/{listName}?pv={pv}` retorna `200 {"allowed": false}` para PV ausente na lista
- [ ] `GET /allow-list/{listName}?pv={pv}` retorna `404` para lista inexistente
- [ ] Parâmetro `pv` inválido (não-numérico) retorna `400`
- [ ] Aplicação só aceita tráfego após todas as listas estarem carregadas (`/actuator/health` retorna `UP`)
- [ ] Testes de integração via HTTP seam passam (sem mocks do serviço)
- [ ] Testes unitários do `AllowListService` cobrem hit, miss e lista inexistente
- [ ] `./gradlew test` passa sem erros

## Blocked by

None - can start immediately
