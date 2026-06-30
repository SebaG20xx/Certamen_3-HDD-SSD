package cl.usm.sansaweigh.services;

import cl.usm.sansaweigh.entities.EstadoPesaje;
import cl.usm.sansaweigh.entities.PesoCategoria;
import cl.usm.sansaweigh.entities.RegistroPesaje;
import cl.usm.sansaweigh.exceptions.IllegalWeighingStateException;
import cl.usm.sansaweigh.repositories.RegistroPesajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistroPesajeServiceImpl implements RegistroPesajeService {

    @Autowired
    private RegistroPesajeRepository registroPesajeRepository;

    @Override
    public RegistroPesaje crearRegistro(RegistroPesaje registro) {
        PesoCategoria categoria = clasificar(registro.getPesoEnSansas());
        validarRestriccionHoraria(categoria);
        validarBalanzaPrima(registro.getIdBalanza(), categoria);

        registro.setCategoria(categoria);
        registro.setEstado(EstadoPesaje.INGRESADO);
        registro.setCreatedAt(LocalDateTime.now());
        registro.setUpdatedAt(LocalDateTime.now());

        return registroPesajeRepository.insert(registro);
    }

    @Override
    public RegistroPesaje actualizarEstado(String id, EstadoPesaje nuevoEstado) {
        RegistroPesaje registro = registroPesajeRepository.findById(id);
        if (registro == null) {
            return null;
        }

        validarTransicion(registro.getEstado(), nuevoEstado);

        registro.setEstado(nuevoEstado);
        registro.setUpdatedAt(LocalDateTime.now());

        return registroPesajeRepository.update(registro);
    }

    @Override
    public RegistroPesaje obtenerPorId(String id) {
        return registroPesajeRepository.findById(id);
    }

    @Override
    public List<RegistroPesaje> obtenerTodos() {
        return registroPesajeRepository.findAll();
    }

    @Override
    public List<RegistroPesaje> obtenerPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        return registroPesajeRepository.findByFecha(desde, hasta);
    }

    private PesoCategoria clasificar(double pesoEnSansas) {
        if (pesoEnSansas <= 10) return PesoCategoria.LIVIANO;
        if (pesoEnSansas <= 50) return PesoCategoria.MEDIANO;
        return PesoCategoria.PESADO;
    }

    private void validarRestriccionHoraria(PesoCategoria categoria) {
        if (categoria != PesoCategoria.PESADO) return;
        int hora = LocalTime.now().getHour();
        if (hora >= 20 || hora < 6) {
            throw new IllegalWeighingStateException(
                "No se permite procesar paquetes Pesados en horario nocturno (20:00 - 06:00)"
            );
        }
    }

    private void validarBalanzaPrima(String idBalanza, PesoCategoria categoria) {
        if (categoria != PesoCategoria.PESADO) return;
        try {
            int id = Integer.parseInt(idBalanza);
            if (esPrimo(id)) {
                int diaMes = LocalDate.now().getDayOfMonth();
                if (diaMes % 2 != 0) {
                    throw new IllegalWeighingStateException(
                        "Balanza prima no puede registrar paquetes Pesados en días impares del mes"
                    );
                }
            }
        } catch (NumberFormatException e) {
            // ID no numérico, no aplica la regla de balanza prima
        }
    }

    private boolean esPrimo(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private void validarTransicion(EstadoPesaje actual, EstadoPesaje nuevo) {
        boolean valida = switch (actual) {
            case INGRESADO -> nuevo == EstadoPesaje.PESADO;
            case PESADO -> nuevo == EstadoPesaje.APROBADO || nuevo == EstadoPesaje.RECHAZADO;
            case APROBADO, RECHAZADO -> nuevo == EstadoPesaje.DESPACHADO;
            case DESPACHADO -> false;
        };
        if (!valida) {
            throw new IllegalWeighingStateException(
                "Transición no permitida: " + actual + " → " + nuevo
            );
        }
    }
}
