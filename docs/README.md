# SansaWeigh

Microservicio de gestión de estaciones de pesaje de paquetes desarrollado con **Spring Boot 4.x**.

## ¿Qué hace?

SansaWeigh permite registrar y gestionar el pesaje de paquetes en estaciones de logística. El sistema clasifica automáticamente cada paquete según su peso en Sansas, aplica reglas de negocio y persiste el historial en MongoDB.

## Unidad de medida

El sistema trabaja en **Sansas (S)**: 1 Sansa = 1.337 kg.

| Categoría | Rango |
|-----------|-------|
| Liviano   | Hasta 10 S |
| Mediano   | Más de 10 S y hasta 50 S |
| Pesado    | Más de 50 S |

## Ciclo de vida de un pesaje

```
INGRESADO → PESADO → APROBADO o RECHAZADO → DESPACHADO
```

## Tecnologías

- Java 25 / Spring Boot 4.1.0
- MongoDB (persistencia)
- Redis (caché de especificaciones de balanzas)
- JUnit 5 + Mockito (suite de pruebas)
