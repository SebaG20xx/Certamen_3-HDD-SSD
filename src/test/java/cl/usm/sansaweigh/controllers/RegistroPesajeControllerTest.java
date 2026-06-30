package cl.usm.sansaweigh.controllers;

import cl.usm.sansaweigh.entities.EstadoPesaje;
import cl.usm.sansaweigh.entities.PesoCategoria;
import cl.usm.sansaweigh.entities.RegistroPesaje;
import cl.usm.sansaweigh.exceptions.IllegalWeighingStateException;
import cl.usm.sansaweigh.services.RegistroPesajeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPesajeControllerTest {

    @Mock
    private RegistroPesajeService registroPesajeService;

    @InjectMocks
    private RegistroPesajeController controller;

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
    }

    // --- POST /registros ---

    @Test
    @DisplayName("POST /registros: retorna 200 cuando se crea el registro exitosamente")
    void crearRegistro_exito_retornaOk() {
        when(registroPesajeService.crearRegistro(any())).thenReturn(registro);

        ResponseEntity<?> response = controller.crearRegistro(registro);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(registro, response.getBody());
        verify(registroPesajeService).crearRegistro(registro);
    }

    @Test
    @DisplayName("POST /registros: retorna 400 cuando se viola una regla de negocio")
    void crearRegistro_reglaDeNegocioViolada_retorna400() {
        when(registroPesajeService.crearRegistro(any()))
                .thenThrow(new IllegalWeighingStateException("Restricción horaria nocturna"));

        ResponseEntity<?> response = controller.crearRegistro(registro);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Restricción horaria nocturna", response.getBody());
    }

    @Test
    @DisplayName("POST /registros: retorna 500 cuando el servicio lanza una excepción inesperada")
    void crearRegistro_excepcionInesperada_retorna500() {
        when(registroPesajeService.crearRegistro(any()))
                .thenThrow(new RuntimeException("error interno"));

        ResponseEntity<?> response = controller.crearRegistro(registro);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // --- PATCH /registros/{id}/estado ---

    @Test
    @DisplayName("PATCH /registros/{id}/estado: retorna 200 cuando la transición es válida")
    void actualizarEstado_transicionValida_retornaOk() {
        registro.setEstado(EstadoPesaje.PESADO);
        when(registroPesajeService.actualizarEstado("1", EstadoPesaje.PESADO)).thenReturn(registro);

        ResponseEntity<?> response = controller.actualizarEstado("1", EstadoPesaje.PESADO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(registro, response.getBody());
    }

    @Test
    @DisplayName("PATCH /registros/{id}/estado: retorna 404 cuando el registro no existe")
    void actualizarEstado_registroInexistente_retorna404() {
        when(registroPesajeService.actualizarEstado(anyString(), any())).thenReturn(null);

        ResponseEntity<?> response = controller.actualizarEstado("999", EstadoPesaje.PESADO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("PATCH /registros/{id}/estado: retorna 400 cuando la transición es inválida")
    void actualizarEstado_transicionInvalida_retorna400() {
        when(registroPesajeService.actualizarEstado(anyString(), any()))
                .thenThrow(new IllegalWeighingStateException("Transición no permitida"));

        ResponseEntity<?> response = controller.actualizarEstado("1", EstadoPesaje.APROBADO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Transición no permitida", response.getBody());
    }

    @Test
    @DisplayName("PATCH /registros/{id}/estado: retorna 500 cuando el servicio lanza una excepción inesperada")
    void actualizarEstado_excepcionInesperada_retorna500() {
        when(registroPesajeService.actualizarEstado(anyString(), any()))
                .thenThrow(new RuntimeException("error interno"));

        ResponseEntity<?> response = controller.actualizarEstado("1", EstadoPesaje.PESADO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // --- GET /registros ---

    @Test
    @DisplayName("GET /registros sin fechas: retorna 200 con todos los registros")
    void obtenerRegistros_sinFiltro_retornaOk() {
        List<RegistroPesaje> lista = Arrays.asList(registro);
        when(registroPesajeService.obtenerTodos()).thenReturn(lista);

        ResponseEntity<?> response = controller.obtenerRegistros(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lista, response.getBody());
        verify(registroPesajeService).obtenerTodos();
        verify(registroPesajeService, never()).obtenerPorFecha(any(), any());
    }

    @Test
    @DisplayName("GET /registros con fechas: retorna 200 con los registros filtrados")
    void obtenerRegistros_conFiltroFecha_retornaFiltrados() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        LocalDateTime hasta = LocalDateTime.now();
        List<RegistroPesaje> lista = Arrays.asList(registro);
        when(registroPesajeService.obtenerPorFecha(desde, hasta)).thenReturn(lista);

        ResponseEntity<?> response = controller.obtenerRegistros(desde, hasta);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lista, response.getBody());
        verify(registroPesajeService).obtenerPorFecha(desde, hasta);
        verify(registroPesajeService, never()).obtenerTodos();
    }

    @Test
    @DisplayName("GET /registros: retorna 500 cuando el servicio lanza una excepción inesperada")
    void obtenerRegistros_excepcionInesperada_retorna500() {
        when(registroPesajeService.obtenerTodos()).thenThrow(new RuntimeException("error interno"));

        ResponseEntity<?> response = controller.obtenerRegistros(null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // --- GET /registros/{id} ---

    @Test
    @DisplayName("GET /registros/{id}: retorna 200 cuando el registro existe")
    void obtenerPorId_existente_retornaOk() {
        when(registroPesajeService.obtenerPorId("1")).thenReturn(registro);

        ResponseEntity<?> response = controller.obtenerPorId("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(registro, response.getBody());
    }

    @Test
    @DisplayName("GET /registros/{id}: retorna 404 cuando el registro no existe")
    void obtenerPorId_inexistente_retorna404() {
        when(registroPesajeService.obtenerPorId("999")).thenReturn(null);

        ResponseEntity<?> response = controller.obtenerPorId("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /registros/{id}: retorna 500 cuando el servicio lanza una excepción inesperada")
    void obtenerPorId_excepcionInesperada_retorna500() {
        when(registroPesajeService.obtenerPorId(anyString()))
                .thenThrow(new RuntimeException("error interno"));

        ResponseEntity<?> response = controller.obtenerPorId("1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
