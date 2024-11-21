package com.filamenthub.hub.controller;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filamenthub.hub.model.Device;
import com.filamenthub.hub.model.Measurement;
import com.filamenthub.hub.model.User;
import com.filamenthub.hub.service.DataService;
import com.filamenthub.hub.service.DeviceService;
import com.filamenthub.hub.service.MeasurementService;
import com.filamenthub.hub.service.UserService;

import jakarta.annotation.PostConstruct;

import javax.net.ssl.SSLSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Clase que se encarga de recibir y procesar los mensajes MQTT
 */
@Component
@RestController
public class MqttController {

    private MqttAsyncClient client;   
    private final DataService dataService;
    private final DeviceService deviceService;
    private final MeasurementService measurementService;
    private final UserService userService;                      // Cliente MQTT de Eclipse Paho                  // Servicio de datos (mediciones)
    private static final Logger logger = LoggerFactory.getLogger(MqttController.class);


    @Value("${mqtt.broker.url}")    // Lee de application.properties el valor de mqtt.broker.url
    private String brokerUrl;
    @Value("${mqtt.client.id}")     // Lee de application.properties el valor de mqtt.client.id
    private String clientId;
    @Value("${mqtt.username}")      // Lee de application.properties el valor de mqtt.username
    private String username;
    @Value("${mqtt.password}")      // Lee de application.properties el valor de mqtt.password
    private String password;
    @Value("${mqtt.qos}")           // Lee de application.properties el valor de mqtt.qos
    private int qos;
    @Value("${mqtt.topic}")         // Lee de application.properties el valor de mqtt.topic
    private String topic;


    /**
     * Constructor de la clase MqttController que inyecta los servicios necesarios
     */
    public MqttController(UserService userService, MeasurementService measurementService, DeviceService deviceService, DataService dataService) {
        this.userService = userService;
        this.measurementService = measurementService;
        this.deviceService = deviceService;
        this.dataService = dataService;
    }

