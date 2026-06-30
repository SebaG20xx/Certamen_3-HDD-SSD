# Arquitectura

## Estructura del proyecto

```
src/main/java/cl/usm/sansaweigh/
├── config/
│   └── AppConfig.java              # Bean RestTemplate
├── controllers/
│   └── RegistroPesajeController.java
├── entities/
│   ├── EstadoPesaje.java           # Enum de estados
│   ├── PesoCategoria.java          # Enum de categorías
│   └── RegistroPesaje.java         # Documento MongoDB
├── exceptions/
│   └── IllegalWeighingStateException.java
├── integration/
│   ├── EspecificacionBalanza.java  # Modelo de la API externa
│   └── ExternalScaleClient.java    # Cliente con reintentos y caché
├── repositories/
│   ├── RegistroPesajeRepository.java
│   └── RegistroPesajeRepositoryImpl.java
└── services/
    ├── RegistroPesajeService.java
    └── RegistroPesajeServiceImpl.java
```

## Capas

| Capa | Responsabilidad |
|------|-----------------|
| Controller | Recibe solicitudes HTTP y devuelve respuestas |
| Service | Lógica de negocio: clasificación, restricciones, estados |
| Repository | Acceso a MongoDB via `MongoTemplate` |
| Integration | Consulta API externa de balanzas con fallback Redis |

## Persistencia e integración

- **MongoDB**: almacena documentos `RegistroPesaje` en la colección `registros_pesaje`.
- **Redis**: cachea especificaciones de balanza con TTL de 120 segundos. Si la API externa falla, se sirve desde caché o desde el registro por defecto (id `-1`).

## Reglas de negocio destacadas

- Paquetes **Pesados** no se procesan entre las 20:00 y las 06:00 horas.
- Balanzas con ID **primo** no pueden registrar Pesados en días impares del mes.
- Las transiciones de estado inválidas lanzan `IllegalWeighingStateException` → HTTP 400.
