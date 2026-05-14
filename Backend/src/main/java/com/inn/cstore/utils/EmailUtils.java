package com.inn.cstore.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMessage(String to, String subject,String text, List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tyronetorresandrade@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        if (list != null && list.size() > 0) 
            message.setCc(getCcArray(list));
    
        mailSender.send(message);
        
    }

    private String[] getCcArray(List<String> CcList){


        String[] cc = new String[CcList.size()];
        for (int i=0;i<CcList.size();i++){
            cc[i]=CcList.get(i);
        }

        return cc;
    }

    public void forgotMail(String to, String subject, String password) throws MessagingException{

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
    
        helper.setFrom("dt025055@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMessage = "<p>"
        + "<b>Your login details for CheapStore Management System</b><br>"
        + "<b>Email:</b> " + to + "<br>"
        + "<b>Password:</b> " + password + "<br>"
        + "<a href=\"http://localhost:4200/\">Click here to login</a>"
        + "</p>";
        mimeMessage.setContent(htmlMessage,"text/html");
        mailSender.send(mimeMessage);
    }

}
