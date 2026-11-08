package es.uji.ei1048.rutas.domain.model

/**
 * Excepción lanzada cuando el nombre de un vehículo es inválido (vacío o solo espacios).
 */
class NombreVehiculoInvalidoException(
    message: String = "El nombre del vehículo no puede estar vacío.",
) : IllegalArgumentException(message)

/**
 * Excepción lanzada cuando el consumo de un vehículo es menor o igual a cero.
 */
class ConsumoVehiculoInvalidoException(
    message: String = "El consumo debe ser mayor que cero.",
) : IllegalArgumentException(message)

/**
 * Excepción lanzada cuando se intenta realizar una operación sin una sesión de usuario activa.
 */
class UsuarioNoAutenticadoException(
    message: String = "Debe iniciar sesión para realizar esta operación.",
) : IllegalStateException(message)
