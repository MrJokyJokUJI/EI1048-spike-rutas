# Spike iteración 0 – Cálculo de rutas

Spike del proyecto conjunto EI1039–EI1048. Antes de empezar con las historias de usuario evaluamos dos alternativas tecnológicas con el mismo caso de uso:

**Caso de uso:** el usuario indica un origen, un destino y un método de movilidad (coche, bici o a pie), y la aplicación muestra la ruta en un mapa con su distancia y su duración, usando la API de openrouteservice (geocoding + directions).

| Carpeta | Alternativa | Tecnologías | Pruebas |
|---|---|---|---|
| [`android/`](android) | App Android nativa | Kotlin, Jetpack Compose, osmdroid (OpenStreetMap), OkHttp | JUnit 4 + MockWebServer |
| [`web/`](web) | Web adaptada a móvil | Java 21, Spring Boot 3.4, Leaflet | JUnit 5 + Mockito + MockRestServiceServer |

En las dos se prueban un escenario válido (ruta entre dos lugares existentes) y uno inválido (lugar inexistente) con la API simulada, para no depender del servicio real.

## API key

Las dos versiones necesitan una key gratuita de https://openrouteservice.org. La key **no se sube** al repositorio:

- Android: añadir `ors.api-key=TU_KEY` al fichero `android/local.properties`.
- Web: crear `web/secrets.properties` con `ors.api-key=TU_KEY`.

## Cómo ejecutarlo

**Android:** abrir la carpeta `android/` en Android Studio o IntelliJ IDEA, esperar a que sincronice Gradle y ejecutar la configuración `app` en un emulador o móvil. Tests: `gradlew testDebugUnitTest`.

**Web:** abrir `web/` en IntelliJ y ejecutar `SpikeApplication`, luego entrar en http://localhost:8080. Tests: `mvn test`.
