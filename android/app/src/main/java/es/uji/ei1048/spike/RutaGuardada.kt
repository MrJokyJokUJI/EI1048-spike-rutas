package es.uji.ei1048.spike

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Ruta guardada en Supabase. Las columnas usan snake_case; se mapean con @SerialName.
// Los campos id/usuarioId/creadaEn los rellena la base de datos, por eso son nulos
// y se omiten al insertar (explicitNulls = false en el cliente).
@Serializable
data class RutaGuardada(
    val id: String? = null,
    @SerialName("usuario_id") val usuarioId: String? = null,
    val origen: String = "",
    val destino: String = "",
    val modo: String = "",
    @SerialName("distancia_km") val distanciaKm: Double = 0.0,
    @SerialName("duracion_min") val duracionMin: Double = 0.0,
    @SerialName("creada_en") val creadaEn: String? = null,
)
