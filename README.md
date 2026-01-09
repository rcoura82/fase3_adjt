# 🏥 Sistema de Gerenciamento de Consultas Médicas - Hospital Management System

Sistema backend modular e escalável para gerenciamento de consultas médicas em ambiente hospitalar, desenvolvido com Spring Boot, GraphQL e RabbitMQ.

## 📋 Visão Geral

Este sistema oferece uma solução completa para:
- ✅ Agendamento de consultas médicas
- 🔒 Controle de acesso por perfil (Médicos, Enfermeiros, Pacientes)
- 📊 Histórico completo de consultas via GraphQL
- 📧 Notificações automáticas para pacientes
- 🔄 Comunicação assíncrona entre serviços

## 🏗️ Arquitetura

O sistema é composto por 3 microserviços independentes:

### 1. **Scheduling Service** (Porta 8080)
- **Responsabilidade**: Gerenciamento de consultas (CRUD)
- **Tecnologias**: Spring Boot, Spring Security, PostgreSQL, RabbitMQ
- **Funcionalidades**:
  - Criar, editar, listar e cancelar consultas
  - Autenticação e autorização via Spring Security
  - Publicação de eventos no RabbitMQ

### 2. **Notification Service** (Porta 8081)
- **Responsabilidade**: Envio de notificações aos pacientes
- **Tecnologias**: Spring Boot, RabbitMQ
- **Funcionalidades**:
  - Consumir eventos de agendamento
  - Enviar lembretes automáticos (email/log)
  - Processar atualizações e cancelamentos

### 3. **History Service** (Porta 8082)
- **Responsabilidade**: Histórico de consultas com GraphQL
- **Tecnologias**: Spring Boot, GraphQL, PostgreSQL, RabbitMQ
- **Funcionalidades**:
  - Consultas flexíveis via GraphQL
  - Sincronização automática via RabbitMQ
  - GraphQL Playground para testes

### Infraestrutura
- **RabbitMQ**: Message broker para comunicação assíncrona
- **PostgreSQL**: Banco de dados relacional
- **Docker Compose**: Orquestração de containers

## 📊 Fluxo de Dados

```
Médico/Enfermeiro → [REST API] → Scheduling Service
                                        ↓
                                 Criar/Editar Consulta
                                        ↓
                                  PostgreSQL (salva)
                                        ↓
                            Publicar evento → RabbitMQ
                                        ↓
                        ┌───────────────┴───────────────┐
                        ↓                               ↓
            Notification Service              History Service
                        ↓                               ↓
                Enviar notificação            Sincronizar histórico
                  ao paciente                  (disponível via GraphQL)
```

## 🔐 Controle de Acesso

### Perfis de Usuário

| Perfil | Username | Password | Permissões |
|--------|----------|----------|------------|
| **Médico** | `doctor` | `doctor123` | ✅ Criar, editar, visualizar e cancelar todas as consultas |
| **Enfermeiro** | `nurse` | `nurse123` | ✅ Criar, editar, visualizar e cancelar todas as consultas |
| **Paciente** | `patient` | `patient123` | 👁️ Visualizar apenas suas próprias consultas |

## 🚀 Instalação e Execução

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker e Docker Compose
- Git

### 1. Clonar o Repositório
```bash
git clone https://github.com/rcoura82/fase3_adjt.git
cd fase3_adjt
```

### 2. Iniciar Infraestrutura (RabbitMQ e PostgreSQL)
```bash
# Dar permissão aos scripts
chmod +x start-infrastructure.sh build-all.sh stop-all.sh

# Iniciar containers
./start-infrastructure.sh
```

Aguarde até ver:
```
✅ Infrastructure services started!
```

### 3. Build dos Serviços
```bash
./build-all.sh
```

### 4. Executar os Serviços

#### Terminal 1 - Scheduling Service
```bash
cd scheduling-service
mvn spring-boot:run
```

#### Terminal 2 - Notification Service
```bash
cd notification-service
mvn spring-boot:run
```

#### Terminal 3 - History Service (Opcional)
```bash
cd history-service
mvn spring-boot:run
```

### 5. Verificar Serviços

- **Scheduling API**: http://localhost:8080/api/appointments
- **Notification Service**: Logs no terminal
- **GraphQL Playground**: http://localhost:8082/graphiql
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

## 📚 Endpoints da API

### REST API - Scheduling Service

#### 🔓 Criar Consulta (Doctor/Nurse)
```bash
POST http://localhost:8080/api/appointments
Authorization: Basic doctor:doctor123
Content-Type: application/json

{
  "patientId": 1,
  "patientName": "João Silva",
  "patientEmail": "joao.silva@email.com",
  "doctorId": 100,
  "doctorName": "Dr. Maria Santos",
  "appointmentDate": "2026-02-15T10:00:00",
  "notes": "Consulta de rotina"
}
```

