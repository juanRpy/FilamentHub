package com.filamenthub.hub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Aquí se especifica la estrategia de generación
    private Long id;

    private LocalDateTime time;
    private String deviceId;
    private Double temperature;
    private Double humidity;

    public Measurement() {
        this.time = LocalDateTime.now();
    }
}