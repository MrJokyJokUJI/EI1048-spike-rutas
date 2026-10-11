package es.uji.ei1048.spike

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Resultado de una ruta: distancia en km, duración en minutos y puntos (latitud, longitud)
data class Ruta(val distanciaKm: Double, val duracionMin: Double, val puntos: List<Pair<Double, Double>>)

class LugarNoEncontradoException(lugar: String) : Exception("No se ha encontrado el lugar: $lugar")

// Cliente de openrouteservice: geocoding de un topónimo y cálculo de ruta entre dos puntos.
class OrsClient(
    private val apiKey: String,
    private val baseUrl: HttpUrl = "https://api.openrouteservice.org".toHttpUrl(),
    private val http: OkHttpClient = OkHttpClient()
) {

    private fun get(url: HttpUrl): JSONObject {
        val respuesta = http.newCall(Request.Builder().url(url).build()).execute()
        respuesta.use { return JSONObject(it.body!!.string()) }
    }

    // Devuelve (longitud, latitud) del primer resultado
    fun geocodificar(lugar: String): Pair<Double, Double> {
        val url = baseUrl.newBuilder().addPathSegments("geocode/search")
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("text", lugar)
            .addQueryParameter("size", "1")
            .addQueryParameter("boundary.country", "ES")
            .build()
        val features = get(url).getJSONArray("features")
        if (features.length() == 0) throw LugarNoEncontradoException(lugar)
        val c = features.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")
        return Pair(c.getDouble(0), c.getDouble(1))
    }

    // modo: driving-car, cycling-regular o foot-walking
    fun calcularRuta(origen: String, destino: String, modo: String): Ruta {
        val inicio = geocodificar(origen)
        val fin = geocodificar(destino)
        val url = baseUrl.newBuilder().addPathSegments("v2/directions/$modo")
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("start", "${inicio.first},${inicio.second}")
            .addQueryParameter("end", "${fin.first},${fin.second}")
            .build()

        val feature = get(url).getJSONArray("features").getJSONObject(0)
        val resumen = feature.getJSONObject("properties").getJSONObject("summary")
        val coords = feature.getJSONObject("geometry").getJSONArray("coordinates")
        val puntos = (0 until coords.length()).map {
            val p = coords.getJSONArray(it)
            Pair(p.getDouble(1), p.getDouble(0))
        }
        return Ruta(resumen.getDouble("distance") / 1000, resumen.getDouble("duration") / 60, puntos)
    }
}
