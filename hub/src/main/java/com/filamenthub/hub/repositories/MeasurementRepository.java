package com.filamenthub.hub.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filamenthub.hub.model.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    // Buscar una medida por su nombre
    Optional<Measurement> findByName(String name);
}
