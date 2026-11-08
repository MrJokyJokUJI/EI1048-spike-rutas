package es.uji.ei1048.rutas.domain.model

/**
 * Tipo de energía utilizado por el vehículo para su propulsión.
 *
 * Determina la fórmula de cálculo de coste en las rutas (HU18) y el precio
 * de carburante aplicable (HU16).
 */
enum class TipoEnergia {
    COMBUSTIBLE,
    ELECTRICO,
}
