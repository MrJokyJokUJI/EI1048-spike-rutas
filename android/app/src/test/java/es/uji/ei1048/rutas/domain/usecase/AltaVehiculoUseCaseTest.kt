package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.ConsumoVehiculoInvalidoException
import es.uji.ei1048.rutas.domain.model.NombreVehiculoInvalidoException
import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.UsuarioNoAutenticadoException
import es.uji.ei1048.rutas.domain.model.Vehiculo
import es.uji.ei1048.rutas.domain.repository.VehiculosRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AltaVehiculoUseCaseTest {

    private class FakeVehiculosRepository : VehiculosRepository {
        var ultimoVehiculoGuardado: Vehiculo? = null
        var forzarError: Throwable? = null

        override suspend fun altaVehiculo(vehiculo: Vehiculo): Result<Vehiculo> {
            forzarError?.let { return Result.failure(it) }
            ultimoVehiculoGuardado = vehiculo
            return Result.success(vehiculo)
        }

        override suspend fun consultarVehiculos(usuarioId: String): Result<List<Vehiculo>> {
            return Result.success(listOfNotNull(ultimoVehiculoGuardado))
        }
    }

    private lateinit var repository: FakeVehiculosRepository
    private lateinit var useCase: AltaVehiculoUseCase

    @Before
    fun setUp() {
        repository = FakeVehiculosRepository()
        useCase = AltaVehiculoUseCase(repository)
    }

    @Test
    fun `dar de alta vehiculo de combustible con datos validos retorna exito`() = runBlocking {
        val resultado = useCase(
            usuarioId = "user-123",
            nombre = "Ford Focus",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 6.0,
        )

        assertTrue(resultado.isSuccess)
        val vehiculo = resultado.getOrThrow()
        assertEquals("Ford Focus", vehiculo.nombre)
        assertEquals(TipoEnergia.COMBUSTIBLE, vehiculo.tipoEnergia)
        assertEquals(6.0, vehiculo.consumo, 0.001)
        assertEquals("user-123", vehiculo.usuarioId)
        assertEquals(0.0, vehiculo.kmRecorridos, 0.001)
        assertEquals(false, vehiculo.favorito)
    }

    @Test
    fun `dar de alta vehiculo electrico con datos validos retorna exito`() = runBlocking {
        val resultado = useCase(
            usuarioId = "user-123",
            nombre = "Hyundai Ioniq 5",
            tipoEnergia = TipoEnergia.ELECTRICO,
            consumo = 16.5,
        )

        assertTrue(resultado.isSuccess)
        val vehiculo = resultado.getOrThrow()
        assertEquals("Hyundai Ioniq 5", vehiculo.nombre)
        assertEquals(TipoEnergia.ELECTRICO, vehiculo.tipoEnergia)
        assertEquals(16.5, vehiculo.consumo, 0.001)
    }

    @Test
    fun `dar de alta con nombre vacio lanza NombreVehiculoInvalidoException`() = runBlocking {
        val resultado = useCase(
            usuarioId = "user-123",
            nombre = "",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 5.0,
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is NombreVehiculoInvalidoException)
    }

    @Test
    fun `dar de alta con consumo menor o igual a cero lanza ConsumoVehiculoInvalidoException`() = runBlocking {
        val resultado = useCase(
            usuarioId = "user-123",
            nombre = "Coche",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 0.0,
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is ConsumoVehiculoInvalidoException)
    }

    @Test
    fun `dar de alta sin usuario autenticado lanza UsuarioNoAutenticadoException`() = runBlocking {
        val resultado = useCase(
            usuarioId = "   ",
            nombre = "Coche",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 5.0,
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is UsuarioNoAutenticadoException)
    }
}
