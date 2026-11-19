package es.uji.ei1048.rutas.data.repository

import es.uji.ei1048.rutas.data.mapper.toDomain
import es.uji.ei1048.rutas.data.remote.supabase.AuthRemoteDataSource
import es.uji.ei1048.rutas.domain.model.AuthException
import es.uji.ei1048.rutas.domain.model.CredencialesInvalidasException
import es.uji.ei1048.rutas.domain.model.EmailYaRegistradoException
import es.uji.ei1048.rutas.domain.model.FormatoEmailInvalidoException
import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.model.SinSesionActivaException
import es.uji.ei1048.rutas.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del puerto [AuthRepository] sobre Supabase Auth (patrón Repository).
 *
 * Sustituye al SUT sin implementar de las ramas h-02 y h-03: mantiene el mismo
 * nombre, constructor sin argumentos y firma de métodos.
 */
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource = AuthRemoteDataSource(),
) : AuthRepository {

    @Volatile
    private var sesion: Sesion? = null

    override suspend fun registrar(email: String, password: String): Sesion = withContext(Dispatchers.IO) {
        try {
            val dto = remoteDataSource.registrar(email, password)
            dto.toDomain(email).also { sesion = it }
        } catch (e: EmailYaRegistradoException) {
            throw e
        } catch (e: FormatoEmailInvalidoException) {
            throw e
        } catch (e: Exception) {
            throw AuthException(e.message ?: "Error al registrar el usuario")
        }
    }

    override suspend fun iniciarSesion(email: String, password: String): Sesion = withContext(Dispatchers.IO) {
        try {
            val dto = remoteDataSource.iniciarSesion(email, password)
            dto.toDomain(email).also { sesion = it }
        } catch (e: Exception) {
            sesion = null
            throw CredencialesInvalidasException()
        }
    }

    override suspend fun cerrarSesion() = withContext(Dispatchers.IO) {
        val actual = sesion ?: throw SinSesionActivaException()
        remoteDataSource.cerrarSesion(actual.token)
        sesion = null
    }

    override suspend fun sesionActual(): Sesion? = sesion
}
