package com.example.demo.model;

public enum CategoriaPeso {
    LIVIANO,
    MEDIANO,
    PESADO;

    private static final double LIMITE_LIVIANO = 10.0;
    private static final double LIMITE_MEDIANO = 50.0;

    public static CategoriaPeso clasificar(double pesoEnSansas) {
        if (pesoEnSansas <= LIMITE_LIVIANO) {
            return LIVIANO;
        } else if (pesoEnSansas <= LIMITE_MEDIANO) {
            return MEDIANO;
        } else {
            return PESADO;
        }
    }
}
