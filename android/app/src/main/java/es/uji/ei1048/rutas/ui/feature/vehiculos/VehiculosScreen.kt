package es.uji.ei1048.rutas.ui.feature.vehiculos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.uji.ei1048.rutas.domain.model.TipoEnergia

/**
 * Pantalla de Alta de Vehículo (HU11).
 *
 * Componente Composable stateless: solo lee [state] y despacha [onEvent].
 * No contiene lógica de negocio ni llamadas a red/repositorios (ver ARQUITECTURA.md).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    state: VehiculosUiState,
    onEvent: (VehiculosEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onEvent(VehiculosEvent.ErrorDescartado)
        }
    }

    LaunchedEffect(state.mensajeExito) {
        state.mensajeExito?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onEvent(VehiculosEvent.MensajeExitoDescartado)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Alta de Vehículo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Datos del vehículo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    OutlinedTextField(
                        value = state.nombre,
                        onValueChange = { onEvent(VehiculosEvent.NombreCambiado(it)) },
                        label = { Text("Nombre del vehículo") },
                        placeholder = { Text("Ej: Seat León, Tesla Model 3...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                    )

                    Text(
                        text = "Tipo de energía",
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.tipoEnergia == TipoEnergia.COMBUSTIBLE,
                            onClick = { onEvent(VehiculosEvent.TipoEnergiaCambiado(TipoEnergia.COMBUSTIBLE)) },
                            label = { Text("Combustible") },
                            enabled = !state.isLoading,
                        )
                        FilterChip(
                            selected = state.tipoEnergia == TipoEnergia.ELECTRICO,
                            onClick = { onEvent(VehiculosEvent.TipoEnergiaCambiado(TipoEnergia.ELECTRICO)) },
                            label = { Text("Eléctrico") },
                            enabled = !state.isLoading,
                        )
                    }

                    val etiquetaConsumo = if (state.tipoEnergia == TipoEnergia.COMBUSTIBLE) {
                        "Consumo medio (l/100 km)"
                    } else {
                        "Consumo medio (kWh/100 km)"
                    }

                    OutlinedTextField(
                        value = state.consumo,
                        onValueChange = { onEvent(VehiculosEvent.ConsumoCambiado(it)) },
                        label = { Text(etiquetaConsumo) },
                        placeholder = { Text("Ej: 6.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                    )
                }
            }

            Button(
                onClick = { onEvent(VehiculosEvent.GuardarVehiculo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading && state.nombre.isNotBlank() && state.consumo.isNotBlank(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrando...")
                } else {
                    Text("Dar de alta vehículo")
                }
            }
        }
    }
}
