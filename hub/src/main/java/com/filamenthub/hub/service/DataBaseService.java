package com.filamenthub.hub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.filamenthub.hub.model.Measurement;
import com.filamenthub.hub.repositories.MeasurementRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DataBaseService {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseService.class);

    private final MeasurementRepository measurementRepository;
    private final DataSource dataSource;


    public DataBaseService(MeasurementRepository measurementRepository, DataSource dataSource) {
        this.measurementRepository = measurementRepository;
        this.dataSource = dataSource;
    }

    /**
     * Verifica si hay conexión a la base de datos.
     * @return true si la conexión es válida, false en caso contrario.
     */
    public boolean isConnected() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2); // Espera hasta 2 segundos para comprobar la conexión
            logger.info("Verificación de conexión a la base de datos: {}", isValid ? "Conectado" : "Desconectado");
            return isValid;
        } catch (SQLException e) {
            logger.error("Error al verificar la conexión a la base de datos", e);
            return false;
        }
    }

    /**
     * Guarda los datos de temperatura y humedad en la base de datos.
     * @param temperature la temperatura registrada
     * @param humidity la humedad registrada
     * @return el objeto Measurement guardado
     */
    @Transactional
    public Measurement saveMeasurement(String deviceId, double temperature, double humidity) {
        Measurement data = new Measurement();
        data.setTemperature(temperature);
        data.setHumidity(humidity);
        data.setDeviceId(deviceId);
        
        try {
            Measurement savedData = measurementRepository.save(data);
            logger.info("Datos de medición guardados correctamente: {}", savedData);
            return savedData;
        } catch (Exception e) {
            logger.error("Error al guardar datos de medición en la base de datos", e);
            throw e;  // Re-lanzar la excepción para que el controlador la maneje si es necesario
        }
    }
}
