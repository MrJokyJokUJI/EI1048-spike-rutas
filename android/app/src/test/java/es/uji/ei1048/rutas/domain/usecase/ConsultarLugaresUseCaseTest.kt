package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.FalloConexionServidorException
import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.Punto
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.repository.LugaresRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para ConsultarLugaresUseCase con fakes manuales en memoria (ARQUITECTURA.md).
 */
class ConsultarLugaresUseCaseTest {

    private class FakeLugaresRepository : LugaresRepository {
        val lugares = mutableListOf<LugarInteres>()
        var falloSimulado: Throwable? = null

        override suspend fun altaLugar(lugar: LugarInteres): Result<LugarInteres> {
            falloSimulado?.let { return Result.failure(it) }
            lugares.add(lugar)
            return Result.success(lugar)
        }

        override suspend fun consultarLugares(usuarioId: String): Result<List<LugarInteres>> {
            falloSimulado?.let { return Result.failure(it) }
            return Result.success(lugares.filter { it.usuarioId == usuarioId })
        }

        override suspend fun existePorNombre(usuarioId: String, nombre: String): Result<Boolean> {
            falloSimulado?.let { return Result.failure(it) }
            return Result.success(lugares.any { it.usuarioId == usuarioId && it.nombre.equals(nombre, ignoreCase = true) })
        }
    }

    private lateinit var repository: FakeLugaresRepository
    private lateinit var useCase: ConsultarLugaresUseCase
    private val usuarioId = "u1"

    @Before
    fun setUp() {
        repository = FakeLugaresRepository()
        useCase = ConsultarLugaresUseCase(repository)
    }

    @Test
    fun consultaLugares_devuelveSoloLugaresDelUsuario() = runBlocking {
        repository.lugares.add(LugarInteres("1", usuarioId, "Casa", Punto(39.98, -0.05)))
        repository.lugares.add(LugarInteres("2", "otroUsuario", "Oficina", Punto(40.0, -0.1)))

        val res = useCase(usuarioId)

        assertTrue(res.isSuccess)
        val lista = res.getOrThrow()
        assertEquals(1, lista.size)
        assertEquals("Casa", lista.first().nombre)
    }

    @Test
    fun usuarioVacio_devuelveFalloPorFaltaDeAutenticacion() = runBlocking {
        val res = useCase("")

        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is UsuarioNoAutenticadoException)
    }

    @Test
    fun falloRepositorio_sePropaga() = runBlocking {
        repository.falloSimulado = FalloConexionServidorException()

        val res = useCase(usuarioId)

        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is FalloConexionServidorException)
    }
}
