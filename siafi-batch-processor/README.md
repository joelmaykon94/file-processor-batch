# SIAFI Batch File Integration

Este subprojeto contém o sistema de integração e processamento de arquivos batch do SIAFI para layouts de **Documento Hábil (DH001)** e **Programação Financeira (PF001)** em XML e CSV.

## 📚 Documentação do Projeto

*   **[Manual do Usuário e API](docs/siafi_user_guide.md)**: Guia completo de utilização da interface web e comandos cURL da API REST.
*   **[Decisões de Arquitetura](docs/architecture_decisions.md)**: Detalhamento técnico da estrutura Clean/Hexagonal e do Spring Batch.
*   **[Mockup de Interface](docs/images/siafi_dashboard_mockup.jpg)**: Imagem de demonstração da interface gráfica (Glassmorphism).

## 🚀 Como Iniciar

1.  Compile o projeto usando o Maven Wrapper:
    ```bash
    ./mvnw clean compile
    ```
2.  Inicie a aplicação Spring Boot:
    ```bash
    ./mvnw spring-boot:run
    ```
3.  Acesse o Painel de Controle no seu navegador: **`http://localhost:8080/`**
