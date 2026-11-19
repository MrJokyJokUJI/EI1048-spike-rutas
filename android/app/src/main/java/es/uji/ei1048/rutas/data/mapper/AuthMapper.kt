package es.uji.ei1048.rutas.data.mapper

import es.uji.ei1048.rutas.data.remote.supabase.dto.AuthRespuestaDto
import es.uji.ei1048.rutas.domain.model.AuthException
import es.uji.ei1048.rutas.domain.model.Sesion

/**
 * Patrón Mapper: convierte la respuesta de Supabase Auth al modelo de dominio.
 */
fun AuthRespuestaDto.toDomain(emailSolicitado: String): Sesion {
    val token = accessToken ?: throw AuthException(
        "Supabase no devolvió sesión. Desactiva la confirmación de email en el proyecto de pruebas."
    )
    return Sesion(
        usuarioId = usuario?.id.orEmpty(),
        email = usuario?.email ?: emailSolicitado,
        token = token,
    )
}
