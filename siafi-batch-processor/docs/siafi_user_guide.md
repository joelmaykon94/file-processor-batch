# Manual do Sistema - SIAFI Batch File Integration

Este guia descreve detalhadamente o funcionamento, a arquitetura e a utilização do sistema **SIAFI Batch File Integration** (processador de arquivos de carga do SIAFI para **Documento Hábil - DH001** e **Programação Financeira - PF001**).

---

## 1. Mockup da Interface Esperada (Resultado Visual)

Abaixo está o design visual do Painel de Controle Web integrado. Ele utiliza princípios modernos de **glassmorphism**, cores HSL curadas (ciano neon e roxo neon) e monitoramento dinâmico.

![Mockup do Painel de Controle SIAFI](images/siafi_dashboard_mockup.jpg)

---

## 2. Funcionamento Interno do Processamento Batch

O fluxo de processamento foi desenhado para garantir tolerância a falhas e isolamento de camadas em conformidade com as regras de **Arquitetura Limpa (Clean Architecture)**:

1. **Ingestão (Input)**: Os arquivos XML/CSV entram no sistema via upload manual ou são coletados da pasta monitorada.
2. **Sniffing & Routing**: O sistema lê as primeiras linhas do arquivo, identifica se o layout é `DH001` ou `PF001` e inicia o respectivo job do Spring Batch de forma assíncrona.
3. **Chunking**: O arquivo é fatiado em pedaços de 5 em 5 registros (`chunk size = 5`). Cada bloco de 5 é lido, processado e commitado no banco de dados isoladamente. Em caso de falha em uma linha específica, apenas aquele chunk sofre rollback, mantendo os registros anteriores salvos com segurança.
4. **Isolamento de Entidades**: Para atender a regras rígidas de arquitetura (AA-1200+), as entidades JPA de banco de dados **nunca** são transmitidas via rede. O sistema mapeia os dados para estruturas imutáveis (**Java Records**) na camada de transporte (DTO).

---

## 3. Guia de Utilização - Interface Web

Acesse a interface em **`http://localhost:8080/`** no seu navegador:

* **Upload de Arquivos**: Arraste um arquivo XML ou CSV ou clique na caixa pontilhada **File Ingestion** para carregar um lote. Ele será processado imediatamente.
* **Consumo Automático**: Copie seus arquivos para a pasta descrita no campo **WATCH FOLDER** na tela. O sistema irá consumi-los de forma autônoma a cada 10 segundos.
* **Visualização de Histórico**: Clique na aba respectiva (**Documento Hábil** ou **Programação Financeira**) para ver a lista de lotes carregados com status (`SUCCESS`, `PROCESSING`, `FAILED`).
* **Visualização de Logs**: Para ver mensagens detalhadas de um lote específico, clique em **View Logs**.
* **Exploração de Dados**: A tabela inferior exibe os registros de pagamentos extraídos em tempo real de forma legível.

---

## 4. Guia de Utilização - Integração via API (cURL)

A API REST do sistema é totalmente desacoplada e pode ser consumida por outros microsserviços ou scripts bash:

### A. Obter Status do Monitoramento
Retorna o status ativo e o caminho da pasta monitorada no disco:
```bash
curl -X GET http://localhost:8080/api/siafi/status
```
*Resposta esperada (JSON):*
```json
{
  "active": true,
  "dhCount": 3,
  "pfCount": 1,
  "incomingDir": "/home/joelmaykon/file-processor-batch/siafi-batch-processor/siafi-files/incoming"
}
```

### B. Upload Manual de Lote (XML ou CSV)
Envia um arquivo para processamento batch imediato:
```bash
curl -X POST -F "file=@/caminho/do/seu/arquivo/DH_Sample.xml" http://localhost:8080/api/siafi/upload
```
*Resposta esperada:*
```json
{
  "message": "File uploaded and processed successfully: DH_Sample.xml"
}
```

### C. Listar Lotes Processados de Documento Hábil (DH001)
Retorna a lista de cargas de DH convertidas de forma limpa para DTO:
```bash
curl -X GET http://localhost:8080/api/siafi/dh-batches
```

### D. Listar Lotes Processados de Programação Financeira (PF001)
Retorna a lista de cargas de PF convertidas para DTO:
```bash
curl -X GET http://localhost:8080/api/siafi/pf-batches
```

### E. Limpar Histórico de Cargas
Deleta o histórico dos lotes e detalhes salvos no H2 Database:
```bash
curl -X DELETE http://localhost:8080/api/siafi/clear
```
