package es.uji.ei1048.spike

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

object SupabaseClientProvider {

    // engine es opcional: sirve para inyectar un MockEngine en los tests.
    fun build(url: String, key: String, engine: HttpClientEngine? = null): SupabaseClient =
        createSupabaseClient(url, key) {
            defaultSerializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false // omite los nulos al insertar: la BD rellena id/usuario_id/creada_en
                }
            )
            install(Auth)
            install(Postgrest)
            if (engine != null) {
                httpEngine = engine
            }
        }
}
