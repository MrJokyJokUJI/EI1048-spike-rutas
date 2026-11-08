package es.uji.ei1048.rutas

import android.app.Application
import es.uji.ei1048.rutas.di.AppContainer

/**
 * Punto de entrada de la aplicación.
 *
 * Construye y publica el [AppContainer] (patrón Singleton), que es la única
 * fuente de dependencias del proyecto. La inyección es manual: no se usa Hilt,
 * Dagger ni Koin (ver ARQUITECTURA.md).
 */
class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
