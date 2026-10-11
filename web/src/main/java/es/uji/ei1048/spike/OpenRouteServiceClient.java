package es.uji.ei1048.spike;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

// Cliente de openrouteservice: geocoding de un topónimo y cálculo de ruta entre dos puntos.
@Component
public class OpenRouteServiceClient {

    private final RestClient restClient;
    private final String apiKey;

    public OpenRouteServiceClient(RestClient.Builder builder,
                                  @Value("${ors.base-url}") String baseUrl,
                                  @Value("${ors.api-key}") String apiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    // Devuelve {longitud, latitud} del primer resultado del geocoding.
    public double[] geocodificar(String lugar) {
        JsonNode respuesta = restClient.get()
                .uri("/geocode/search?api_key={key}&text={lugar}&size=1&boundary.country=ES", apiKey, lugar)
                .retrieve()
                .body(JsonNode.class);

        JsonNode features = respuesta.path("features");
        if (features.isEmpty()) {
            throw new LugarNoEncontradoException(lugar);
        }
        JsonNode coords = features.get(0).path("geometry").path("coordinates");
        return new double[]{coords.get(0).asDouble(), coords.get(1).asDouble()};
    }

    // modo: driving-car, cycling-regular o foot-walking
    public Ruta calcularRuta(String origen, String destino, String modo) {
        double[] inicio = geocodificar(origen);
        double[] fin = geocodificar(destino);

        JsonNode respuesta = restClient.get()
                .uri("/v2/directions/{modo}?api_key={key}&start={lon1},{lat1}&end={lon2},{lat2}",
                        modo, apiKey, inicio[0], inicio[1], fin[0], fin[1])
                .retrieve()
                .body(JsonNode.class);

        JsonNode feature = respuesta.path("features").get(0);
        JsonNode resumen = feature.path("properties").path("summary");
        double distanciaKm = resumen.path("distance").asDouble() / 1000;
        double duracionMin = resumen.path("duration").asDouble() / 60;

        List<double[]> puntos = new ArrayList<>();
        for (JsonNode p : feature.path("geometry").path("coordinates")) {
            puntos.add(new double[]{p.get(1).asDouble(), p.get(0).asDouble()});
        }
        return new Ruta(origen, destino, modo, distanciaKm, duracionMin, puntos);
    }
}
