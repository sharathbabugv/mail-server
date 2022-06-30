package com.health.application.mailserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {

    private Long id;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private List<Prescription> prescription;
}
