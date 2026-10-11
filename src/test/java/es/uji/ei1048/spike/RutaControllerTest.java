package es.uji.ei1048.spike;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba del caso de uso con el servicio externo simulado (mock), así no depende de la API.
@WebMvcTest(RutaController.class)
class RutaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OpenRouteServiceClient ors;

    @Test
    void rutaEntreDosLugaresValidos() throws Exception {
        // GIVEN el servicio devuelve una ruta en coche de Castellón a Valencia
        Ruta ruta = new Ruta("Castellón", "Valencia", "driving-car", 74.3, 58.0,
                List.of(new double[]{39.98, -0.04}, new double[]{39.47, -0.38}));
        when(ors.calcularRuta("Castellón", "Valencia", "driving-car")).thenReturn(ruta);

        // WHEN pido la ruta / THEN obtengo distancia, duración y el trayecto
        mvc.perform(get("/api/ruta").param("origen", "Castellón").param("destino", "Valencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanciaKm").value(74.3))
                .andExpect(jsonPath("$.duracionMin").value(58.0))
                .andExpect(jsonPath("$.puntos.length()").value(2));
    }

    @Test
    void rutaConLugarInexistente() throws Exception {
        // GIVEN el destino no existe
        when(ors.calcularRuta("Castellón", "Xyzwq", "driving-car"))
                .thenThrow(new LugarNoEncontradoException("Xyzwq"));

        // WHEN pido la ruta / THEN obtengo un error 404
        mvc.perform(get("/api/ruta").param("origen", "Castellón").param("destino", "Xyzwq"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No se ha encontrado el lugar: Xyzwq"));
    }
}
