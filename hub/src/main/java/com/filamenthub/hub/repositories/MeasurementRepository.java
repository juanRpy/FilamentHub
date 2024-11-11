package com.filamenthub.hub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filamenthub.hub.model.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
}
