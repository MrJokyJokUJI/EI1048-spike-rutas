package es.uji.ei1048.rutas.domain.repository

import es.uji.ei1048.rutas.domain.model.Sesion

/**
 * Puerto de autenticación (HU01–HU05). Lo implementa AuthRepositoryImpl en data/repository.
 */
interface AuthRepository {
    suspend fun registrar(email: String, password: String): Sesion
    suspend fun iniciarSesion(email: String, password: String): Sesion
    suspend fun cerrarSesion()
    suspend fun sesionActual(): Sesion?
}
