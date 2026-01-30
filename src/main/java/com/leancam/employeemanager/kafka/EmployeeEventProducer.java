package com.leancam.employeemanager.kafka;

import com.leancam.employeemanager.event.EmployeeEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmployeeEventProducer {

    private final KafkaTemplate<String, EmployeeEvent> kafkaTemplate;

    public EmployeeEventProducer(KafkaTemplate<String, EmployeeEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, EmployeeEvent employeeEvent) {
        kafkaTemplate.send(topic, key, employeeEvent);
    }
}
