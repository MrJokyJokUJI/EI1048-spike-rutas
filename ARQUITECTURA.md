## Arquitectura

**Patrón de vista elegido: MVVM + Repository + Casos de uso** ("Clean ligero"), con flujo de datos
**unidireccional** (estado hacia abajo, eventos hacia arriba). MVP, MVC y MVI están **descartados**:
no los reintroducir.

Alcance funcional vigente: **HU01–HU30** (ver [`HISTORIAS_USUARIO.md`](HISTORIAS_USUARIO.md)). La
numeración anterior HU01–HU57 está derogada; no citar HUs eliminadas en ramas, tests ni PRs.

```
ui (Screen + ViewModel) ──▶ domain/usecase ──▶ domain/repository (puertos) ◀── data/ (Supabase, ORS, DataStore)
        ▲                                                                          │
        └──────────────────── StateFlow<XxxUiState> / XxxEvent ◀───────────────────┘
```

### Patrones de diseño (se exigen **≥ 5**; aquí se documentan 9)

| # | Patrón | Dónde se materializa | Para qué |
|---|---|---|---|
| 1 | **MVVM** (arquitectura de vista) | `ui/feature/<x>/`: `XxxScreen` (View) + `XxxViewModel` + `XxxUiState`/`XxxEvent` | Todas las pantallas |
| 2 | **Repository** | Puerto en `domain/repository/` + implementación en `data/repository/` | Aísla dominio de Supabase/ORS/DataStore (HU01–HU28) |
| 3 | **Caso de uso / Interactor (Command)** | `domain/usecase/`, `suspend operator fun invoke(...)` | Toda operación de negocio; la ViewModel nunca salta al repositorio |
| 4 | **Strategy** | `domain/strategy/`: `CosteStrategy`, `CaloriasStrategy`, `TipoRutaStrategy` | Coste en coche (HU18), calorías (HU19), tipo de ruta (HU20, HU27) |
| 5 | **Observer** | `StateFlow` expuesto por cada ViewModel y `SesionViewModel`; `collectAsStateWithLifecycle` | Estado reactivo de pantalla y sesión (todas); restauración (HU29, HU30) |
| 6 | **Factory** | `XxxViewModelFactory` por feature; construcción de ViewModels desde `AppContainer` | Crear ViewModels con sus casos de uso, sin Hilt |
| 7 | **Singleton** | `AppContainer` (creado en `App.kt`) y `SupabaseClientProvider` | Una única instancia de DataSources/repositorios/clientes |
| 8 | **Adapter** | `data/remote/*DataSource` y `data/local/*DataSource` adaptan ORS, Supabase y DataStore a los puertos de dominio | Desacoplar APIs externas del dominio |
| 9 | **Mapper** | `data/mapper/`: extensiones `toDomain()` / `toDto()` | Convertir DTOs (`@SerialName` snake_case) a modelos de dominio y viceversa |

Regla: **no introducir patrones adicionales** (Hilt, EventBus, MVI, etc.) sin justificarlo en una HU y
anotarlo en este documento.

### Reglas de dependencia (inviolables)

1. **`domain/` es Kotlin puro.** Prohibido importar `android.*`, `androidx.*`, Supabase, OkHttp, Ktor o
   DataStore. Solo stdlib de Kotlin.
2. **Un `@Composable` solo** lee un `StateFlow` de su ViewModel y emite `XxxEvent`. Prohibido usar casos
   de uso, repositorios, DataSource, Supabase u OkHttp desde la UI.
3. **Una ViewModel solo llama a casos de uso** (`domain/usecase/`). Prohibido acceder a DataSource o
   construir repositorios desde ella. *Toda* operación que ejecute la ViewModel pasa por un caso de uso,
   aunque solo delegue.
4. **Los casos de uso solo hablan con interfaces** de `domain/repository/`. Prohibido importar `data.*`.
5. **`data/` implementa los puertos** de `domain/repository/`: es el **único** código que conoce Supabase,
   OkHttp, DataStore u osmdroid.
6. **DI manual, sin Hilt/Dagger/Koin.** Un único `AppContainer` (singleton) creado en `App.kt`; cada
   ViewModel se construye con un `ViewModelProvider.Factory` que recibe sus casos de uso.
