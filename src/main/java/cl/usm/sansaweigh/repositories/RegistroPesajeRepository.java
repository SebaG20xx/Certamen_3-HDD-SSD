package cl.usm.sansaweigh.repositories;

import cl.usm.sansaweigh.entities.RegistroPesaje;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroPesajeRepository {
    RegistroPesaje insert(RegistroPesaje registro);
    RegistroPesaje update(RegistroPesaje registro);
    RegistroPesaje findById(String id);
    List<RegistroPesaje> findAll();
    List<RegistroPesaje> findByFecha(LocalDateTime desde, LocalDateTime hasta);
}
