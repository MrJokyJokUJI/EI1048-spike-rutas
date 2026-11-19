package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.CredencialesInvalidasException
import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository

/**
 * HU02: inicio de sesión de un usuario registrado.
 *
 * Si falta el email o la contraseña se rechaza sin llamar al backend. Cualquier
 * fallo de credenciales llega como [CredencialesInvalidasException], el mismo
 * error para contraseña incorrecta y para email no registrado.
 */
class IniciarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Sesion {
        val emailLimpio = email.trim()
        if (emailLimpio.isEmpty() || password.isEmpty()) {
            throw CredencialesInvalidasException()
        }
        return authRepository.iniciarSesion(emailLimpio, password)
    }
}
