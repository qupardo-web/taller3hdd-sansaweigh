package com.example.demo.integration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleSpecification {
    private String id;
    private String name;
    private String brand;
    private double maxCapacity;
    private double precision;
    private double lastCalibrationOffset;
}
