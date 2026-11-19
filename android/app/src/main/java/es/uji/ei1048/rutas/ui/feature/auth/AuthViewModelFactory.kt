package es.uji.ei1048.rutas.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.uji.ei1048.rutas.domain.usecase.CerrarSesionUseCase
import es.uji.ei1048.rutas.domain.usecase.IniciarSesionUseCase
import es.uji.ei1048.rutas.domain.usecase.RegistrarUseCase

/**
 * Patrón Factory: construye [AuthViewModel] con sus casos de uso sin Hilt/Koin.
 */
class AuthViewModelFactory(
    private val registrarUseCase: RegistrarUseCase,
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                registrarUseCase = registrarUseCase,
                iniciarSesionUseCase = iniciarSesionUseCase,
                cerrarSesionUseCase = cerrarSesionUseCase,
            ) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
