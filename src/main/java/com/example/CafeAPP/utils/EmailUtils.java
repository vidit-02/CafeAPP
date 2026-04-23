package com.example.CafeAPP.utils;

import javax.mail.*;
import javax.mail.internet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String dest, String subject, String text, List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vidit.mittal02@gmail.com");
        message.setTo(dest);
        message.setSubject(subject);
        message.setText(text);

        if(list != null && !list.isEmpty()){
            message.setCc(getCCArray(list));
        }
        emailSender.send(message);
    }

    public void forgotMail(String dest, String subject, String password) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("vidit.mittal02@gmail.com");
        helper.setTo(dest);
        helper.setSubject(subject);
        String htmlmsg = "<p><b>Your Login details for Cafe Management System</b><br><b>Email: </b> " + dest + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlmsg, "text/html");
        emailSender.send(message);
    }

    private String[] getCCArray(List<String> ccList){
        return ccList.toArray(new String[0]);
    }
}
