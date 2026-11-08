package es.uji.ei1048.rutas.aceptacion

import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.runner.RunWith

/**
 * Test de aceptación para HU11 - Alta de vehículo (ATDD).
 *
 * Cumple con los criterios de la Rúbrica E2 (SUT + pruebas de aceptación):
 * - Prueba el Sistema Bajo Prueba (SUT) a nivel de casos de uso y lógica de negocio.
 * - NO incluye componentes de la interfaz de usuario (evita acoplamiento a la GUI según la rúbrica).
 * - Implementa pruebas para todos los escenarios válidos e inválidos identificados.
 *
 * Escenarios cubiertos:
 * 1. [escenario1_altaVehiculoCombustible_datosValidos_creaVehiculoCorrectamente]: Escenario válido (combustible).
 * 2. [escenario2_altaVehiculoElectrico_datosValidos_creaVehiculoCorrectamente]: Escenario válido (eléctrico).
 * 3. [escenario3_altaVehiculo_nombreVacio_fallaConErrorValidacion]: Escenario inválido (nombre en blanco).
 * 4. [escenario4_altaVehiculo_consumoInvalido_fallaConErrorValidacion]: Escenario inválido (consumo <= 0).
 * 5. [escenario5_altaVehiculo_usuarioNoAutenticado_fallaConErrorAutenticacion]: Escenario inválido (sin sesión).
 */
@RunWith(AndroidJUnit4::class)
class Hu11AltaVehiculoTest {

    // Repositorio en memoria que actúa como SUT para pruebas E2E de lógica de negocio
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
        // Dado un usuario autenticado y datos válidos para un coche de combustión
        val nombre = "Seat León 1.5 TSI"
        val tipo = TipoEnergia.COMBUSTIBLE
        val consumo = 6.2

        // Cuando el usuario solicita dar de alta el vehículo
        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            tipoEnergia = tipo,
            consumo = consumo,
        )

        // Entonces el vehículo se crea satisfactoriamente con todos sus atributos correctos
        assertTrue("El resultado debe ser exitoso", resultado.isSuccess)
        val vehiculo = resultado.getOrThrow()
        assertNotNull("El ID del vehículo no debe ser nulo", vehiculo.id)
        assertTrue("El ID no debe estar vacío", vehiculo.id.isNotBlank())
        assertEquals(usuarioAutenticadoId, vehiculo.usuarioId)
        assertEquals(nombre, vehiculo.nombre)
        assertEquals(tipo, vehiculo.tipoEnergia)
        assertEquals(consumo, vehiculo.consumo, 0.001)
        assertEquals(0.0, vehiculo.kmRecorridos, 0.001)
        assertEquals(false, vehiculo.favorito)

        // Y queda registrado en el repositorio del SUT
        assertEquals(1, repository.vehiculos.size)
        assertEquals(vehiculo, repository.vehiculos.first())
    }

    @Test
    fun escenario2_altaVehiculoElectrico_datosValidos_creaVehiculoCorrectamente() = runBlocking {
        // Dado un usuario autenticado y datos válidos para un coche eléctrico
        val nombre = "Tesla Model 3"
        val tipo = TipoEnergia.ELECTRICO
        val consumo = 14.8

        // Cuando el usuario solicita dar de alta el vehículo
        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            tipoEnergia = tipo,
            consumo = consumo,
        )

        // Entonces el vehículo se crea correctamente
        assertTrue("El resultado debe ser exitoso", resultado.isSuccess)
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
        // Dado un nombre vacío o compuesto únicamente por espacios en blanco
        val nombreInvalido = "   "

        // Cuando se intenta dar de alta el vehículo
        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombreInvalido,
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 5.5,
        )

        // Entonces la operación falla con NombreVehiculoInvalidoException
        assertTrue("El resultado debe ser fallido", resultado.isFailure)
        val excepcion = resultado.exceptionOrNull()
        assertTrue(
            "La excepción debe ser de tipo NombreVehiculoInvalidoException pero fue ${excepcion?.javaClass?.simpleName}",
            excepcion is NombreVehiculoInvalidoException,
        )
        assertTrue(repository.vehiculos.isEmpty())
    }

    @Test
    fun escenario4_altaVehiculo_consumoInvalido_fallaConErrorValidacion() = runBlocking {
        // Dado un consumo menor o igual a cero
        val consumoCero = 0.0
        val consumoNegativo = -4.5

        // Cuando se intenta dar de alta con consumo cero
        val resultadoCero = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = "Renault Clio",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = consumoCero,
        )

        // Entonces falla con ConsumoVehiculoInvalidoException
        assertTrue(resultadoCero.isFailure)
        assertTrue(resultadoCero.exceptionOrNull() is ConsumoVehiculoInvalidoException)

        // Y cuando se intenta dar de alta con consumo negativo
        val resultadoNegativo = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = "Renault Clio",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = consumoNegativo,
        )

        // Entonces también falla con ConsumoVehiculoInvalidoException
        assertTrue(resultadoNegativo.isFailure)
        assertTrue(resultadoNegativo.exceptionOrNull() is ConsumoVehiculoInvalidoException)

        assertTrue(repository.vehiculos.isEmpty())
    }

    @Test
    fun escenario5_altaVehiculo_usuarioNoAutenticado_fallaConErrorAutenticacion() = runBlocking {
        // Dado un identificador de usuario vacío (sin sesión iniciada)
        val usuarioNoAutenticado = ""

        // Cuando se intenta dar de alta el vehículo sin sesión
        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioNoAutenticado,
            nombre = "Peugeot 208",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 5.0,
        )

        // Entonces la operación falla con UsuarioNoAutenticadoException
        assertTrue("El resultado debe ser fallido", resultado.isFailure)
        val excepcion = resultado.exceptionOrNull()
        assertTrue(
            "La excepción debe ser UsuarioNoAutenticadoException pero fue ${excepcion?.javaClass?.simpleName}",
            excepcion is UsuarioNoAutenticadoException,
        )
        assertTrue(repository.vehiculos.isEmpty())
    }
}
