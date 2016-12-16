package ru.alidi.horeca.jobrunner.service.work;

import java.util.List;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.alidi.horeca.jobrunnerapi.service.MailAttachmentService;
import ru.alidi.horeca.jobrunnerapi.service.MailQueueService;
import ru.alidi.horeca.jobrunnerapi.service.MailService;
import ru.alidi.horeca.jobrunner.service.MailingService;
import ru.alidi.horeca.jobrunner.service.dto.AttachmentFileDTO;
import ru.alidi.horeca.jobrunnerapi.service.dto.MailDTO;
import ru.alidi.horeca.persistence.entity.MailEntity;

/**
 *
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Component
@Scope("prototype")
public class MailWork extends AbstractWork {
    
    @Autowired
    private MailService mailService;
    @Autowired
    private MailAttachmentService attachService;
    @Autowired
    private MailingService mailingService;
    @Autowired
    private MailQueueService queueService;
    
    private final Long mailId;
    
    private MailDTO mail;

    private List<AttachmentFileDTO> attachments;
    
    public Long getMailId() {
        return this.mailId;
    }

    @Override
    public void run() {
        //Check, is it still in queue
        if (queueService.getByMailId(this.mailId) == null) {
            this.skipped = true;
            return;
        }
        //Load mail
        if (mail == null) {
            //Load initial data
            mail = this.mailService.get(mailId);
            attachments = mail.getAttachments().stream()
                    .map(_ad->new AttachmentFileDTO(attachService.get(_ad.getId())))
                    .collect(Collectors.toList());
        } else {
            //Update attempt count
            MailDTO updated = this.mailService.get(mail.getId());
            if (updated.getAttempts() + 1 != this.getAttemptCount()) {
                mail.setAttempts(updated.getAttempts());
                this.setAttemptCount(updated.getAttempts());
            }
        }

        try {
            this.mailingService.sendMail(mail.getFrom(), mail.getTo(),
                    mail.getTitle(), mail.getContent(), attachments);
        } catch (MessagingException e){
            throw new RuntimeException("Error on mail sending: "+e.getMessage(), e);
        }
    }

    @Override
    public void errorProcessing(Throwable error) {
        if (mail != null) {
            mail.setAttempts(this.attemptCount);
            mail.setErrorText(this.lastError.getMessage());
            mail.setStatus(MailEntity.Status.ERROR.toString());
            mail.setLastAttempt(this.getLastAttempt());
            this.mailService.saveMail(mail);
        }
    }

    @Override
    public void onSuccess() {
        mail.setErrorText(null);
        mail.setStatus(MailEntity.Status.SENDED.toString());
        mail.setLastAttempt(this.getLastAttempt());
        mail.setAttempts(this.attemptCount);
        this.mailService.saveMail(mail);
        this.queueService.deleteMail(mailId);
    }

    @Override
    public void onSkip() {
        mail.setAttempts(this.attemptCount);
        if (this.lastError != null) {
            mail.setErrorText(this.lastError.getMessage());
        }
        mail.setStatus(MailEntity.Status.SKIPPED.toString());
        mail.setLastAttempt(this.getLastAttempt());
        this.mailService.saveMail(mail);
    }

    @Override
    public String toString() {
        return String.format(
                "%s{mailId:%d}",
                this.getClass().getSimpleName(), this.getMailId()
        );
    }
    
    public MailWork(Long mailId) {
        this.mailId = mailId;
    }
}
