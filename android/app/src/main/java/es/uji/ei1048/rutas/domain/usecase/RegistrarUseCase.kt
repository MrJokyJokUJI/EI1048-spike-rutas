package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.FormatoEmailInvalidoException
import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository

/**
 * HU01: registro de un usuario no registrado.
 *
 * Caso de uso (patrón Command): valida la entrada y delega el registro en el
 * puerto [AuthRepository]. La ViewModel nunca accede al repositorio directamente.
 */
class RegistrarUseCase(
    private val authRepository: AuthRepository,
) {

    suspend operator fun invoke(email: String, password: String): Sesion {
        if (!EMAIL_REGEX.matches(email.trim())) {
            throw FormatoEmailInvalidoException()
        }
        return authRepository.registrar(email.trim(), password)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    }
}
