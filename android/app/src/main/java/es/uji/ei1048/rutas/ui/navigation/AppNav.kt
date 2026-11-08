package es.uji.ei1048.rutas.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.uji.ei1048.rutas.App
import es.uji.ei1048.rutas.di.AppContainer
import es.uji.ei1048.rutas.ui.feature.vehiculos.VehiculosScreen
import es.uji.ei1048.rutas.ui.feature.vehiculos.VehiculosViewModel
import es.uji.ei1048.rutas.ui.feature.vehiculos.VehiculosViewModelFactory

/**
 * Rutas de navegación de la aplicación.
 *
 * Todas las rutas se declaran en este fichero (constantes + NavHost). Cada
 * feature expone su XxxScreen y se registra aquí. Prohibido `startActivity`.
 */
object Destinos {
    const val INICIO = "inicio"
    const val VEHICULOS = "vehiculos"
}

@Composable
fun AppNav(
    appContainer: AppContainer? = null,
) {
    val context = LocalContext.current
    val container = appContainer
        ?: (context.applicationContext as? App)?.container
        ?: AppContainer()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinos.INICIO,
    ) {
        composable(Destinos.INICIO) {
            InicioScreen(
                onIrAVehiculos = { navController.navigate(Destinos.VEHICULOS) },
            )
        }

        composable(Destinos.VEHICULOS) {
            VehiculosRoute(factory = container.vehiculosViewModelFactory)
        }
    }
}

@Composable
fun InicioScreen(
    onIrAVehiculos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "RutasApp",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onIrAVehiculos) {
            Text("Dar de alta vehículo (HU11)")
        }
    }
}

@Composable
fun VehiculosRoute(
    factory: VehiculosViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val viewModel: VehiculosViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    VehiculosScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
