package es.uji.ei1048.rutas.di

import es.uji.ei1048.rutas.BuildConfig
import es.uji.ei1048.rutas.data.remote.supabase.VehiculosRemoteDataSource
import es.uji.ei1048.rutas.data.repository.VehiculosRepositoryImpl
import es.uji.ei1048.rutas.domain.repository.VehiculosRepository
import es.uji.ei1048.rutas.domain.usecase.AltaVehiculoUseCase
import es.uji.ei1048.rutas.ui.feature.vehiculos.VehiculosViewModelFactory
import okhttp3.OkHttpClient

/**
 * Contenedor de inyección de dependencias manual (patrón Singleton).
 *
 * Centraliza la creación y provisión de DataSources, repositorios y casos de uso.
 * Sin frameworks de DI externos como Hilt o Koin (ver ARQUITECTURA.md).
 */
class AppContainer(
    val okHttpClient: OkHttpClient = OkHttpClient(),
    val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY,
    private val sesionUsuarioIdProvider: () -> String? = { "demo-user-id" },
    private val authTokenProvider: () -> String? = { null },
) {

    // --- Vehículos (HU11–HU16) ---
    val vehiculosRemoteDataSource: VehiculosRemoteDataSource by lazy {
        VehiculosRemoteDataSource(
            client = okHttpClient,
            supabaseUrl = supabaseUrl,
            supabaseAnonKey = supabaseAnonKey,
        )
    }

    val vehiculosRepository: VehiculosRepository by lazy {
        VehiculosRepositoryImpl(
            remoteDataSource = vehiculosRemoteDataSource,
            authTokenProvider = authTokenProvider,
        )
    }

    val altaVehiculoUseCase: AltaVehiculoUseCase by lazy {
        AltaVehiculoUseCase(vehiculosRepository = vehiculosRepository)
    }

    val vehiculosViewModelFactory: VehiculosViewModelFactory by lazy {
        VehiculosViewModelFactory(
            altaVehiculoUseCase = altaVehiculoUseCase,
            obtenerUsuarioIdActual = sesionUsuarioIdProvider,
        )
    }
}
