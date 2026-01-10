# Instructions for fase3_adjt

## Project Overview
**Sistema de Gerenciamento de Consultas Médicas** - Backend Java escalável e modular para ambiente hospitalar com suporte a múltiplos perfis de usuários (Médicos, Enfermeiros, Pacientes), agendamento de consultas, histórico médico via GraphQL e notificações automáticas.

### Technology Stack
- **Framework**: Spring Boot 3.x com Spring Security
- **API**: GraphQL (resolver para histórico) + REST (agendamento, autenticação)
- **Messaging**: RabbitMQ para comunicação assíncrona
- **Database**: PostgreSQL (recomendado) ou H2 para testes
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build**: Maven

## Architecture Overview

### Serviços Principais
1. **Service de Autenticação & Autorização**
   - Spring Security com roles: ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT
   - JWT ou Basic Auth para endpoints
   - Controlador de acesso por role em cada operação

2. **Serviço de Agendamento** (Scheduling Service)
   - Gerencia CRUD de consultas
   - Publica eventos para RabbitMQ quando consulta é criada/editada
   - REST API: `/api/appointments/*`
   - Validações: data/hora futura, médico/paciente válidos

3. **Serviço de Notificações** (Notification Service)
   - Consome mensagens de agendamento do RabbitMQ
   - Envia lembretes (simulado em dev ou via email)
   - Listeners: `ConsultationScheduledListener`, `ConsultationUpdatedListener`

4. **Serviço de Histórico** (History Service) - OPCIONAL
   - GraphQL para consultas flexíveis do histórico médico
   - Schema GraphQL: Query { patientAppointments, futureAppointments }
   - Replica dados do Service de Agendamento via RabbitMQ

### Data Flow
```
Doctor/Nurse → REST API (Appointment Service)
    ↓
Appointment Created/Updated Event
    ↓
RabbitMQ Message (topic: "appointment.events")
    ↓
Notification Service Listener
    ↓
Send Reminder to Patient
```

## Project Structure & Conventions

### Directory Structure
```
fase3_adjt/
├── scheduling-service/        # Serviço de agendamento
│   ├── src/main/java/
│   │   └── com/hospital/scheduling/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── exception/
│   │       └── config/
│   ├── pom.xml
│   └── application.yml
├── notification-service/      # Serviço de notificações
│   ├── src/main/java/
│   │   └── com/hospital/notification/
│   │       ├── listener/
│   │       ├── service/
│   │       ├── config/
│   │       └── event/
│   ├── pom.xml
│   └── application.yml
├── history-service/           # Serviço de histórico (opcional)
│   ├── src/main/java/
│   │   └── com/hospital/history/
│   │       ├── graphql/
│   │       ├── resolver/
│   │       ├── service/
│   │       └── config/
│   ├── pom.xml
│   └── application.yml
├── docker-compose.yml         # RabbitMQ + PostgreSQL
└── postman-collection.json    # Testes API
```

### Naming Conventions
- **Entity Classes**: `Appointment`, `Patient`, `Doctor`, `Nurse` (PascalCase)
- **DTOs**: `AppointmentDTO`, `CreateAppointmentRequest` (PascalCase)
- **Service Methods**: `createAppointment()`, `listPatientAppointments()` (camelCase)
- **Event Classes**: `AppointmentScheduledEvent`, `AppointmentUpdatedEvent`
- **REST Endpoints**: `/api/appointments`, `/api/appointments/{id}`, `/api/patients/{patientId}/appointments`
- **RabbitMQ Queues**: `appointment.created.queue`, `appointment.updated.queue`
- **RabbitMQ Exchange**: `appointment.events` (topic exchange)

## Critical Development Patterns

### 1. Role-Based Access Control (Spring Security)
```java
// Controller example
@PostMapping("/appointments")
@PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
public AppointmentDTO createAppointment(@RequestBody CreateAppointmentRequest req) {
    // Only doctors and nurses can create appointments
}

@GetMapping("/appointments/{id}")
@PreAuthorize("@appointmentSecurity.canAccess(#id, authentication)")
public AppointmentDTO getAppointment(@PathVariable Long id) {
    // Doctors/nurses see all, patients only their own
}
```

