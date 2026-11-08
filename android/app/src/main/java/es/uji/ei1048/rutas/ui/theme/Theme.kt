package es.uji.ei1048.rutas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = SuperficieClara,
    primaryContainer = AzulPrimarioOscuro,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = SuperficieClara,
    onSurface = TextoClaro,
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulPrimarioOscuroTema,
    onPrimary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = SuperficieOscura,
    onSurface = TextoOscuro,
)

/**
 * Tema de la aplicación. De momento sigue el tema del sistema; HU28 lo
 * conectará con las preferencias persistidas (DataStore).
 */
@Composable
fun RutasTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content,
    )
}
