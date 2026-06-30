package cl.usm.sansaweigh.integration;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EspecificacionBalanza implements Serializable {

    private String id;
    private String name;
    private String brand;
    private Double maxCapacity;
    private Double precision;
    private Double lastCalibrationOffset;
}
