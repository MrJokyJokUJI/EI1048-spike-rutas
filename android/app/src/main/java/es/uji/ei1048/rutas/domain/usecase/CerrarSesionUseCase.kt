package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.SinSesionActivaException
import es.uji.ei1048.rutas.domain.repository.AuthRepository

/**
 * HU03: cierre de sesión.
 *
 * Si no hay ninguna sesión activa se rechaza con [SinSesionActivaException]
 * sin llamar al backend.
 */
class CerrarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.sesionActual() ?: throw SinSesionActivaException()
        authRepository.cerrarSesion()
    }
}
