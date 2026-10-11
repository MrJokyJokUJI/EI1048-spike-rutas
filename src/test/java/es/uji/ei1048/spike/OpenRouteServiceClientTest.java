package es.uji.ei1048.spike;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

// Prueba de integración del cliente: el servidor de openrouteservice se simula con respuestas
// con el mismo formato que la API real, así se comprueba que se leen bien los JSON.
@RestClientTest(OpenRouteServiceClient.class)
@TestPropertySource(properties = {"ors.base-url=https://ors.test", "ors.api-key=clave-test"})
class OpenRouteServiceClientTest {

    @Autowired
    private OpenRouteServiceClient cliente;

    @Autowired
    private MockRestServiceServer servidor;

    private static final String GEO_CASTELLON = """
            {"features":[{"geometry":{"coordinates":[-0.0499,39.9864]}}]}""";
    private static final String GEO_VALENCIA = """
            {"features":[{"geometry":{"coordinates":[-0.3763,39.4699]}}]}""";
    private static final String RUTA = """
            {"features":[{"properties":{"summary":{"distance":74300.0,"duration":3480.0}},
              "geometry":{"coordinates":[[-0.0499,39.9864],[-0.2,39.7],[-0.3763,39.4699]]}}]}""";

    @Test
    void calculaRutaEnCoche() {
        servidor.expect(requestTo(containsString("text=Castell")))
                .andRespond(withSuccess(GEO_CASTELLON, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(containsString("text=Valencia")))
                .andRespond(withSuccess(GEO_VALENCIA, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(containsString("/v2/directions/driving-car")))
                .andRespond(withSuccess(RUTA, MediaType.APPLICATION_JSON));

        Ruta ruta = cliente.calcularRuta("Castellón", "Valencia", "driving-car");

        assertEquals(74.3, ruta.distanciaKm(), 0.01);
        assertEquals(58.0, ruta.duracionMin(), 0.01);
        assertEquals(3, ruta.puntos().size());
        // los puntos se devuelven como [latitud, longitud] para Leaflet
        assertEquals(39.9864, ruta.puntos().get(0)[0], 0.0001);
        servidor.verify();
    }

    @Test
    void lugarInexistenteLanzaExcepcion() {
        servidor.expect(requestTo(containsString("text=Xyzwq")))
                .andRespond(withSuccess("{\"features\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(LugarNoEncontradoException.class, () -> cliente.geocodificar("Xyzwq"));
    }
}
