package com.filamenthub.hub.service;

import org.springframework.stereotype.Service;

import com.filamenthub.hub.model.Data;
import com.filamenthub.hub.model.Device;
import com.filamenthub.hub.model.Measurement;
import com.filamenthub.hub.repositories.DataRepository;

import java.time.ZonedDateTime;

@Service
public class DataService {

    private final DataRepository dataRepository;

    public DataService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void createData(float value, Device device, Measurement variable, ZonedDateTime time) {
        if (device == null || variable == null || time == null) {
            throw new IllegalArgumentException("Device, Measurement, and Timestamp cannot be null");
        }
        Data data = new Data();
        data.setVariableValue(value);
        data.setDevice(device);
        data.setVariable(variable);
        data.setBaseTime(time);
        dataRepository.save(data);
    }
}
