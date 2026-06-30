package cl.usm.sansaweigh.controllers;

import cl.usm.sansaweigh.entities.EstadoPesaje;
import cl.usm.sansaweigh.entities.RegistroPesaje;
import cl.usm.sansaweigh.exceptions.IllegalWeighingStateException;
import cl.usm.sansaweigh.services.RegistroPesajeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/registros")
public class RegistroPesajeController {

    @Autowired
    private RegistroPesajeService registroPesajeService;

    @PostMapping
    public ResponseEntity<?> crearRegistro(@RequestBody @Valid RegistroPesaje registro) {
        try {
            RegistroPesaje creado = registroPesajeService.crearRegistro(registro);
            return ResponseEntity.ok(creado);
        } catch (IllegalWeighingStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable String id,
            @RequestParam EstadoPesaje nuevoEstado) {
        try {
            RegistroPesaje actualizado = registroPesajeService.actualizarEstado(id, nuevoEstado);
            if (actualizado == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(actualizado);
        } catch (IllegalWeighingStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerRegistros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        try {
            if (desde != null && hasta != null) {
                List<RegistroPesaje> filtrados = registroPesajeService.obtenerPorFecha(desde, hasta);
                return ResponseEntity.ok(filtrados);
            }
            List<RegistroPesaje> todos = registroPesajeService.obtenerTodos();
            return ResponseEntity.ok(todos);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        try {
            RegistroPesaje registro = registroPesajeService.obtenerPorId(id);
            if (registro == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(registro);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
