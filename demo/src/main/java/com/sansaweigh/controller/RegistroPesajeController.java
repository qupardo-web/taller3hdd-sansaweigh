package com.sansaweigh.controller;

import com.sansaweigh.dto.ActualizarRegistroRequest;
import com.sansaweigh.dto.CrearRegistroRequest;
import com.sansaweigh.model.RegistroPesaje;
import com.sansaweigh.service.RegistroPesajeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/registros")
@Tag(name = "Registro de Pesaje", description = "CRUD y transiciones de estado del sistema SansaWeigh")
public class RegistroPesajeController {

    private final RegistroPesajeService service;

    public RegistroPesajeController(RegistroPesajeService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo registro de pesaje", description = "Clasifica el paquete y valida reglas de negocio (horaria, balanza prima)")
    public ResponseEntity<RegistroPesaje> crear(@RequestBody CrearRegistroRequest request) {
        RegistroPesaje registro = service.crearRegistro(
                request.getIdBalanza(),
                request.getIdPaquete(),
                request.getPesoEnSansas());
        return ResponseEntity.status(HttpStatus.CREATED).body(registro);
    }

    @PutMapping("/{id}/pesar")
    @Operation(summary = "Pesar un registro", description = "Transiciona de INGRESADO a PESADO")
    public ResponseEntity<RegistroPesaje> pesar(@PathVariable String id) {
        return ResponseEntity.ok(service.pesarRegistro(id));
    }

    @PutMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar un registro", description = "Transiciona de PESADO a APROBADO")
    public ResponseEntity<RegistroPesaje> aprobar(@PathVariable String id) {
        return ResponseEntity.ok(service.aprobarRegistro(id));
    }

    @PutMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar un registro", description = "Transiciona de PESADO a RECHAZADO")
    public ResponseEntity<RegistroPesaje> rechazar(@PathVariable String id) {
        return ResponseEntity.ok(service.rechazarRegistro(id));
    }

    @PutMapping("/{id}/despachar")
    @Operation(summary = "Despachar un registro", description = "Transiciona de APROBADO a DESPACHADO")
    public ResponseEntity<RegistroPesaje> despachar(@PathVariable String id) {
        return ResponseEntity.ok(service.despacharRegistro(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro por ID")
    public ResponseEntity<RegistroPesaje> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar registros (todos o por rango de fechas)")
    public ResponseEntity<List<RegistroPesaje>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(service.obtenerRegistros(desde, hasta));
        }
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar peso de un registro", description = "Solo permitido en estado INGRESADO")
    public ResponseEntity<RegistroPesaje> actualizar(
            @PathVariable String id,
            @RequestBody ActualizarRegistroRequest request) {
        return ResponseEntity.ok(service.actualizarRegistro(id, request.getNuevoPesoEnSansas()));
    }
}
