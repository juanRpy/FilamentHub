package com.filamenthub.hub.service;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MQTTPublishService {

    private final AWSIoTMQTTClient awsIoTMQTTClient;

    @Value("${aws.iot.topic}")
    private String topic;

    public MQTTPublishService(AWSIoTMQTTClient awsIoTMQTTClient) {
        this.awsIoTMQTTClient = awsIoTMQTTClient;
    }

    public void publishMessage(String message) {
        try {
            MqttClient mqttClient = awsIoTMQTTClient.getClient();
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttClient.publish(topic, mqttMessage);
                System.out.println("[MQTT] Mensaje publicado en el tema '" + topic + "': " + message);
            }
        } catch (MqttException e) {
            System.err.println("Error al publicar el mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
