package es.uji.ei1048.rutas.aceptacion

import es.uji.ei1048.rutas.data.repository.AuthRepositoryImpl
import es.uji.ei1048.rutas.domain.model.SinSesionActivaException
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import es.uji.ei1048.rutas.domain.usecase.CerrarSesionUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de aceptación de la HU03: cierre de sesión.
 * Sin mocks: se usa el repositorio real contra el backend de pruebas.
 * El SUT está definido pero sin implementar, así que las pruebas fallan.
 */
class Hu03CerrarSesionTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var cerrarSesion: CerrarSesionUseCase

    // Email distinto en cada ejecución para que el usuario de prueba no exista de antes
    private val email = "pepito.${System.currentTimeMillis()}@email.com"
    private val password = "pepitoPassword"

    @Before
    fun setUp() {
        authRepository = AuthRepositoryImpl()
        cerrarSesion = CerrarSesionUseCase(authRepository)
    }

    /**
     * HU03-E1 (válido):
     * GIVEN el usuario pepito está registrado y tiene la sesión iniciada,
     * WHEN cierra la sesión,
     * THEN el sistema cierra la sesión sin errores
     * AND ya no hay ninguna sesión activa.
     */
    @Test
    fun hu03_e1_sesionIniciada_cierraSesion() = runBlocking {
        // GIVEN
        authRepository.registrar(email, password)
        authRepository.iniciarSesion(email, password)
        assertNotNull(authRepository.sesionActual())

        // WHEN
        cerrarSesion()

        // THEN
        assertNull(authRepository.sesionActual())
    }

    /**
     * HU03-E2 (inválido):
     * GIVEN no hay ninguna sesión activa,
     * WHEN se intenta cerrar la sesión,
     * THEN el sistema rechaza la operación con el error «No hay ninguna sesión activa»
     * AND sigue sin haber ninguna sesión activa.
     */
    @Test
    fun hu03_e2_sinSesionActiva_rechazaCierre() = runBlocking {
        // GIVEN
        assertNull(authRepository.sesionActual())

        // WHEN
        try {
            cerrarSesion()
            fail("Se esperaba SinSesionActivaException")
        } catch (e: SinSesionActivaException) {
            // THEN
            assertEquals("No hay ninguna sesión activa", e.message)
        }
        assertNull(authRepository.sesionActual())
    }
}
