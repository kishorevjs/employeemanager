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
        kafkaTemplate.send(topic, key, employeeEvent)
                .whenComplete((employee, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Error sending event to Kafka: " + throwable.getMessage());
                    }
                    else  {
                        var meta = employee.getRecordMetadata();
                        System.out.println("Published to topic " + meta.topic()
                                + " partition " + meta.partition()
                                + " offset " + meta.offset());
                    }
                });
    }
}
