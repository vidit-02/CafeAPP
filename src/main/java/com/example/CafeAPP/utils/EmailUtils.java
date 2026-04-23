package com.example.CafeAPP.utils;

import javax.mail.*;
import javax.mail.internet.*;

import com.example.CafeAPP.exception.CafeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(
            String dest,
            String subject,
            String text,
            List<String> list) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom("vidit.mittal02@gmail.com");
            message.setTo(dest);
            message.setSubject(subject);
            message.setText(text);

            if (list != null && !list.isEmpty()) {
                message.setCc(getCCArray(list));
            }

            emailSender.send(message);

        } catch (MailException ex) {

            throw new CafeException(
                    "Unable to send email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            throw new CafeException(
                    "Unexpected error while sending email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public void forgotMail(
            String dest,
            String subject,
            String password) {

        try {

            MimeMessage message =
                    emailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom("vidit.mittal02@gmail.com");
            helper.setTo(dest);
            helper.setSubject(subject);

            String htmlMsg =
                    "<p><b>Your Login details for Cafe Management System</b><br>"
                            + "<b>Email: </b> " + dest
                            + "<br><b>Password: </b> " + password
                            + "<br><a href=\"http://localhost:4200/\">"
                            + "Click here to login</a></p>";

            helper.setText(htmlMsg, true);

            emailSender.send(message);

        } catch (MessagingException ex) {

            throw new CafeException(
                    "Unable to prepare email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (MailException ex) {

            throw new CafeException(
                    "Unable to send email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            throw new CafeException(
                    "Unexpected error while sending forgot password email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String[] getCCArray(List<String> ccList){
        return ccList.toArray(new String[0]);
    }
}
