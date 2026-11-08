package es.uji.ei1048.rutas.data.repository

import es.uji.ei1048.rutas.domain.model.Sesion
import es.uji.ei1048.rutas.domain.repository.AuthRepository

/**
 * Implementación del puerto de autenticación sobre Supabase Auth.
 * Métodos definidos SIN implementación: las pruebas de aceptación fallan.
 */
class AuthRepositoryImpl : AuthRepository {

    override suspend fun registrar(email: String, password: String): Sesion {
        TODO("Not implemented")
    }

    override suspend fun iniciarSesion(email: String, password: String): Sesion {
        TODO("Not implemented")
    }

    override suspend fun cerrarSesion() {
        TODO("Not implemented")
    }

    override suspend fun sesionActual(): Sesion? {
        TODO("Not implemented")
    }
}
