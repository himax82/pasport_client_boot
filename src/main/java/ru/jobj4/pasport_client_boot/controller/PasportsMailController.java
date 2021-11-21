package ru.jobj4.pasport_client_boot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.jobj4.pasport_client_boot.Communication;
import ru.jobj4.pasport_client_boot.domain.Pasport;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("pasport")
public class PasportsMailController {

    final private KafkaTemplate<String, String> kafkaTemplate;
    final private Communication communication;


    public PasportsMailController(KafkaTemplate<String, String> kafkaTemplate, Communication communication) {
        this.kafkaTemplate = kafkaTemplate;
        this.communication = communication;
    }

    @Scheduled(fixedDelay = 10000)
    public void checkPassports() throws JsonProcessingException {
        System.out.println("Start checking");
        List<Pasport> pasports = this.communication.showAll("/unavailable");
        if (!pasports.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            UUID uuid = UUID.randomUUID();
            var message = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pasports);
            System.out.println("unavaliabe in message " + uuid.toString() + " sending");
            this.sendOrder(uuid.toString(), message);
        }
    }


    @PostMapping
    public void sendOrder(String msgId, String msg) {
        kafkaTemplate.send("pasport", msgId, msg);
    }
}
