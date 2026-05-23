// controller/CiudadanoController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.request.DomicilioRequest;
import com.itc.recolecta.recolectaDemo.dto.response.ApiResponse;
import com.itc.recolecta.recolectaDemo.dto.response.DomicilioResponse;
import com.itc.recolecta.recolectaDemo.service.DomicilioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ciudadano")
@RequiredArgsConstructor
public class CiudadanoController {

    private final DomicilioService domicilioService;

    // POST /api/ciudadano/domicilios
    @PostMapping("/domicilios")
    public ResponseEntity<ApiResponse<DomicilioResponse>> registrarDomicilio(
            @Valid @RequestBody DomicilioRequest request) {

        DomicilioResponse response = domicilioService.registrar(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Domicilio registrado exitosamente", response));
    }

    // GET /api/ciudadano/domicilios
    @GetMapping("/domicilios")
    public ResponseEntity<ApiResponse<List<DomicilioResponse>>> listarDomicilios() {

        List<DomicilioResponse> response = domicilioService.listarMios();
        return ResponseEntity.ok(
                ApiResponse.ok("Domicilios obtenidos", response)
        );
    }

    // GET /api/ciudadano/domicilios/{id}
    @GetMapping("/domicilios/{id}")
    public ResponseEntity<ApiResponse<DomicilioResponse>> obtenerDomicilio(
            @PathVariable Long id) {

        DomicilioResponse response = domicilioService.obtenerPorId(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Domicilio obtenido", response)
        );
    }

    // DELETE /api/ciudadano/domicilios/{id}
    @DeleteMapping("/domicilios/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarDomicilio(
            @PathVariable Long id) {

        domicilioService.eliminar(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Domicilio eliminado exitosamente")
        );
    }
}