package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.Vehiculo
import es.uji.ei1048.rutas.domain.model.VehiculoFactory
import es.uji.ei1048.rutas.domain.repository.VehiculosRepository

/**
 * Caso de uso: Alta de un vehículo (HU11).
 *
 * Permite a un usuario con sesión iniciada dar de alta un vehículo
 * para emplearlo como método de transporte en sus rutas.
 *
 * Valida invariantes de negocio mediante [VehiculoFactory] y delega
 * la persistencia en el puerto [VehiculosRepository].
 */
class AltaVehiculoUseCase(
    private val vehiculosRepository: VehiculosRepository,
) {

    /**
     * Ejecuta el caso de uso de alta de vehículo.
     *
     * @param usuarioId Identificador del usuario autenticado.
     * @param nombre Nombre descriptivo del vehículo (no vacío).
     * @param tipoEnergia Tipo de energía (combustible o eléctrico).
     * @param consumo Consumo medio (> 0).
     * @return [Result.success] con el [Vehiculo] creado o [Result.failure] con la excepción correspondiente.
     */
    suspend operator fun invoke(
        usuarioId: String,
        nombre: String,
        tipoEnergia: TipoEnergia,
        consumo: Double,
    ): Result<Vehiculo> {
        return runCatching {
            val vehiculo = VehiculoFactory.crear(
                usuarioId = usuarioId,
                nombre = nombre,
                tipoEnergia = tipoEnergia,
                consumo = consumo,
            )
            vehiculosRepository.altaVehiculo(vehiculo).getOrThrow()
        }
    }
}
