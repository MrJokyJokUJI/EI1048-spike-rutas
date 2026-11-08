package es.uji.ei1048.rutas.aceptacion

import es.uji.ei1048.rutas.domain.model.ConsumoVehiculoInvalidoException
import es.uji.ei1048.rutas.domain.model.NombreVehiculoInvalidoException
import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.model.Vehiculo
import es.uji.ei1048.rutas.domain.repository.VehiculosRepository
import es.uji.ei1048.rutas.domain.usecase.AltaVehiculoUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test de aceptación de JVM para HU11 - Alta de vehículo (ATDD).
 *
 * Ejecutable en la JVM como parte de la suite de pruebas automatizadas continuas.
 * Cubre todos los escenarios válidos e inválidos del SUT sin acoplamiento a componentes de interfaz.
 */
class Hu11AltaVehiculoTest {

    private class SUTFakeVehiculosRepository : VehiculosRepository {
        val vehiculos = mutableListOf<Vehiculo>()

        override suspend fun altaVehiculo(vehiculo: Vehiculo): Result<Vehiculo> {
            vehiculos.add(vehiculo)
            return Result.success(vehiculo)
        }

        override suspend fun consultarVehiculos(usuarioId: String): Result<List<Vehiculo>> {
            return Result.success(vehiculos.filter { it.usuarioId == usuarioId })
        }
    }

    private lateinit var repository: SUTFakeVehiculosRepository
    private lateinit var altaVehiculoUseCase: AltaVehiculoUseCase

    private val usuarioAutenticadoId = "user-test-uuid-1234"

    @Before
    fun setUp() {
        repository = SUTFakeVehiculosRepository()
        altaVehiculoUseCase = AltaVehiculoUseCase(repository)
    }

    @Test
    fun escenario1_altaVehiculoCombustible_datosValidos_creaVehiculoCorrectamente() = runBlocking {
        val nombre = "Seat León 1.5 TSI"
        val tipo = TipoEnergia.COMBUSTIBLE
        val consumo = 6.2

        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            tipoEnergia = tipo,
            consumo = consumo,
        )

        assertTrue(resultado.isSuccess)
        val vehiculo = resultado.getOrThrow()
        assertNotNull(vehiculo.id)
        assertTrue(vehiculo.id.isNotBlank())
        assertEquals(usuarioAutenticadoId, vehiculo.usuarioId)
        assertEquals(nombre, vehiculo.nombre)
        assertEquals(tipo, vehiculo.tipoEnergia)
        assertEquals(consumo, vehiculo.consumo, 0.001)
        assertEquals(0.0, vehiculo.kmRecorridos, 0.001)
        assertEquals(false, vehiculo.favorito)

        assertEquals(1, repository.vehiculos.size)
        assertEquals(vehiculo, repository.vehiculos.first())
    }

    @Test
    fun escenario2_altaVehiculoElectrico_datosValidos_creaVehiculoCorrectamente() = runBlocking {
        val nombre = "Tesla Model 3"
        val tipo = TipoEnergia.ELECTRICO
        val consumo = 14.8

        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            tipoEnergia = tipo,
            consumo = consumo,
        )

        assertTrue(resultado.isSuccess)
        val vehiculo = resultado.getOrThrow()
        assertEquals(usuarioAutenticadoId, vehiculo.usuarioId)
        assertEquals(nombre, vehiculo.nombre)
        assertEquals(tipo, vehiculo.tipoEnergia)
        assertEquals(consumo, vehiculo.consumo, 0.001)
        assertEquals(0.0, vehiculo.kmRecorridos, 0.001)
        assertEquals(false, vehiculo.favorito)
    }

    @Test
    fun escenario3_altaVehiculo_nombreVacio_fallaConErrorValidacion() = runBlocking {
        val nombreInvalido = "   "

        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombreInvalido,
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 5.5,
        )

        assertTrue(resultado.isFailure)
        val excepcion = resultado.exceptionOrNull()
        assertTrue(excepcion is NombreVehiculoInvalidoException)
        assertTrue(repository.vehiculos.isEmpty())
    }

    @Test
    fun escenario4_altaVehiculo_consumoInvalido_fallaConErrorValidacion() = runBlocking {
        val consumoCero = 0.0
        val consumoNegativo = -4.5

        val resultadoCero = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = "Renault Clio",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = consumoCero,
        )
        assertTrue(resultadoCero.isFailure)
        assertTrue(resultadoCero.exceptionOrNull() is ConsumoVehiculoInvalidoException)

        val resultadoNegativo = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = "Renault Clio",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = consumoNegativo,
        )
        assertTrue(resultadoNegativo.isFailure)
        assertTrue(resultadoNegativo.exceptionOrNull() is ConsumoVehiculoInvalidoException)

        assertTrue(repository.vehiculos.isEmpty())
    }

    @Test
    fun escenario5_altaVehiculo_usuarioNoAutenticado_fallaConErrorAutenticacion() = runBlocking {
        val usuarioNoAutenticado = ""

        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioNoAutenticado,
            nombre = "Peugeot 208",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 5.0,
        )

        assertTrue(resultado.isFailure)
        val excepcion = resultado.exceptionOrNull()
        assertTrue(excepcion is UsuarioNoAutenticadoException)
        assertTrue(repository.vehiculos.isEmpty())
    }
}
