package com.filamenthub.hub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.iot.client.AWSIotException;
import com.filamenthub.hub.service.MQTTPublishService;

@RestController
public class ConfigController {

    @Autowired
    MQTTPublishService service;

    @PostMapping("/publish")
    public String publishMessaged() throws AWSIotException {
        service.publishMessage("Hola desde spring boot");
        return "message published successfully";
    }
}