7. **Un solo módulo Gradle:** `:app`. No crear módulos nuevos.
8. **Concurrencia:** toda I/O es `suspend`; corutinas de UI en `viewModelScope`; los DataSource exponen
   `suspend` y usan `Dispatchers.IO` internamente. Prohibido `GlobalScope`, `Thread(...)` o bloquear el
   hilo principal.
9. **Fuente única de verdad:** la ViewModel nunca mezcla "remoto + local" a mano; cada repositorio decide
   red/caché y expone un único flujo. Los favoritos y las entidades viven en Supabase; solo las
   preferencias de configuración viven en DataStore.

### Responsabilidades por tipo de clase

| Tipo | Ubicación | Regla |
|---|---|---|
| `XxxScreen` (Composable) | `ui/feature/<x>/` | Dibuja `XxxUiState` y emite `XxxEvent`. Stateless: recibe estado + callback. Sin lógica de negocio |
| `XxxViewModel` | `ui/feature/<x>/` | Expone `val state: StateFlow<XxxUiState>` y `fun onEvent(e: XxxEvent)`. Una por pantalla. Sobrevive a rotación |
| `XxxUiState` | `ui/feature/<x>/` | `data class` inmutable con **todo** el estado de la pantalla (incluidos cargando/mensaje de error) |
| `XxxEvent` | `ui/feature/<x>/` | `sealed interface` con todas las acciones del usuario de esa pantalla |
| `XxxUseCase` | `domain/usecase/` | `class XxxUseCase(...)` con `suspend operator fun invoke(...): R`. Orquesta repositorios/estrategias. Sin estado |
| `XxxRepository` (interfaz) | `domain/repository/` | Puerto: métodos `suspend` o `Flow`. Sin tipos de Supabase en las firmas |
| `XxxRepositoryImpl` | `data/repository/` | Implementa el puerto; compone DataSource remoto y local |
| `XxxDataSource` | `data/remote/` o `data/local/` | Un fichero por fuente concreta (ORS, tabla Supabase, DataStore) |
| `XxxDto` | `data/*/dto/` | Modelos `@Serializable` de API/BD (`@SerialName` snake_case). Se mapean con `toDomain()` |
| `XxxStrategy` | `domain/strategy/` | Interfaz + variantes seleccionables en runtime (coste, calorías, tipo de ruta) |

Los modelos de `domain/model/` son `data class` **sin anotaciones de serialización**: la serialización
vive solo en `data/`.

### Puertos de dominio y adaptadores

| Puerto (`domain/repository/`) | Implementación (`data/repository/`) | Fuente (`data/`) | HU |
|---|---|---|---|
| `AuthRepository` | `AuthRepositoryImpl` | `AuthRemoteDataSource` (Supabase Auth) | HU01–HU05 |
| `LugaresRepository` | `LugaresRepositoryImpl` | `LugaresRemoteDataSource` | HU06–HU10 |
| `GeocodingRepository` | `GeocodingRepositoryImpl` | `OrsDataSource` (geocoding) | HU07 |
| `VehiculosRepository` | `VehiculosRepositoryImpl` | `VehiculosRemoteDataSource` | HU11–HU16 |
| `PrecioCarburanteRepository` | `PrecioCarburanteRepositoryImpl` | `PrecioCarburanteDataSource` (API externa, OkHttp) | HU16, HU18 |
| `CalculoRutaRepository` | `CalculoRutaRepositoryImpl` | `OrsDataSource` (directions) | HU17, HU20 |
| `RutasRepository` | `RutasRepositoryImpl` | `RutasRemoteDataSource` (`ruta_guardada`) | HU21–HU23 |
| `HistorialRepository` | `HistorialRepositoryImpl` | `HistorialRemoteDataSource` (`ruta_realizada`) | HU24 |
| `FavoritosRepository` | `FavoritosRepositoryImpl` | `FavoritosRemoteDataSource` (columnas `favorito` de las tres tablas) | HU25 |
| `PreferenciasRepository` | `PreferenciasRepositoryImpl` | `PreferenciasLocalDataSource` (DataStore) | HU26–HU28 |

### Operaciones derivadas necesarias

La lista vigente no numera algunas acciones imprescindibles; se implementan como casos de uso y **no**
como HUs nuevas:

