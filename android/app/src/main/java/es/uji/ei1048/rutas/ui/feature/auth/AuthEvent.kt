package es.uji.ei1048.rutas.ui.feature.auth

/**
 * Acciones del usuario en la pantalla de autenticación.
 *
 * Flujo unidireccional: la Screen emite eventos, la ViewModel los procesa.
 */
sealed interface AuthEvent {
    data class EmailCambiado(val email: String) : AuthEvent
    data class PasswordCambiado(val password: String) : AuthEvent
    data class ConfirmacionCambiada(val confirmacion: String) : AuthEvent
    data object Registrar : AuthEvent
    data object ErrorDescartado : AuthEvent
    data object MensajeExitoDescartado : AuthEvent
}
