# ms-pedidos360-report

Microservicio de **reportería y KPI de Pedidos360**. Consume eventos de Apache Kafka publicados por el microservicio Orders, mantiene una proyección de los pedidos en PostgreSQL y expone indicadores mediante una API REST.

> **Estado de esta entrega:** integración Kafka probada en entorno local. El despliegue en AWS, la autenticación JWT por roles y la integración visual con Angular/Grafana deben verificarse por separado; no se consideran implementados por el solo hecho de ejecutar este microservicio.

## Tecnologías

- Java 21 y Spring Boot 4.1.1.
- Spring Web MVC y Spring Data JPA.
- Spring for Apache Kafka (consumidores `@KafkaListener`).
- PostgreSQL 16 (instancia utilizada en el entorno Docker local).
- Docker y Docker Compose.
- Maven Wrapper (`mvnw.cmd`).
- JUnit 5 y Mockito.

## Arquitectura e integración

```text
Orders (Spring Boot)
  |-- Transactional Outbox -- Kafka producer
  |       |-- pedidos360.pedidos.estado
  |       `-- pedidos360.pedidos.ventas
  v
Kafka (dos brokers: kafka-1 y kafka-2)
  |
  `-- Reportes: consumidores @KafkaListener
         |-- PedidoReportConsumer (deserialización)
         |-- ReporteEventoService (validaciones y reglas de negocio)
         `-- ReporteRepository (JPA)
                |
                v
         PostgreSQL: pedidos360_report
                |
                v
         ReportController -> API REST de indicadores
```

La responsabilidad de **Orders** es generar y publicar los eventos. **Reportes** recibe los eventos y calcula los indicadores a partir de sus propios datos; no consulta directamente la base de datos de Orders.

### Tópicos Kafka

| Tópico                      | Productor | Consumidor | Uso                                                   |
| --------------------------- | --------- | ---------- | ----------------------------------------------------- |
| `pedidos360.pedidos.estado` | Orders    | Reportes   | Creación y cambios de estado de pedidos.              |
| `pedidos360.pedidos.ventas` | Orders    | Reportes   | Registro de ventas al alcanzar el estado `ENTREGADO`. |

- **Consumer group:** `pedidos360-reportes`.
- **Bootstrap servers locales:** `localhost:9092,localhost:9093`.
- **Bootstrap servers dentro de Docker:** `kafka-1:29092,kafka-2:29092`.
- Los eventos se deserializan a contratos tipados (`PedidoEstadoEvento` y `PedidoVentaEvento`).
- El consumo utiliza confirmación de offsets por registro (`ack-mode=record`) y no habilita `auto-commit`.

### Integridad de los datos

`ReporteEventoService` aplica las siguientes reglas:

- Valida identificadores, versión de esquema, datos obligatorios, fechas y montos.
- Evita reprocesar el último evento de estado mediante `eventoId`.
- Evita duplicar ventas mediante `eventoVentaId` y la identificación del pedido.
- No permite que un evento de estado con versión anterior revierta el estado más reciente; puede recuperar campos faltantes cuando corresponde.
- Registra las ventas al recibir eventos del tópico de ventas.
- La entidad `Reporte` incorpora restricciones de unicidad para `pedidoId` y `eventoVentaId`.

**Alcance de manejo de errores:** se validan los eventos y se lanzan errores ante cargas inválidas. No se documenta una Dead Letter Topic ni un mecanismo propio de recuperación del consumidor como ya implementados; para producción se debe revisar su política de reintentos, registro y tratamiento de mensajes defectuosos.

## Estructura principal

```text
src/main/java/com/pedidos360_report/
  consumer/PedidoReportConsumer.java
  controller/ReportController.java
  dto/KpisDTO.java
  entity/Reporte.java
  event/PedidoEstadoEvento.java
  event/PedidoVentaEvento.java
  repository/ReporteRepository.java
  service/ReportService.java
  service/ReporteEventoService.java

src/test/java/com/pedidos360_report/
  MsPedidos360ReportApplicationTests.java
  service/ReporteEventoServiceTest.java
```

## API REST

| Método | Ruta                          | Función                                                                            |
| ------ | ----------------------------- | ---------------------------------------------------------------------------------- |
| GET    | `/api/report/kpis`            | Totales de pedidos, entregados, en proceso, ventas y lead time promedio.           |
| GET    | `/api/report/estados-activos` | Agrupación de pedidos por estado.                                                  |
| GET    | `/api/report/lead-time`       | Tiempo promedio de entrega.                                                        |
| GET    | `/api/report/ventas-por-hora` | Ventas agrupadas por hora.                                                         |
| POST   | `/api/report`                 | Alta manual heredada para pruebas; no es el mecanismo de ingreso de eventos Kafka. |

> **Seguridad:** la API de esta rama no acredita protección JWT ni autorización por roles Admin/Cliente. El endpoint POST manual debe restringirse o retirarse antes de un despliegue productivo.

Ejemplo local de consulta:

```powershell
Invoke-RestMethod http://127.0.0.1:5000/api/report/kpis |
    ConvertTo-Json -Depth 5
