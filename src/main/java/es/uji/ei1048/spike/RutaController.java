package es.uji.ei1048.spike;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RutaController {

    private final OpenRouteServiceClient ors;

    public RutaController(OpenRouteServiceClient ors) {
        this.ors = ors;
    }

    @GetMapping("/api/ruta")
    public Ruta ruta(@RequestParam String origen,
                     @RequestParam String destino,
                     @RequestParam(defaultValue = "driving-car") String modo) {
        return ors.calcularRuta(origen, destino, modo);
    }

    @ExceptionHandler(LugarNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> lugarNoEncontrado(LugarNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
