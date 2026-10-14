# Spike iteración 0 – Cálculo de rutas en Android

Spike del proyecto conjunto EI1039–EI1048. Sirve para comprobar que las tecnologías elegidas para el proyecto funcionan juntas antes de empezar con las historias de usuario.

**Caso de uso:** el usuario indica un origen, un destino y un método de movilidad (coche, bici o a pie), y la aplicación muestra la ruta en un mapa con su distancia y su duración.

## Tecnologías

- **Kotlin + Jetpack Compose**: aplicación Android nativa.
- **osmdroid**: mapa con OpenStreetMap, donde se dibuja la ruta.
- **openrouteservice**: geocoding del topónimo y cálculo de la ruta, con peticiones HTTP mediante OkHttp.
- **JUnit 4 + MockWebServer**: pruebas del caso de uso simulando la API.

## Cómo ejecutarlo

1. Consigue una API key gratuita en https://openrouteservice.org.
2. Añade esta línea al fichero `android/local.properties` (no se sube a GitHub):
   ```
   ors.api-key=TU_API_KEY
   ```
3. Abre la carpeta `android/` en Android Studio o IntelliJ IDEA, espera a que sincronice Gradle y ejecuta la configuración `app` en un emulador o un móvil.

## Pruebas

```
cd android
gradlew testDebugUnitTest
```

`OrsClientTest` cubre un escenario válido (ruta en coche entre Castellón y Valencia) y uno inválido (destino inexistente).
