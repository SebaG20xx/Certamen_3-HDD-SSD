# Configuración del entorno

## Requisitos

- Java 17 o superior
- MongoDB corriendo en `localhost:27017`
- Redis corriendo en `localhost:6379`
- Gradle (incluido via wrapper `./gradlew`)

## Variables de configuración

El archivo `src/main/resources/application.properties` contiene:

```properties
spring.application.name=sansaweigh
server.port=8095

spring.data.mongodb.uri=mongodb://localhost:27017/sansaweigh

spring.data.redis.host=localhost
spring.data.redis.port=6379

scale.api.url=http://localhost:9090/scales/
```

## Levantar la aplicación

```bash
./gradlew bootRun
```

La API quedará disponible en `http://localhost:8095`.

## Ejecutar los tests

```bash
./gradlew test
```

Los reportes se generan en `build/reports/tests/test/index.html`.

## Swagger UI

Con la aplicación corriendo, accede a:

```
http://localhost:8095/swagger-ui.html
```

## Base de datos Redis — registro por defecto

Para que el fallback funcione, carga el registro por defecto en Redis:

```bash
redis-cli SET "scale:-1" '{"id":"-1","name":"Balanza Por Defecto","brand":"SansaScale-Default","maxCapacity":100.0,"precision":0.1,"lastCalibrationOffset":0.0}'
```
