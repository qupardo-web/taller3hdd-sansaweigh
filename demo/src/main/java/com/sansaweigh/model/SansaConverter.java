package com.sansaweigh.model;

public final class SansaConverter {

    public static final double KG_PER_SANSA = 1.337;

    private SansaConverter() {
    }

    public static double sansasToKg(double sansas) {
        return sansas * KG_PER_SANSA;
    }

    public static double kgToSansas(double kg) {
        return kg / KG_PER_SANSA;
    }
}
