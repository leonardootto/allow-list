# Dados das allow-lists embarcados como JSON no classpath

Os dados das allow-lists são arquivos `.json` empacotados dentro do JAR, carregados via Carregamento Eager na inicialização. Escolhemos essa abordagem — em vez de Redis, banco de dados ou configuração externa — porque a solução deve ser completamente estática e sem dependências de infraestrutura em runtime. Cada pod Kubernetes carrega sua própria cópia isolada, eliminando qualquer ponto único de falha e tornando o scale-out trivial: um novo pod sobe com os dados já presentes.

## Consequences

- Atualizar uma allow-list requer redeploy da aplicação. Isso é aceitável porque as listas mudam com baixa frequência.
- O tamanho do JAR aumenta proporcionalmente ao volume das listas (estimado: ~5MB por lista de 500k PVs em JSON).
