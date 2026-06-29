package com.sansaweigh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sansaweigh.dto.ActualizarRegistroRequest;
import com.sansaweigh.dto.CrearRegistroRequest;
import com.sansaweigh.exception.GlobalExceptionHandler;
import com.sansaweigh.exception.IllegalWeighingStateException;
import com.sansaweigh.exception.RegistroNoEncontradoException;
import com.sansaweigh.model.CategoriaPeso;
import com.sansaweigh.model.EstadoPesaje;
import com.sansaweigh.model.RegistroPesaje;
import com.sansaweigh.service.RegistroPesajeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RegistroPesajeControllerTest {

    @Mock
    private RegistroPesajeService service;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RegistroPesajeController controller = new RegistroPesajeController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private RegistroPesaje buildRegistro() {
        return RegistroPesaje.builder()
                .id("abc123")
                .idBalanza("101")
                .idPaquete("PKG-001")
                .pesoEnSansas(8.0)
                .categoria(CategoriaPeso.LIVIANO)
                .estado(EstadoPesaje.INGRESADO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void crearRegistroDevuelve201() throws Exception {
        when(service.crearRegistro(anyString(), anyString(), anyDouble())).thenReturn(buildRegistro());

        CrearRegistroRequest request = new CrearRegistroRequest("101", "PKG-001", 8.0);

        mockMvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idBalanza").value("101"))
                .andExpect(jsonPath("$.estado").value("INGRESADO"));
    }

    @Test
    void pesarRegistroDevuelve200() throws Exception {
        when(service.pesarRegistro("abc123")).thenReturn(buildRegistro());

        mockMvc.perform(put("/api/registros/abc123/pesar"))
                .andExpect(status().isOk());
    }

    @Test
    void aprobarRegistroDevuelve200() throws Exception {
        when(service.aprobarRegistro("abc123")).thenReturn(buildRegistro());

        mockMvc.perform(put("/api/registros/abc123/aprobar"))
                .andExpect(status().isOk());
    }

    @Test
    void rechazarRegistroDevuelve200() throws Exception {
        when(service.rechazarRegistro("abc123")).thenReturn(buildRegistro());

        mockMvc.perform(put("/api/registros/abc123/rechazar"))
                .andExpect(status().isOk());
    }

    @Test
    void despacharRegistroDevuelve200() throws Exception {
        when(service.despacharRegistro("abc123")).thenReturn(buildRegistro());

        mockMvc.perform(put("/api/registros/abc123/despachar"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPorIdDevuelve200() throws Exception {
        when(service.obtenerPorId("abc123")).thenReturn(buildRegistro());

        mockMvc.perform(get("/api/registros/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc123"));
    }

    @Test
    void obtenerPorIdInexistenteDevuelve404() throws Exception {
        when(service.obtenerPorId("999")).thenThrow(new RegistroNoEncontradoException("no encontrado"));

        mockMvc.perform(get("/api/registros/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarSinFechasDevuelve200() throws Exception {
        when(service.obtenerTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/registros"))
                .andExpect(status().isOk());
    }

    @Test
    void listarSoloConDesdeDevuelveTodos() throws Exception {
        when(service.obtenerTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/registros")
                        .param("desde", "2026-01-01"))
                .andExpect(status().isOk());
    }

    @Test
    void listarSoloConHastaDevuelveTodos() throws Exception {
        when(service.obtenerTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/registros")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    void listarConFechasDevuelve200() throws Exception {
        when(service.obtenerRegistros(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/registros")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarRegistroDevuelve200() throws Exception {
        when(service.actualizarRegistro(anyString(), anyDouble())).thenReturn(buildRegistro());

        ActualizarRegistroRequest request = new ActualizarRegistroRequest(45.0);

        mockMvc.perform(put("/api/registros/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void transicionInvalidaDevuelve400() throws Exception {
        when(service.pesarRegistro("abc123"))
                .thenThrow(new IllegalWeighingStateException("transición inválida"));

        mockMvc.perform(put("/api/registros/abc123/pesar"))
                .andExpect(status().isBadRequest());
    }
}
