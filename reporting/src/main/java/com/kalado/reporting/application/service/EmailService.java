package com.kalado.reporting.application.service;

import com.kalado.common.enums.ReportStatus;
import com.kalado.common.feign.user.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final UserApi userApi;
    private static final String FROM_EMAIL = "noreply@kalado.com";

    public void sendReportConfirmation(Long reporterId) {
        String userEmail = userApi.getUserProfile(reporterId).getUsername();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(userEmail);
        message.setSubject("Report Received");
        message.setText("Your report has been received and will be reviewed by our team. " +
                "You will be notified when there are updates to your report.");

        mailSender.send(message);
    }

    public void sendReportStatusUpdate(Long reporterId, ReportStatus newStatus) {
        String userEmail = userApi.getUserProfile(reporterId).getUsername();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(userEmail);
        message.setSubject("Report Status Updated");
        message.setText("The status of your report has been updated to: " + newStatus + "\n" +
                "You can check the details in your reports section.");

        mailSender.send(message);
    }
}