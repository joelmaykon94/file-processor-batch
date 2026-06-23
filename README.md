# file-processor-batch

# Spring Batch: Performance e Validação Local para Produção

## 1. Persistência (`JobRepository`)
- Armazena metadados (status, parâmetros) em banco de dados.
- Permite restart, monitoramento e evita reprocessamento.
- Em testes, pode-se usar repositório em memória (`MapJobRepositoryFactoryBean`).

## 2. Processamento em Memória (Chunk)
- Lê, processa e acumula itens em memória até o `commit-interval`.
- Exemplo: `chunk(10)` processa 10 itens antes de commitar.
- Balanceamento: chunks menores = menos memória, mais commits; chunks maiores = mais memória, maior throughput.

## 3. Estratégias de Paralelismo (Testáveis Localmente)

| Estratégia | Descrição | Quando Usar |
|------------|-----------|-------------|
| **Multi-threaded Step** | Processa chunks em múltiplas threads | Processamento intensivo de CPU (exige `ItemProcessor` thread-safe) |
| **Parallel Steps** | Executa steps independentes em paralelo | Steps que não dependem entre si |
| **Local Partitioning** | Divide dados em partições e processa em paralelo | Gargalo em leitura/escrita (I/O) |

## 4. Validação de Performance Local
- **Profiling**: VisualVM, JProfiler, YourKit (CPU, memória, I/O).
- **Métricas**: Spring Boot Actuator (tempo de execução, itens processados).
- **Logs**: Identificar etapas mais lentas.

## 5. Cuidados para Produção
- Configurações que funcionam localmente podem não escalar com grandes volumes.
- Teste em homologação com dados e hardware próximos à produção.
- Ajuste fino de `chunk size`, `pool-size` e `grid-size` conforme o ambiente.

## 6. Habilidades Essenciais
- Configurar `JobRepository` e `DataSource`.
- Dominar `ItemReader`, `ItemProcessor`, `ItemWriter`.
- Ajustar tamanho do chunk e políticas de `skip`/`retry`.
- Implementar `faultTolerant()` para tolerância a falhas.
- Testar com `@SpringBatchTest` e repositórios em memória.

## Resumo
- **Desenvolva e teste localmente** com paralelismo single-process.
- **Monitore e ajuste** usando profiling e métricas.
- **Valide em homologação** com dados reais antes de produzir.
