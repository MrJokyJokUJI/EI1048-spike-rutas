package es.uji.ei1048.rutas.aceptacion

import androidx.test.ext.junit.runners.AndroidJUnit4
import es.uji.ei1048.rutas.data.remote.ors.OrsDataSource
import es.uji.ei1048.rutas.data.repository.AuthRepositoryImpl
import es.uji.ei1048.rutas.data.repository.CalculoRutaRepositoryImpl
import es.uji.ei1048.rutas.data.repository.LugaresRepositoryImpl
import es.uji.ei1048.rutas.domain.model.FaltanPrerrequisitosLugaresException
import es.uji.ei1048.rutas.domain.model.LugarInteres
import es.uji.ei1048.rutas.domain.model.Modo
import es.uji.ei1048.rutas.domain.model.Punto
import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import es.uji.ei1048.rutas.domain.repository.CalculoRutaRepository
import es.uji.ei1048.rutas.domain.repository.LugaresRepository
import es.uji.ei1048.rutas.domain.usecase.CalcularRutaUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Pruebas de aceptación de la HU17: cálculo de ruta entre dos lugares de interés (ATDD).
 *
 * Sin mocks: se usan los repositorios reales contra el backend de pruebas
 * (Supabase para los lugares y la sesión, openrouteservice para el trayecto).
 * Las credenciales se leen de android/local.properties a través de BuildConfig.
 *
 * El caso de uso recibe ya resueltos el origen y el destino por el llamador:
 * un valor null significa que ese lugar no está dado de alta y, por tanto,
 * falta un prerrequisito para calcular la ruta.
 *
 * Escenarios cubiertos:
 * - HU17-E1 (válido): Castellón y Valencia dados de alta, modo coche gasolina ->
 *   se devuelven geometría, distancia y duración y la base de datos no cambia.
 * - HU17-E2 (inválido): Castellón dado de alta pero Valencia no ->
 *   error de falta de prerrequisitos de lugares y la base de datos no cambia.
 */
