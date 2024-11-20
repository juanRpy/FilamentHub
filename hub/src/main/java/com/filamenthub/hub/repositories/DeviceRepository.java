package com.filamenthub.hub.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filamenthub.hub.model.Device;


/**
 * Repositorio JPA para la entidad que representa los dispositivos
 */
public interface DeviceRepository extends JpaRepository<Device, Long> {
   // Buscar un dispositivo por su client-id y ubicación
    Optional<Device> findByClientId(String clientId); 
}
