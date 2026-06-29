package com.example.demo.controller;

import com.example.demo.dto.ActualizarRegistroRequest;
import com.example.demo.dto.CrearRegistroRequest;
import com.example.demo.model.RegistroPesaje;
import com.example.demo.service.RegistroPesajeService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/registros")
public class RegistroPesajeController {

    private final RegistroPesajeService service;

    public RegistroPesajeController(RegistroPesajeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RegistroPesaje> crear(@RequestBody CrearRegistroRequest request) {
        RegistroPesaje registro = service.crearRegistro(
                request.getIdBalanza(),
                request.getIdPaquete(),
                request.getPesoEnSansas());
        return ResponseEntity.status(HttpStatus.CREATED).body(registro);
    }

    @PutMapping("/{id}/pesar")
    public ResponseEntity<RegistroPesaje> pesar(@PathVariable String id) {
        return ResponseEntity.ok(service.pesarRegistro(id));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<RegistroPesaje> aprobar(@PathVariable String id) {
        return ResponseEntity.ok(service.aprobarRegistro(id));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<RegistroPesaje> rechazar(@PathVariable String id) {
        return ResponseEntity.ok(service.rechazarRegistro(id));
    }

    @PutMapping("/{id}/despachar")
    public ResponseEntity<RegistroPesaje> despachar(@PathVariable String id) {
        return ResponseEntity.ok(service.despacharRegistro(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroPesaje> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<RegistroPesaje>> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(service.obtenerRegistros(desde, hasta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistroPesaje> actualizar(
            @PathVariable String id,
            @RequestBody ActualizarRegistroRequest request) {
        return ResponseEntity.ok(service.actualizarRegistro(id, request.getNuevoPesoEnSansas()));
    }
}
