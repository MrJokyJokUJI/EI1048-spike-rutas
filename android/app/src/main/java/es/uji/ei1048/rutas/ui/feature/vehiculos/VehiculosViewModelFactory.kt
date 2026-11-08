package es.uji.ei1048.rutas.ui.feature.vehiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.uji.ei1048.rutas.domain.usecase.AltaVehiculoUseCase

/**
 * Patrón Factory: Fábrica para construir [VehiculosViewModel] con sus dependencias
 * sin utilizar librerías externas de inyección (ver ARQUITECTURA.md regla 6).
 */
class VehiculosViewModelFactory(
    private val altaVehiculoUseCase: AltaVehiculoUseCase,
    private val obtenerUsuarioIdActual: () -> String? = { "demo-user-id" },
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehiculosViewModel::class.java)) {
            return VehiculosViewModel(
                altaVehiculoUseCase = altaVehiculoUseCase,
                obtenerUsuarioIdActual = obtenerUsuarioIdActual,
            ) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