#### 📋 Listar Todas as Consultas (Doctor/Nurse)
```bash
GET http://localhost:8080/api/appointments
Authorization: Basic doctor:doctor123
```

#### 🔍 Buscar Consulta por ID
```bash
GET http://localhost:8080/api/appointments/1
Authorization: Basic doctor:doctor123
```

#### 👤 Listar Consultas do Paciente
```bash
GET http://localhost:8080/api/appointments/patient/1
Authorization: Basic doctor:doctor123
```

#### ⏭️ Listar Consultas Futuras do Paciente
```bash
GET http://localhost:8080/api/appointments/patient/1/future
Authorization: Basic doctor:doctor123
```

#### ✏️ Atualizar Consulta (Doctor/Nurse)
```bash
PUT http://localhost:8080/api/appointments/1
Authorization: Basic doctor:doctor123
Content-Type: application/json

{
  "appointmentDate": "2026-02-16T11:00:00",
  "notes": "Horário alterado"
}
```

#### ❌ Cancelar Consulta (Doctor/Nurse)
```bash
DELETE http://localhost:8080/api/appointments/1
Authorization: Basic doctor:doctor123
```

### GraphQL API - History Service

Acesse o **GraphQL Playground**: http://localhost:8082/graphiql

#### Query: Histórico do Paciente
```graphql
query {
  patientAppointments(patientId: 1) {
    id
    patientName
    doctorName
    appointmentDate
    status
    notes
  }
}
```

#### Query: Consultas Futuras
```graphql
query {
  futureAppointments(patientId: 1) {
    id
    doctorName
    appointmentDate
    notes
  }
}
```

#### Query: Consulta por ID
```graphql
query {
  appointment(id: 1) {
    id
    patientName
    patientEmail
    doctorName
    appointmentDate
    notes
    status
    createdAt
    updatedAt
  }
}
```

#### Query: Todas as Consultas
```graphql
query {
  allAppointments {
    id
    patientName
    doctorName
    appointmentDate
    status
  }
}
```

## 🧪 Testes com Postman

Importe a collection:
```bash
postman-collection.json
```

A collection inclui:
- ✅ 8 requests REST (Scheduling Service)
- ✅ 4 queries GraphQL (History Service)
- ✅ Autenticação pré-configurada
- ✅ Exemplos de dados

## 🛠️ Estrutura do Projeto

```
fase3_adjt/
├── scheduling-service/          # Serviço de Agendamento
│   ├── src/main/java/com/hospital/scheduling/
│   │   ├── controller/         # REST Controllers
│   │   ├── service/            # Business Logic
│   │   ├── repository/         # JPA Repositories
│   │   ├── entity/             # JPA Entities
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── config/             # Spring Configuration
│   │   ├── security/           # Security Components
│   │   ├── event/              # RabbitMQ Events
│   │   └── exception/          # Exception Handlers
│   └── pom.xml
│
├── notification-service/        # Serviço de Notificações
│   ├── src/main/java/com/hospital/notification/
│   │   ├── listener/           # RabbitMQ Listeners
│   │   ├── service/            # Notification Logic
│   │   ├── event/              # Event Models
│   │   └── config/             # RabbitMQ Config
│   └── pom.xml
│
├── history-service/             # Serviço de Histórico
│   ├── src/main/java/com/hospital/history/
│   │   ├── controller/         # GraphQL Controllers
│   │   ├── repository/         # JPA Repositories
│   │   ├── entity/             # JPA Entities
│   │   ├── listener/           # RabbitMQ Listeners
│   │   ├── event/              # Event Models
│   │   └── config/             # Configuration
│   ├── src/main/resources/graphql/
│   │   └── schema.graphqls     # GraphQL Schema
│   └── pom.xml
│
├── docker-compose.yml           # Infraestrutura
├── postman-collection.json      # Testes API
├── build-all.sh                 # Script de build
├── start-infrastructure.sh      # Iniciar containers
├── stop-all.sh                  # Parar containers
└── README.md                    # Este arquivo
```

## 🧩 Tecnologias Utilizadas

### Backend
- **Spring Boot 3.2.1** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Spring AMQP** - Integração RabbitMQ
- **Spring GraphQL** - API GraphQL

### Banco de Dados
- **PostgreSQL 15** - Banco relacional principal
- **H2** - Banco em memória para testes

### Mensageria
- **RabbitMQ 3.12** - Message broker

