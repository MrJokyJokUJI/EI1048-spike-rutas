package es.uji.ei1048.rutas.domain.model

/**
 * Excepciones de dominio para el subsistema de Lugares de Interés (HU06–HU10).
 */
sealed class LugaresException(message: String) : Exception(message)

class UsuarioNoAutenticadoException(message: String = "Acceso no autorizado: se requiere sesión activa") :
    LugaresException(message)

class FalloConexionServidorException(message: String = "Fallo de conexión con el servidor") :
    LugaresException(message)
