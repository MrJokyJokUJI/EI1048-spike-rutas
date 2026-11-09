package es.uji.ei1048.rutas.domain.model

/**
 * Coordenadas geográficas (latitud, longitud).
 * Pertenece a domain/model (Kotlin puro, sin librerías externas).
 */
data class Punto(
    val latitud: Double,
    val longitud: Double,
) {
    fun sonValidas(): Boolean =
        latitud in -90.0..90.0 && longitud in -180.0..180.0
}
