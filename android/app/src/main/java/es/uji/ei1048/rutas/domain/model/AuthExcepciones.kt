package es.uji.ei1048.rutas.domain.model

/**
 * Errores de negocio de la autenticación (HU01–HU05).
 *
 * Mismo fichero y nombres que en las ramas h-02 y h-03 para que los merges
 * no introduzcan clases duplicadas.
 */
open class AuthException(mensaje: String) : Exception(mensaje)

class CredencialesInvalidasException : AuthException("Credenciales inválidas")

class SinSesionActivaException : AuthException("No hay ninguna sesión activa")

class EmailYaRegistradoException : AuthException("Email ya registrado")

class FormatoEmailInvalidoException : AuthException("Formato de email inválido")
