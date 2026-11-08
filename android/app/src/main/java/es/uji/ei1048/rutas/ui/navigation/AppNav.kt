package es.uji.ei1048.rutas.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Rutas de navegación de la aplicación.
 *
 * Todas las rutas se declaran en este fichero (constantes + NavHost). Cada
 * feature expone su XxxScreen y se registra aquí. Prohibido `startActivity`.
 */
object Destinos {
    const val INICIO = "inicio"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinos.INICIO,
    ) {
        composable(Destinos.INICIO) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("RutasApp")
            }
        }
    }
}
