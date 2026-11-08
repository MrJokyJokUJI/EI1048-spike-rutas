package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.repository.AuthRepository

/**
 * HU03: cierre de sesión.
 */
class CerrarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        TODO("Not implemented")
    }
}
