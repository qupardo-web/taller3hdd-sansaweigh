package com.sansaweigh.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalWeighingStateException() {
        var ex = new IllegalWeighingStateException("transición inválida");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "transición inválida");
    }

    @Test
    void handlePesajeNocturnoException() {
        var ex = new PesajeNocturnoException("horario no permitido");
        ResponseEntity<Map<String, Object>> response = handler.handleNocturno(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "horario no permitido");
    }

    @Test
    void handleBalanzaPrimaException() {
        var ex = new BalanzaPrimaException("balanza prima no permitida");
        ResponseEntity<Map<String, Object>> response = handler.handleBalanzaPrima(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleRegistroNoEncontradoException() {
        var ex = new RegistroNoEncontradoException("registro no encontrado 123");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("error", "Not Found");
    }
}
