package es.uji.ei1048.spike

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Pruebas del caso de uso con el servidor de openrouteservice simulado (MockWebServer),
// usando respuestas con el mismo formato que la API real.
class OrsClientTest {

    private lateinit var servidor: MockWebServer
    private lateinit var cliente: OrsClient

    @Before
    fun setUp() {
        servidor = MockWebServer()
        servidor.start()
        cliente = OrsClient("clave-test", servidor.url("/"))
    }

    @After
    fun tearDown() {
        servidor.shutdown()
    }

    private fun responder(json: String) = servidor.enqueue(MockResponse().setBody(json))

    @Test
    fun rutaEntreDosLugaresValidos() {
        // GIVEN Castellón y Valencia existen y hay ruta en coche entre ellos
        responder("""{"features":[{"geometry":{"coordinates":[-0.0499,39.9864]}}]}""")
        responder("""{"features":[{"geometry":{"coordinates":[-0.3763,39.4699]}}]}""")
        responder("""{"features":[{"properties":{"summary":{"distance":74300.0,"duration":3480.0}},
            "geometry":{"coordinates":[[-0.0499,39.9864],[-0.2,39.7],[-0.3763,39.4699]]}}]}""")

        // WHEN calculo la ruta
        val ruta = cliente.calcularRuta("Castellón", "Valencia", "driving-car")

        // THEN obtengo distancia, duración y el trayecto en (latitud, longitud)
        assertEquals(74.3, ruta.distanciaKm, 0.01)
        assertEquals(58.0, ruta.duracionMin, 0.01)
        assertEquals(3, ruta.puntos.size)
        assertEquals(39.9864, ruta.puntos[0].first, 0.0001)
        servidor.takeRequest()
        servidor.takeRequest()
        assertEquals("/v2/directions/driving-car", servidor.takeRequest().requestUrl!!.encodedPath)
    }

    @Test(expected = LugarNoEncontradoException::class)
    fun rutaConLugarInexistente() {
        // GIVEN el destino no existe
        responder("""{"features":[{"geometry":{"coordinates":[-0.0499,39.9864]}}]}""")
        responder("""{"features":[]}""")

        // WHEN calculo la ruta THEN salta LugarNoEncontradoException
        cliente.calcularRuta("Castellón", "Xyzwq", "driving-car")
    }
}
