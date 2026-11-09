package es.uji.ei1048.rutas.domain.repository

import es.uji.ei1048.rutas.domain.model.LugarInteres

/**
 * Puerto de dominio para la persistencia de Lugares de Interés (HU06–HU10).
 * Kotlin puro: solo métodos suspend y tipos de dominio (ARQUITECTURA.md).
 */
interface LugaresRepository {
    suspend fun altaLugar(lugar: LugarInteres): Result<LugarInteres>
    suspend fun consultarLugares(usuarioId: String): Result<List<LugarInteres>>
    suspend fun existePorNombre(usuarioId: String, nombre: String): Result<Boolean>
}
