package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.CredencialesInvalidasException
import es.uji.ei1048.rutas.domain.model.EmailYaRegistradoException
import es.uji.ei1048.rutas.domain.model.FormatoEmailInvalidoException
import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * Tests unitarios del caso de uso de registro (HU01).
 *
 * No hay mocks: se usa un fake manual del puerto [AuthRepository] (ver ARQUITECTURA.md).
 */
class RegistrarUseCaseTest {

    private class FakeAuthRepository : AuthRepository {
        var sesion: Sesion? = null
        var llamadasRegistro = 0
        var emailYaRegistrado = false

        override suspend fun registrar(email: String, password: String): Sesion {
            llamadasRegistro++
            if (emailYaRegistrado) throw EmailYaRegistradoException()
            return Sesion(usuarioId = "id-1", email = email, token = "token-1")
                .also { sesion = it }
        }

        override suspend fun iniciarSesion(email: String, password: String): Sesion =
            sesion ?: throw CredencialesInvalidasException()

        override suspend fun cerrarSesion() {
            sesion = null
        }

        override suspend fun sesionActual(): Sesion? = sesion
    }

    @Test
    fun datosValidos_devuelveSesionYDelegaEnElRepositorio() = runTest {
        val repositorio = FakeAuthRepository()
        val casoUso = RegistrarUseCase(repositorio)

        val sesion = casoUso("pepito@email.com", "pepitoPassword")

        assertEquals("pepito@email.com", sesion.email)
        assertEquals("token-1", sesion.token)
        assertEquals(1, repositorio.llamadasRegistro)
    }

    @Test
    fun emailConFormatoInvalido_rechazaSinLlamarAlRepositorio() = runTest {
        val repositorio = FakeAuthRepository()
        val casoUso = RegistrarUseCase(repositorio)

        try {
            casoUso("pepito@email@email.com", "pepitoPassword")
            fail("Se esperaba FormatoEmailInvalidoException")
        } catch (_: FormatoEmailInvalidoException) {
            // THEN: no se ha llegado a la red
        }
        assertEquals(0, repositorio.llamadasRegistro)
    }

    @Test
    fun emailYaRegistrado_propagaElError() = runTest {
        val repositorio = FakeAuthRepository().apply { emailYaRegistrado = true }
        val casoUso = RegistrarUseCase(repositorio)

        try {
            casoUso("pepito@email.com", "pepitoPassword")
            fail("Se esperaba EmailYaRegistradoException")
        } catch (e: EmailYaRegistradoException) {
            assertEquals("Email ya registrado", e.message)
        }
    }
}
