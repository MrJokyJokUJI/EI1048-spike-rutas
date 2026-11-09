package es.uji.ei1048.rutas.domain.model

/**
 * Excepciones de dominio para el subsistema de Lugares de Interés (HU06–HU10).
 */
sealed class LugaresException(message: String) : Exception(message)

class NombreLugarInvalidoException(message: String = "El nombre del lugar no puede estar vacío") :
    LugaresException(message)

class CoordenadasFueraDeRangoException(message: String = "Las coordenadas especificadas están fuera de rango") :
    LugaresException(message)

class LugarYaExisteException(message: String = "Ya existe un lugar con ese nombre registrado por el usuario") :
    LugaresException(message)

class UsuarioNoAutenticadoException(message: String = "Acceso no autorizado: se requiere sesión activa") :
    LugaresException(message)

class FalloConexionServidorException(message: String = "Fallo de conexión con el servidor") :
    LugaresException(message)
