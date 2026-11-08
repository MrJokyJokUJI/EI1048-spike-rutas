package es.uji.ei1048.rutas.data.repository

import es.uji.ei1048.rutas.data.remote.supabase.VehiculosRemoteDataSource
import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.Vehiculo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VehiculosRepositoryImplTest {

    private lateinit var remoteDataSource: VehiculosRemoteDataSource
    private lateinit var repository: VehiculosRepositoryImpl

    @Before
    fun setUp() {
        // En modo offline/testing sin URLs externas usa el almacén seguro en memoria
        remoteDataSource = VehiculosRemoteDataSource()
        repository = VehiculosRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `altaVehiculo y consultarVehiculos persisten y recuperan vehiculos correctamente`() = runBlocking {
        val vehiculo = Vehiculo(
            id = "veh-test-1",
            usuarioId = "usuario-repo-test",
            nombre = "Seat Arona",
            tipoEnergia = TipoEnergia.COMBUSTIBLE,
            consumo = 6.1,
        )

        val resultadoAlta = repository.altaVehiculo(vehiculo)
        assertTrue(resultadoAlta.isSuccess)

        val resultadoConsulta = repository.consultarVehiculos("usuario-repo-test")
        assertTrue(resultadoConsulta.isSuccess)
        val lista = resultadoConsulta.getOrThrow()
        assertEquals(1, lista.size)
        assertEquals("Seat Arona", lista.first().nombre)
        assertEquals(TipoEnergia.COMBUSTIBLE, lista.first().tipoEnergia)
        assertEquals(6.1, lista.first().consumo, 0.001)
    }
}
