// controller/AdminController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.response.ApiResponse;
import com.itc.recolecta.recolectaDemo.service.SimuladorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SimuladorService simuladorService;

    // Avanzar ruta manualmente para demo
    @PostMapping("/demo/ruta/{routeId}/avanzar")
    public ResponseEntity<ApiResponse<String>> avanzarRuta(
            @PathVariable String routeId) {

        String resultado = simuladorService.avanzarManual(routeId);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    // Reiniciar ruta para demo
    @PostMapping("/demo/ruta/{routeId}/reiniciar")
    public ResponseEntity<ApiResponse<String>> reiniciarRuta(
            @PathVariable String routeId) {

        String resultado = simuladorService.reiniciarRuta(routeId);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
}