package es.uji.ei1048.rutas.ui.feature.auth

import es.uji.ei1048.rutas.domain.model.Sesion

/**
 * Estado inmutable de la pantalla de autenticación (HU01–HU03).
 *
 * Contiene todo lo que dibuja la vista: campos, carga, errores y sesión.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null,
    val mensajeExito: String? = null,
)
