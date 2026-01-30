package com.leancam.employeemanager.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEvent {

    private String eventType;
    private Long employeeId;
    private Instant eventTime;
}
