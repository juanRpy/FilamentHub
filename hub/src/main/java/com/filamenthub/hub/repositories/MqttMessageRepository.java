package com.filamenthub.hub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.filamenthub.hub.model.MqttMessage;

/**
 * Repositorio JPA para la entidad que representa los mensajes MQTT
 */
public interface MqttMessageRepository extends JpaRepository<MqttMessage, Long> {
}