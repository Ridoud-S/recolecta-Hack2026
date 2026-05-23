
package com.itc.recolecta.recolectaDemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingOperadorResponse {
    private Integer posicion;
    private Long camioneroId;
    private String nombre;
    private Integer puntosTotales;
    private Integer rutasCompletadas;
    private Integer rutasATiempo;
    private Integer mes;
    private Integer anio;
}