package es.uji.ei1048.rutas.aceptacion

import androidx.test.ext.junit.runners.AndroidJUnit4
import es.uji.ei1048.rutas.domain.model.FalloConexionServidorException
import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.Punto
import es.uji.ei1048.rutas.domain.repository.LugaresRepository
import es.uji.ei1048.rutas.domain.usecase.ConsultarLugaresUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de aceptación de la HU08: Consulta de lista de lugares de interés dados de alta (ATDD).
 *
 * Basadas estrictamente en el análisis de escenarios de Entrega 1:
 * - HU08-E1 (Válido): servidor conectado, sesión iniciada y lugares dados de alta (Casa y Trabajo) ->
 *                     el sistema devuelve la lista completa de lugares y la base de datos se mantiene inalterada.
 * - HU08-E2 (Inválido): servidor no disponible, sesión iniciada ->
 *                       el sistema devuelve un error de fallo de conexión con el servidor y la persistencia no cambia.
 */
@RunWith(AndroidJUnit4::class)
class Hu08ListarLugaresTest {

    private class SUTFakeLugaresRepository : LugaresRepository {
        val lugares = mutableListOf<LugarInteres>()
        var servidorDisponible: Boolean = true

        override suspend fun altaLugar(lugar: LugarInteres): Result<LugarInteres> {
            if (!servidorDisponible) {
                return Result.failure(FalloConexionServidorException())
            }
            lugares.add(lugar)
            return Result.success(lugar)
        }

        override suspend fun consultarLugares(usuarioId: String): Result<List<LugarInteres>> {
            if (!servidorDisponible) {
                return Result.failure(FalloConexionServidorException())
            }
            return Result.success(lugares.filter { it.usuarioId == usuarioId })
        }

        override suspend fun existePorNombre(usuarioId: String, nombre: String): Result<Boolean> {
            if (!servidorDisponible) {
                return Result.failure(FalloConexionServidorException())
            }
            return Result.success(lugares.any { it.usuarioId == usuarioId && it.nombre.equals(nombre, ignoreCase = true) })
        }
    }

    private lateinit var repository: SUTFakeLugaresRepository
    private lateinit var consultarLugares: ConsultarLugaresUseCase

    private val usuarioAutenticadoId = "user-test-uuid-9876"

    @Before
    fun setUp() {
        repository = SUTFakeLugaresRepository()
        consultarLugares = ConsultarLugaresUseCase(repository)
    }

    /**
     * HU08-E1 (Válido):
     * GIVEN el servidor está conectado, tengo la sesión iniciada y tengo dados de alta previamente
     *       los lugares Casa y Trabajo,
     * WHEN solicito consultar la lista de lugares de interés,
     * THEN el sistema devuelve la lista completa de lugares registrados (Casa y Trabajo)
     *      y la base de datos se mantiene inalterada.
     */
    @Test
    fun hu08_e1_consultaListaLugares_devuelveListaCompletaYPersistenciaInalterada() = runBlocking {
        // GIVEN: dados de alta previamente Casa y Trabajo
        val casa = LugarInteres(
            id = "lugar-1",
            usuarioId = usuarioAutenticadoId,
            nombre = "Casa",
            coordenadas = Punto(39.9864, -0.0513),
        )
        val trabajo = LugarInteres(
            id = "lugar-2",
            usuarioId = usuarioAutenticadoId,
            nombre = "Trabajo",
            coordenadas = Punto(39.9920, -0.0680),
        )
        repository.lugares.add(casa)
        repository.lugares.add(trabajo)

        val cantidadInicial = repository.lugares.size

        // WHEN
        val resultado = consultarLugares(usuarioId = usuarioAutenticadoId)

        // THEN
        assertTrue("El resultado debe ser exitoso", resultado.isSuccess)
        val lista = resultado.getOrThrow()
        assertEquals(2, lista.size)
        assertEquals("Casa", lista[0].nombre)
        assertEquals("Trabajo", lista[1].nombre)

        // Persistencia inalterada
        assertEquals(cantidadInicial, repository.lugares.size)
    }

    /**
     * HU08-E2 (Inválido):
     * GIVEN el servidor no está disponible, tengo la sesión iniciada y existen lugares registrados
     *       previamente en la cuenta,
     * WHEN solicito consultar la lista de lugares,
     * THEN el sistema devuelve un error de fallo de conexión con el servidor y el estado de la
     *      persistencia no cambia.
     */
    @Test
    fun hu08_e2_servidorNoDisponible_devuelveErrorDeConexion() = runBlocking {
        // GIVEN
        repository.lugares.add(
            LugarInteres(
                id = "lugar-1",
                usuarioId = usuarioAutenticadoId,
                nombre = "Casa",
                coordenadas = Punto(39.9864, -0.0513),
            )
        )
        repository.servidorDisponible = false

        // WHEN
        val resultado = consultarLugares(usuarioId = usuarioAutenticadoId)

        // THEN
        assertTrue("El resultado debe ser fallido", resultado.isFailure)
        assertTrue(
            "Se esperaba FalloConexionServidorException",
            resultado.exceptionOrNull() is FalloConexionServidorException,
        )
        // El estado de la persistencia no cambia
        assertEquals(1, repository.lugares.size)
    }
}
