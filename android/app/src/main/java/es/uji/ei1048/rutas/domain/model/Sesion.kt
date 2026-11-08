package es.uji.ei1048.rutas.domain.model

/**
 * Sesión de un usuario autenticado (HU02, HU03).
 */
data class Sesion(
    val usuarioId: String,
    val email: String,
    val token: String
)
