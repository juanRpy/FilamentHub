package com.filamenthub.hub.service;


import org.springframework.stereotype.Service;

import com.filamenthub.hub.model.Measurement;
import com.filamenthub.hub.repositories.MeasurementRepository;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepository;

    public MeasurementService(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    public Measurement getOrCreateMeasurement(String variable) {
        return measurementRepository.findByName(variable).orElseGet(() -> {
            Measurement newMeasurement = new Measurement();
            newMeasurement.setName(variable);
            newMeasurement.setUnit("default_unit"); // Puedes personalizar la unidad
            return measurementRepository.save(newMeasurement);
        });
    }
}
