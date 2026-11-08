package es.uji.ei1048.rutas.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO para la serialización de la tabla `vehiculo` en Supabase (PostgREST).
 *
 * Utiliza snake_case para mapear con las columnas de PostgreSQL.
 */
@Serializable
data class VehiculoDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("nombre")
    val nombre: String,
    @SerialName("tipo_energia")
    val tipoEnergia: String,
    @SerialName("consumo")
    val consumo: Double,
    @SerialName("precio_carburante")
    val precioCarburante: Double? = null,
    @SerialName("km_recorridos")
    val kmRecorridos: Double = 0.0,
    @SerialName("favorito")
    val favorito: Boolean = false,
)
