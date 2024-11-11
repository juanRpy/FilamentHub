package com.filamenthub.hub.service;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MQTTSubscriptionService {

    private final AWSIoTMQTTClient awsIoTMQTTClient;
    private final DataBaseService dataBaseService;

    @Value("${aws.iot.topic}")
    private String topic;

    public MQTTSubscriptionService(AWSIoTMQTTClient awsIoTMQTTClient, DataBaseService dataBaseService) {
        this.awsIoTMQTTClient = awsIoTMQTTClient;
        this.dataBaseService = dataBaseService;
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
        try {
            String payload = new String(message.getPayload());
            System.out.println("[MQTT] Mensaje recibido en el tema '" + topic + "': " + payload);

            JSONObject json = new JSONObject(payload);
            double temperature = json.getDouble("temperatura");
            double humidity = json.getDouble("humedad");
            String deviceId = json.getString("device_id");

            dataBaseService.saveMeasurement(deviceId, temperature, humidity);

        } catch (Exception e) {
            System.err.println("Error al procesar el mensaje MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