### 2. RabbitMQ Event Publishing
```java
// When appointment is created/updated, publish event
@Service
public class AppointmentService {
    @Autowired private RabbitTemplate rabbitTemplate;
    
    public void createAppointment(CreateAppointmentRequest req) {
        Appointment appointment = new Appointment(/* ... */);
        appointmentRepository.save(appointment);
        
        // Publish event
        rabbitTemplate.convertAndSend(
            "appointment.events",           // exchange
            "appointment.created",          // routing key
            new AppointmentScheduledEvent(appointment)
        );
    }
}
```

### 3. RabbitMQ Event Listening (Notification Service)
```java
@Component
public class ConsultationScheduledListener {
    @RabbitListener(queues = "appointment.created.queue")
    public void handleAppointmentCreated(AppointmentScheduledEvent event) {
        // Send reminder email/SMS to patient
        notificationService.sendReminder(event.getPatient(), event.getAppointmentDate());
    }
}
```

### 4. GraphQL Resolver (History Service)
```java
@Component
public class QueryResolver implements GraphQLQueryResolver {
    @Autowired private AppointmentRepository repo;
    
    public List<Appointment> patientAppointments(Long patientId) {
        return repo.findByPatientId(patientId);
    }
    
    public List<Appointment> futureAppointments(Long patientId) {
        return repo.findByPatientIdAndDateGreaterThan(patientId, LocalDateTime.now());
    }
}
```

## Configuration & Environment

### Application Properties
Each service needs:
- **Spring Security**: Configure user roles and JWT/Basic Auth
- **RabbitMQ**: `spring.rabbitmq.host`, `spring.rabbitmq.username`, `spring.rabbitmq.password`
- **Database**: `spring.datasource.url`, `spring.jpa.hibernate.ddl-auto=update`
- **GraphQL** (history-service): `spring.graphql.graphiql.enabled=true`

### Docker Compose
Must include RabbitMQ and PostgreSQL:
```yaml
services:
  rabbitmq:
    image: rabbitmq:3.12-management
    ports:
      - "5672:5672"
      - "15672:15672"
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: hospital_db
```

## Testing Strategy

### Unit Tests
- Use Mockito para mockar repositories, services e RabbitTemplate
- Test cada serviço isoladamente

### Integration Tests
- Use TestContainers para RabbitMQ e PostgreSQL
- Test fluxo completo: criar consulta → publicar evento → processar na notification service

### Postman Collection
Deve incluir:
- POST `/api/appointments` - criar consulta (doctor/nurse)
- GET `/api/appointments/{id}` - consultar detalhes
- GET `/api/patients/{patientId}/appointments` - histórico do paciente
- PUT `/api/appointments/{id}` - editar consulta
- DELETE `/api/appointments/{id}` - cancelar consulta
- GraphQL query endpoint para histórico

## Key Files to Reference

| Arquivo | Responsabilidade |
|---------|------------------|
| `Appointment.java` | Entity principal com validações de datas |
| `AppointmentService.java` | Lógica de agendamento e publicação RabbitMQ |
| `AppointmentController.java` | Endpoints REST com `@PreAuthorize` |
| `ConsultationScheduledListener.java` | Consumer RabbitMQ para notificações |
| `QueryResolver.java` | GraphQL resolver para histórico |
| `SecurityConfig.java` | Configuração Spring Security com roles |

## Build & Run Commands

```bash
# Build individual services
mvn clean package -pl scheduling-service

# Run with Docker Compose
docker-compose up -d

# Run specific service locally
cd scheduling-service && mvn spring-boot:run

# Run tests
mvn test -pl scheduling-service

# Run integration tests with TestContainers
mvn verify -pl notification-service
```

## Common Pitfalls & Best Practices

1. **Sempre valide datas de consulta** - não permitir datas no passado
2. **Use transações** - @Transactional ao criar/editar para garantir consistência
3. **Trate exceções RabbitMQ** - implemente retry policies e dead-letter queues
4. **Segurança**: Nunca exponha senhas em logs; use Spring Security properly
5. **GraphQL queries devem ser paginadas** - para históricos grandes
6. **Crie índices no banco** - em patientId, doctorId, appointmentDate

## External Dependencies & Integration Points

- **RabbitMQ**: Message broker para async communication
- **PostgreSQL**: Persistent data storage
- **Spring Security**: Authentication/Authorization
- **GraphQL**: Query language para History Service
- **JPA/Hibernate**: ORM para database operations

---
*Última atualização: Janeiro 2026 | Versão: 1.0*