- **`RegistrarRutaRealizadaUseCase`**: marca una ruta calculada como realizada. Es la entrada de datos
  de HU24 (historial) y, en la misma transacción lógica, dispara:
  - **HU10**: incrementa el contador `lugar_interes.visitas` de cada lugar por el que pasa la ruta.
  - **HU15**: acumula la distancia en `vehiculo.km_recorridos` del vehículo usado.
  - **HU16**: consulta el precio vigente en la API externa de carburantes, guarda una instantánea en
    `ruta_realizada.precio_carburante`, actualiza `vehiculo.precio_carburante` con el último precio
    conocido y calcula el coste de esa ruta con ese precio (HU18), guardándolo en `ruta_realizada.coste`.

  La UI de `ruta` ofrece la acción "Realizar ruta" tras calcularla.
- **`DetectarPasoPorLugaresUseCase`**: al registrar la ruta realizada, compara su geometría con los
  lugares de interés del usuario y devuelve los que están a menos de un umbral (50 m por defecto). El
  incremento de `visitas` se hace en Supabase con una operación atómica (`UPDATE ... SET visitas =
  visitas + 1` vía RPC), nunca con leer-modificar-escribir en cliente.
- **`ActualizarPrecioCarburanteUseCase`**: obtiene el precio de la API externa (HU16) para el tipo de
  energía del vehículo y lo persiste. Si la API falla, se conserva el último precio guardado y se
  informa en la UI; no se bloquea el registro de la ruta.
- **HU20** "más económica" se resuelve evaluando las rutas candidatas con `CosteStrategy` y eligiendo el
  coste mínimo; no es una métrica nativa de ORS.

### Contrato MVVM (patrón a copiar en cada pantalla)

```kotlin
// ui/feature/ruta/
data class RutaUiState(
    val origen: String = "", val destino: String = "",
    val modo: Modo = Modo.COCHE, val tipoRuta: TipoRuta = TipoRuta.RAPIDA,
    val ruta: Ruta? = null, val mensaje: String = "",
)
sealed interface RutaEvent {
    data class OrigenCambiado(val v: String) : RutaEvent
    object Calcular : RutaEvent
    object Realizar : RutaEvent
}
class RutaViewModel(
    private val calcularRuta: CalcularRutaUseCase,
    private val registrarRutaRealizada: RegistrarRutaRealizadaUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(RutaUiState())
    val state: StateFlow<RutaUiState> = _state.asStateFlow()
    fun onEvent(e: RutaEvent) { viewModelScope.launch { when (e) { /* ... */ } } }
}
@Composable fun RutaScreen(state: RutaUiState, onEvent: (RutaEvent) -> Unit) { /* ... */ }
```

### Proyecto y paquetes

- Proyecto Android en `android/` (módulo `:app`, `rootProject.name = "RutasApp"`).
- Paquete base: **`es.uji.ei1048.rutas`** (el spike usa `es.uji.ei1048.spike`; al portar código de la rama
  `spike`, renombrar el paquete).
- Vocabulario de negocio (modelos, pantallas, casos de uso) **en español**, coherente con las HU.

### Mapa de directorios y ficheros

