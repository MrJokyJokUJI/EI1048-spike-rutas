package es.uji.ei1048.rutas.data.mapper

import es.uji.ei1048.rutas.data.remote.supabase.dto.VehiculoDto
import es.uji.ei1048.rutas.domain.model.TipoEnergia
import es.uji.ei1048.rutas.domain.model.Vehiculo
import org.junit.Assert.assertEquals
import org.junit.Test

class VehiculoMapperTest {

    @Test
    fun `toDomain mapea correctamente los campos del DTO`() {
        val dto = VehiculoDto(
            id = "v-1",
            usuarioId = "u-1",
            nombre = "Seat Ibiza",
            tipoEnergia = "COMBUSTIBLE",
            consumo = 5.8,
            precioCarburante = 1.65,
            kmRecorridos = 120.0,
            favorito = true,
        )

        val domain = dto.toDomain()

        assertEquals("v-1", domain.id)
        assertEquals("u-1", domain.usuarioId)
        assertEquals("Seat Ibiza", domain.nombre)
        assertEquals(TipoEnergia.COMBUSTIBLE, domain.tipoEnergia)
        assertEquals(5.8, domain.consumo, 0.001)
        assertEquals(1.65, domain.precioCarburante!!, 0.001)
        assertEquals(120.0, domain.kmRecorridos, 0.001)
        assertEquals(true, domain.favorito)
    }

    @Test
    fun `toDto mapea correctamente los campos de la entidad de dominio`() {
        val domain = Vehiculo(
            id = "v-2",
            usuarioId = "u-2",
            nombre = "Tesla Model Y",
            tipoEnergia = TipoEnergia.ELECTRICO,
            consumo = 15.2,
            precioCarburante = 0.22,
            kmRecorridos = 350.0,
            favorito = false,
        )

        val dto = domain.toDto()

        assertEquals("v-2", dto.id)
        assertEquals("u-2", dto.usuarioId)
        assertEquals("Tesla Model Y", dto.nombre)
        assertEquals("ELECTRICO", dto.tipoEnergia)
        assertEquals(15.2, dto.consumo, 0.001)
        assertEquals(0.22, dto.precioCarburante!!, 0.001)
        assertEquals(350.0, dto.kmRecorridos, 0.001)
        assertEquals(false, dto.favorito)
    }
}
