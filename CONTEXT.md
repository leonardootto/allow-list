# Allow-List

Serviço estático de verificação de permissão por estabelecimento. Determina se um PV específico está habilitado para uma funcionalidade, consultando listas pré-carregadas em memória.

## Language

**PV**:
Identificador numérico Long de um estabelecimento comercial.
_Avoid_: estabelecimento, merchant, client, loja

**Allow-List**:
Conjunto nomeado de PVs habilitados para uma funcionalidade específica. O nome é a chave de acesso à lista.
_Avoid_: whitelist, feature toggle, feature flag

**Hit**:
Resultado de uma consulta onde o PV existe na allow-list consultada.
_Avoid_: match, encontrado, presente

**Miss**:
Resultado de uma consulta onde o PV não existe na allow-list consultada.
_Avoid_: not found, ausente, negativo

**Carregamento Eager**:
Estratégia onde todas as allow-lists são carregadas e indexadas em memória durante a inicialização da aplicação, antes de aceitar tráfego.
_Avoid_: lazy loading, carregamento tardio

## Convenções

- Código-fonte, variáveis e comentários: inglês
- Apresentações, documentação de decisão (ADRs), CONTEXT.md: português
