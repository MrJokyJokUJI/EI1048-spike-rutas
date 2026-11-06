# Configuración para Agentes de IA

## Stack Tecnológico
- **UI & Lenguaje:** Kotlin, Jetpack Compose (Material 3), sin XML.
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`), Java 17, `compileSdk/targetSdk: 35`, `minSdk: 26`.
- **Red & JSON:** OkHttp, `org.json`, `kotlinx-serialization`.
- **Backend & Auth:** Supabase (`supabase-kt` + Ktor), PostgREST (`lugar_interes`, `vehiculo`, `ruta_guardada`, `ruta_realizada`), Auth email.
- **Mapas:** `osmdroid` (OpenStreetMap).
- **Asincronía:** Coroutines (`Dispatchers.IO`, `suspend`).
- **APIs:** OpenRouteService (geocodificación y rutas) y API externa de precios de carburante (HU16).
- **Testing:** JUnit 4, `MockWebServer` (OkHttp), Ktor `MockEngine`.

## Arquitectura
- **Patrón de vista:** MVVM con flujo unidireccional (`StateFlow<UiState>` + `Event`).
- **Patrones de diseño (≥ 5, obligatorio):** Repository, Caso de uso, Strategy, Observer, Factory, Singleton, Adapter y Mapper. Detalle y reglas de dependencia en [`ARQUITECTURA.md`](ARQUITECTURA.md).
- **Alcance funcional vigente:** HU01–HU30 en [`HISTORIAS_USUARIO.md`](HISTORIAS_USUARIO.md).

## Metodología y Flujo Git (ATDD)
- **Nomenclatura de ramas:**
  - Iteración: `it-XX` (ej. `it-00`)
  - Historia de Usuario: `h-XX` (creada desde `it-XX`)
- **Nomenclatura de Pull Requests:**
  - `h-XX` $\rightarrow$ `it-XX`: `ITxx-xx` (ej. `IT00-01`)
  - `it-XX` $\rightarrow$ `main`: `ITxx` (ej. `IT00`)
- **Reglas estricta de ATDD:**
  1. El **primer commit** en `h-XX` DEBE ser el **test de aceptación**.
  2. Los tests de aceptación se ejecutan **sin mocks** (cobertura completa E2E).
- **Commits:** Formato *Conventional Commits* -> `<type>(<scope>): <description>` (ej. `feat(auth): add login form`).

## Guía de Estilo & Restricciones
*(Extensible según convenga al equipo)*
