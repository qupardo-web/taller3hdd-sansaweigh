package com.example.demo.model;

import java.util.Set;

public enum EstadoPesaje {
    INGRESADO,
    PESADO,
    APROBADO,
    RECHAZADO,
    DESPACHADO;

    static {
        INGRESADO.transicionesPermitidas = Set.of(PESADO);
        PESADO.transicionesPermitidas = Set.of(APROBADO, RECHAZADO);
        APROBADO.transicionesPermitidas = Set.of(DESPACHADO);
        RECHAZADO.transicionesPermitidas = Set.of();
        DESPACHADO.transicionesPermitidas = Set.of();
    }

    private Set<EstadoPesaje> transicionesPermitidas;

    public boolean puedeTransicionarA(EstadoPesaje destino) {
        return transicionesPermitidas != null && transicionesPermitidas.contains(destino);
    }
}
