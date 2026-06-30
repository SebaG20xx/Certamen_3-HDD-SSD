package cl.usm.sansaweigh.services;

import cl.usm.sansaweigh.entities.EstadoPesaje;
import cl.usm.sansaweigh.entities.PesoCategoria;
import cl.usm.sansaweigh.entities.RegistroPesaje;
import cl.usm.sansaweigh.exceptions.IllegalWeighingStateException;
import cl.usm.sansaweigh.repositories.RegistroPesajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPesajeServiceImplTest {

    @Mock
    private RegistroPesajeRepository registroPesajeRepository;

    @InjectMocks
    private RegistroPesajeServiceImpl service;

    private RegistroPesaje registro;

    @BeforeEach
    void setUp() {
        registro = new RegistroPesaje();
        registro.setIdBalanza("4");
        registro.setIdPaquete("PKG-001");
    }

    // --- clasificación ---

    @Test
    @DisplayName("crearRegistro: clasifica como LIVIANO paquetes de hasta 10 Sansas")
    void crearRegistro_pesoHasta10_clasificaComoLiviano() {
        registro.setPesoEnSansas(5.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertEquals(PesoCategoria.LIVIANO, resultado.getCategoria());
        assertEquals(EstadoPesaje.INGRESADO, resultado.getEstado());
        assertNotNull(resultado.getCreatedAt());
        assertNotNull(resultado.getUpdatedAt());
        verify(registroPesajeRepository).insert(registro);
    }

    @Test
    @DisplayName("crearRegistro: clasifica como MEDIANO paquetes entre 10 y 50 Sansas")
    void crearRegistro_peso10a50_clasificaComoMediano() {
        registro.setPesoEnSansas(25.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertEquals(PesoCategoria.MEDIANO, resultado.getCategoria());
        assertEquals(EstadoPesaje.INGRESADO, resultado.getEstado());
        verify(registroPesajeRepository).insert(registro);
    }

    @Test
    @DisplayName("crearRegistro: clasifica exactamente 10 Sansas como LIVIANO (límite)")
    void crearRegistro_peso10Exacto_clasificaComoLiviano() {
        registro.setPesoEnSansas(10.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertEquals(PesoCategoria.LIVIANO, resultado.getCategoria());
    }

    @Test
    @DisplayName("crearRegistro: clasifica exactamente 50 Sansas como MEDIANO (límite)")
    void crearRegistro_peso50Exacto_clasificaComoMediano() {
        registro.setPesoEnSansas(50.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertEquals(PesoCategoria.MEDIANO, resultado.getCategoria());
    }

    // --- balanza prima ---

    @Test
    @DisplayName("crearRegistro: balanza no prima con paquete LIVIANO siempre es válida")
    void crearRegistro_balanzaNoPrima_liviano_exito() {
        registro.setIdBalanza("4");
        registro.setPesoEnSansas(5.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertNotNull(resultado);
        verify(registroPesajeRepository).insert(registro);
    }

    @Test
    @DisplayName("crearRegistro: balanza con ID no numérico no aplica regla prima")
    void crearRegistro_idBalanzaNoNumerico_liviano_exito() {
        registro.setIdBalanza("BALANZA-SUR");
        registro.setPesoEnSansas(5.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("crearRegistro: balanza con ID 1 (no primo) no aplica restricción prima")
    void crearRegistro_idBalanza1_noPrimo_exito() {
        registro.setIdBalanza("1");
        registro.setPesoEnSansas(5.0);
        when(registroPesajeRepository.insert(any())).thenReturn(registro);

        RegistroPesaje resultado = service.crearRegistro(registro);

        assertNotNull(resultado);
    }

    // --- máquina de estados ---

    @Test
    @DisplayName("actualizarEstado: INGRESADO a PESADO es una transición válida")
    void actualizarEstado_ingresadoAPesado_exito() {
        registro.setEstado(EstadoPesaje.INGRESADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);
        when(registroPesajeRepository.update(any())).thenReturn(registro);

        RegistroPesaje resultado = service.actualizarEstado("1", EstadoPesaje.PESADO);

        assertNotNull(resultado);
        verify(registroPesajeRepository).update(registro);
    }

    @Test
    @DisplayName("actualizarEstado: PESADO a APROBADO es una transición válida")
    void actualizarEstado_pesadoAAprobado_exito() {
        registro.setEstado(EstadoPesaje.PESADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);
        when(registroPesajeRepository.update(any())).thenReturn(registro);

        RegistroPesaje resultado = service.actualizarEstado("1", EstadoPesaje.APROBADO);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("actualizarEstado: PESADO a RECHAZADO es una transición válida")
    void actualizarEstado_pesadoARechazado_exito() {
        registro.setEstado(EstadoPesaje.PESADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);
        when(registroPesajeRepository.update(any())).thenReturn(registro);

        RegistroPesaje resultado = service.actualizarEstado("1", EstadoPesaje.RECHAZADO);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("actualizarEstado: APROBADO a DESPACHADO es una transición válida")
    void actualizarEstado_aprobadoADespachado_exito() {
        registro.setEstado(EstadoPesaje.APROBADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);
        when(registroPesajeRepository.update(any())).thenReturn(registro);

        RegistroPesaje resultado = service.actualizarEstado("1", EstadoPesaje.DESPACHADO);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("actualizarEstado: RECHAZADO a DESPACHADO es una transición válida")
    void actualizarEstado_rechazadoADespachado_exito() {
        registro.setEstado(EstadoPesaje.RECHAZADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);
        when(registroPesajeRepository.update(any())).thenReturn(registro);

        RegistroPesaje resultado = service.actualizarEstado("1", EstadoPesaje.DESPACHADO);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("actualizarEstado: INGRESADO a APROBADO lanza IllegalWeighingStateException")
    void actualizarEstado_ingresadoAAprobado_lanzaExcepcion() {
        registro.setEstado(EstadoPesaje.INGRESADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);

        assertThrows(IllegalWeighingStateException.class,
                () -> service.actualizarEstado("1", EstadoPesaje.APROBADO));
        verify(registroPesajeRepository, never()).update(any());
    }

    @Test
    @DisplayName("actualizarEstado: INGRESADO a DESPACHADO lanza IllegalWeighingStateException")
    void actualizarEstado_ingresadoADespachado_lanzaExcepcion() {
        registro.setEstado(EstadoPesaje.INGRESADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);

        assertThrows(IllegalWeighingStateException.class,
                () -> service.actualizarEstado("1", EstadoPesaje.DESPACHADO));
    }

    @Test
    @DisplayName("actualizarEstado: DESPACHADO a cualquier estado lanza IllegalWeighingStateException")
    void actualizarEstado_despachado_lanzaExcepcion() {
        registro.setEstado(EstadoPesaje.DESPACHADO);
        when(registroPesajeRepository.findById("1")).thenReturn(registro);

        assertThrows(IllegalWeighingStateException.class,
                () -> service.actualizarEstado("1", EstadoPesaje.INGRESADO));
    }

    @Test
    @DisplayName("actualizarEstado: retorna null cuando el registro no existe")
    void actualizarEstado_idInexistente_retornaNull() {
        when(registroPesajeRepository.findById("999")).thenReturn(null);

        RegistroPesaje resultado = service.actualizarEstado("999", EstadoPesaje.PESADO);

        assertNull(resultado);
        verify(registroPesajeRepository, never()).update(any());
    }

    // --- consultas ---

    @Test
    @DisplayName("obtenerTodos: delega en el repositorio y retorna la lista completa")
    void obtenerTodos_retornaListaCompleta() {
        List<RegistroPesaje> lista = Arrays.asList(registro, new RegistroPesaje());
        when(registroPesajeRepository.findAll()).thenReturn(lista);

        List<RegistroPesaje> resultado = service.obtenerTodos();

        assertEquals(2, resultado.size());
        verify(registroPesajeRepository).findAll();
    }

    @Test
    @DisplayName("obtenerPorId: retorna el registro cuando existe")
    void obtenerPorId_existente_retornaRegistro() {
        when(registroPesajeRepository.findById("1")).thenReturn(registro);

        RegistroPesaje resultado = service.obtenerPorId("1");

        assertNotNull(resultado);
        verify(registroPesajeRepository).findById("1");
    }

    @Test
    @DisplayName("obtenerPorId: retorna null cuando el registro no existe")
    void obtenerPorId_inexistente_retornaNull() {
        when(registroPesajeRepository.findById("999")).thenReturn(null);

        RegistroPesaje resultado = service.obtenerPorId("999");

        assertNull(resultado);
    }

    @Test
    @DisplayName("obtenerPorFecha: delega en el repositorio con las fechas indicadas")
    void obtenerPorFecha_retornaRegistrosFiltrados() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        LocalDateTime hasta = LocalDateTime.now();
        List<RegistroPesaje> lista = Arrays.asList(registro);
        when(registroPesajeRepository.findByFecha(desde, hasta)).thenReturn(lista);

        List<RegistroPesaje> resultado = service.obtenerPorFecha(desde, hasta);

        assertEquals(1, resultado.size());
        verify(registroPesajeRepository).findByFecha(desde, hasta);
    }
}
