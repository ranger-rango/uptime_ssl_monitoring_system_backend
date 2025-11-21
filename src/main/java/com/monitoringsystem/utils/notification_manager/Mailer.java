package com.monitoringsystem.utils.notification_manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import com.monitoringsystem.utils.Constants;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


public class Mailer
{
    private static String host = Constants.SMTP_SERVER;
    private static String port = Constants.SMTP_SERVER_PORT;
    private static String username = Constants.SMTP_SERVER_USERNAME;
    private static String password = Constants.SMTP_SERVER_PASSWORD;
    private static Properties props = new Properties();
    public static String getHtmlContent(String templateName)
    {
        String htmlTemplate = null;
        try
        {
            htmlTemplate = new String(Files.readAllBytes(Paths.get("email-templates" + File.separator + templateName)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return htmlTemplate;
    }

    public static void sendEmailNotification(String templateName, String recipientEmail, String recipientName, String serviceUrl, String notificationTrigger)
    {
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, password);
            }
        });

        try
        {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

            String subject = templateName.replace(".html", "").replace("-template", "").replace("-", " ");
            message.setSubject(subject);

            String htmlContent = String.format(getHtmlContent(templateName), notificationTrigger, recipientName, serviceUrl, serviceUrl);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }
    }

    public static void sendResgistrationUrl(String templateName, String recipientEmail, String recipientName, String registrationUrl)
    {
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, password);
            }
        });

        try
        {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

            String subject = templateName.replace(".html", "").replace("-template", "").replace("-", " ");
            message.setSubject(subject);

            String htmlContent = String.format(getHtmlContent(templateName), recipientName, registrationUrl);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }
    }

}
