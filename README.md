# SansaWeigh

> Microservicio de gestión de estaciones de pesaje de paquetes — Universidad Técnica Federico Santa María

## Documentación

La documentación completa del proyecto está disponible en Docsify (carpeta `docs/`).

Para verla localmente, abre `docs/index.html` con cualquier servidor estático, por ejemplo:

```bash
npx serve docs
```

## Inicio rápido

1. Clonar el repositorio:

```bash
git clone https://github.com/sebag20xx/sansaweigh.git
```

2. Abrir el proyecto en **IntelliJ IDEA**: `File → Open` y seleccionar la carpeta `sansaweigh`.

3. Asegurarse de tener **MongoDB** y **Redis** corriendo localmente.

4. Ejecutar la aplicación desde IntelliJ: abrir `SansaweighApplication.java` y presionar el botón ▶ verde.

La API queda disponible en `http://localhost:8095`.

Swagger UI: `http://localhost:8095/swagger-ui.html`

## Ejecutar tests

Desde IntelliJ: click derecho sobre la carpeta `test` → **Run 'All Tests'**.

O desde la terminal:

```bash
./gradlew test
```

## Tecnologías

- Java 25 / Spring Boot 4.1.0
- MongoDB · Redis · JUnit 5 · Mockito
