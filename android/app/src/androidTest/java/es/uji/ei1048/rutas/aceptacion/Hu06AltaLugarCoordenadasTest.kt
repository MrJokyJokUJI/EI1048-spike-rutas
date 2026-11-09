package es.uji.ei1048.rutas.aceptacion

import androidx.test.ext.junit.runners.AndroidJUnit4
import es.uji.ei1048.rutas.domain.model.CoordenadasFueraDeRangoException
import es.uji.ei1048.rutas.domain.model.FalloConexionServidorException
import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.LugarYaExisteException
import es.uji.ei1048.rutas.domain.model.NombreLugarInvalidoException
import es.uji.ei1048.rutas.domain.model.Punto
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.repository.LugaresRepository
import es.uji.ei1048.rutas.domain.usecase.AltaLugarPorCoordenadasUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de aceptación de la HU06: Alta de lugar de interés usando coordenadas (ATDD).
 *
 * Basadas en el análisis de escenarios de Entrega 1 arreglada:
 * - HU06-E1 (Válido): servidor conectado, sesión iniciada, coordenadas y nombre válidos -> lugar creado y persistido.
 * - HU06-E2 (Inválido): servidor conectado, sesión iniciada, coordenadas fuera de rango -> error CoordenadasFueraDeRangoException.
 * - HU06-E3 (Inválido): servidor no disponible -> error FalloConexionServidorException.
 * - Adicionales de robustez: nombre inválido/vacío, usuario no autenticado, lugar duplicado.
 */
