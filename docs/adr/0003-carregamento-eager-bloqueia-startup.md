# Carregamento Eager bloqueia startup até todas as listas estarem prontas

A aplicação carrega e indexa todas as allow-lists em memória durante a inicialização, antes de aceitar qualquer tráfego. Escolhemos essa estratégia porque a latência da primeira consulta deve ser idêntica à de qualquer outra — um cold start no primeiro request seria inaceitável em produção, onde a solução compete diretamente com alternativas como Redis.

No Kubernetes, o readiness probe (`/actuator/health`) só retorna `UP` após a inicialização completa, garantindo que o pod não recebe tráfego antes de estar pronto.

## Considered Options

- **Lazy loading**: primeira consulta a cada lista dispara o carregamento. Startup imediato, mas primeiro request paga o custo de I/O — quebraria o argumento de latência previsível.
- **Background loading com fallback**: app sobe imediatamente, listas carregam em background, consultas retornam 503 até a lista estar pronta. Mais complexo e introduz estado transitório difícil de testar.

## Consequences

- Startup time proporcional ao volume total das listas (estimado: 3–8s para 20 listas de 500k PVs cada).
- Rolling update no Kubernetes funciona corretamente: novo pod só entra em rotação após carregamento completo.
