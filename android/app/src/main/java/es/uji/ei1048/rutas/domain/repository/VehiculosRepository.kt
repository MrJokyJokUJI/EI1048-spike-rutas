package es.uji.ei1048.rutas.domain.repository

import es.uji.ei1048.rutas.domain.model.Vehiculo

/**
 * Puerto de dominio para la persistencia y gestión de vehículos (HU11–HU16).
 *
 * Sigue el patrón Repository. Métodos puros en Kotlin sin dependencias de base de datos ni red.
 */
interface VehiculosRepository {

    /**
     * Da de alta un nuevo vehículo para el usuario autenticado (HU11).
     *
     * @param vehiculo Entidad de dominio validada.
     * @return [Result.success] con el vehículo persistido o [Result.failure] si ocurre un error.
     */
    suspend fun altaVehiculo(vehiculo: Vehiculo): Result<Vehiculo>

    /**
     * Consulta la lista de vehículos dados de alta por el usuario (HU12).
     */
    suspend fun consultarVehiculos(usuarioId: String): Result<List<Vehiculo>>
}
