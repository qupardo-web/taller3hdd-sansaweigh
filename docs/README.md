# SansaWeigh

## Descripción

Microservicio para gestionar estaciones de pesaje de paquetes. Clasifica paquetes por peso en **Sansas**, calcula tarifas y persiste el historial en MongoDB.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Backend | Spring Boot 4.x |
| Persistencia | MongoDB |
| Caché | Redis |
| Documentación | Docsify + Swagger |
| Tests | JUnit 5 + Mockito + AssertJ |

## Unidad de Medida

**1 Sansa (S) = 1.337 kg**

## Clasificación

| Categoría | Rango (Sansas) |
|---|---|
| Liviano | ≤ 10 S |
| Mediano | > 10 S y ≤ 50 S |
| Pesado | > 50 S |

## Estados del Pesaje

```
INGRESADO → PESADO → APROBADO / RECHAZADO → DESPACHADO
```

## Configuración del Entorno

### Requisitos
- Java 17+
- MongoDB
- Redis
- Mockoon (para simular API externa)

### Configuración

```properties
# application.properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=sansaweigh

spring.data.redis.host=localhost
spring.data.redis.port=6379

app.external-scale-api.url=http://localhost:3000
```
