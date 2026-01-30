package com.leancam.employeemanager.kafka;

import com.leancam.employeemanager.event.EmployeeEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeEventConsumer {

    @KafkaListener(topics = "employee.events", groupId = "employee-manager")
    public void listen(EmployeeEvent  employeeEvent) {
        System.out.println("Consumed Event: " + employeeEvent);
    }
}
