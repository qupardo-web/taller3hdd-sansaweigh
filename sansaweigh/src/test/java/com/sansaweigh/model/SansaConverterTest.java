package com.sansaweigh.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SansaConverterTest {

    @Test
    void sansasToKg() {
        assertThat(SansaConverter.sansasToKg(1.0)).isCloseTo(1.337, within(0.001));
        assertThat(SansaConverter.sansasToKg(10.0)).isCloseTo(13.37, within(0.001));
        assertThat(SansaConverter.sansasToKg(0.0)).isCloseTo(0.0, within(0.001));
    }

    @Test
    void kgToSansas() {
        assertThat(SansaConverter.kgToSansas(1.337)).isCloseTo(1.0, within(0.001));
        assertThat(SansaConverter.kgToSansas(13.37)).isCloseTo(10.0, within(0.001));
        assertThat(SansaConverter.kgToSansas(0.0)).isCloseTo(0.0, within(0.001));
    }

    @Test
    void idaYVuelta() {
        double original = 50.0;
        double enKg = SansaConverter.sansasToKg(original);
        double vuelta = SansaConverter.kgToSansas(enKg);
        assertThat(vuelta).isCloseTo(original, within(0.001));
    }

    @Test
    void constanteCorrecta() {
        assertThat(SansaConverter.KG_PER_SANSA).isEqualTo(1.337);
    }
}
