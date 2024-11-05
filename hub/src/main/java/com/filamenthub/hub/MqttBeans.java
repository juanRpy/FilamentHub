package com.filamenthub.hub;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import org.springframework.messaging.MessageHandler;

import com.filamenthub.hub.controller.MqttController;



@Configuration
public class MqttBeans {

    @Autowired
    private MqttController mqttController;

    public MqttPahoClientFactory mqttClientFactory(){
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{"tcp://localhost:1883"});
        options.setUserName("admin");
        String pass = "12345678";
        options.setPassword(pass.toCharArray());
        options.setCleanSession(true);
    
        factory.setConnectionOptions(options);

        return factory; 

    }
    
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(  
                        "SpringBootClient",  
                        mqttClientFactory(),    // Reemplaza por tu clientId
                        "maquina/temperatura", "maquina/humedad"  // Tópicos a los que te suscribes
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
            String payload = message.getPayload().toString();

            System.out.println("Mensaje recibido del tópico: " + topic);
            System.out.println("Contenido: " + payload);
            
            // Aquí puedes procesar los datos recibidos, almacenarlos en una base de datos o mostrarlos en una página web
            if (topic.equals("maquina/temperatura")) {
                mqttController.actualizarDatos(payload, mqttController.getUltimaHumedad()); // Suponiendo que tienes un método para obtener la última humedad
            } else if (topic.equals("maquina/humedad")) {
                mqttController.actualizarDatos(mqttController.getUltimaTemperatura(), payload); // Suponiendo que tienes un método para obtener la última temperatura
            }
        };
    }

}
