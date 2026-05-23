// dto/response/OperadorStatsResponse.java
package com.itc.recolecta.recolectaDemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperadorStatsResponse {
    private Long camioneroId;
    private String nombre;
    private Integer puntosTotales;
    private Integer rutasCompletadas;
    private Integer rutasATiempo;
    private Integer incidenciasReportadas;
    private Double promedioCalificacion;
    private Integer totalBadges;
    private List<String> badges;
    private Integer mes;
    private Integer anio;
}