```
android/
├── build.gradle.kts                  # claves de local.properties → BuildConfig
├── settings.gradle.kts
├── local.properties                  # ors.api-key, supabase.url, supabase.anon-key,
│                                     # combustible.url, combustible.api-key (NUNCA en git)
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml   # solo permiso INTERNET (no hay seguimiento en vivo)
        │   └── java/es/uji/ei1048/rutas/
        │       ├── App.kt                        # Application: construye y publica el AppContainer
        │       ├── di/
        │       │   └── AppContainer.kt           # SINGLETON de DI: DataSource, repositorios, casos de uso
        │       ├── domain/                        # ★ Kotlin puro, cero imports de Android/red/BD
        │       │   ├── model/                     # LugarInteres, Vehiculo, Ruta, RutaGuardada,
        │       │   │                              # RutaRealizada, PrecioCarburante, Preferencias,
        │       │   │                              # Tema, Punto, Modo, TipoRuta
        │       │   ├── repository/                # interfaces (puertos), una por agregado
        │       │   ├── strategy/
        │       │   │   ├── CosteStrategy.kt       # HU18: CombustibleStrategy / ElectricoStrategy
        │       │   │   ├── CaloriasStrategy.kt    # HU19: a pie / en bicicleta
        │       │   │   └── TipoRutaStrategy.kt    # HU20, HU27: rápida / corta / económica
        │       │   └── usecase/                   # un fichero por operación de negocio
        │       ├── data/
        │       │   ├── remote/
        │       │   │   ├── ors/
        │       │   │   │   ├── OrsDataSource.kt   # geocoding (HU07) + directions (HU17)
        │       │   │   │   └── dto/               # respuestas @Serializable de ORS
        │       │   │   ├── supabase/
        │       │   │   │   ├── SupabaseClientProvider.kt
        │       │   │   │   ├── AuthRemoteDataSource.kt      # HU01–HU05
        │       │   │   │   ├── LugaresRemoteDataSource.kt   # HU06–HU10
        │       │   │   │   ├── VehiculosRemoteDataSource.kt # HU11–HU16
        │       │   │   │   ├── RutasRemoteDataSource.kt     # HU21–HU23 (ruta_guardada)
        │       │   │   │   ├── HistorialRemoteDataSource.kt # HU24 (ruta_realizada)
        │       │   │   │   ├── FavoritosRemoteDataSource.kt # HU25 (columnas favorito)
        │       │   │   │   └── dto/               # @Serializable + @SerialName snake_case
        │       │   │   ├── combustible/
        │       │   │   │   └── PrecioCarburanteDataSource.kt # HU16: API externa de precios (OkHttp)
        │       │   ├── local/
        │       │   │   └── PreferenciasLocalDataSource.kt   # DataStore: HU26, HU27, HU28
        │       │   ├── mapper/                    # toDomain() / toDto() por agregado
        │       │   └── repository/                # XxxRepositoryImpl (implementa domain/repository)
        │       ├── ui/
        │       │   ├── theme/                     # HU28: Theme/Color/Type, claro y oscuro
        │       │   ├── navigation/
        │       │   │   └── AppNav.kt              # NavHost: TODOS los destinos y rutas registrados aquí
        │       │   ├── components/                # Composables reutilizables (Mapa.kt, campos comunes...)
        │       │   ├── session/
        │       │   │   └── SesionViewModel.kt     # estado de sesión observable por toda la app
        │       │   └── feature/<nombre>/          # una carpeta por pantalla (ver mapa HU abajo)
        │       │       ├── XxxViewModel.kt
        │       │       ├── XxxUiState.kt
        │       │       ├── XxxEvent.kt
        │       │       └── XxxScreen.kt
        ├── test/java/es/uji/ei1048/rutas/        # unit tests JVM: MISMA estructura de packages que main
        └── androidTest/java/es/uji/ei1048/rutas/aceptacion/   # tests de aceptación E2E (ATDD, sin mocks)
supabase/
└── migrations/                                    # SQL versionado: NNNN_descripcion.sql, en orden
```

### Mapa HU → feature → ficheros

