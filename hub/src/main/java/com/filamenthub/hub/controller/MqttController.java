package com.filamenthub.hub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MqttController {

    private String ultimaTemperatura = "No disponible";
    private String ultimaHumedad = "No disponible";

    // Este método debe ser llamado dentro del handler de MQTT
    public void actualizarDatos(String temperatura, String humedad) {
        this.ultimaTemperatura = temperatura;
        this.ultimaHumedad = humedad;
    }

    public String getUltimaTemperatura() {
        return ultimaTemperatura;
    }

    public String getUltimaHumedad() {
        return ultimaHumedad;
    }

    @GetMapping("/datos")
    public String mostrarDatos(Model model) {
        model.addAttribute("temperatura", ultimaTemperatura);
        model.addAttribute("humedad", ultimaHumedad);
        return "data";  // La vista "data.html" mostrará los valores
    }

}

