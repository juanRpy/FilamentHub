package com.filamenthub.hub.service;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MQTTSubscriptionService {

    private final AWSIoTMQTTClient awsIoTMQTTClient;

    @Value("${aws.iot.topic}")
    private String topic;

    public MQTTSubscriptionService(AWSIoTMQTTClient awsIoTMQTTClient) {
        this.awsIoTMQTTClient = awsIoTMQTTClient;
    }

    @PostConstruct
    public void subscribeToTopic() {
        try {
            MqttClient mqttClient = awsIoTMQTTClient.getClient();
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topic, this::handleMessage);
                System.out.println("[MQTT] Suscrito al tema: " + topic);
            }
        } catch (MqttException e) {
            System.err.println("Error al suscribirse al tema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("[MQTT] Mensaje recibido en el tema '" + topic + "': " + payload);
    }
}

