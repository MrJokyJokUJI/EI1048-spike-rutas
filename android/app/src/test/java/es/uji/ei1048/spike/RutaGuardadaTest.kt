package es.uji.ei1048.spike

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutaGuardadaTest {

    // Misma configuración que usa el cliente (SupabaseClientProvider).
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun serializaConClavesSnakeCaseYOmiteNulos() {
        val ruta = RutaGuardada(origen = "Castellón", destino = "Valencia", modo = "driving-car", distanciaKm = 74.3, duracionMin = 58.0)
        val texto = json.encodeToString(ruta)

        assertTrue(texto.contains("\"distancia_km\":74.3"))
        assertTrue(texto.contains("\"duracion_min\":58.0"))
        // Los campos que rellena la BD no deben enviarse como null
        assertFalse(texto.contains("\"id\""))
        assertFalse(texto.contains("usuario_id"))
        assertFalse(texto.contains("creada_en"))
    }

    @Test
    fun incluyeUsuarioIdCuandoSeConoce() {
        val ruta = RutaGuardada(usuarioId = "u1", origen = "Castellón", destino = "Valencia", modo = "driving-car", distanciaKm = 74.3, duracionMin = 58.0)
        val texto = json.encodeToString(ruta)
        assertTrue(texto.contains("\"usuario_id\":\"u1\""))
    }

    @Test
    fun deserializaRespuestaDePostgrest() {
        val texto = """[{"id":"a1","usuario_id":"u1","origen":"Castellón","destino":"Valencia","modo":"driving-car","distancia_km":74.3,"duracion_min":58.0,"creada_en":"2026-10-15T00:00:00Z"}]"""
        val rutas = json.decodeFromString<List<RutaGuardada>>(texto)

        assertEquals(1, rutas.size)
        assertEquals("u1", rutas[0].usuarioId)
        assertEquals("Castellón", rutas[0].origen)
        assertEquals(74.3, rutas[0].distanciaKm, 0.001)
    }
}
