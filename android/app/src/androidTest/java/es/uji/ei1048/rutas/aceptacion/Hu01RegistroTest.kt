package es.uji.ei1048.rutas.aceptacion

import androidx.test.ext.junit.runners.AndroidJUnit4
import es.uji.ei1048.rutas.data.repository.AuthRepositoryImpl
import es.uji.ei1048.rutas.domain.model.CredencialesInvalidasException
import es.uji.ei1048.rutas.domain.model.EmailYaRegistradoException
import es.uji.ei1048.rutas.domain.model.FormatoEmailInvalidoException
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import es.uji.ei1048.rutas.domain.usecase.RegistrarUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de aceptación de la HU01: registro de un usuario no registrado.
 */
@RunWith(AndroidJUnit4::class)
class Hu01RegistroTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var registrarUseCase: RegistrarUseCase

    private val password = "pepitoPassword"

    // Email distinto en cada ejecución para que el usuario de prueba no exista de antes.
    private val email = "pepito.${System.currentTimeMillis()}@email.com"

    @Before
    fun setUp() {
        authRepository = AuthRepositoryImpl()
        registrarUseCase = RegistrarUseCase(authRepository)
    }

    /**
     * HU01-E1 (válido):
     * GIVEN el servidor está conectado y el usuario con email pepito.<timestamp>@email.com no
     *       existe previamente en el sistema,
     * WHEN registro al usuario con ese email y la contraseña pepitoPassword,
     * THEN el sistema registra al usuario, devuelve su sesión y deja esa sesión activa
     *      para poder utilizar sus servicios.
     */
    @Test
    fun hu01_e1_datosValidos_registraUsuarioConSesionIniciada() = runBlocking {
        // GIVEN: no hay ninguna sesión activa y el email no está registrado.
        authRepository.cerrarSesion()
        assertNull(authRepository.sesionActual())

        // WHEN
        val sesion = registrarUseCase(email, password)

        // THEN
        assertEquals(email, sesion.email)
        assertNotNull("La sesión debe tener token", sesion.token)
        assertEquals(sesion, authRepository.sesionActual())
    }

    /**
     * HU01-E2 (inválido):
     * GIVEN el servidor está conectado y el usuario con email pepito.<timestamp>@email.com ya
     *       existe en el sistema,
     * WHEN se intenta registrar de nuevo con ese mismo email,
     * THEN el sistema rechaza el registro con el error «Email ya registrado»
     * AND no queda ninguna sesión activa.
     */
    @Test
    fun hu01_e2_emailYaRegistrado_rechazaRegistroYNoIniciaSesion() = runBlocking {
        // GIVEN: el usuario ya está registrado.
        authRepository.registrar(email, password)
        authRepository.cerrarSesion()
        assertNull(authRepository.sesionActual())

        // WHEN
        try {
            registrarUseCase(email, password)
            fail("Se esperaba EmailYaRegistradoException")
        } catch (e: EmailYaRegistradoException) {
            // THEN
            assertEquals("Email ya registrado", e.message)
        }
        assertNull(authRepository.sesionActual())
    }

    /**
     * HU01-E6 (inválido):
     * GIVEN el servidor está conectado y el usuario con email pepito@email@email.com no existe
     *       previamente en el sistema,
     * WHEN se intenta registrar al usuario con ese email y la contraseña pepitoPassword,
     * THEN el sistema rechaza el registro con un error de formato de email inválido
     * AND la base de datos se mantiene inalterada.
     */
    @Test
    fun hu01_e6_emailConFormatoInvalido_rechazaRegistroYNoModificaLaBaseDeDatos() = runBlocking {
        // GIVEN
        val emailInvalido = "pepito@email@email.com"
        authRepository.cerrarSesion()
        assertNull(authRepository.sesionActual())

        // WHEN
        try {
            registrarUseCase(emailInvalido, password)
            fail("Se esperaba FormatoEmailInvalidoException")
        } catch (e: FormatoEmailInvalidoException) {
            // THEN: error de formato de email...
            assertEquals("Formato de email inválido", e.message)
        }

        // ...AND la base de datos se mantiene inalterada: el usuario no se ha creado, luego
        // no puede iniciar sesión con esas credenciales.
        try {
            authRepository.iniciarSesion(emailInvalido, password)
            fail("Se esperaba CredencialesInvalidasException: el usuario no debería existir")
        } catch (e: CredencialesInvalidasException) {
            assertEquals("Credenciales inválidas", e.message)
        }
        assertNull(authRepository.sesionActual())
    }
}
