package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.CoordenadasFueraDeRangoException
import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.LugarYaExisteException
import es.uji.ei1048.rutas.domain.model.NombreLugarInvalidoException
import es.uji.ei1048.rutas.domain.model.Punto
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.repository.LugaresRepository
import java.util.UUID

/**
 * Caso de uso: Alta de lugar de interés por coordenadas (HU06).
 * Implementa el patrón Caso de uso / Interactor (ARQUITECTURA.md).
 *
 * Contrato de invocación:
 * suspend operator fun invoke(...)
 */
class AltaLugarPorCoordenadasUseCase(
    private val lugaresRepository: LugaresRepository,
) {
    suspend operator fun invoke(
        usuarioId: String,
        nombre: String,
        latitud: Double,
        longitud: Double,
    ): Result<LugarInteres> {
        if (usuarioId.isBlank()) {
            return Result.failure(UsuarioNoAutenticadoException())
        }

        val nombreNormalizado = nombre.trim()
        if (nombreNormalizado.isBlank()) {
            return Result.failure(NombreLugarInvalidoException())
        }

        val punto = Punto(latitud = latitud, longitud = longitud)
        if (!punto.sonValidas()) {
            return Result.failure(CoordenadasFueraDeRangoException())
        }

        val existeRes = lugaresRepository.existePorNombre(usuarioId, nombreNormalizado)
        if (existeRes.isFailure) {
            return Result.failure(existeRes.exceptionOrNull() ?: Exception("Error al verificar existencia"))
        }
        if (existeRes.getOrDefault(false)) {
            return Result.failure(LugarYaExisteException())
        }

        val nuevoLugar = LugarInteres(
            id = UUID.randomUUID().toString(),
            usuarioId = usuarioId,
            nombre = nombreNormalizado,
            coordenadas = punto,
            favorito = false,
            visitas = 0,
        )

        return lugaresRepository.altaLugar(nuevoLugar)
    }
}
