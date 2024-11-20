package com.filamenthub.hub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * DataEntity que representa los datos de las mediciones de los dispositivos
 */
@Getter
@Setter
@Entity
@Table(name = "data")
public class Data {

    @Id
    @Column(name = "unix_time", nullable = false)
    private Long unixTime;
    private float variableValue;

    @NotNull(message = "Base time cannot be null")
    private ZonedDateTime baseTime;

    @NotNull(message = "Measurement cannot be null")
    @ManyToOne
    @JoinColumn(name = "variable_id")
    private Measurement variable;

    @NotNull(message = "Device cannot be null")
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    @PrePersist
    public void prePersist() {
        this.unixTime = System.currentTimeMillis();
        ZoneId zoneId = ZoneId.of("America/Bogota");
        this.setBaseTime(ZonedDateTime.now(zoneId));
    }
}
