package com.health.application.mailserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    private Long id;
    private String name;
    private boolean morning;
    private boolean afternoon;
    private boolean night;
    private String numberOfDays;

    private Diagnosis diagnosis;
}
