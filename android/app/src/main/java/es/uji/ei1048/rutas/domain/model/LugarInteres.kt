package es.uji.ei1048.rutas.domain.model

/**
 * Entidad de dominio Lugar de Interés (HU06–HU10, HU25).
 * Modelo en Kotlin puro sin anotaciones de serialización (ARQUITECTURA.md).
 */
data class LugarInteres(
    val id: String,
    val usuarioId: String,
    val nombre: String,
    val coordenadas: Punto,
    val favorito: Boolean = false,
    val visitas: Int = 0,
)