| Feature (`ui/feature/`) | HU | Ficheros clave (además de los 4 de la pantalla) |
|---|---|---|
| `auth` | HU01, HU02, HU03 | `AuthRemoteDataSource` (HU01–HU05), `AuthRepository(Impl)`, `RegistrarUseCase`, `IniciarSesionUseCase`, `CerrarSesionUseCase` |
| `cuenta` | HU04, HU05 | `AuthRepository(Impl)` (compartido), `EliminarCuentaUseCase`, `CambiarPasswordUseCase` |
| `lugares` | HU06, HU07, HU08, HU09, HU10 | `LugaresRemoteDataSource`, `LugaresRepository(Impl)`, `GeocodingRepository(Impl)`, `AltaLugarPorCoordenadasUseCase`, `AltaLugarPorToponimoUseCase` (usa ORS geocoding), `ConsultarLugaresUseCase`, `EliminarLugarUseCase`, `ConsultarVisitasLugarUseCase`; HU10 → `DetectarPasoPorLugaresUseCase` |
| `vehiculos` | HU11, HU12, HU13, HU14, HU15, HU16 | `VehiculosRemoteDataSource`, `VehiculosRepository(Impl)`, `PrecioCarburanteRepository(Impl)`, `AltaVehiculoUseCase`, `ConsultarVehiculosUseCase`, `EliminarVehiculoUseCase`, `ActualizarVehiculoUseCase`, `ConsultarKmVehiculoUseCase` (lee `km_recorridos`), `ConsultarPrecioCarburanteUseCase` (lee el último precio guardado) |
| `ruta` | HU17, HU18, HU19, HU20 | `OrsDataSource`, `CalculoRutaRepository(Impl)`, `PrecioCarburanteDataSource`, `CalcularRutaUseCase`, `CalcularCosteUseCase` + `CosteStrategy`, `CalcularCaloriasUseCase` + `CaloriasStrategy`, `SeleccionarTipoRutaUseCase` + `TipoRutaStrategy`, `RegistrarRutaRealizadaUseCase` (derivada: actualiza contadores, km y precio), `ActualizarPrecioCarburanteUseCase`, `MapaComposable` |
| `rutas_guardadas` | HU21, HU22, HU23 | `RutasRemoteDataSource`, `RutasRepository(Impl)`, `GuardarRutaUseCase`, `ConsultarRutasGuardadasUseCase`, `EliminarRutaGuardadaUseCase` |
| `historial` | HU24 | `HistorialRemoteDataSource`, `HistorialRepository(Impl)`, `ConsultarHistorialUseCase` |
| `preferencias` | HU25, HU26, HU27, HU28 | `PreferenciasLocalDataSource` (DataStore), `PreferenciasRepository(Impl)`, `FavoritosRepository(Impl)`, `MarcarFavoritoUseCase`, `ListarFavoritosUseCase`, `EstablecerVehiculoPorDefectoUseCase`, `EstablecerTipoRutaPorDefectoUseCase`, `CambiarTemaUseCase`, `ui/theme/` |
| *(transversal)* | HU29, HU30 | Sin pantalla propia: sesión persistida por `supabase-kt`, entidades en Supabase, preferencias en DataStore y `SavedStateHandle` en las ViewModels que deban restaurar estado tras muerte de proceso |

### Cálculo de coste y esfuerzo (HU18, HU19)

Implementación de referencia en `domain/strategy/`:

- **Coche de combustible:** `coste(€) = distancia(km)/100 × consumo(l/100 km) × precio(€/l)`.
- **Coche eléctrico:** `coste(€) = distancia(km)/100 × consumo(kWh/100 km) × precio(€/kWh)`.
- **A pie / bicicleta:** `calorías = distancia(km) × kcalPorKm(modo)`; sin coste monetario.
- Consumo y tipo de energía proceden del vehículo elegido (HU14). El precio es la última cotización
  guardada en `vehiculo.precio_carburante`, actualizada desde la **API externa de precios** al registrar
  una ruta (HU16); `ruta_realizada` conserva el precio y el coste de ese momento. Si aún no hay precio,
  se consulta la API o se usa el valor por defecto del modo.

### Persistencia (R6)

- **Supabase (PostgreSQL) = fuente de verdad de las entidades.** Tablas previstas (migraciones en
  `supabase/migrations/`, **RLS obligatoria** en cada una, política `auth.uid() = usuario_id`):

| Tabla | HU | Notas |
|---|---|---|
| `lugar_interes` | HU06–HU10, HU25 | Coordenadas y/o topónimo, `favorito boolean`, `visitas int` (contador de pasos) |
| `vehiculo` | HU11–HU16, HU25 | Nombre, tipo de energía, consumo, `precio_carburante` (último precio de la API), `km_recorridos` (acumulado), `favorito` |
| `ruta_guardada` | HU21–HU23, HU25 | Ya existe en el spike (`supabase/schema.sql`); añadir `favorito` |
| `ruta_realizada` | HU15, HU16, HU24 (+HU10) | **Distinta** de `ruta_guardada`: fecha, distancia, duración, geometría, `vehiculo_id`, `precio_carburante` (instantánea) y `coste`; alimenta el historial y es el evento que incrementa km y visitas |
| `auth.users` | HU01–HU05 | Gestionada por Supabase Auth; **no** hay tabla `perfil` porque ninguna HU vigente la requiere |

- **Sin tablas de** perfil/avatar/XP/logros/ranking, actividad social, canciones ni letras: ese alcance
  fue eliminado.
- **Contadores materializados (HU10, HU15)**: `lugar_interes.visitas` y `vehiculo.km_recorridos` se
  incrementan al registrar una ruta realizada mediante RPC atómicas de Supabase. Así la consulta es una
  lectura directa y no un `SUM`/`COUNT` cada vez que se listan lugares o vehículos.
