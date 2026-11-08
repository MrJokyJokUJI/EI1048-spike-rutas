package es.uji.ei1048.rutas.domain.model

/**
 * Errores de negocio de la autenticación (HU01–HU05).
 */
open class AuthException(mensaje: String) : Exception(mensaje)

class CredencialesInvalidasException : AuthException("Credenciales inválidas")
