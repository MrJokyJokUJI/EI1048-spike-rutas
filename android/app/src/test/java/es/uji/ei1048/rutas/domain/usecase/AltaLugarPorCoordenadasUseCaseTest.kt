package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.CoordenadasFueraDeRangoException
import es.uji.ei1048.rutas.domain.model.FalloConexionServidorException
import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.LugarYaExisteException
import es.uji.ei1048.rutas.domain.model.NombreLugarInvalidoException
import es.uji.ei1048.rutas.domain.model.Punto
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.repository.LugaresRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para AltaLugarPorCoordenadasUseCase con fakes manuales (ARQUITECTURA.md).
 * Cubre los escenarios E1, E2, E3 de Entrega 1 y comprobaciones unitarias del caso de uso.
 */
class AltaLugarPorCoordenadasUseCaseTest {

    private class FakeLugaresRepository : LugaresRepository {
        val guardados = mutableListOf<LugarInteres>()
        var falloSimulado: Throwable? = null

        override suspend fun altaLugar(lugar: LugarInteres): Result<LugarInteres> {
            falloSimulado?.let { return Result.failure(it) }
            guardados.add(lugar)
            return Result.success(lugar)
        }

        override suspend fun consultarLugares(usuarioId: String): Result<List<LugarInteres>> {
            falloSimulado?.let { return Result.failure(it) }
            return Result.success(guardados.filter { it.usuarioId == usuarioId })
        }

        override suspend fun existePorNombre(usuarioId: String, nombre: String): Result<Boolean> {
            falloSimulado?.let { return Result.failure(it) }
            return Result.success(guardados.any { it.usuarioId == usuarioId && it.nombre.equals(nombre, ignoreCase = true) })
        }
    }

    private lateinit var repository: FakeLugaresRepository
    private lateinit var useCase: AltaLugarPorCoordenadasUseCase
    private val usuarioId = "u1"

    @Before
    fun setUp() {
        repository = FakeLugaresRepository()
        useCase = AltaLugarPorCoordenadasUseCase(repository)
    }

    @Test
    fun e1_altaCorrecta_creaLugarConCoordenadas() = runBlocking {
        val res = useCase(usuarioId, "Castellón", 39.9864, -0.0513)

        assertTrue(res.isSuccess)
        val lugar = res.getOrThrow()
        assertEquals("Castellón", lugar.nombre)
        assertEquals(39.9864, lugar.coordenadas.latitud, 0.0001)
        assertEquals(-0.0513, lugar.coordenadas.longitud, 0.0001)
        assertEquals(1, repository.guardados.size)
    }

    @Test
    fun e2_coordenadasInvalidas_devuelveFallo() = runBlocking {
        val res = useCase(usuarioId, "Castellón", 100.0, -0.0513)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is CoordenadasFueraDeRangoException)
    }

    @Test
    fun e3_servidorNoDisponible_devuelveFallo() = runBlocking {
        repository.falloSimulado = FalloConexionServidorException()
        val res = useCase(usuarioId, "Castellón", 39.9864, -0.0513)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is FalloConexionServidorException)
    }

    @Test
    fun nombreVacio_devuelveFallo() = runBlocking {
        val res = useCase(usuarioId, "", 39.9864, -0.0513)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is NombreLugarInvalidoException)
    }

    @Test
    fun usuarioVacio_devuelveFallo() = runBlocking {
        val res = useCase("", "Castellón", 39.9864, -0.0513)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is UsuarioNoAutenticadoException)
    }

    @Test
    fun lugarDuplicado_devuelveFallo() = runBlocking {
        repository.guardados.add(
            LugarInteres("id1", usuarioId, "Castellón", Punto(39.9864, -0.0513))
        )
        val res = useCase(usuarioId, "castellón", 40.0, -0.1)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull() is LugarYaExisteException)
    }
}
