package ru.alidi.horeca.jobrunner.service;

import com.mongodb.gridfs.GridFSDBFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.alidi.horeca.jobrunner.service.dto.AttachmentFileDTO;

/**
 * Service for e-mail sending
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Service
@PropertySource("classpath:mail.properties")
public class MailingService {
    
    Logger log = LoggerFactory.getLogger(MailingService.class);
    
    private Session session = null;
    
    @Autowired
    Environment env;
    
    /**
     * Send e-mail
     * @param from
     * @param to
     * @param subject
     * @param content
     * @param attachments
     * @throws MessagingException 
     */
    public void sendMail(String from, String to, String subject, String content,
            List<AttachmentFileDTO> attachments) throws MessagingException {
        if (session == null) {
            log.error(String.format(
                    "Can not send email to %s: session is not initialized", to
            ));
            throw new RuntimeException("Mailing session is not initialized");
        }
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        //Build message body
        Multipart body = new MimeMultipart();
        BodyPart textPart = new MimeBodyPart();
        textPart.setText(content);
        body.addBodyPart(textPart);
        for (AttachmentFileDTO file : attachments) {
            try {
                    MimeBodyPart attachment = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(
                            file.getInputStream(),
                            file.getContentType()
                    );
                    DataHandler handler = new DataHandler(source);
                    attachment.setDataHandler(handler);
                    attachment.setFileName(file.getFilename());
                    body.addBodyPart(attachment);
            } catch (IOException e) {
                throw new RuntimeException(String.format(
                        "Error on attaching file %s",
                        file.getFilename()
                ), e);
            }            
        }
        //
        message.setContent(body);
        Transport.send(message);
    }
    
    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", env.getProperty("mail.host"));
        props.put("mail.smtp.port", env.getProperty("mail.port"));
        
        session = Session.getInstance(props, new javax.mail.Authenticator() {
            
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        env.getProperty("mail.username"),
                        env.getProperty("mail.password")
                );
            }
            
        });
    }
    
}
