# SansaWeigh

Microservicio de gestión de estaciones de pesaje de paquetes.

## Documentación

La documentación completa está disponible en [GitHub Pages](https://TU-USUARIO.github.io/taller3hdd-sansaweigh).

También podés verla localmente:

```bash
npm install -g docsify-cli
docsify serve docs/
```

## Tecnologías

- Java 17 + Spring Boot 4.x
- MongoDB + Redis
- JUnit 5 + Mockito + AssertJ
- Docsify + Swagger

## Ejecutar

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080/api/registros`.
