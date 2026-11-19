package es.uji.ei1048.rutas.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uji.ei1048.rutas.domain.usecase.RegistrarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de autenticación (patrón MVVM).
 *
 * Solo llama a casos de uso; nunca accede a repositorios ni a Supabase.
 */
class AuthViewModel(
    private val registrarUseCase: RegistrarUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailCambiado -> _state.update { it.copy(email = event.email, error = null) }
            is AuthEvent.PasswordCambiado -> _state.update { it.copy(password = event.password, error = null) }
            is AuthEvent.ConfirmacionCambiada ->
                _state.update { it.copy(confirmacion = event.confirmacion, error = null) }
            is AuthEvent.ErrorDescartado -> _state.update { it.copy(error = null) }
            is AuthEvent.MensajeExitoDescartado -> _state.update { it.copy(mensajeExito = null) }
            is AuthEvent.Registrar -> registrar()
        }
    }

    private fun registrar() {
        val actual = _state.value
        when {
            actual.email.isBlank() || actual.password.isBlank() -> {
                _state.update { it.copy(error = "Introduce el email y la contraseña.") }
                return
            }
            actual.password != actual.confirmacion -> {
                _state.update { it.copy(error = "Las contraseñas no coinciden.") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { registrarUseCase(actual.email, actual.password) }
                .onSuccess { sesion ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            sesion = sesion,
                            password = "",
                            confirmacion = "",
                            mensajeExito = "Usuario registrado correctamente.",
                        )
                    }
                }
                .onFailure { fallo ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = fallo.message ?: "Error desconocido al registrar el usuario.",
                        )
                    }
                }
        }
    }
}