@RunWith(AndroidJUnit4::class)
class Hu17CalcularRutaTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var lugaresRepository: LugaresRepository
    private lateinit var calculoRutaRepository: CalculoRutaRepository
    private lateinit var calcularRuta: CalcularRutaUseCase

    private val password = "pepitoPassword"

    // Email distinto en cada ejecución para que el usuario de prueba no exista de antes.
    private val email = "pepito.${System.currentTimeMillis()}@email.com"

    private val castellon = Punto(latitud = 39.9864, longitud = -0.0499)
    private val valencia = Punto(latitud = 39.4699, longitud = -0.3763)

    @Before
    fun setUp() {
        authRepository = AuthRepositoryImpl()
        lugaresRepository = LugaresRepositoryImpl()
        calculoRutaRepository = CalculoRutaRepositoryImpl(OrsDataSource())
        calcularRuta = CalcularRutaUseCase(calculoRutaRepository)
    }

    /**
     * HU17-E1 (válido):
     * GIVEN el servidor está conectado, tengo la sesión iniciada y los lugares
     *       Castellón de la Plana y Valencia están dados de alta previamente,
     * WHEN solicito calcular la ruta entre Castellón de la Plana y Valencia en Coche gasolina,
     * THEN el sistema devuelve la geometría del trayecto, la distancia y la duración estimada,
     *      sin alterar la base de datos.
     */
    @Test
    fun hu17_e1_lugaresRegistradosYModoCoche_devuelveGeometriaDistanciaYDuracion() = runBlocking {
        // GIVEN: usuario autenticado con sus dos lugares dados de alta.
        val sesion: Sesion = authRepository.registrar(email, password)
        assertNotNull(sesion.token)
        assertNotNull(authRepository.sesionActual())

        lugaresRepository.altaLugar(
            LugarInteres(
                id = UUID.randomUUID().toString(),
                usuarioId = sesion.usuarioId,
                nombre = "Castellón de la Plana",
                coordenadas = castellon,
            ),
        ).getOrThrow()
        lugaresRepository.altaLugar(
            LugarInteres(
                id = UUID.randomUUID().toString(),
                usuarioId = sesion.usuarioId,
                nombre = "Valencia",
                coordenadas = valencia,
            ),
        ).getOrThrow()

        // El llamador resuelve los lugares dados de alta antes de invocar el caso de uso.
        val lugaresAntes = lugaresRepository.consultarLugares(sesion.usuarioId).getOrThrow()
        val origen = lugaresAntes.firstOrNull { it.nombre == "Castellón de la Plana" }
        val destino = lugaresAntes.firstOrNull { it.nombre == "Valencia" }
        assertNotNull("Castellón de la Plana debe estar dado de alta", origen)
        assertNotNull("Valencia debe estar dado de alta", destino)

        // WHEN: modo COCHE = coche de gasolina (perfil driving-car de openrouteservice).
        val resultado = calcularRuta(origen, destino, Modo.COCHE)

        // THEN: geometría, distancia y duración.
        assertTrue("El resultado debe ser exitoso: ${resultado.exceptionOrNull()?.message}", resultado.isSuccess)
        val ruta = resultado.getOrThrow()
        assertTrue("La geometría debe tener al menos dos puntos", ruta.puntos.size >= 2)
        assertTrue("La distancia debe ser mayor que cero", ruta.distanciaKm > 0.0)
        assertTrue("La duración debe ser mayor que cero", ruta.duracionMin > 0.0)
        assertEquals(castellon.latitud, ruta.puntos.first().latitud, 0.05)
        assertEquals(castellon.longitud, ruta.puntos.first().longitud, 0.05)
        assertEquals(valencia.latitud, ruta.puntos.last().latitud, 0.05)
        assertEquals(valencia.longitud, ruta.puntos.last().longitud, 0.05)

        // AND: la base de datos se mantiene inalterada.
        assertEquals(lugaresAntes, lugaresRepository.consultarLugares(sesion.usuarioId).getOrThrow())
    }

    /**
     * HU17-E2 (inválido):
     * GIVEN el servidor está conectado, tengo la sesión iniciada, Castellón de la Plana
     *       está dado de alta previamente pero Valencia no está dado de alta en el sistema,
     * WHEN intento calcular la ruta entre Castellón de la Plana y Valencia,
     * THEN el sistema devuelve un error indicando la falta de prerrequisitos de lugares
     *      y la base de datos se mantiene inalterada.
     */
    @Test
    fun hu17_e2_valenciaNoRegistrada_devuelveErrorFaltanPrerrequisitos() = runBlocking {
        // GIVEN: solo Castellón de la Plana está dado de alta.
        val sesion: Sesion = authRepository.registrar(email, password)
        assertNotNull(sesion.token)
        assertNotNull(authRepository.sesionActual())

        lugaresRepository.altaLugar(
            LugarInteres(
                id = UUID.randomUUID().toString(),
                usuarioId = sesion.usuarioId,
                nombre = "Castellón de la Plana",
                coordenadas = castellon,
            ),
        ).getOrThrow()

        val lugaresAntes = lugaresRepository.consultarLugares(sesion.usuarioId).getOrThrow()
        val origen = lugaresAntes.firstOrNull { it.nombre == "Castellón de la Plana" }
        val destino = lugaresAntes.firstOrNull { it.nombre == "Valencia" }
        assertNotNull("Castellón de la Plana debe estar dado de alta", origen)
        assertNull("Valencia no debe estar dada de alta", destino)

        // WHEN
        val resultado = calcularRuta(origen, destino, Modo.COCHE)

        // THEN: error de falta de prerrequisitos de lugares.
        assertTrue("El resultado debe ser fallido", resultado.isFailure)
        val excepcion = resultado.exceptionOrNull()
        assertTrue(
            "Se esperaba FaltanPrerrequisitosLugaresException pero fue ${excepcion?.javaClass?.simpleName}",
            excepcion is FaltanPrerrequisitosLugaresException,
        )
        assertEquals(
            "Faltan prerrequisitos: el origen y el destino deben estar dados de alta.",
            excepcion.message,
        )

        // AND: la base de datos se mantiene inalterada.
        assertEquals(lugaresAntes, lugaresRepository.consultarLugares(sesion.usuarioId).getOrThrow())
    }
}
