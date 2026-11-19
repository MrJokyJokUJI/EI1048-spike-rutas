package es.uji.ei1048.rutas.domain.usecase

import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.model.SinSesionActivaException
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba de integración de HU03: el caso de uso con el puerto [AuthRepository]
 * sustituido por un fake manual (convención del proyecto, ver ARQUITECTURA.md).
 */
class CerrarSesionUseCaseTest {

    private class FakeAuthRepository(var sesion: Sesion? = null) : AuthRepository {
        var llamadasCierre = 0

        override suspend fun registrar(email: String, password: String): Sesion =
            Sesion(usuarioId = "id-1", email = email, token = "token-1").also { sesion = it }

        override suspend fun iniciarSesion(email: String, password: String): Sesion =
            Sesion(usuarioId = "id-1", email = email, token = "token-1").also { sesion = it }

        override suspend fun cerrarSesion() {
            llamadasCierre++
            sesion = null
        }

        override suspend fun sesionActual(): Sesion? = sesion
    }

    @Test
    fun invoke_sesionIniciada_cierraSesion() = runTest {
        val repositorio = FakeAuthRepository(Sesion("id-1", "pepito@email.com", "token-1"))

        CerrarSesionUseCase(repositorio)()

        assertNull(repositorio.sesionActual())
        assertEquals(1, repositorio.llamadasCierre)
    }

    @Test
    fun invoke_sinSesionActiva_lanzaSinSesionActivaSinLlamarAlRepositorio() = runTest {
        val repositorio = FakeAuthRepository()

        val resultado = runCatching { CerrarSesionUseCase(repositorio)() }

        assertTrue(resultado.exceptionOrNull() is SinSesionActivaException)
        assertEquals("No hay ninguna sesión activa", resultado.exceptionOrNull()?.message)
        assertEquals(0, repositorio.llamadasCierre)
    }
}
