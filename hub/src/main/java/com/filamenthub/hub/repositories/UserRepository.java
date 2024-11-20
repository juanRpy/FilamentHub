package com.filamenthub.hub.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filamenthub.hub.model.User;

/**
 * Repositorio JPA para la entidad que representa los usuarios MQTT
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Buscar un usuario por su nombre de usuario
    Optional<User> findByUsername(String username);
}