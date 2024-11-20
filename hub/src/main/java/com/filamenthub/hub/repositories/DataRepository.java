package com.filamenthub.hub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filamenthub.hub.model.Data;

/**
 * Repositorio JPA para la entidad que representa los datos de las mediciones de los dispositivos
 */
public interface DataRepository extends JpaRepository<Data, Long> {
}
