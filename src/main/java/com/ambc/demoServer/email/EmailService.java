package com.ambc.demoServer.email;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static com.ambc.demoServer.email.EmailConstants.*;

@Service
public class EmailService {
    public void sendNewPassWord(String userFirstName, String userPassword, String userEmail) throws MessagingException {
        Message message = createEmail(userFirstName, userPassword, userEmail);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String userFirstName, String userPassword, String userEmail) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC, false));
        message.setSubject(SUBJECT);
        message.setText("Hello " + userFirstName + ", \n \n Your new account password is : " + userPassword + "\n \n The Help Support Team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        return Session.getInstance(properties, null);
    }
}
