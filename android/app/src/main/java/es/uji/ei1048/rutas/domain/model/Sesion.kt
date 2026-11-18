package es.uji.ei1048.rutas.domain.model

/**
 * Sesión de un usuario autenticado (HU01, HU02, HU03).
 *
 * Modelo de dominio puro: sin anotaciones de serialización (ver ARQUITECTURA.md).
 */
data class Sesion(
    val usuarioId: String,
    val email: String,
    val token: String,
)
