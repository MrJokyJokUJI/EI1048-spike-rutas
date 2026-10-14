package es.uji.ei1048.spike

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from

class RutasRepository(private val client: SupabaseClient) {

    suspend fun registrar(email: String, password: String): UserInfo? =
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

    suspend fun iniciarSesion(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun cerrarSesion() {
        client.auth.signOut()
    }

    // Espera a que se cargue la sesión persistida (HU48), si existe.
    suspend fun cargarSesion() {
        client.auth.awaitInitialization()
    }

    fun usuarioActual(): UserInfo? = client.auth.currentUserOrNull()

    // Diagnóstico: rol del token que se envía a PostgREST ("authenticated" = usuario, "anon" = sin sesión).
    fun rolDelToken(): String {
        val token = client.auth.currentAccessTokenOrNull() ?: return "sin token"
        return try {
            val payload = token.split(".")[1]
            val json = String(java.util.Base64.getUrlDecoder().decode(payload))
            org.json.JSONObject(json).optString("role", "?")
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }

    suspend fun guardar(ruta: RutaGuardada) {
        // Enviamos usuario_id explícitamente con el id de la sesión para que la política
        // RLS (auth.uid() = usuario_id) se cumpla sin depender del valor por defecto.
        // defaultToNull = false hace que id y creada_en usen el valor por defecto de la BD.
        val usuarioId = client.pluginManager.getPluginOrNull(Auth)?.currentUserOrNull()?.id
        val fila = if (usuarioId != null) ruta.copy(usuarioId = usuarioId) else ruta
        client.from("ruta_guardada").insert(fila) {
            defaultToNull = false
        }
    }

    suspend fun listar(): List<RutaGuardada> =
        client.from("ruta_guardada").select().decodeList()
}
