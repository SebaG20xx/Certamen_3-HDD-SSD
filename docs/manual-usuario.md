# Manual de usuario

## Crear un registro de pesaje

**POST** `/registros`

```json
{
  "idBalanza": "4",
  "idPaquete": "PKG-001",
  "pesoEnSansas": 25.5
}
```

El sistema asigna automáticamente la categoría y el estado inicial `INGRESADO`.

**Respuestas posibles:**

| Código | Descripción |
|--------|-------------|
| 200 | Registro creado exitosamente |
| 400 | Violación de regla de negocio (horario nocturno o balanza prima en día impar) |
| 500 | Error interno |

---

## Avanzar el estado de un pesaje

**PATCH** `/registros/{id}/estado?nuevoEstado=PESADO`

Transiciones válidas:

```
INGRESADO → PESADO → APROBADO
                   → RECHAZADO → DESPACHADO
```

**Respuestas posibles:**

| Código | Descripción |
|--------|-------------|
| 200 | Estado actualizado |
| 400 | Transición no permitida |
| 404 | Registro no encontrado |

---

## Consultar registros

**GET** `/registros` — todos los registros

**GET** `/registros?desde=2026-06-01T00:00:00&hasta=2026-06-30T23:59:59` — filtrado por fecha

**GET** `/registros/{id}` — un registro específico

---

## Ejemplo de flujo completo

```bash
# 1. Crear registro
curl -X POST http://localhost:8095/registros \
  -H "Content-Type: application/json" \
  -d '{"idBalanza":"4","idPaquete":"PKG-001","pesoEnSansas":25.5}'

# 2. Pesar el paquete
curl -X PATCH "http://localhost:8095/registros/{id}/estado?nuevoEstado=PESADO"

# 3. Aprobar
curl -X PATCH "http://localhost:8095/registros/{id}/estado?nuevoEstado=APROBADO"

# 4. Despachar
curl -X PATCH "http://localhost:8095/registros/{id}/estado?nuevoEstado=DESPACHADO"
```
