package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.repository.LugaresRepository

/**
 * Caso de uso: Consulta de lista de lugares de interés dados de alta (HU08).
 * Implementa el patrón Caso de uso / Interactor (ARQUITECTURA.md).
 *
 * Contrato de invocación:
 * suspend operator fun invoke(usuarioId: String): Result<List<LugarInteres>>
 */
class ConsultarLugaresUseCase(
    private val lugaresRepository: LugaresRepository,
) {
    suspend operator fun invoke(usuarioId: String): Result<List<LugarInteres>> {
        if (usuarioId.isBlank()) {
            return Result.failure(UsuarioNoAutenticadoException())
        }
        return lugaresRepository.consultarLugares(usuarioId)
    }
}
