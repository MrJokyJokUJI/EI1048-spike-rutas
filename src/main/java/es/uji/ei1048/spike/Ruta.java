package es.uji.ei1048.spike;

import java.util.List;

// Resultado de calcular una ruta: distancia en km, duración en minutos
// y la lista de puntos [latitud, longitud] para dibujarla en el mapa.
public record Ruta(String origen, String destino, String modo,
                   double distanciaKm, double duracionMin, List<double[]> puntos) {
}
