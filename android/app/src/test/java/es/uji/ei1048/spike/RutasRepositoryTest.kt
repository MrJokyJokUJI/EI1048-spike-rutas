package es.uji.ei1048.spike

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del repositorio con un cliente Supabase apuntando a un MockEngine de Ktor,
// simulando las respuestas de PostgREST (la API REST autogenerada de Supabase).
class RutasRepositoryTest {

    private fun cliente(respuestaSelect: String): Pair<SupabaseClient, MockEngine> {
        val motor = MockEngine { request ->
            val camino = request.url.encodedPath
            when {
                camino == "/rest/v1/ruta_guardada" && request.method == HttpMethod.Get ->
                    respond(respuestaSelect, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                camino == "/rest/v1/ruta_guardada" && request.method == HttpMethod.Post ->
                    respond("""[{"id":"nueva"}]""", HttpStatusCode.Created, headersOf(HttpHeaders.ContentType, "application/json"))
                else -> error("Petición inesperada: ${request.method} $camino")
            }
        }
        val client = createSupabaseClient("http://localhost", "anon-key") {
            defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true; explicitNulls = false })
            install(Postgrest)
            httpEngine = motor
        }
        return client to motor
    }

    @Test
    fun listarDecodificaLasRutasGuardadas() = runBlocking {
        val (client, motor) = cliente("""[{"id":"a1","origen":"Castellón","destino":"Valencia","modo":"driving-car","distancia_km":74.3,"duracion_min":58.0}]""")
        val repo = RutasRepository(client)

        val rutas = repo.listar()

        assertEquals(1, rutas.size)
        assertEquals("Castellón", rutas[0].origen)
        assertEquals(74.3, rutas[0].distanciaKm, 0.001)

        val peticion = motor.requestHistory.single()
        assertEquals(HttpMethod.Get, peticion.method)
        assertEquals("/rest/v1/ruta_guardada", peticion.url.encodedPath)
    }

    @Test
    fun guardarEnviaPostALaTabla() = runBlocking {
        val (client, motor) = cliente("[]")
        val repo = RutasRepository(client)

        repo.guardar(RutaGuardada(origen = "Castellón", destino = "Valencia", modo = "driving-car", distanciaKm = 74.3, duracionMin = 58.0))

        val peticion = motor.requestHistory.single()
        assertEquals(HttpMethod.Post, peticion.method)
        assertEquals("/rest/v1/ruta_guardada", peticion.url.encodedPath)
        // Debe pedir que las columnas omitidas usen su valor por defecto (auth.uid()),
        // si no, PostgREST las pone a NULL y la política RLS rechaza la fila.
        val prefer = peticion.headers["Prefer"].orEmpty()
        assertTrue(prefer.contains("missing=default"))
    }
}
