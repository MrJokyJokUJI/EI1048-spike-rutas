# Historias de Usuario (vigentes: HU01–HU30)

> Fuente única de verdad del alcance funcional. Cualquier número de HU citado en
> `ARQUITECTURA.md`, ramas `h-XX`, tests de aceptación o Pull Requests debe
> corresponder a esta lista. La numeración anterior (HU01–HU57) está derogada.

## R1 – Gestión de usuarios

- **HU01** - Como usuario no registrado en la aplicación quiero poder registrarme en la misma para poder utilizar sus servicios.
- **HU02** - Como usuario registrado quiero iniciar sesión en la aplicación para utilizar sus servicios.
- **HU03** - Como usuario quiero poder cerrar la sesión de mi cuenta para salir del sistema.
- **HU04** - Como usuario quiero poder eliminar mi cuenta para que se eliminen mis datos asociados cuando ya no quiero usar el sistema.
- **HU05** - Como usuario quiero poder cambiar mi contraseña para cuando no la recuerde o pierda.

## R2 – Gestión de lugares de interés

*(todas con la sesión iniciada)*

- **HU06** - Como usuario quiero poder dar de alta un lugar de interés usando sus coordenadas para poder usarlo en una ruta.
- **HU07** - Como usuario quiero poder dar de alta un lugar de interés usando su topónimo para poder usarlo en una ruta.
- **HU08** - Como usuario quiero poder consultar la lista de lugares de interés dados de alta.
- **HU09** - Como usuario quiero poder eliminar un lugar de interés cuando ya no voy a utilizarlo más.
- **HU10** - Como usuario quiero que cuando una ruta pase por un lugar de interés se cuente para poder ver cuántas veces he pasado por el lugar de interés.

## R3 – Gestión de vehículos

*(todas con la sesión iniciada)*

- **HU11** - Como usuario quiero poder dar de alta un vehículo para poder emplearlo como método de transporte en mis rutas.
- **HU12** - Como usuario quiero poder consultar la lista de vehículos dados de alta.
- **HU13** - Como usuario quiero poder eliminar un vehículo cuando ya no vaya a utilizarlo más.
- **HU14** - Como usuario quiero poder cambiar los datos de un vehículo para poder actualizar su información.
- **HU15** - Como usuario quiero poder consultar el número de km que he recorrido con el vehículo para conocer cuánto lo he usado.
- **HU16** - Como usuario quiero poder consultar el precio del carburante utilizado por mi vehículo para calcular el coste estimado de mis rutas.

## R4 – Gestión de rutas

*(todas con la sesión iniciada)*

- **HU17** - Como usuario, dados dos lugares de interés (deben estar dados de alta para poder calcular la ruta entre ellos) y un método de movilidad, quiero obtener una ruta (trayecto, distancia y duración) entre ambos lugares para visualizarla en un mapa.
- **HU18** - Como usuario quiero conocer el coste asociado a la realización de una ruta en coche para saber cuánto me va a costar.
- **HU19** - Como usuario quiero conocer el coste asociado a la realización de una ruta a pie o en bicicleta (calorías) para saber cuánto esfuerzo me va a costar.
- **HU20** - Como usuario quiero conocer la ruta más rápida/corta/económica entre dos puntos.
- **HU21** - Como usuario quiero poder guardar una ruta para visualizarla más adelante.
- **HU22** - Como usuario quiero poder consultar el listado de rutas guardadas.
- **HU23** - Como usuario quiero poder eliminar una ruta guardada cuando ya no me resulta de utilidad.
- **HU24** - Como usuario quiero poder consultar mi historial de rutas realizadas para revisar mis desplazamientos anteriores.

## R5 – Gestión de preferencias

*(todas con la sesión iniciada)*

- **HU25** - Como usuario quiero poder marcar como favorito cada elemento de la aplicación (lugares de interés, vehículos y rutas) para que aparezcan los primeros cuando se listan.
- **HU26** - Como usuario quiero establecer un vehículo/modo de transporte por defecto para el cálculo de rutas para no tener que indicarlo a mano cada vez.
- **HU27** - Como usuario quiero establecer un tipo de ruta (más rápida, más corta, más económica) por defecto en el cálculo de rutas para no tener que indicarlo a mano cada vez.
- **HU28** - Como usuario quiero poder cambiar el tema a claro u oscuro para adaptar la apariencia a mis preferencias.

## R6 – Persistencia de los contenidos de la aplicación

*(todas con la sesión iniciada)*

- **HU29** - Como usuario quiero que cada vez que inicie la aplicación, sus contenidos y aspecto sean idénticos a los que había la última vez que se cerró, con el fin de evitar configurarla en cada uso.
- **HU30** - Como usuario quiero que el estado y los contenidos de la aplicación se restauren tras un cierre involuntario para no perder mi configuración (p. ej. un corte de luz).
