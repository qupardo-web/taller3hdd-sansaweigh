package com.sansaweigh.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaPesoTest {

    @ParameterizedTest
    @CsvSource({
            "0.0, LIVIANO",
            "5.0, LIVIANO",
            "10.0, LIVIANO",
            "10.1, MEDIANO",
            "25.0, MEDIANO",
            "50.0, MEDIANO",
            "50.1, PESADO",
            "100.0, PESADO"
    })
    void clasificar(double peso, CategoriaPeso expected) {
        assertThat(CategoriaPeso.clasificar(peso)).isEqualTo(expected);
    }

    @Test
    void pesoNegativoEsLiviano() {
        assertThat(CategoriaPeso.clasificar(-5.0)).isEqualTo(CategoriaPeso.LIVIANO);
    }
}
