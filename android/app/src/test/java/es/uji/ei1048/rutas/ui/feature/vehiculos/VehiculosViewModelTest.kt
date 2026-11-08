package es.uji.ei1048.rutas.ui.feature.vehiculos

import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.Vehiculo
import es.uji.ei1048.rutas.domain.repository.VehiculosRepository
import es.uji.ei1048.rutas.domain.usecase.AltaVehiculoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehiculosViewModelTest {

    private class FakeVehiculosRepository : VehiculosRepository {
        var ultimoVehiculo: Vehiculo? = null

        override suspend fun altaVehiculo(vehiculo: Vehiculo): Result<Vehiculo> {
            ultimoVehiculo = vehiculo
            return Result.success(vehiculo)
        }

        override suspend fun consultarVehiculos(usuarioId: String): Result<List<Vehiculo>> {
            return Result.success(listOfNotNull(ultimoVehiculo))
        }
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeVehiculosRepository
    private lateinit var useCase: AltaVehiculoUseCase
    private lateinit var viewModel: VehiculosViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeVehiculosRepository()
        useCase = AltaVehiculoUseCase(repository)
        viewModel = VehiculosViewModel(
            altaVehiculoUseCase = useCase,
            obtenerUsuarioIdActual = { "usuario-test" },
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `al cambiar nombre se actualiza el estado`() {
        viewModel.onEvent(VehiculosEvent.NombreCambiado("Toyota Corolla"))
        assertEquals("Toyota Corolla", viewModel.state.value.nombre)
    }

    @Test
    fun `al cambiar tipo de energia se actualiza el estado`() {
        viewModel.onEvent(VehiculosEvent.TipoEnergiaCambiado(TipoEnergia.ELECTRICO))
        assertEquals(TipoEnergia.ELECTRICO, viewModel.state.value.tipoEnergia)
    }

    @Test
    fun `al cambiar consumo se actualiza el estado`() {
        viewModel.onEvent(VehiculosEvent.ConsumoCambiado("5.4"))
        assertEquals("5.4", viewModel.state.value.consumo)
    }

    @Test
    fun `guardar vehiculo con datos validos completa correctamente`() = runTest(testDispatcher) {
        viewModel.onEvent(VehiculosEvent.NombreCambiado("Toyota Corolla"))
        viewModel.onEvent(VehiculosEvent.TipoEnergiaCambiado(TipoEnergia.COMBUSTIBLE))
        viewModel.onEvent(VehiculosEvent.ConsumoCambiado("5.4"))

        viewModel.onEvent(VehiculosEvent.GuardarVehiculo)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.error)
        assertNotNull(state.vehiculoCreado)
        assertEquals("Toyota Corolla", state.vehiculoCreado?.nombre)
        assertNotNull(state.mensajeExito)
        assertEquals("", state.nombre)
        assertEquals("", state.consumo)
    }

    @Test
    fun `guardar vehiculo con consumo invalido asigna mensaje de error`() = runTest(testDispatcher) {
        viewModel.onEvent(VehiculosEvent.NombreCambiado("Toyota Corolla"))
        viewModel.onEvent(VehiculosEvent.ConsumoCambiado("-1"))

        viewModel.onEvent(VehiculosEvent.GuardarVehiculo)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.error)
        assertNull(state.vehiculoCreado)
    }
}
