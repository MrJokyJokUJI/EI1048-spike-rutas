package es.uji.ei1048.rutas.ui.feature.vehiculos

import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.Vehiculo

/**
 * Estado inmutable de la pantalla de gestión/alta de vehículos (HU11).
 *
 * Contiene todo el estado reactivo que dibuja la vista (patrón MVVM + Observer).
 */
data class VehiculosUiState(
    val nombre: String = "",
    val tipoEnergia: TipoEnergia = TipoEnergia.COMBUSTIBLE,
    val consumo: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val vehiculoCreado: Vehiculo? = null,
    val mensajeExito: String? = null,
)
