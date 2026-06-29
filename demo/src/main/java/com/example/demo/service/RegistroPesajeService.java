package com.example.demo.service;

import com.example.demo.exception.IllegalWeighingStateException;
import com.example.demo.exception.PesajeNocturnoException;
import com.example.demo.exception.BalanzaPrimaException;
import com.example.demo.exception.RegistroNoEncontradoException;
import com.example.demo.integration.ExternalScaleClient;
import com.example.demo.model.CategoriaPeso;
import com.example.demo.model.EstadoPesaje;
import com.example.demo.model.RegistroPesaje;
import com.example.demo.model.TransicionEstado;
import com.example.demo.repository.RegistroPesajeRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistroPesajeService {

    private final RegistroPesajeRepository repository;
    private final ExternalScaleClient externalScaleClient;

    public RegistroPesajeService(RegistroPesajeRepository repository,
                                  ExternalScaleClient externalScaleClient) {
        this.repository = repository;
        this.externalScaleClient = externalScaleClient;
    }

    public RegistroPesaje crearRegistro(String idBalanza, String idPaquete, double pesoEnSansas) {
        CategoriaPeso categoria = CategoriaPeso.clasificar(pesoEnSansas);

        if (categoria == CategoriaPeso.PESADO) {
            validarRestriccionHoraria();
            validarBalanzaPrima(idBalanza);
        }

        LocalDateTime now = LocalDateTime.now();
        RegistroPesaje registro = RegistroPesaje.builder()
                .idBalanza(idBalanza)
                .idPaquete(idPaquete)
                .pesoEnSansas(pesoEnSansas)
                .categoria(categoria)
                .estado(EstadoPesaje.INGRESADO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        registro.getHistorialEstados().add(
                new TransicionEstado(EstadoPesaje.INGRESADO, now));

        return repository.save(registro);
    }

    public RegistroPesaje pesarRegistro(String id) {
        RegistroPesaje registro = obtenerPorId(id);

        if (!registro.getEstado().puedeTransicionarA(EstadoPesaje.PESADO)) {
            throw new IllegalWeighingStateException(
                    "No se puede transicionar de " + registro.getEstado() + " a PESADO");
        }

        if (registro.getCategoria() == CategoriaPeso.PESADO) {
            validarRestriccionHoraria();
            validarBalanzaPrima(registro.getIdBalanza());
        }

        transicionar(registro, EstadoPesaje.PESADO);
        return repository.save(registro);
    }

    public RegistroPesaje aprobarRegistro(String id) {
        RegistroPesaje registro = obtenerPorId(id);

        if (!registro.getEstado().puedeTransicionarA(EstadoPesaje.APROBADO)) {
            throw new IllegalWeighingStateException(
                    "No se puede transicionar de " + registro.getEstado() + " a APROBADO");
        }

        transicionar(registro, EstadoPesaje.APROBADO);
        return repository.save(registro);
    }

    public RegistroPesaje rechazarRegistro(String id) {
        RegistroPesaje registro = obtenerPorId(id);

        if (!registro.getEstado().puedeTransicionarA(EstadoPesaje.RECHAZADO)) {
            throw new IllegalWeighingStateException(
                    "No se puede transicionar de " + registro.getEstado() + " a RECHAZADO");
        }

        transicionar(registro, EstadoPesaje.RECHAZADO);
        return repository.save(registro);
    }

    public RegistroPesaje despacharRegistro(String id) {
        RegistroPesaje registro = obtenerPorId(id);

        if (!registro.getEstado().puedeTransicionarA(EstadoPesaje.DESPACHADO)) {
            throw new IllegalWeighingStateException(
                    "No se puede transicionar de " + registro.getEstado() + " a DESPACHADO");
        }

        transicionar(registro, EstadoPesaje.DESPACHADO);
        return repository.save(registro);
    }

    public RegistroPesaje obtenerPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "Registro de pesaje no encontrado con ID: " + id));
    }

    public List<RegistroPesaje> obtenerRegistros(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);
        return repository.findByCreatedAtBetween(inicio, fin);
    }

    public RegistroPesaje actualizarRegistro(String id, double nuevoPesoEnSansas) {
        RegistroPesaje registro = obtenerPorId(id);

        if (registro.getEstado() != EstadoPesaje.INGRESADO) {
            throw new IllegalWeighingStateException(
                    "Solo se puede modificar un registro en estado INGRESADO");
        }

        registro.setPesoEnSansas(nuevoPesoEnSansas);
        registro.setCategoria(CategoriaPeso.clasificar(nuevoPesoEnSansas));
        registro.setUpdatedAt(LocalDateTime.now());

        return repository.save(registro);
    }

    private void validarRestriccionHoraria() {
        LocalTime ahora = LocalTime.now();
        if (ahora.isAfter(LocalTime.of(20, 0))
                || ahora.isBefore(LocalTime.of(6, 0))) {
            throw new PesajeNocturnoException(
                    "No se puede pesar paquetes pesados entre 20:00 y 06:00");
        }
    }

    private void validarBalanzaPrima(String idBalanza) {
        int idNumerico;
        try {
            idNumerico = Integer.parseInt(idBalanza);
        } catch (NumberFormatException e) {
            return;
        }

        if (esPrimo(idNumerico) && LocalDate.now().getDayOfMonth() % 2 != 0) {
            throw new BalanzaPrimaException(
                    "Balanza con ID primo no puede registrar paquetes pesados en días impares");
        }

        externalScaleClient.getScaleSpecifications(idBalanza);
    }

    private boolean esPrimo(int n) {
        if (n < 2) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    private void transicionar(RegistroPesaje registro, EstadoPesaje nuevoEstado) {
        LocalDateTime now = LocalDateTime.now();
        registro.setEstado(nuevoEstado);
        registro.setUpdatedAt(now);
        registro.getHistorialEstados().add(new TransicionEstado(nuevoEstado, now));
    }
}
