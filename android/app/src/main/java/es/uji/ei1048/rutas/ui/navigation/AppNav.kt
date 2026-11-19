package es.uji.ei1048.rutas.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.uji.ei1048.rutas.App
import es.uji.ei1048.rutas.di.AppContainer
import es.uji.ei1048.rutas.ui.feature.auth.AuthScreen
import es.uji.ei1048.rutas.ui.feature.auth.AuthViewModel
import es.uji.ei1048.rutas.ui.feature.auth.AuthViewModelFactory

/**
 * Todas las rutas de la aplicación se declaran aquí (constantes + NavHost).
 * Cada feature expone su XxxScreen y se registra en este fichero.
 */
object Destinos {
    const val AUTH = "auth"
    const val INICIO = "inicio"
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
        startDestination = Destinos.AUTH,
    ) {
        composable(Destinos.AUTH) {
            AuthRoute(
                factory = container.authViewModelFactory,
                onContinuar = { navController.navigate(Destinos.INICIO) },
            )
        }

        composable(Destinos.INICIO) {
            InicioScreen(
                onVolver = { navController.navigate(Destinos.AUTH) },
            )
        }
    }
}

@Composable
fun AuthRoute(
    factory: AuthViewModelFactory,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onContinuar = onContinuar,
        modifier = modifier,
    )
}

@Composable
fun InicioScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("RutasApp", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onVolver) {
            Text("Volver al registro")
        }
    }
}
