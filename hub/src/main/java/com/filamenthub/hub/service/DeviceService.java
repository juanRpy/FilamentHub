package com.filamenthub.hub.service;

import org.springframework.stereotype.Service;

import com.filamenthub.hub.model.Device;
import com.filamenthub.hub.model.User;
import com.filamenthub.hub.repositories.DeviceRepository;

import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device getOrCreateDevice(String clientId, User user) {
        // Busca si ya existe un dispositivo con el clientId 
        Optional<Device> existingDevice = deviceRepository.findByClientId(clientId);

        return existingDevice.orElseGet(() -> {
            // Crear un nuevo dispositivo si no existe
            Device newDevice = new Device();
            newDevice.setClientId(clientId);
            newDevice.setUser(user);
            return deviceRepository.save(newDevice);
        });
    }
}
