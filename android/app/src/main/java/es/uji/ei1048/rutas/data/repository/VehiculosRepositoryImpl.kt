package es.uji.ei1048.rutas.data.repository

import es.uji.ei1048.rutas.data.mapper.toDomain
import es.uji.ei1048.rutas.data.mapper.toDto
import es.uji.ei1048.rutas.data.remote.supabase.VehiculosRemoteDataSource
import es.uji.ei1048.rutas.domain.model.Vehiculo
import es.uji.ei1048.rutas.domain.repository.VehiculosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del puerto [VehiculosRepository] (patrón Repository).
 *
 * Aísla el dominio de los detalles de acceso a datos y comunicación con Supabase.
 */
class VehiculosRepositoryImpl(
    private val remoteDataSource: VehiculosRemoteDataSource,
    private val authTokenProvider: () -> String? = { null },
) : VehiculosRepository {

    override suspend fun altaVehiculo(vehiculo: Vehiculo): Result<Vehiculo> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = vehiculo.toDto()
            val token = authTokenProvider()
            val resultadoDto = remoteDataSource.altaVehiculo(dto, token)
            resultadoDto.toDomain()
        }
    }

    override suspend fun consultarVehiculos(usuarioId: String): Result<List<Vehiculo>> = withContext(Dispatchers.IO) {
        runCatching {
            val token = authTokenProvider()
            val listaDto = remoteDataSource.consultarVehiculos(usuarioId, token)
            listaDto.map { it.toDomain() }
        }
    }
}
