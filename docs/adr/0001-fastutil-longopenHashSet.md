# fastutil LongOpenHashSet para armazenamento das allow-lists

As allow-lists armazenam até 500k PVs (Long) cada, com até 20 listas simultâneas. Escolhemos `LongOpenHashSet` da biblioteca fastutil em vez de `HashSet<Long>` padrão do Java porque fastutil armazena primitivos `long` diretamente — sem boxing — reduzindo o consumo de memória de ~28MB para ~7MB por lista de 500k entradas (~4x). Com 20 listas, a diferença é ~140MB vs ~560MB. O lookup permanece O(1).

## Considered Options

- **`HashSet<Long>` (JDK)**: familiar, mas boxing de Long custa 48 bytes por entrada vs 8 bytes no fastutil.
- **Array primitivo ordenado + busca binária**: ~4MB por lista, mas O(log n) no lookup — inaceitável para o volume de consultas esperado.
- **Bloom Filter**: falsos positivos não são aceitáveis para um controle de acesso.
