package es.uji.ei1048.rutas.data.remote.supabase

import es.uji.ei1048.rutas.data.remote.supabase.dto.VehiculoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Patrón Adapter: Conecta y adapta las operaciones de vehículos a la API PostgREST de Supabase.
 *
 * Utiliza OkHttp y kotlinx.serialization para comunicación HTTP directa con la tabla `vehiculo`.
 * Si no hay URL configurada (modo spike/offline), opera en memoria de forma segura.
 */
class VehiculosRemoteDataSource(
    private val client: OkHttpClient = OkHttpClient(),
    private val supabaseUrl: String = "",
    private val supabaseAnonKey: String = "",
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Almacén en memoria para modo desarrollo/offline o fallback
    private val cacheLocal = ConcurrentHashMap<String, VehiculoDto>()

    suspend fun altaVehiculo(
        dto: VehiculoDto,
        authToken: String? = null,
    ): VehiculoDto = withContext(Dispatchers.IO) {
        val vehiculoId = dto.id ?: UUID.randomUUID().toString()
        val dtoConId = dto.copy(id = vehiculoId)

        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            cacheLocal[vehiculoId] = dtoConId
            return@withContext dtoConId
        }

        val url = "${supabaseUrl.trimEnd('/')}/rest/v1/vehiculo"
        val token = authToken ?: supabaseAnonKey
        val bodyString = json.encodeToString(dtoConId)
        val body = bodyString.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Error en Supabase PostgREST (${response.code}): ${response.body?.string()}")
                }
                val responseBody = response.body?.string().orEmpty()
                val parsedList = runCatching {
                    json.decodeFromString<List<VehiculoDto>>(responseBody)
                }.getOrNull()

                val resultDto = parsedList?.firstOrNull() ?: dtoConId
                cacheLocal[resultDto.id ?: vehiculoId] = resultDto
                resultDto
            }
        } catch (e: Exception) {
            // Si la conexión falla, se mantiene en memoria para no romper la experiencia de desarrollo
            cacheLocal[vehiculoId] = dtoConId
            dtoConId
        }
    }

    suspend fun consultarVehiculos(
        usuarioId: String,
        authToken: String? = null,
    ): List<VehiculoDto> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            return@withContext cacheLocal.values.filter { it.usuarioId == usuarioId }
        }

        val url = "${supabaseUrl.trimEnd('/')}/rest/v1/vehiculo?usuario_id=eq.$usuarioId"
        val token = authToken ?: supabaseAnonKey

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext cacheLocal.values.filter { it.usuarioId == usuarioId }
                }
                val responseBody = response.body?.string().orEmpty()
                val lista = json.decodeFromString<List<VehiculoDto>>(responseBody)
                lista.forEach { it.id?.let { id -> cacheLocal[id] = it } }
                lista
            }
        } catch (_: Exception) {
            cacheLocal.values.filter { it.usuarioId == usuarioId }
        }
    }
}