- **Precio del carburante (HU16)**: se obtiene de la API externa al registrar la ruta; `vehiculo`
  conserva el último precio y `ruta_realizada` guarda la instantánea usada para el coste. Si la API no
  responde, se mantiene el último valor y la ruta se registra igualmente.
- **HU04**: eliminar la cuenta borra el usuario de `auth.users` y, por `ON DELETE CASCADE` en las FK
  `usuario_id`, todas sus filas (lugares, vehículos, rutas y sus contadores). Nunca se dejan datos huérfanos.
- **DataStore (preferencias locales)**: tema claro/oscuro (HU28), vehículo/modo de transporte por defecto
  (HU26), tipo de ruta por defecto (HU27). Los **favoritos no van en DataStore**: son estado de entidad
  en Supabase para conservarse entre sesiones (HU25 + HU29).
- **HU29/HU30 (restaurar al abrir, incluso tras corte de luz)**: combinación de 1) sesión persistida por
  `supabase-kt` (auto), 2) entidades en Supabase, 3) preferencias en DataStore y 4) `SavedStateHandle` en
  las ViewModels cuyo estado de pantalla deba restaurarse. **Sin Room** por ahora; si una HU exigiera
  caché offline real, se valorará en su rama y se anotará aquí.

### Navegación

- **Navigation-Compose.** Todas las rutas se declaran en `ui/navigation/AppNav.kt` (constantes de ruta +
  `NavHost`). Cada feature expone su `XxxScreen` y se registra ahí. Prohibido navegar con `startActivity`.

### Pruebas (relación con ATDD)

- **Test de aceptación (ATDD, primer commit de `h-XX`)**:
  `app/src/androidTest/.../aceptacion/HuXX<Descripcion>Test.kt`. Compose UI Test
  (`createAndroidComposeRule`), **sin mocks**, contra backend real (Supabase de pruebas + ORS) usando las
  credenciales de `local.properties`. Recorre la UI real. Ejemplos: `Hu10ContarPasoLugarTest`,
  `Hu17CalcularRutaTest`, `Hu28CambiarTemaTest`, `Hu29RestaurarSesionTest`.
- **Tests unitarios**: `app/src/test/...` con la **misma estructura de packages** que `main/`. ViewModel
  con *fakes* manuales (implementaciones en memoria de los puertos de `domain/repository`); red simulada
  con `MockWebServer` (OkHttp/ORS) y `MockEngine` (Supabase/Ktor). Sin framework de mocking añadido (no
  mockk).
- **Nombres**: aceptación `Hu17CalcularRutaTest`; unitarios `RutaViewModelTest`, `OrsDataSourceTest`,
  `CalcularRutaUseCaseTest`.

### Decisiones cerradas y pendientes explícitos

- **Cerrado**: MVVM (no MVP/MVI), DI manual (no Hilt), sin Room, un solo módulo, nombres de negocio en
  español, `es.uji.ei1048.rutas` como paquete base, **≥ 5 patrones de diseño** documentados arriba.
- **Fuera de alcance (eliminado en la nueva numeración; no reintroducir sin HU nueva)**: edición de
  perfil, avatar, restablecer perfil, experiencia/XP, logros y récords, ranking, interacción social,
  música (canción sonando, letra, controles), seguimiento en vivo sobre el mapa y preferencias de vías y
  musicales.
- **El código del spike se porta reubicándolo**: `OrsClient` → `data/remote/ors/OrsDataSource`;
  `SupabaseClientProvider` → `data/remote/supabase/`; `RutasRepository` → interfaz en
  `domain/repository/RutasRepository` + `data/repository/RutasRepositoryImpl`; `MainActivity` →
  `ui/navigation/AppNav` + features.
- **Librerías nuevas solo si una HU lo exige**, añadidas en la rama de esa HU y anotadas aquí. Con el
  alcance actual no se prevén: no hace falta Coil (sin avatar), ni FusedLocation/servicio en primer plano
  (sin seguimiento en vivo), ni MediaSession (sin música). La API externa de precios de carburante
  (HU16) se consume con OkHttp, ya presente; su URL y clave van en `local.properties`.
- **Pendiente de validar con el equipo**: proveedor y formato de la API externa de precios de carburante,
  umbral de paso por lugar de interés (50 m por defecto) y valores de `kcalPorKm` para caminar y
  bicicleta.
