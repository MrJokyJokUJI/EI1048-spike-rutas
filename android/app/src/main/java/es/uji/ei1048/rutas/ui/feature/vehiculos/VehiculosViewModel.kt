package es.uji.ei1048.rutas.ui.feature.vehiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uji.ei1048.rutas.domain.usecase.AltaVehiculoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de vehículos (patrón MVVM).
 *
 * Expone un [StateFlow] inmutable y procesa eventos mediante [onEvent].
 * No accede a repositorios directamente: delega exclusivamente en casos de uso.
 */
class VehiculosViewModel(
    private val altaVehiculoUseCase: AltaVehiculoUseCase,
    private val obtenerUsuarioIdActual: () -> String? = { "demo-user-id" },
) : ViewModel() {

    private val _state = MutableStateFlow(VehiculosUiState())
    val state: StateFlow<VehiculosUiState> = _state.asStateFlow()

    fun onEvent(event: VehiculosEvent) {
        when (event) {
            is VehiculosEvent.NombreCambiado -> _state.update { it.copy(nombre = event.nombre, error = null) }
            is VehiculosEvent.TipoEnergiaCambiado -> _state.update { it.copy(tipoEnergia = event.tipo) }
            is VehiculosEvent.ConsumoCambiado -> _state.update { it.copy(consumo = event.consumo, error = null) }
            is VehiculosEvent.ErrorDescartado -> _state.update { it.copy(error = null) }
            is VehiculosEvent.MensajeExitoDescartado -> _state.update { it.copy(mensajeExito = null) }
            is VehiculosEvent.GuardarVehiculo -> guardarVehiculo()
        }
    }

    private fun guardarVehiculo() {
        val currentState = _state.value
        val usuarioId = obtenerUsuarioIdActual()
        if (usuarioId.isNullOrBlank()) {
            _state.update { it.copy(error = "Debe iniciar sesión para dar de alta un vehículo.") }
            return
        }

        val consumoDouble = currentState.consumo.toDoubleOrNull()
        if (consumoDouble == null || consumoDouble <= 0.0) {
            _state.update { it.copy(error = "El consumo debe ser un número mayor que cero.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val resultado = altaVehiculoUseCase(
                usuarioId = usuarioId,
                nombre = currentState.nombre,
                tipoEnergia = currentState.tipoEnergia,
                consumo = consumoDouble,
            )
            resultado.fold(
                onSuccess = { vehiculo ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            nombre = "",
                            consumo = "",
                            vehiculoCreado = vehiculo,
                            mensajeExito = "Vehículo \"${vehiculo.nombre}\" dado de alta correctamente.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Error desconocido al dar de alta el vehículo.",
                        )
                    }
                }
            )
        }
    }
}