    /**
     * Metodo de inicialización que se ejecuta al arrancar la aplicación
     */
    @PostConstruct
    public void init() throws CertificateException, KeyStoreException, MqttException, NoSuchAlgorithmException, IOException, KeyManagementException {

        // Cargar el certificado de Let's Encrypt para TLS
        SSLSocketFactory socketFactory = getSocketFactoryWithLetsEncrypt();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(this.username);
        options.setPassword(this.password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(60);
        options.setConnectionTimeout(30);
        options.setCleanSession(false);
        options.setSocketFactory(socketFactory);  // Configuración de TLS

        // Crear el cliente MQTT con el ID, el broker URL y con persistencia en memoria
        client = new MqttAsyncClient(this.brokerUrl, this.generateClientId(), new MemoryPersistence());
        MqttCallback callback = new MqttCallback() {

            // Metodo que se ejecuta cuando se pierde la conexión
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Conexión perdida: {}", cause.getMessage());
                logger.info("Intento de reconexion al broker MQTT...");
                connect(options, true); // Intentar reconectar
                logger.info("Reconectado al broker MQTT");
            }

            // Metodo que se ejecuta cuando se recibe un mensaje
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                processMessage(topic, message); // Procesar el mensaje recibido
            }

            // Metodo que se ejecuta cuando se completa la entrega de un mensaje
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.info("Entrega completa");
            }
        };
        client.setCallback(callback);  // Establecer el callback
        this.connect(options, false); // Conectar al broker MQTT
    }

    /**
     * Metodo que se encarga de conectar el cliente MQTT
     */
    private void connect(MqttConnectOptions options, boolean reconnect) {
        try {
            if (reconnect) {
                logger.info("Reconectando al broker MQTT: {}", this.brokerUrl);
                if (client.isConnected()) {
                    logger.info("El cliente aun esta en estado conectado");
                    IMqttToken disconnectToken = this.client.disconnect();
                    disconnectToken.waitForCompletion(10000);
                    logger.info("Desconexion exitosa");
                }
            }
            logger.info("Conectando al broker MQTT: {}", this.brokerUrl);
            client.connect(options).waitForCompletion();
            logger.info("Conexion exitosa");
            client.subscribe(this.topic, this.qos);
            logger.info("Suscrito al topico: {}", this.topic);
        } catch (MqttException e) {
            logger.error("Error al conectar: {}", e.getMessage());
        }
    }

    /**
     * Metodo que se encarga de procesar los mensajes recibidos
     *
     * @param topic   Tópico del mensaje
     * @param message Mensaje MQTT
     */
    private void processMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        logger.info("Mensaje recibido en el topic '{}': {}", topic, payload);

        try {
            // Convertir el payload JSON a un Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonPayload = objectMapper.readValue(payload, Map.class);

            // Obtener el usuario (puedes modificar la lógica si el usuario depende de otro campo)
            String username = "default_user"; // Cambiar por el nombre de usuario real
            User userObj = userService.getUser(username);

            // Obtener el ID del dispositivo (supone que está incluido en el JSON)
            String deviceId = (String) jsonPayload.get("device_id");

            // Crear o recuperar el dispositivo
            Device deviceObj = deviceService.getOrCreateDevice(deviceId, userObj);

            // Iterar sobre las variables del JSON y registrar las mediciones
            for (Map.Entry<String, Object> entry : jsonPayload.entrySet()) {
                String variable = entry.getKey();

                // Ignorar claves específicas que no representan mediciones
                if ("device_id".equals(variable)) {
                    continue;
                }

                float value = ((Number) entry.getValue()).floatValue();

                // Obtener o crear la variable de medición
                Measurement variableObj = measurementService.getOrCreateMeasurement(variable);

                // Registrar la medición en la base de datos
                dataService.createData(value, deviceObj, variableObj, ZonedDateTime.now());
            }

            logger.info("Datos procesados y registrados correctamente para el dispositivo '{}'", deviceId);

        } catch (Exception e) {
            logger.error("Error al procesar el mensaje MQTT: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica un mensaje en un tópico dinámico basado en deviceId y subTopic.
     *
     * @param deviceId ID del dispositivo (por ejemplo, "DeviceA", "DeviceB").
     * @param subTopic Sub-tópico (por ejemplo, "motor", "cooler").
     * @param payload  Mensaje JSON a publicar.
     * @return Confirmación de la publicación.
     */
    @PostMapping("/{deviceId}/sub/{subTopic}")
    public String publishDynamicMessage(
            @PathVariable String deviceId,
            @PathVariable String subTopic,
            @RequestBody String payload) {
        // Construcción del tópico dinámico
        String dynamicTopic = deviceId + "/sub/" + subTopic;

        try {
            MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
            mqttMessage.setQos(this.qos); // Utiliza el QoS definido en application.properties
            client.publish(dynamicTopic, mqttMessage); // Publica en el tópico dinámico

            logger.info("Mensaje publicado al tópico {}: {}", dynamicTopic, payload);
            return "Mensaje publicado exitosamente en el tópico: " + dynamicTopic;
        } catch (MqttException e) {
            logger.error("Error al publicar mensaje en el tópico {}: {}", dynamicTopic, e.getMessage());
            return "Error al publicar el mensaje: " + e.getMessage();
        }
    }


    /**
     * Metodo que se encarga de configurar el contexto SSL con el certificado público de Let's Encrypt
     *
     * @return SocketFactory configurado con el certificado de Let's Encrypt
     * @throws Exception Excepción en caso de error al cargar el certificado
     */
    private SSLSocketFactory getSocketFactoryWithLetsEncrypt() throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, KeyManagementException {
        // Cargar el certificado público de Let's Encrypt desde src/main/resources
        InputStream fis = getClass().getClassLoader().getResourceAsStream("certs/isrgrootx1.pem");

        if (fis == null) {
            throw new FileNotFoundException("No se pudo encontrar el archivo de certificado 'isrgrootx1.pem'");
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(fis);

        // Configurar KeyStore con el certificado público
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("caCert", caCert);

        // Configurar TrustManager con el KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        // Crear el contexto SSL con el TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    /**
     * Metodo que se encarga de generar un ID de cliente único MQTT
     *
     * @return ID de cliente único
     */
    public String generateClientId() {
        String hostname = "unknown";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Error al obtener el nombre del host: {}", e.getMessage());
        }
        long timestamp = System.currentTimeMillis();
        return this.clientId + "-" + hostname + "-" + timestamp;
    }
}

