package com.example.invoice_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    private String vehicleId;
    private String make;
    private String model;
    private String year;
    private String color;
    private String vin;
    private double price;
    private String engineNumber;
    private String chassisNumber;
}