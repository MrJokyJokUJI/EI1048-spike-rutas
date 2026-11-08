package es.uji.ei1048.rutas.domain.model

import java.util.UUID

/**
 * Patrón Factory para la creación y validación de instancias de [Vehiculo].
 *
 * Encapsula la lógica de instanciación y validación de reglas de negocio
 * para garantizar la validez del modelo de dominio.
 */
object VehiculoFactory {

    /**
     * Crea una nueva instancia de [Vehiculo] validando sus invariantes.
     *
     * @throws UsuarioNoAutenticadoException si [usuarioId] está en blanco.
     * @throws NombreVehiculoInvalidoException si [nombre] está en blanco.
     * @throws ConsumoVehiculoInvalidoException si [consumo] <= 0.
     */
    fun crear(
        id: String = UUID.randomUUID().toString(),
        usuarioId: String,
        nombre: String,
        tipoEnergia: TipoEnergia,
        consumo: Double,
        precioCarburante: Double? = null,
        kmRecorridos: Double = 0.0,
        favorito: Boolean = false,
    ): Vehiculo {
        if (usuarioId.isBlank()) {
            throw UsuarioNoAutenticadoException()
        }
        if (nombre.isBlank()) {
            throw NombreVehiculoInvalidoException()
        }
        if (consumo <= 0.0) {
            throw ConsumoVehiculoInvalidoException()
        }

        return Vehiculo(
            id = id,
            usuarioId = usuarioId.trim(),
            nombre = nombre.trim(),
            tipoEnergia = tipoEnergia,
            consumo = consumo,
            precioCarburante = precioCarburante,
            kmRecorridos = kmRecorridos,
            favorito = favorito,
        )
    }
}
