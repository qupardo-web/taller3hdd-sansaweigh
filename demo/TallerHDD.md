## **Taller** 

## **Herramientas de Desarrollo** 

## **Instrucciones:** 

- **Integrantes** : Máximo 3 estudiantes por grupo. 

- **Entrega:** Repositorio GitHub público. La fecha límite de commits está regida por la plataforma académica. 

- **Documentación Obligatoria:** Reemplazar el archivo README.md principal por la guía de inicio de Docsify. Todo el detalle del proyecto debe estar documentado interactivamente usando Docsify expuesto en el mismo repositorio (carpeta docs/). 

- **Especificación de API:** Incluir la especificación completa de la API en formato OpenAPI 3.0/3.1 (archivo docs/openapi.yaml) y levantar la interfaz de Swagger UI en el proyecto. 

- **Cobertura Mínima:** La suite de pruebas unitarias y de integración (JUnit 5 & Mockito) debe certificar al menos un 90% de cobertura de líneas (Line Coverage) acumulado en todas las capas. 

## 1. Caso: Sistema “SansaWeigh” 

La empresa de logística “SansaWeigh” necesita un microservicio para gestionar estaciones de pesaje de paquetes. El sistema debe clasificar paquetes por peso, calcular tarifas de procesamiento con reglas dinámicas, persistir el historial en MongoDB, cachear configuraciones de balanzas en Redis e integrarse con un registro externo de especificaciones de balanzas. 

## 2. Arquitectura del Sistema 

El proyecto debe ser desarrollado sobre Spring Boot 4.x y estructurarse en la arquitectura vista en clases. 

## Tecnologías Requeridas 

- Java 17 o superior. 

- Spring Boot 4.x (Spring Web, Spring Data MongoDB, Spring Data Redis). 

- Lombok (para código boilerplate). 

- JUnit 5, Mockito & AssertJ (para la suite de pruebas). 

- Docsify y Swagger (alojado en `docs/` ). 

## 3. Especificaciones y Reglas de Negocio 

Se definen los siguientes requerimientos específicos. Considerar que la api debe proveer mecanismos de: 

- Creación de registros 

- Actualización de registros 

- Obtención de registros (filtrando por fecha) 

## A. Unidad de Medida Propietaria 

El sistema no trabaja directamente en kilogramos. Debe implementarse la conversión obligatoria: * Unidad **`Sansa`** ( **`S`** ): 1 Sansa equivale exactamente a 1. 337 𝑘𝑔 . 

## B. Reglas de Clasificación y Restricciones de Balanza 

Los paquetes se clasifican según su peso en `Sansas` : 

1. Liviano: Hasta 10 Sansas. 

2. Mediano: Más de 10 y hasta 50 Sansas. 

3. Pesado: Más de 50 Sansas. 

## Restricciones de Procesamiento: 

- Restricción Horaria para Paquetes Pesados: No se permite pesar ni procesar paquetes calificados como Pesados en horario nocturno (entre las 20:00 y las 06:00 horas del servidor). 

- Regla de Balanza Prima: Cada balanza tiene un identificador numérico. Si el ID de la balanza es un número primo, dicha balanza no puede registrar paquetes Pesados durante días calendario impares (ej: 1, 3, 5… del mes). Si se intenta, el sistema debe arrojar una excepción de negocio. 

## C. Ciclo de Vida y Transición de Estados de Pesaje 

Cada registro de pesaje posee una máquina de estados con las siguientes transiciones: `INGRESADO` ➔ `PESADO` ➔ `APROBADO` o `RECHAZADO` ➔ `DESPACHADO` 

Reglas de transición: Un paquete solo puede pasar a `APROBADO` o `RECHAZADO` después de haber estado en estado `PESADO` . Cualquier intento de transición no permitida debe lanzar una excepción personalizada `IllegalWeighingStateException` y responder un codigo de error 400 

## D. Integración con API de Balanza y Mock JSON 

El servicio debe consultar las especificaciones técnicas de la balanza a una API externa utilizando `ExternalScaleClient` . 

## Estructura JSON Base de la API Externa: 

```
{
```

```
"id": "101",
```

```
"name": "Balanza Central Sur",
```

```
"brand": "SansaScale-Pro",
```

```
"maxCapacity": 150.0,
```

```
"precision": 0.01,
```

```
"lastCalibrationOffset": -0.05
```

```
}
```

Regla de Fallback JSON: Si la API externa no está disponible, arroja un error HTTP o es inaccesible, el cliente de integración debe capturar la excepción y, de forma resiliente entregar la versión en caché de la respuesta, si la respuesta no existe en caché, debe cagar una especificación por defecto desde Redis con el id “-1” 

## 4. Requerimientos de Código y Estructura 

## Capa de Persistencia (MongoDB) 

- Documento `RegistroPesaje` : ID autogenerado, ID de balanza, ID de paquete, peso (en Sansas), categoría de peso, estado actual, e historial de marcas de tiempo de las transiciones de estado (created_at, updated_at). 

## Capa de Caché (Redis) 

- Las especificaciones de la balanza recuperadas desde la API externa deben guardarse en Redis con un tiempo de expiración (TTL) de 120 segundos. Esto evita consultas redundantes al cliente de integración o lecturas de archivos recurrentes. 

## Capa de Integración 

- El cliente `ExternalScaleClient` debe implementar un método `getScaleSpecifications(String scaleId)` . 

- Debe implementar reintentos exponenciales en caso de errores transitorios de red (máximo 3 reintentos) antes de aplicar el uso de cache. 

## 5. Entregables y Documentación 

1. Código Fuente: Repositorio GitHub público. 

2. Docsify: Carpeta `docs/` con manual de usuario, configuración del entorno y arquitectura. Reemplaza al README principal. 

3. Swagger: Interfaz Swagger UI disponible dentro de docsify. 

## 6. Rúbrica de Evaluación 

|Criterio|Puntos|Obtenidos|
|---|---|---|
|Diseño y Buenas Prácticas<br>Git|10 pts||
|Lógica de Negocio e<br>Implementación|25 pts||
|Persistencia e Integración<br>(Mongo)|20 pts||
|Integración con API Externa y<br>Fallback Redis-JSON|15 pts||
|Suite de Pruebas y Cobertura<br>(90%+)|20 pts||
|Docsify ySwagger Docs|10pts||



