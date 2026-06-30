package cl.usm.sansaweigh.repositories;

import cl.usm.sansaweigh.entities.EstadoPesaje;
import cl.usm.sansaweigh.entities.PesoCategoria;
import cl.usm.sansaweigh.entities.RegistroPesaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPesajeRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RegistroPesajeRepositoryImpl repository;

    private RegistroPesaje registro;

    @BeforeEach
    void setUp() {
        registro = new RegistroPesaje();
        registro.setId("1");
        registro.setIdBalanza("4");
        registro.setIdPaquete("PKG-001");
        registro.setPesoEnSansas(5.0);
        registro.setCategoria(PesoCategoria.LIVIANO);
        registro.setEstado(EstadoPesaje.INGRESADO);
        registro.setCreatedAt(LocalDateTime.now());
        registro.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("insert: delega en MongoTemplate y retorna el registro insertado")
    void insert_exito_retornaRegistro() {
        when(mongoTemplate.insert(registro)).thenReturn(registro);

        RegistroPesaje resultado = repository.insert(registro);

        assertNotNull(resultado);
        assertEquals("1", resultado.getId());
        verify(mongoTemplate).insert(registro);
    }

    @Test
    @DisplayName("update: delega en MongoTemplate.save y retorna el registro actualizado")
    void update_exito_retornaRegistroActualizado() {
        registro.setEstado(EstadoPesaje.PESADO);
        when(mongoTemplate.save(registro)).thenReturn(registro);

        RegistroPesaje resultado = repository.update(registro);

        assertNotNull(resultado);
        assertEquals(EstadoPesaje.PESADO, resultado.getEstado());
        verify(mongoTemplate).save(registro);
    }

    @Test
    @DisplayName("findById: retorna el registro cuando existe en MongoDB")
    void findById_existente_retornaRegistro() {
        when(mongoTemplate.findById("1", RegistroPesaje.class)).thenReturn(registro);

        RegistroPesaje resultado = repository.findById("1");

        assertNotNull(resultado);
        assertEquals("PKG-001", resultado.getIdPaquete());
        verify(mongoTemplate).findById("1", RegistroPesaje.class);
    }

    @Test
    @DisplayName("findById: retorna null cuando el registro no existe en MongoDB")
    void findById_inexistente_retornaNull() {
        when(mongoTemplate.findById("999", RegistroPesaje.class)).thenReturn(null);

        RegistroPesaje resultado = repository.findById("999");

        assertNull(resultado);
    }

    @Test
    @DisplayName("findAll: retorna la lista completa de registros desde MongoDB")
    void findAll_retornaListaCompleta() {
        List<RegistroPesaje> lista = Arrays.asList(registro, new RegistroPesaje());
        when(mongoTemplate.findAll(RegistroPesaje.class)).thenReturn(lista);

        List<RegistroPesaje> resultado = repository.findAll();

        assertEquals(2, resultado.size());
        verify(mongoTemplate).findAll(RegistroPesaje.class);
    }

    @Test
    @DisplayName("findByFecha: delega en MongoTemplate con la query de rango de fechas")
    void findByFecha_retornaRegistrosFiltrados() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        LocalDateTime hasta = LocalDateTime.now();
        List<RegistroPesaje> lista = Arrays.asList(registro);
        when(mongoTemplate.find(any(Query.class), eq(RegistroPesaje.class))).thenReturn(lista);

        List<RegistroPesaje> resultado = repository.findByFecha(desde, hasta);

        assertEquals(1, resultado.size());
        verify(mongoTemplate).find(any(Query.class), eq(RegistroPesaje.class));
    }

    @Test
    @DisplayName("findByFecha: retorna lista vacía cuando no hay registros en el rango")
    void findByFecha_sinResultados_retornaListaVacia() {
        LocalDateTime desde = LocalDateTime.now().minusDays(1);
        LocalDateTime hasta = LocalDateTime.now();
        when(mongoTemplate.find(any(Query.class), eq(RegistroPesaje.class))).thenReturn(List.of());

        List<RegistroPesaje> resultado = repository.findByFecha(desde, hasta);

        assertTrue(resultado.isEmpty());
    }
}
