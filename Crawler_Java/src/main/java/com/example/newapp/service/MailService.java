package com.example.newapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class MailService {

    @Value("${spring.mail.username}")
    private String to;

    @Value("${app.client.url}")
    private String portUrl;

    @Autowired
    private JavaMailSender mailSender;

    public void SendMail(String mail,String password) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setFrom(to);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("Change Password to Protected Your Account");
        String htmlContent = readHtmlEmailTemplate();
        htmlContent = htmlContent.replace("{{var_href}}", portUrl+"changePassword?email="+mail + " &password="+password);
        message.setContent(htmlContent,"text/html; charset=utf-8");
        log.info("send email success : " );
        mailSender.send(message);
    }

    // Hàm để đọc nội dung từ tệp HTML
    public String readHtmlEmailTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("email-template.html");
            byte[] fileData = FileCopyUtils.copyToByteArray(resource.getInputStream());
            log.info("read template success : " );
            return new String(fileData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read template failure : " );
            e.printStackTrace();
            // Handle the exception appropriately
            return null;
        }
    }
}
