package es.uji.ei1048.rutas.aceptacion

import es.uji.ei1048.rutas.data.repository.AuthRepositoryImpl
import es.uji.ei1048.rutas.domain.model.CredencialesInvalidasException
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import es.uji.ei1048.rutas.domain.usecase.IniciarSesionUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de aceptación de la HU02: inicio de sesión de un usuario registrado.
 * Sin mocks: se usa el repositorio real contra el backend de pruebas.
 * El SUT está definido pero sin implementar, así que las pruebas fallan.
 */
class Hu02IniciarSesionTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var iniciarSesion: IniciarSesionUseCase

    // Email distinto en cada ejecución para que el usuario de prueba no exista de antes
    private val email = "pepito.${System.currentTimeMillis()}@email.com"
    private val password = "pepitoPassword"

    @Before
    fun setUp() {
        authRepository = AuthRepositoryImpl()
        iniciarSesion = IniciarSesionUseCase(authRepository)
    }

    /**
     * HU02-E1 (válido):
     * GIVEN el usuario pepito está registrado con la contraseña pepitoPassword y no hay ninguna sesión activa,
     * WHEN inicia sesión con ese email y la contraseña pepitoPassword,
     * THEN el sistema devuelve una sesión con un token para pepito
     * AND la sesión activa del sistema es la de pepito.
     */
    @Test
    fun hu02_e1_credencialesCorrectas_iniciaSesion() = runBlocking {
        // GIVEN
        authRepository.registrar(email, password)
        authRepository.cerrarSesion()

        // WHEN
        val sesion = iniciarSesion(email, password)

        // THEN
        assertEquals(email, sesion.email)
        assertNotNull(sesion.token)
        assertEquals(sesion, authRepository.sesionActual())
    }

    /**
     * HU02-E2 (inválido):
     * GIVEN el usuario pepito está registrado con la contraseña pepitoPassword y no hay ninguna sesión activa,
     * WHEN inicia sesión con ese email y la contraseña incorrecta otraPassword,
     * THEN el sistema rechaza el inicio de sesión con el error «Credenciales inválidas»
     * AND no queda ninguna sesión activa.
     */
    @Test
    fun hu02_e2_contrasenaIncorrecta_rechazaYNoCreaSesion() = runBlocking {
        // GIVEN
        authRepository.registrar(email, password)
        authRepository.cerrarSesion()

        // WHEN
        try {
            iniciarSesion(email, "otraPassword")
            fail("Se esperaba CredencialesInvalidasException")
        } catch (e: CredencialesInvalidasException) {
            // THEN
            assertEquals("Credenciales inválidas", e.message)
        }
        assertNull(authRepository.sesionActual())
    }

    /**
     * HU02-E3 (inválido):
     * GIVEN no existe ningún usuario registrado con el email noexiste@email.com y no hay ninguna sesión activa,
     * WHEN se intenta iniciar sesión con noexiste@email.com y la contraseña pepitoPassword,
     * THEN el sistema rechaza el inicio de sesión con el error «Credenciales inválidas»
     * AND no queda ninguna sesión activa.
     */
    @Test
    fun hu02_e3_usuarioNoRegistrado_rechazaYNoCreaSesion() = runBlocking {
        // GIVEN
        authRepository.cerrarSesion()

        // WHEN
        try {
            iniciarSesion("noexiste.${System.currentTimeMillis()}@email.com", password)
            fail("Se esperaba CredencialesInvalidasException")
        } catch (e: CredencialesInvalidasException) {
            // THEN
            assertEquals("Credenciales inválidas", e.message)
        }
        assertNull(authRepository.sesionActual())
    }
}