@RunWith(AndroidJUnit4::class)
class Hu06AltaLugarCoordenadasTest {

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
            val existe = lugares.any { it.usuarioId == usuarioId && it.nombre.equals(nombre, ignoreCase = true) }
            return Result.success(existe)
        }
    }

    private lateinit var repository: SUTFakeLugaresRepository
    private lateinit var altaLugarPorCoordenadas: AltaLugarPorCoordenadasUseCase

    private val usuarioAutenticadoId = "user-test-uuid-4567"

    @Before
    fun setUp() {
        repository = SUTFakeLugaresRepository()
        altaLugarPorCoordenadas = AltaLugarPorCoordenadasUseCase(repository)
    }

    /**
     * HU06-E1:
     * GIVEN el servidor está conectado, tengo la sesión iniciada y el lugar Mirador de Madrid no existe previamente en mi lista,
     * WHEN introduzco las coordenadas latitud 40.4168 y longitud -3.7038 y el nombre Mirador de Madrid y guardo,
     * THEN el sistema devuelve los datos del nuevo lugar creado y se persiste el registro del lugar de interés asociado al usuario.
     */
    @Test
    fun hu06_e1_coordenadasYNombreValidos_creaYPersisteLugar() = runBlocking {
        // GIVEN
        val nombre = "Mirador de Madrid"
        val latitud = 40.4168
        val longitud = -3.7038

        // WHEN
        val resultado = altaLugarPorCoordenadas(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            latitud = latitud,
            longitud = longitud,
        )

        // THEN
        assertTrue("El resultado debe ser exitoso", resultado.isSuccess)
        val lugar = resultado.getOrThrow()
        assertNotNull("El ID del lugar debe generarse", lugar.id)
        assertTrue("El ID no debe ser vacío", lugar.id.isNotBlank())
        assertEquals(usuarioAutenticadoId, lugar.usuarioId)
        assertEquals(nombre, lugar.nombre)
        assertEquals(latitud, lugar.coordenadas.latitud, 0.0001)
        assertEquals(longitud, lugar.coordenadas.longitud, 0.0001)
        assertEquals(false, lugar.favorito)
        assertEquals(0, lugar.visitas)

        // Persistido en el repositorio
        assertEquals(1, repository.lugares.size)
        assertEquals(lugar, repository.lugares.first())
    }

    /**
     * HU06-E2:
     * GIVEN el servidor está conectado, tengo la sesión iniciada y la lista de lugares no contiene previamente la ubicación a registrar,
     * WHEN introduzco las coordenadas fuera de rango latitud 999.0 y longitud -3.7038 y guardo,
     * THEN el sistema devuelve un error de coordenadas fuera de rango y no se añade ningún lugar a la base de datos.
     */
    @Test
    fun hu06_e2_coordenadasFueraDeRango_rechazaAltaYNoPersiste() = runBlocking {
        // GIVEN
        val nombre = "Lugar Fuera de Rango"
        val latitudInvalida = 999.0
        val longitud = -3.7038

        // WHEN
        val resultado = altaLugarPorCoordenadas(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            latitud = latitudInvalida,
            longitud = longitud,
        )

        // THEN
        assertTrue("El resultado debe ser fallido", resultado.isFailure)
        val excepcion = resultado.exceptionOrNull()
        assertTrue(
            "Se esperaba CoordenadasFueraDeRangoException pero fue ${excepcion?.javaClass?.simpleName}",
            excepcion is CoordenadasFueraDeRangoException,
        )
        assertTrue("La base de datos debe permanecer inalterada", repository.lugares.isEmpty())
    }

    /**
     * HU06-E3:
     * GIVEN el servidor no está disponible y tengo la sesión iniciada,
     * WHEN intento dar de alta un lugar con datos válidos,
     * THEN el sistema devuelve un error de fallo de conexión con el servidor y la persistencia no varía.
     */
    @Test
    fun hu06_e3_servidorNoDisponible_devuelveErrorDeConexion() = runBlocking {
        // GIVEN
        repository.servidorDisponible = false

        // WHEN
        val resultado = altaLugarPorCoordenadas(
            usuarioId = usuarioAutenticadoId,
            nombre = "Castillo de Morella",
            latitud = 40.6190,
            longitud = -0.1008,
        )

        // THEN
        assertTrue("El resultado debe ser fallido por conexión", resultado.isFailure)
        assertTrue(
            "Se esperaba FalloConexionServidorException",
            resultado.exceptionOrNull() is FalloConexionServidorException,
        )
        assertTrue(repository.lugares.isEmpty())
    }

    /**
     * Escenario adicional de robustez: nombre vacío o en blanco.
     */
    @Test
    fun hu06_extra_nombreVacio_fallaConNombreInvalidoException() = runBlocking {
        val resultado = altaLugarPorCoordenadas(
            usuarioId = usuarioAutenticadoId,
            nombre = "   ",
            latitud = 40.4168,
            longitud = -3.7038,
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is NombreLugarInvalidoException)
        assertTrue(repository.lugares.isEmpty())
    }

    /**
     * Escenario adicional: usuario sin sesión iniciada.
     */
    @Test
    fun hu06_extra_sinSesion_fallaConUsuarioNoAutenticadoException() = runBlocking {
        val resultado = altaLugarPorCoordenadas(
            usuarioId = "",
            nombre = "Plaza Mayor",
            latitud = 39.9864,
            longitud = -0.0513,
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is UsuarioNoAutenticadoException)
        assertTrue(repository.lugares.isEmpty())
    }

    /**
     * Escenario adicional: lugar ya existente con ese nombre para el mismo usuario.
     */
    @Test
    fun hu06_extra_lugarYaExistente_fallaConLugarYaExisteException() = runBlocking {
        // GIVEN: ya existe "Casa"
        repository.lugares.add(
            LugarInteres(
                id = "1",
                usuarioId = usuarioAutenticadoId,
                nombre = "Casa",
                coordenadas = Punto(39.9864, -0.0513),
            )
        )

        // WHEN: se intenta dar de alta otro con el mismo nombre
        val resultado = altaLugarPorCoordenadas(
            usuarioId = usuarioAutenticadoId,
            nombre = "Casa",
            latitud = 40.0,
            longitud = -0.1,
        )

        // THEN
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is LugarYaExisteException)
        assertEquals(1, repository.lugares.size)
    }
}
