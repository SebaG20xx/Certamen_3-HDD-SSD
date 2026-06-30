package cl.usm.sansaweigh.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "registros_pesaje")
public class RegistroPesaje implements Serializable {

    @Id
    private String id;

    @NotBlank(message = "Debe ingresar un ID de balanza")
    private String idBalanza;

    @NotBlank(message = "Debe ingresar un ID de paquete")
    private String idPaquete;

    @NotNull(message = "Debe ingresar el peso en Sansas")
    @Positive(message = "El peso debe ser mayor a cero")
    private Double pesoEnSansas;

    private PesoCategoria categoria;

    private EstadoPesaje estado;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
