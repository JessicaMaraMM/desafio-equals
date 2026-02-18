# Desafio Técnico - Equals

## Sistema de importação e consulta de vendas a partir de layout posicional.

Aplicação fullstack para processamento de arquivos .txt em layout posicional, 
com validação estrutural e de regras de negócio, persistência em PostgreSQL 
e consulta de vendas por período através de interface web.

## Stack Utilizada

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven

### Frontend
- React
- Vite
- JavaScript

### Infraestrutura
- Docker
- Docker Compose
- Nginx (reverse proxy)

### Funcionalidades Implementadas
- Importação de arquivo .txt via API
- Validação de layout (tamanho mínimo e campos obrigatórios)
- Validações de negócio:
    - transactionCode obrigatório (32 caracteres)
    - valores não negativos
    - campos obrigatórios
- Persistência em banco PostgreSQL
- Filtro de vendas por período
- Listagem de vendas
- Tratamento de erros de importação
- Resumo do processamento:
  - total de linhas
  - linhas de detalhe
  - ignoradas
  - inválidas
  - salvas
  - lista de erros (linha + motivo)
- Testes unitários para:
  - Parser
  - Serviço de importação

## Melhorias UX
- Validação de período no frontend
- Possibilidade de filtrar apenas com data inicial ou final
- Mensagens de erro
- Tratamento de respostas não-JSON da API
- Formatação de valores monetários no padrão pt-BR

## Como Executar o Projeto

### Execução Completa via Docker

Na raiz do projeto:

```bash
docker compose up --build
```

A aplicação ficará disponível em:

http://localhost


### Arquitetura Docker
- PostgreSQL -> porta 5433 (host)
- Backend Spring Boot -> porta 8080 (container interno)
- Frontend React -> servido via Nginx na porta 80
- Nginx atua como reverve proxy:
  - /api/* -> backend
  - / -> frontend
  
Fluxo:

1. Usuário acessa http://localhost
2. Nginx serve o frontend
3. Requisições /api são redirecionadas para o backend
4. Backend comunica com o banco via rede interna Docker

## Execução Manual (Modo Desenvolvimento)

### Banco (Docker)

```bash
docker compose up -d db
```

Banco disponível em:
- Host: localhost
- Porta: 5433
- Database: equals
- Usuário: equals
- Senha: equals

### Backend

Na pasta backend:

```bash
./mvnw spring-boot:run
```

API disponível em:
http://localhost:8080

### Rodar os testes

Na pasta backend:

```bash
./mvnw.cmd test
```

### Frontend

```bash
npm install
npm run dev
```

## Endpoints Principais

### Importação

POST /imports

Recebe arquivo .txt via multipart/form-data.

Retorno:

```json
{
  "totalLines": 127,
  "detailLines": 125,
  "saved": 125,
  "ignored": 2,
  "invalid": 0,
  "errors": []
}
```

### Filtro por período

GET /sales?start=yyyy-MM-dd&end=yyyy-MM-dd

Retorna lista de vendas dentro do período informado.

## Decisões Técnicas
- Separação clara entre Controller, Service, Parser e Repository
- Centralização do layout posicional em classe específica (SaleLayout)
- Uso de generics no JpaRepository
- Validação de dados antes da persistência
- Tratamento global de exceções
- Testes unitários focados em regras críticas
- Frontend desacoplado e consumindo API
- Aplicação totalmente containerizada

## Estrutura do Projeto

```
desafio-equals/
│
├── backend/
│
├── frontend/
│
└── docker-compose.yml
```

Desenvolvido por Jéssica Mara de Morais Machado