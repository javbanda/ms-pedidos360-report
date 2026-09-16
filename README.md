# ms-pedidos360-report

Microservicio de **reportería y KPIs** del proyecto semestral **Pedidos360**, red de 20 PyMEs de cafetería/panadería. Expone endpoints de solo lectura para métricas operativas: estados activos de pedidos, tiempo de entrega promedio (lead time) y ventas agrupadas por hora.

## Stack

- Java 21
- Spring Boot 4.1.1 (Spring Web, Spring Data JPA)
- PostgreSQL 16
- Docker / Docker Compose

## Arquitectura

```
com.pedidos360_report
├── controller/    ReportController      → expone los endpoints REST
├── service/       ReportService          → lógica de negocio (agrupaciones, promedios)
├── repository/    ReporteRepository      → acceso a datos (JpaRepository)
├── dto/           EstadoActivoDTO, LeadTimeDTO, VentasPorHoraDTO
└── entity/        Reporte                → pedidoId, estado, fechaCreacion, fechaEntrega, montoTotal
```

Diseño en capas: el `Controller` delega toda la lógica al `Service`, que usa el `Repository` solo para consultar o guardar. Los `DTO` evitan exponer la entidad JPA directamente en las respuestas.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/report/kpis` | Chequeo de salud del servicio |
| GET | `/api/report/estados-activos` | Cantidad de pedidos agrupados por estado |
| GET | `/api/report/lead-time` | Promedio de minutos entre creación y entrega (pedidos `ENTREGADO`) |
| GET | `/api/report/ventas-por-hora` | Suma de `montoTotal` agrupada por hora de creación |
| POST | `/api/report` | Crea un reporte de prueba manualmente (simula un evento que en producción llegaría por Kafka) |

### Ejemplo de body para POST

```json
{
  "pedidoId": 1,
  "estado": "ENTREGADO",
  "fechaCreacion": "2026-09-15T08:30:00",
  "fechaEntrega": "2026-09-15T09:10:00",
  "montoTotal": 4500.0
}
```

## Cómo levantarlo

### Con Docker (recomendado)

```bash
docker compose up --build
```

Levanta el microservicio (puerto `5000`) y su base de datos Postgres (puerto `5433`) en la misma red interna de Docker.

### Local (sin Docker)

1. Levanta solo la base de datos: `docker compose up postgres-report -d`
2. Corre la app desde tu IDE o con `mvnw spring-boot:run`
3. La app queda disponible en `http://localhost:5000`

## Configuración

La conexión a la base de datos se define en `src/main/resources/application.properties` para desarrollo local, y se sobreescribe con variables de entorno (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) al correr con Docker Compose — así no hace falta tocar código para apuntar a un entorno distinto (por ejemplo, Amazon RDS en producción).

## Estado del proyecto

- [x] Arquitectura del microservicio (controller/service/repository/dto)
- [x] Base de datos PostgreSQL
- [x] Tres KPIs implementados y probados (estados activos, lead time, ventas por hora)
- [x] Dockerizado (build multi-etapa + docker-compose)
- [ ] Despliegue en AWS Elastic Beanstalk (pendiente: base de datos en la nube)
- [ ] Integración con API Gateway
- [ ] Consumo de eventos vía Kafka (etapa futura, fuera del alcance de esta entrega)

## Contexto del proyecto

Este microservicio es parte de la arquitectura de Pedidos360, que incluye además: `ms-pedidos360-orders`, `ms-pedidos360-catalog`, `ms-pedidos360-notify`, `ms-pedidos360-audit`, y un frontend Angular. Ver el enunciado general del proyecto para más detalle sobre roles, autenticación (Azure AD / Amazon Cognito) y la arquitectura completa de microservicios.
