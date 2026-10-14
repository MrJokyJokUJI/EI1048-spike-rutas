package es.uji.ei1048.spike

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        val ors = OrsClient(BuildConfig.ORS_API_KEY)
        val supabase = SupabaseClientProvider.build(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
        val repo = RutasRepository(supabase)
        setContent {
            MaterialTheme { PantallaRuta(ors, repo) }
        }
    }
}

private val modos = listOf("driving-car" to "Coche", "cycling-regular" to "Bici", "foot-walking" to "A pie")

@Composable
fun PantallaRuta(ors: OrsClient, repo: RutasRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var sesionEmail by remember { mutableStateOf(repo.usuarioActual()?.email) }
    var mensajeAuth by remember { mutableStateOf("") }

    var origen by remember { mutableStateOf("Castellón de la Plana") }
    var destino by remember { mutableStateOf("Valencia") }
    var modo by remember { mutableStateOf("driving-car") }
    var mensaje by remember { mutableStateOf("") }
    var ruta by remember { mutableStateOf<Ruta?>(null) }
    var rutasGuardadas by remember { mutableStateOf<List<RutaGuardada>>(emptyList()) }

    val scope = rememberCoroutineScope()

    // Recupera la sesión persistida entre ejecuciones (HU48).
    LaunchedEffect(Unit) {
        repo.cargarSesion()
        sesionEmail = repo.usuarioActual()?.email
    }

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())
    ) {
        // --- Sesión / auth (HU01, HU02, HU03) ---
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sesionEmail == null) {
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        mensajeAuth = "Registrando..."
                        scope.launch {
                            mensajeAuth = try {
                                repo.registrar(email, password)
                                sesionEmail = repo.usuarioActual()?.email
                                "Registrado"
                            } catch (e: Exception) {
                                "Error al registrar: ${e.message}"
                            }
                        }
                    }) { Text("Registrar") }
                    Button(onClick = {
                        mensajeAuth = "Entrando..."
                        scope.launch {
                            mensajeAuth = try {
                                repo.iniciarSesion(email, password)
                                sesionEmail = repo.usuarioActual()?.email
                                "Sesión iniciada"
                            } catch (e: Exception) {
                                "Error al entrar: ${e.message}"
                            }
                        }
                    }) { Text("Entrar") }
                }
            } else {
                Text("Conectado como $sesionEmail")
                Text("[diagnóstico] token=${repo.rolDelToken()}")
                Button(onClick = {
                    scope.launch {
                        mensajeAuth = try {
                            repo.cerrarSesion()
                            sesionEmail = null
                            rutasGuardadas = emptyList()
                            ""
                        } catch (e: Exception) {
                            "Error al salir: ${e.message}"
                        }
                    }
                }) { Text("Salir") }
            }
            // El mensaje se muestra siempre, tanto logueado como no.
            if (mensajeAuth.isNotBlank()) Text(mensajeAuth)
        }

        HorizontalDivider()

        // --- Cálculo de ruta (HU27) ---
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(origen, { origen = it }, label = { Text("Origen") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(destino, { destino = it }, label = { Text("Destino") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modos.forEach { (clave, nombre) ->
                    FilterChip(selected = modo == clave, onClick = { modo = clave }, label = { Text(nombre) })
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                if (BuildConfig.ORS_API_KEY.isBlank()) {
                    mensaje = "Falta ors.api-key en android/local.properties"
                } else {
                    mensaje = "Calculando..."
                    scope.launch {
                        try {
                            val r = withContext(Dispatchers.IO) { ors.calcularRuta(origen, destino, modo) }
                            ruta = r
                            mensaje = "Distancia: %.1f km · Duración: %d min".format(r.distanciaKm, r.duracionMin.toInt())
                        } catch (e: LugarNoEncontradoException) {
                            mensaje = e.message ?: "Lugar no encontrado"
                        } catch (e: Exception) {
                            mensaje = "Error al calcular la ruta: ${e.message}"
                        }
                    }
                }
            }) { Text("Calcular ruta") }
            Text(mensaje)

            // --- Persistencia (HU32, HU33) ---
            if (ruta != null && sesionEmail != null) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    val r = ruta!!
                    scope.launch {
                        mensajeAuth = try {
                            repo.guardar(
                                RutaGuardada(
                                    origen = origen,
                                    destino = destino,
                                    modo = modo,
                                    distanciaKm = r.distanciaKm,
                                    duracionMin = r.duracionMin
                                )
                            )
                            "Ruta guardada"
                        } catch (e: Exception) {
                            "Error al guardar: ${e.message}"
                        }
                    }
                }) { Text("Guardar ruta") }
            }

            if (sesionEmail != null) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        mensajeAuth = try {
                            rutasGuardadas = repo.listar()
                            if (rutasGuardadas.isEmpty()) "Sin rutas guardadas" else "${rutasGuardadas.size} ruta(s) guardada(s)"
                        } catch (e: Exception) {
                            "Error al listar: ${e.message}"
                        }
                    }
                }) { Text("Mis rutas guardadas") }
                rutasGuardadas.forEach { rg ->
                    Text("%s → %s · %.1f km".format(rg.origen, rg.destino, rg.distanciaKm))
                }
            }
        }

        Mapa(ruta, Modifier.fillMaxWidth().height(400.dp).clipToBounds())
    }
}

@Composable
fun Mapa(ruta: Ruta?, modifier: Modifier) {
    AndroidView(modifier = modifier, factory = { context ->
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(9.0)
            controller.setCenter(GeoPoint(39.98, -0.05))
        }
    }, update = { mapa ->
        mapa.overlays.clear()
        if (ruta != null) {
            val puntos = ruta.puntos.map { GeoPoint(it.first, it.second) }
            mapa.overlays.add(Polyline().apply {
                setPoints(puntos)
                outlinePaint.color = Color.rgb(37, 99, 235)
                outlinePaint.strokeWidth = 12f
            })
            mapa.post { mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(puntos), true, 80) }
        }
        mapa.invalidate()
    })
}
