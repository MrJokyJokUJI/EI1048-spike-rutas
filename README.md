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

## Supabase (auth + persistencia)

El spike integra Supabase (PostgreSQL) para cubrir registro/login (HU01/HU02) y guardar/listar rutas (HU32/HU33, R6).

### Configuración

1. Crea el proyecto en https://supabase.com y copia la **URL** y la **anon key** (*Project Settings → API*).
2. Añádelas a `android/local.properties`:
   ```
   supabase.url=https://TUPROYECTO.supabase.co
   supabase.anon-key=TU_ANON_KEY
   ```
3. Ejecuta `supabase/schema.sql` en el **SQL Editor** de Supabase. Crea la tabla `ruta_guardada` y su política de Row Level Security (cada usuario solo ve/guarda sus filas).
4. Desactiva la confirmación de email para el spike: *Authentication → Sign In / Providers → Email → desmarca "Confirm email"*. Así `registrar` devuelve sesión directamente.

### Pruebas

Los tests de persistencia usan un `MockEngine` de Ktor (sin red): `RutaGuardadaTest` (mapeo de columnas) y `RutasRepositoryTest` (POST/GET contra la API simulada).

```
cd android
gradlew testDebugUnitTest
```