```

La ruta `/api/report/kpis` devuelve indicadores, **no es un endpoint de salud**. Esta versión no expone `/actuator/health` en su configuración actual.

## Requisitos para ejecución local

1. Java 21, Docker Desktop y PowerShell.
2. Los brokers Kafka de la infraestructura de Pedidos360 levantados y accesibles por `9092` y `9093`.
3. Base de datos de Reportes disponible en `localhost:5433`.
4. Para ejecutar Reportes dentro de Docker, la red externa `pedidos360-kafka` debe existir y los brokers deben ser accesibles por sus nombres internos.

### Ejecutar Reportes desde PowerShell

Ubicarse en la raíz del repositorio y levantar PostgreSQL con el servicio definido en `docker-compose.yml`:

```powershell
docker compose up -d postgres-report
```

Para evitar heredar el puerto `8081` de una sesión de Orders:

```powershell
Remove-Item Env:SERVER_PORT -ErrorAction SilentlyContinue
```

Iniciar Reportes:

```powershell
.\mvnw.cmd spring-boot:run
```

Por defecto, escucha en `http://localhost:5000`. Cuando se ejecuta desde Windows usa Kafka en `localhost:9092,localhost:9093` y PostgreSQL en `localhost:5433`.

### Ejecutar Reportes con Docker Compose

**Primero** levantar la infraestructura Kafka y la red `pedidos360-kafka` desde su propio proyecto. **Después**, en este repositorio:

```powershell
docker compose config --quiet
docker compose up -d --build
```

El servicio `ms-report` usa las direcciones internas `kafka-1:29092,kafka-2:29092` y se conecta a PostgreSQL mediante la red de Docker Compose.

No ejecutar un segundo Reportes sobre el mismo puerto `5000` si ya existe una instancia local activa.

## Variables de entorno

| Variable                     | Propósito               | Valor de desarrollo por defecto                      |
| ---------------------------- | ----------------------- | ---------------------------------------------------- |
| `SERVER_PORT`                | Puerto HTTP de Reportes | `5000`                                               |
| `SPRING_DATASOURCE_URL`      | URL JDBC de PostgreSQL  | `jdbc:postgresql://localhost:5433/pedidos360_report` |
| `SPRING_DATASOURCE_USERNAME` | Usuario PostgreSQL      | `report_user`                                        |
| `SPRING_DATASOURCE_PASSWORD` | Clave PostgreSQL        | Configurada para el entorno local                    |
| `KAFKA_BOOTSTRAP_SERVERS`    | Direcciones de brokers  | `localhost:9092,localhost:9093`                      |

En Docker, `docker-compose.yml` define las direcciones internas. En AWS, utilizar configuración específica y gestión segura de credenciales; **no** reutilizar credenciales de desarrollo ni subir archivos `.env`, tokens o contraseñas reales.

## Pruebas y evidencias

Ejecutar:

```powershell
.\mvnw.cmd test
```

### Última ejecución registrada: 22-11-2026

| Indicador          | Resultado       |
| ------------------ | --------------- |
| Pruebas ejecutadas | 8               |
| Fallos             | 0               |
| Errores            | 0               |
| Omitidas           | 0               |
| Resultado Maven    | `BUILD SUCCESS` |

Las pruebas incluyen el arranque del contexto de Spring y siete pruebas de `ReporteEventoService` para procesamiento, duplicados, versiones antiguas y validaciones. La prueba de contexto utiliza las dependencias locales configuradas, por lo que se debe disponer de PostgreSQL y Kafka para reproducir la misma ejecución.

### Evidencia de integración real

En la prueba local con **pedido 31**:

- Orders creó el pedido con estado `CREADO` y monto `$2.490`.
- La Outbox de Orders marcó el evento de estado como `PUBLICADO`.
- Reportes persistió el pedido en PostgreSQL (`versionPedido=0`) con identificador del evento de estado.
- El KPI `totalPedidos` cambió de **4 a 5**, y `pedidosEnProceso` de **2 a 3**.
- El consumer group `pedidos360-reportes` mostró `LAG=0` en las particiones con mensajes pendientes.

Esta ejecución demuestra el flujo de **estado** de extremo a extremo. El recorrido de ventas del pedido 31 hasta `ENTREGADO` no se ha vuelto a ejecutar en esta sesión; el tópico de ventas sí cuenta con registros de consumo anteriores y pruebas unitarias de su lógica.

## Pendientes de integración del proyecto completo

- Integración definitiva con IDaaS/JWT, autorización por roles Admin/Cliente y protección de API.
- Despliegue y verificación en AWS (EC2 y RDS), si corresponde al entorno de la entrega.
- Integración con Angular y paneles Grafana/Prometheus.
- Estrategia de errores/reintentos para eventos inválidos en el consumidor Kafka.

Estos puntos pertenecen al cierre general de la Experiencia 3 y **no deben presentarse como implementados sin evidencias adicionales**.
