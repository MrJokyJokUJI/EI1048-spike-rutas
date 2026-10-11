# Spike iteración 0 – Cálculo de rutas

Spike del proyecto conjunto EI1039–EI1048. Sirve para probar las tecnologías del proyecto antes de empezar con las historias de usuario.

**Caso de uso:** el usuario escribe un origen, un destino y un método de movilidad (coche, bici o a pie), y la aplicación muestra la ruta en un mapa con su distancia y su duración.

## Tecnologías probadas

- **Spring Boot 3.4 con Java 21**: backend con una API REST (`GET /api/ruta`).
- **openrouteservice**: geocoding del topónimo y cálculo de la ruta.
- **Leaflet + OpenStreetMap**: mapa en una página web adaptada al móvil.
- **JUnit 5 + Mockito + MockRestServiceServer**: pruebas del caso de uso sin depender de la API real.

## Cómo ejecutarlo

1. Consigue una API key gratuita en https://openrouteservice.org.
2. Crea un fichero `secrets.properties` en la raíz del proyecto con esta línea (no se sube a GitHub):
   ```
   ors.api-key=TU_API_KEY
   ```
3. Arranca la aplicación desde IntelliJ (`SpikeApplication`) o con `mvn spring-boot:run`.
4. Abre http://localhost:8080 en el navegador.

## Pruebas

```
mvn test
```

- `RutaControllerTest`: escenario válido (ruta entre dos lugares) e inválido (lugar inexistente, devuelve 404), con el cliente de la API simulado.
- `OpenRouteServiceClientTest`: comprueba que se leen bien las respuestas de openrouteservice, simulando el servidor.
