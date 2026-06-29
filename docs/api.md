# API REST

## Base URL

```
http://localhost:8080/api
```

## Endpoints

### Crear Registro
```
POST /api/registros
```

**Body:**
```json
{
  "idBalanza": "101",
  "idPaquete": "PKG-001",
  "pesoEnSansas": 8.5
}
```

**Response:** `201 Created`

---

### Pesar Registro
```
PUT /api/registros/{id}/pesar
```

Transiciona de `INGRESADO` → `PESADO`.

---

### Aprobar Registro
```
PUT /api/registros/{id}/aprobar
```

Transiciona de `PESADO` → `APROBADO`.

---

### Rechazar Registro
```
PUT /api/registros/{id}/rechazar
```

Transiciona de `PESADO` → `RECHAZADO`.

---

### Despachar Registro
```
PUT /api/registros/{id}/despachar
```

Transiciona de `APROBADO` → `DESPACHADO`.

---

### Obtener por ID
```
GET /api/registros/{id}
```

---

### Listar Registros
```
GET /api/registros
GET /api/registros?desde=2026-01-01&hasta=2026-06-30
```

---

### Actualizar Peso
```
PUT /api/registros/{id}
```

**Body:**
```json
{
  "nuevoPesoEnSansas": 45.0
}
```

Solo permitido en estado `INGRESADO`.

---

## Códigos de Error

| Código | Significado |
|---|---|
| 400 | Error de negocio (transición inválida, restricción horaria, balanza prima) |
| 404 | Registro no encontrado |
| 201 | Registro creado exitosamente |
| 200 | Operación exitosa |
