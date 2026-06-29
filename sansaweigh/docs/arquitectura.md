# Arquitectura

## Diagrama de Capas

```
┌─────────────────────────────────┐
│         Controller REST          │
│   (RegistroPesajeController)     │
├─────────────────────────────────┤
│           Service                │
│   (RegistroPesajeService)        │
├──────────────┬──────────────────┤
│  Repository   │  Integration     │
│  (MongoDB)    │  (External API)  │
├──────────────┼──────────────────┤
│   MongoDB     │  Redis           │
│   (datos)     │  (caché)         │
└──────────────┴──────────────────┘
```

## Flujo de Petición

1. Cliente envía POST/PUT/GET al Controller
2. Controller delega al Service
3. Service valida reglas de negocio (horaria, balanza prima, transiciones)
4. Service consulta `ExternalScaleClient` → API externa (Mockoon) con reintentos y fallback Redis
5. Service persiste en MongoDB via Repository
6. Controller retorna respuesta HTTP

## Reglas de Negocio

### Restricción Horaria
Paquetes pesados no se procesan entre **20:00 y 06:00**.

### Regla de Balanza Prima
Si el ID de balanza es un **número primo**, no puede registrar paquetes pesados en **días impares** del mes.

### Transiciones de Estado
Solo se permiten las transiciones definidas en la máquina de estados. Transiciones inválidas lanzan `IllegalWeighingStateException` (400).
