package com.sansaweigh.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "registros_pesaje")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPesaje {

    @Id
    private String id;

    private String idBalanza;

    private String idPaquete;

    private double pesoEnSansas;

    private CategoriaPeso categoria;

    private EstadoPesaje estado;

    @Builder.Default
    private List<TransicionEstado> historialEstados = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
