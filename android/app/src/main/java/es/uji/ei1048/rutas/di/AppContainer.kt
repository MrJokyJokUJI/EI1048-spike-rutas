package es.uji.ei1048.rutas.di

import es.uji.ei1048.rutas.BuildConfig
import es.uji.ei1048.rutas.data.remote.supabase.AuthRemoteDataSource
import es.uji.ei1048.rutas.data.repository.AuthRepositoryImpl
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import es.uji.ei1048.rutas.domain.usecase.RegistrarUseCase
import es.uji.ei1048.rutas.ui.feature.auth.AuthViewModelFactory
import okhttp3.OkHttpClient

/**
 * Contenedor de inyección de dependencias manual (Singleton).
 *
 * Centraliza la creación de DataSources, repositorios, casos de uso y factorías
 * de ViewModels. Sin frameworks de DI (ver ARQUITECTURA.md, regla 6).
 */
class AppContainer(
    val okHttpClient: OkHttpClient = OkHttpClient(),
    val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY,
) {

    // --- Autenticación (HU01–HU05) ---
    val authRemoteDataSource: AuthRemoteDataSource by lazy {
        AuthRemoteDataSource(
            client = okHttpClient,
            supabaseUrl = supabaseUrl,
            supabaseAnonKey = supabaseAnonKey,
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(remoteDataSource = authRemoteDataSource)
    }

    val registrarUseCase: RegistrarUseCase by lazy {
        RegistrarUseCase(authRepository = authRepository)
    }

    val authViewModelFactory: AuthViewModelFactory by lazy {
        AuthViewModelFactory(registrarUseCase = registrarUseCase)
    }
}
