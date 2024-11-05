package com.filamenthub.hub.service;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Service
public class AWSIoTMQTTClient {

    @Value("${aws.iot.endpoint}")
    private String endpoint;

    @Value("${aws.iot.clientId}")
    private String clientId;

    @Value("${aws.iot.keystorePath}")
    private String keystorePath;

    @Value("${aws.iot.keystorePassword}")
    private String keystorePassword;

    @Value("${aws.iot.rootCAPath}")
    private String rootCAPath;

    private MqttClient mqttClient;

    public MqttClient getClient() {
        return mqttClient;
    }

    @PostConstruct
    public void connect() {
        try {
            SSLContext sslContext = createSSLContext();
            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(sslContext.getSocketFactory());

            mqttClient = new MqttClient("ssl://" + endpoint + ":8883", clientId);
            mqttClient.connect(options);
            System.out.println("[MQTT] Conectado a AWS IoT");

        } catch (Exception e) {
            System.err.println("Error en la conexión a AWS IoT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                System.out.println("[MQTT] Desconectado de AWS IoT");
            }
        } catch (MqttException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }

    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

        KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream keystoreFile = new FileInputStream(keystorePath)) {
            clientKeyStore.load(keystoreFile, keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientKeyStore, keystorePassword.toCharArray());

        KeyStore rootCAKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        rootCAKeyStore.load(null, null);
        try (FileInputStream rootCAStream = new FileInputStream(rootCAPath)) {
            java.security.cert.Certificate rootCA = java.security.cert.CertificateFactory.getInstance("X.509")
                    .generateCertificate(rootCAStream);
            rootCAKeyStore.setCertificateEntry("rootCA", rootCA);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(rootCAKeyStore);

        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}
