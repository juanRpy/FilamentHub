package com.filamenthub.hub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.filamenthub.hub.service.DataBaseService;
import com.filamenthub.hub.service.MQTTPublishService;

@RestController
public class ConfigController {

    @Autowired
    private MQTTPublishService service;
    
    private final DataBaseService databaseService;

    public ConfigController(DataBaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Endpoint para verificar la conexión a la base de datos
     * @return ResponseEntity con el estado de la conexión
     */
    @GetMapping("/check-connection")
    public ResponseEntity<String> checkConnection() {
        if (databaseService.isConnected()) {
            return ResponseEntity.ok("Conexión a la base de datos exitosa.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en la conexión a la base de datos.");
        }
    }

    /**
     * Endpoint para publicar un mensaje en MQTT
     * @return ResponseEntity con el estado de la publicación
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publishMessage() {
        try {
            service.publishMessage("Hola desde Spring Boot");
            return ResponseEntity.ok("Mensaje publicado exitosamente.");
        } catch (Exception e) {
            // Manejo de cualquier otra excepción
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error desconocido al publicar el mensaje: " + e.getMessage());
        }
    }
}

