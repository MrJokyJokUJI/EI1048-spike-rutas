package es.uji.ei1048.rutas.data.mapper

import es.uji.ei1048.rutas.data.remote.supabase.dto.VehiculoDto
import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.Vehiculo
import java.util.UUID

/**
 * Patrón Mapper: Convierte entre modelos de dominio y DTOs de infraestructura.
 */
fun VehiculoDto.toDomain(): Vehiculo {
    return Vehiculo(
        id = id ?: UUID.randomUUID().toString(),
        usuarioId = usuarioId,
        nombre = nombre,
        tipoEnergia = try {
            TipoEnergia.valueOf(tipoEnergia.uppercase())
        } catch (_: IllegalArgumentException) {
            TipoEnergia.COMBUSTIBLE
        },
        consumo = consumo,
        precioCarburante = precioCarburante,
        kmRecorridos = kmRecorridos,
        favorito = favorito,
    )
}

fun Vehiculo.toDto(): VehiculoDto {
    return VehiculoDto(
        id = id,
        usuarioId = usuarioId,
        nombre = nombre,
        tipoEnergia = tipoEnergia.name,
        consumo = consumo,
        precioCarburante = precioCarburante,
        kmRecorridos = kmRecorridos,
        favorito = favorito,
    )
}
