package es.uji.ei1048.rutas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import es.uji.ei1048.rutas.ui.navigation.AppNav
import es.uji.ei1048.rutas.ui.theme.RutasTheme

/**
 * Única Activity de la aplicación. Solo monta el tema y el grafo de navegación;
 * toda la navegación vive en [AppNav] (ver ARQUITECTURA.md).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RutasTheme {
                AppNav()
            }
        }
    }
}
