package cl.usm.sansaweigh.services;

import cl.usm.sansaweigh.entities.EstadoPesaje;
import cl.usm.sansaweigh.entities.RegistroPesaje;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroPesajeService {
    RegistroPesaje crearRegistro(RegistroPesaje registro);
    RegistroPesaje actualizarEstado(String id, EstadoPesaje nuevoEstado);
    RegistroPesaje obtenerPorId(String id);
    List<RegistroPesaje> obtenerTodos();
    List<RegistroPesaje> obtenerPorFecha(LocalDateTime desde, LocalDateTime hasta);
}
