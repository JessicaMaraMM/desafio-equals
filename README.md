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
- Resumo do processamento (total, ignoradas, inválidas, salvas)

## Como Executar o Projeto

### 1. Subir Banco de Dados (Docker)

Na raiz do projeto:
docker compose up -d

O banco PostgreSQL ficará disponível em:

Host: localhost
Porta: 5433
Database: equals
Usuário: equals
Senha: equals

### 2. Rodar Backend

Na pasta backend:

Windows:
mvnw spring-boot:run

Mac/Linux:
./mvnw spring-boot:run

A API ficará disponível em:
http://localhost:8080

### 3. Rodar Frontend


Na pasta frontend:

npm install
npm run dev

Frontend disponível em:
http://localhost:5174

## Endpoints Principais


### Importação

POST /imports

Recebe arquivo .txt via multipart/form-data.

Retorna resumo do processamento:

{
  "totalLines": 127,
  "detailLines": 125,
  "saved": 125,
  "ignored": 2,
  "invalid": 0,
  "errors": []
}


### Filtro por período

GET /sales?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd

Retorna lista de vendas dentro do período informado.

## Decisões Técnicas

- Separação clara entre Controller, Service, Parser e Repository
- Centralização do layout posicional em classe específica (SaleLayout)
- Uso de generics no JpaRepository
- Validação de dados antes da persistência
- Tratamento global de exceções
- Frontend desacoplado do backend

## Estrutura do Projeto


backend/
frontend/
docker-compose.yml

## Próxima Evolução

- Dockerização completa da aplicação (backend + frontend)
- Build único via Docker Compose
- Ambiente totalmente isolado

Desenvolvido por Jéssica Mara de Morais Machado