package com.health.application.mailserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class KafkaListeners {

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "topic-send-email", groupId = "groupId", containerFactory = "kafkaListenerContainerFactory")
    void listener(@Payload IllnessMail illnessMail, @Headers MessageHeaders messageHeaders) throws MessagingException, JsonProcessingException {
        log.info("Payload received : {}", illnessMail);
        sendEmailToDoctor(illnessMail);
    }

    @KafkaListener(topics = "topic-send-prescription", groupId = "groupId1", containerFactory = "diagnosisMailConcurrentKafkaListenerContainerFactory")
    void diagnosisListener(@Payload DiagnosisMail diagnosisMail, @Headers MessageHeaders messageHeaders) throws MessagingException, JsonProcessingException {
        sendEmailToPatient(diagnosisMail);
    }

    public void sendEmailToPatient(DiagnosisMail diagnosisMail) throws MessagingException {
        String from = "noreplymedicalapp@gmail.com";
        String to = diagnosisMail.getPatientEmail();

        log.info("to email : {}", to);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setSubject("MedApp | Hi " + diagnosisMail.getPatientName() + "! Please find your prescription for your appointment Id : " + diagnosisMail.getAppointmentId());
        helper.setFrom(from);
        helper.setTo(to);

        StringBuilder stringBuilder = new StringBuilder();
        int counter = 1;
        for (Prescription prescription : diagnosisMail.getPrescription()) {
            String morning = prescription.isMorning() ? "1" : "0";
            String afternoon = prescription.isAfternoon() ? "1" : "0";
            String night = prescription.isNight() ? "1" : "0";
            stringBuilder.append("<br><br>");
            stringBuilder
                    .append(counter).append(". ")
                    .append(prescription.getName()).append(" for ").append(prescription.getNumberOfDays()).append(" in the following order ")
                    .append("(")
                    .append(morning)
                    .append("-")
                    .append(afternoon)
                    .append("-")
                    .append(night)
                    .append(")");
            stringBuilder.append("<br>");
            counter++;
        }


        String text = "<h1>Prescription</h1>\n" +
                "<p>Please find the prescription details below</p>" +
                "<p><b>Prescription: </b>" + "\n" + stringBuilder + "</p>\n\n" +
                "<p><b>Doctor Email: </b>" + diagnosisMail.getDoctorEmail() + "</p>\n\n" +
                "<p><b>Appointment ID: </b>" + diagnosisMail.getAppointmentId() + "</p>\n\n" +
                "<p><b>Doctor Name: </b>" + diagnosisMail.getDoctorName() + "</p>\n\n" +
                "<p><b>Time: </b>" + getTime() + "</p>\n\n";
        helper.setText(text, true);

        javaMailSender.send(message);
    }

    private String getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        return now.format(format);
    }

    public void sendEmailToDoctor(IllnessMail illnessMail) throws MessagingException {
        String from = "noreplymedicalapp@gmail.com";
        String to = illnessMail.getDoctorEmail();

        log.info("to email : {}", to);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setSubject("MedApp | Hi Dr." + illnessMail.getDoctorName() + "! You have a new appointment from " + illnessMail.getPatientName());
        helper.setFrom(from);
        helper.setTo(to);

        String text = "<h1>Appointment</h1>\n" +
                "<p>Please find the below illness details</p>" +
                "<p><b>Appointment ID: </b>" + illnessMail.getAppointmentId() + "</p>" +
                "<p><b>Name: </b>" + illnessMail.getPatientName() + "</p>\n\n" +
                "<p><b>Illness: </b>" + illnessMail.getPatientIllness() + "</p>\n\n" +
                "<p><b>Contact: </b>" + illnessMail.getPatientContact() + "</p>\n\n" +
                "<p><b>Email: </b>" + illnessMail.getDoctorEmail() + "</p>\n\n" +
                "<p><b>Age: </b>" + illnessMail.getPatientAge() + "</p>\n\n" +
                "<p><b>Time: </b>" + getTime() + "</p>\n\n";
        helper.setText(text, true);

        javaMailSender.send(message);
    }
}
