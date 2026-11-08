package es.uji.ei1048.rutas.ui.feature.vehiculos

import es.uji.ei1048.rutas.domain.model.TipoEnergia

/**
 * Acciones y eventos generados por la interacción del usuario en la pantalla de vehículos.
 *
 * Flujo unidireccional de eventos hacia la ViewModel.
 */
sealed interface VehiculosEvent {
    data class NombreCambiado(val nombre: String) : VehiculosEvent
    data class TipoEnergiaCambiado(val tipo: TipoEnergia) : VehiculosEvent
    data class ConsumoCambiado(val consumo: String) : VehiculosEvent
    data object GuardarVehiculo : VehiculosEvent
    data object ErrorDescartado : VehiculosEvent
    data object MensajeExitoDescartado : VehiculosEvent
}
