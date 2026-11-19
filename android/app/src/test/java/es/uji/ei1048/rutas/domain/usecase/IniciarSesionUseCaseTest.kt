package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.CredencialesInvalidasException
import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba de integración de HU02: el caso de uso con el puerto [AuthRepository]
 * sustituido por un fake manual (convención del proyecto, ver ARQUITECTURA.md).
 */
class IniciarSesionUseCaseTest {

    private class FakeAuthRepository : AuthRepository {
        private val usuarios = mutableMapOf("pepito@email.com" to "pepitoPassword")
        var sesion: Sesion? = null
        var llamadasInicio = 0

        override suspend fun registrar(email: String, password: String): Sesion {
            usuarios[email] = password
            return Sesion(usuarioId = "id-$email", email = email, token = "token-$email")
        }

        override suspend fun iniciarSesion(email: String, password: String): Sesion {
            llamadasInicio++
            if (usuarios[email] != password) {
                sesion = null
                throw CredencialesInvalidasException()
            }
            return Sesion(usuarioId = "id-$email", email = email, token = "token-$email")
                .also { sesion = it }
        }

        override suspend fun cerrarSesion() {
            sesion = null
        }

        override suspend fun sesionActual(): Sesion? = sesion
    }

    @Test
    fun invoke_credencialesCorrectas_devuelveSesionActiva() = runTest {
        val repositorio = FakeAuthRepository()

        val sesion = IniciarSesionUseCase(repositorio)("pepito@email.com", "pepitoPassword")

        assertEquals("pepito@email.com", sesion.email)
        assertEquals(sesion, repositorio.sesionActual())
    }

    @Test
    fun invoke_contrasenaIncorrecta_lanzaCredencialesInvalidasSinSesion() = runTest {
        val repositorio = FakeAuthRepository()

        val resultado = runCatching { IniciarSesionUseCase(repositorio)("pepito@email.com", "otraPassword") }

        assertTrue(resultado.exceptionOrNull() is CredencialesInvalidasException)
        assertNull(repositorio.sesionActual())
    }

    @Test
    fun invoke_emailConEspacios_iniciaSesionConEmailLimpio() = runTest {
        val repositorio = FakeAuthRepository()

        val sesion = IniciarSesionUseCase(repositorio)("  pepito@email.com ", "pepitoPassword")

        assertEquals("pepito@email.com", sesion.email)
    }

    @Test
    fun invoke_emailVacio_rechazaSinLlamarAlRepositorio() = runTest {
        val repositorio = FakeAuthRepository()

        val resultado = runCatching { IniciarSesionUseCase(repositorio)("", "pepitoPassword") }

        assertTrue(resultado.exceptionOrNull() is CredencialesInvalidasException)
        assertEquals(0, repositorio.llamadasInicio)
    }
}
