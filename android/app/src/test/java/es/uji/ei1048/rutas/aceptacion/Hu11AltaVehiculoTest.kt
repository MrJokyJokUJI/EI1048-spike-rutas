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
 * Test de aceptación para HU11 - Alta de vehículo (ATDD).
 *
 * Implementa exclusivamente los 3 escenarios especificados en la tabla de aceptación:
 * - E1: Servidor = Sí | Coche no registrado = Sí -> Registro con éxito = Sí
 * - E2: Servidor = Sí | Coche no registrado = No -> Registro con éxito = No
 * - E3: Servidor = No | Coche no registrado = Sí -> Registro con éxito = No
 */
@RunWith(AndroidJUnit4::class)
class Hu11AltaVehiculoTest {

    // Repositorio en memoria que actúa como SUT simulando el estado del servidor y los datos
    private class SUTFakeVehiculosRepository : VehiculosRepository {
        val vehiculos = mutableListOf<Vehiculo>()
        var servidorDisponible: Boolean = true

        override suspend fun altaVehiculo(vehiculo: Vehiculo): Result<Vehiculo> {
            // Simulación de caída de servidor (Escenario E3)
            if (!servidorDisponible) {
                return Result.failure(IOException("Servidor no disponible"))
            }

            // Simulación de vehículo ya registrado para ese usuario (Escenario E2)
            val yaRegistrado = vehiculos.any {
                it.usuarioId == vehiculo.usuarioId && it.nombre.equals(vehiculo.nombre, ignoreCase = true)
            }
            if (yaRegistrado) {
                return Result.failure(IllegalStateException("El vehículo ya está registrado"))
            }

            // Registro exitoso (Escenario E1)
            vehiculos.add(vehiculo)
            return Result.success(vehiculo)
        }

        override suspend fun consultarVehiculos(usuarioId: String): Result<List<Vehiculo>> {
            if (!servidorDisponible) {
                return Result.failure(IOException("Servidor no disponible"))
            }
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
    fun e1_servidorDisponible_cocheNoRegistrado_registroConExito() = runBlocking {
        // Dado que el servidor está operativo y el coche no está registrado previamente
        repository.servidorDisponible = true
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

        // Entonces el registro se realiza con éxito
        assertTrue("El registro debe ser exitoso", resultado.isSuccess)
        val vehiculo = resultado.getOrThrow()
        assertNotNull(vehiculo.id)
        assertEquals(usuarioAutenticadoId, vehiculo.usuarioId)
        assertEquals(nombre, vehiculo.nombre)
        assertEquals(tipo, vehiculo.tipoEnergia)
        assertEquals(consumo, vehiculo.consumo, 0.001)
        assertEquals(1, repository.vehiculos.size)
    }

    @Test
    fun e2_servidorDisponible_cocheYaRegistrado_registroFallido() = runBlocking {
        // Dado que el servidor está operativo pero el coche YA está registrado
        repository.servidorDisponible = true
        val nombre = "Seat León 1.5 TSI"

        // Precondición: registramos el vehículo previamente en el sistema
        altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 6.2,
        )
        assertEquals(1, repository.vehiculos.size)

        // Cuando se intenta dar de alta el mismo vehículo de nuevo
        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = nombre,
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 6.2,
        )

        // Entonces el registro falla y no se duplica en el sistema
        assertTrue("El registro debe fallar porque el coche ya existe", resultado.isFailure)
        assertEquals(1, repository.vehiculos.size)
    }

    @Test
    fun e3_servidorNoDisponible_cocheNoRegistrado_registroFallido() = runBlocking {
        // Dado que el servidor NO está disponible y el coche no está registrado
        repository.servidorDisponible = false

        // Cuando el usuario intenta dar de alta el vehículo
        val resultado = altaVehiculoUseCase(
            usuarioId = usuarioAutenticadoId,
            nombre = "Tesla Model 3",
            tipoEnergia = TipoEnergia.ELECTRICO,
            consumo = 14.8,
        )

        // Entonces el registro falla y no se guarda nada
        assertTrue("El registro debe fallar al no haber conexión con el servidor", resultado.isFailure)
        assertTrue(repository.vehiculos.isEmpty())
    }
}
