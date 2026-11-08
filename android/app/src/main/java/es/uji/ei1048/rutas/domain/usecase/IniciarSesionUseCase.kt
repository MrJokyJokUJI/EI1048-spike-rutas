package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository

/**
 * HU02: inicio de sesión de un usuario registrado.
 */
class IniciarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Sesion {
        TODO("Not implemented")
    }
}
