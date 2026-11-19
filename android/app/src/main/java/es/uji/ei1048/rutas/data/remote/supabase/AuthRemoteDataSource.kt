package es.uji.ei1048.rutas.data.remote.supabase

import es.uji.ei1048.rutas.BuildConfig
import es.uji.ei1048.rutas.data.remote.supabase.dto.AuthErrorDto
import es.uji.ei1048.rutas.data.remote.supabase.dto.AuthRespuestaDto
import es.uji.ei1048.rutas.domain.model.AuthException
import es.uji.ei1048.rutas.domain.model.EmailYaRegistradoException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Patrón Adapter: adapta Supabase Auth (GoTrue) al puerto [AuthRepository].
 *
 * Usa OkHttp y kotlinx.serialization, igual que el resto de DataSources del
 * proyecto (ver rama h-11). Todas las operaciones son suspend y hacen I/O en
 * Dispatchers.IO.
 */
class AuthRemoteDataSource(
    private val client: OkHttpClient = OkHttpClient(),
    private val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    private val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun registrar(email: String, password: String): AuthRespuestaDto = withContext(Dispatchers.IO) {
        ejecutar(
            peticionBase("/auth/v1/signup")
                .post(cuerpo(email, password))
                .build()
        )
    }

    suspend fun iniciarSesion(email: String, password: String): AuthRespuestaDto = withContext(Dispatchers.IO) {
        ejecutar(
            peticionBase("/auth/v1/token?grant_type=password")
                .post(cuerpo(email, password))
                .build()
        )
    }

    suspend fun cerrarSesion(token: String) = withContext(Dispatchers.IO) {
        comprobarConfiguracion()
        val request = peticionBase("/auth/v1/logout")
            .header("Authorization", "Bearer $token")
            .post("{}".toRequestBody(jsonMediaType))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw AuthException("Error al cerrar sesión en Supabase (${response.code})")
            }
        }
    }

    private fun peticionBase(path: String): Request.Builder {
        comprobarConfiguracion()
        return Request.Builder()
            .url(supabaseUrl.trimEnd('/') + path)
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .addHeader("Accept", "application/json")
    }

    private fun cuerpo(email: String, password: String) =
        json.encodeToString(mapOf("email" to email, "password" to password))
            .toRequestBody(jsonMediaType)

    private fun ejecutar(request: Request): AuthRespuestaDto {
        comprobarConfiguracion()
        client.newCall(request).execute().use { response ->
            val cuerpoRespuesta = response.body?.string().orEmpty()
            if (!response.isSuccessful) lanzarError(cuerpoRespuesta, response.code)
            if (cuerpoRespuesta.isBlank()) return AuthRespuestaDto()
            return runCatching { json.decodeFromString<AuthRespuestaDto>(cuerpoRespuesta) }
                .getOrElse { throw AuthException("Respuesta de Supabase no válida: ${it.message}") }
        }
    }

    private fun lanzarError(cuerpo: String, codigo: Int): Nothing {
        val error = runCatching { json.decodeFromString<AuthErrorDto>(cuerpo) }.getOrNull()
        val mensaje = listOfNotNull(error?.msg, error?.message, error?.errorDescription)
            .firstOrNull { it.isNotBlank() }
            ?: cuerpo.ifBlank { "sin detalle" }

        if (error?.code == "23505" || mensaje.contains("already registered", ignoreCase = true)) {
            throw EmailYaRegistradoException()
        }
        throw AuthException("Error en Supabase Auth ($codigo): $mensaje")
    }

    private fun comprobarConfiguracion() {
        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            throw AuthException("Faltan supabase.url y supabase.anon-key en android/local.properties")
        }
    }
}
