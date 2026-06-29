package com.sansaweigh.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoPesajeTest {

    @Test
    void ingresadoSoloPuedeTransicionarAPesado() {
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.PESADO)).isTrue();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.APROBADO)).isFalse();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isFalse();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isFalse();
    }

    @Test
    void pesadoPuedeTransicionarAAprobadoYRechazado() {
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.APROBADO)).isTrue();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isTrue();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.INGRESADO)).isFalse();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isFalse();
    }

    @Test
    void aprobadoSoloPuedeTransicionarADespachado() {
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isTrue();
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.PESADO)).isFalse();
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isFalse();
    }

    @Test
    void rechazadoNoPuedeTransicionar() {
        for (EstadoPesaje estado : EstadoPesaje.values()) {
            assertThat(EstadoPesaje.RECHAZADO.puedeTransicionarA(estado)).isFalse();
        }
    }

    @Test
    void despachadoNoPuedeTransicionar() {
        for (EstadoPesaje estado : EstadoPesaje.values()) {
            assertThat(EstadoPesaje.DESPACHADO.puedeTransicionarA(estado)).isFalse();
        }
    }
}
