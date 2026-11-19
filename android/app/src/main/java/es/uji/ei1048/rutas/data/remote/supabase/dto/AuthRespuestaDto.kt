package es.uji.ei1048.rutas.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de las respuestas de Supabase Auth (signup / token).
 *
 * Los nombres snake_case se mapean con @SerialName (ver ARQUITECTURA.md).
 */
@Serializable
data class AuthRespuestaDto(
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("token_type")
    val tokenType: String? = null,
    @SerialName("expires_in")
    val expiresIn: Long? = null,
    @SerialName("user")
    val usuario: UsuarioAuthDto? = null,
)

@Serializable
data class UsuarioAuthDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String? = null,
)

/** Cuerpo de error devuelto por GoTrue (Supabase Auth). */
@Serializable
data class AuthErrorDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
)