### Build & Testes
- **Maven** - Gerenciamento de dependências
- **JUnit 5** - Framework de testes
- **Lombok** - Redução de boilerplate

### DevOps
- **Docker & Docker Compose** - Containerização

## 📝 Validações e Regras de Negócio

### Scheduling Service
✅ Data da consulta deve ser no futuro  
✅ Campos obrigatórios: patientId, patientName, patientEmail, doctorId, doctorName, appointmentDate  
✅ Email deve ter formato válido  
✅ Apenas médicos e enfermeiros podem criar/editar/cancelar  
✅ Pacientes só visualizam suas próprias consultas

### Notification Service
✅ Processa eventos: CREATED, UPDATED, CANCELLED  
✅ Envia notificações personalizadas por tipo de evento  
✅ Log detalhado de todas as notificações

### History Service
✅ Sincronização automática via RabbitMQ  
✅ Consultas flexíveis via GraphQL  
✅ Suporte a filtros (patientId, future appointments)

## 🔍 Monitoramento

### RabbitMQ Management
```
URL: http://localhost:15672
User: guest
Pass: guest
```

**Verificar**:
- Filas: `appointment.created.queue`, `appointment.updated.queue`
- Exchange: `appointment.events`
- Mensagens processadas

### Logs
Todos os serviços emitem logs detalhados:
- **Scheduling**: Criação/edição de consultas + publicação de eventos
- **Notification**: Processamento de eventos + envio de notificações
- **History**: Sincronização de dados

## 🧪 Testando o Sistema

### Cenário 1: Criar e Notificar
1. **POST** `/api/appointments` → Criar consulta (doctor)
2. Verificar logs do **Notification Service** → Notificação enviada
3. Verificar **RabbitMQ** → Mensagem consumida
4. **Query GraphQL** `patientAppointments` → Dados sincronizados

### Cenário 2: Atualizar Consulta
1. **PUT** `/api/appointments/1` → Atualizar horário
2. Verificar logs → Notificação de atualização
3. **Query GraphQL** `appointment(id: 1)` → Dados atualizados

### Cenário 3: Cancelar Consulta
1. **DELETE** `/api/appointments/1` → Cancelar
2. Verificar logs → Notificação de cancelamento
3. **Query GraphQL** → Status alterado para CANCELLED

## ⚠️ Troubleshooting

### Erro: "Connection refused" ao conectar ao PostgreSQL
```bash
# Verificar se o container está rodando
docker ps | grep postgres

# Reiniciar container
docker-compose restart postgres
```

### Erro: "Connection refused" ao conectar ao RabbitMQ
```bash
# Verificar se o container está rodando
docker ps | grep rabbitmq

# Reiniciar container
docker-compose restart rabbitmq
```

### Porta já em uso
```bash
# Verificar processos nas portas
lsof -i :8080  # Scheduling
lsof -i :8081  # Notification
lsof -i :8082  # History

# Matar processo
kill -9 <PID>
```

### Services não iniciam
```bash
# Limpar e rebuildar
./stop-all.sh
./build-all.sh
./start-infrastructure.sh
```

## 🛑 Parar o Sistema

```bash
# Parar containers Docker
./stop-all.sh

# Parar serviços Spring Boot
# Ctrl+C em cada terminal
```

## 📚 Documentação Adicional

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Spring GraphQL](https://docs.spring.io/spring-graphql/docs/current/reference/html/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials/tutorial-one-spring-amqp.html)

## 👨‍💻 Desenvolvimento

### Adicionar Novos Endpoints
1. Criar DTO em `dto/`
2. Adicionar método no Service
3. Criar endpoint no Controller
4. Adicionar anotação `@PreAuthorize` se necessário

### Adicionar Novo Evento RabbitMQ
1. Definir evento em `event/`
2. Configurar fila no `RabbitMQConfig`
3. Publicar com `rabbitTemplate.convertAndSend()`
4. Criar listener com `@RabbitListener`

### Adicionar Query GraphQL
1. Definir schema em `schema.graphqls`
2. Criar método no Controller com `@QueryMapping`
3. Implementar lógica no Repository

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos (Fase 3 - ADJT).

## 👥 Autores

- **Ricardo Coura** - [rcoura82](https://github.com/rcoura82)

---

**⚡ Quick Start:**
```bash
./start-infrastructure.sh  # 1. Iniciar containers
./build-all.sh              # 2. Build dos serviços
# 3. Executar serviços em terminais separados
cd scheduling-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd history-service && mvn spring-boot:run
```

**📊 Test Endpoints:**
- REST API: http://localhost:8080/api/appointments
- GraphQL: http://localhost:8082/graphiql
- RabbitMQ: http://localhost:15672