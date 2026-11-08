package es.uji.ei1048.rutas.domain.model

/**
 * Modelo de dominio que representa un vehículo registrado por un usuario (HU11).
 *
 * Código Kotlin puro sin anotaciones de serialización ni dependencias de Android/BD.
 *
 * @property id Identificador único del vehículo.
 * @property usuarioId Identificador del usuario propietario.
 * @property nombre Nombre descriptivo del vehículo.
 * @property tipoEnergia Tipo de energía (combustible o eléctrico).
 * @property consumo Consumo medio (litros/100 km para combustible, kWh/100 km para eléctrico).
 * @property precioCarburante Último precio de carburante registrado (se actualiza en HU16).
 * @property kmRecorridos Distancia acumulada recorrida en rutas con este vehículo (HU15).
 * @property favorito Indica si el usuario ha marcado el vehículo como favorito (HU25).
 */
data class Vehiculo(
    val id: String,
    val usuarioId: String,
    val nombre: String,
    val tipoEnergia: TipoEnergia,
    val consumo: Double,
    val precioCarburante: Double? = null,
    val kmRecorridos: Double = 0.0,
    val favorito: Boolean = false,
)
