// dto/response/DashboardAdminResponse.java
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
public class DashboardAdminResponse {
    private Integer totalRutasActivas;
    private Integer totalRutasFinalizadas;
    private Integer totalIncidenciasHoy;
    private Double combustibleTotalConsumido;
    private Double kmTotalRecorridos;
    private List<RutaStatusResponse> rutasEnCurso;
    private List<RankingOperadorResponse> topOperadores;
}