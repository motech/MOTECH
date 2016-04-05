package org.motechproject.email.service.impl;

import org.motechproject.email.domain.Mail;
import org.motechproject.email.domain.DeliveryStatus;
import org.motechproject.email.domain.EmailRecord;
import org.motechproject.email.exception.EmailSendException;
import org.motechproject.email.service.EmailRecordService;
import org.motechproject.email.service.EmailSenderService;
import org.motechproject.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.motechproject.commons.date.util.DateUtil.now;

/**
 * The <code>EmailSenderServiceImpl</code> class provides API for sending e-mails
 */

@Service("emailSenderService")
public class EmailSenderServiceImpl implements EmailSenderService {

    private static final String EMAIL_LOG_BODY = "mail.log.body";
    private static final String EMAIL_LOG_ADDRESS = "mail.log.address";
    private static final String EMAIL_LOG_SUBJECT = "mail.log.subject";
    private static final String FALSE = "false";


    @Autowired()
    @Qualifier("emailSettings")
    private SettingsFacade settings;

    @Autowired
    private EmailRecordService emailRecordService;

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderServiceImpl.class);

    @Override
    @Transactional
    public void send(String fromAddress, String toAddress, String subject, String message) throws EmailSendException {
        Mail mail = new Mail(fromAddress, toAddress, subject, message);
        LOGGER.info(String.format("Sending message [%s] from [%s] to [%s] with subject [%s].",
                mail.getMessage(), mail.getFromAddress(), mail.getToAddress(), mail.getSubject()));
        try {
            mailSender.send(getMimeMessagePreparator(mail));
            log(new EmailRecord(mail.getFromAddress(), mail.getToAddress(), mail.getSubject(), mail.getMessage(),
                    now(), DeliveryStatus.SENT));
        } catch (MailException e) {
            log(new EmailRecord(mail.getFromAddress(), mail.getToAddress(), mail.getSubject(), mail.getMessage(),
                    now(), DeliveryStatus.ERROR));
            throw new EmailSendException("Unable to send an email to " + mail.getToAddress(), e);
        }
    }

    private void log(EmailRecord emailRecord) {
        if (FALSE.equals(settings.getProperty(EMAIL_LOG_BODY))) {
            emailRecord.setMessage("");
        }

        if (FALSE.equals(settings.getProperty(EMAIL_LOG_ADDRESS))) {
            emailRecord.setFromAddress("");
            emailRecord.setToAddress("");
        }

        if (FALSE.equals(settings.getProperty(EMAIL_LOG_SUBJECT))) {
            emailRecord.setSubject("");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Logging: {}", emailRecord.toString());
        }

        emailRecordService.create(emailRecord);
    }

    MotechMimeMessagePreparator getMimeMessagePreparator(Mail mail) {
        return new MotechMimeMessagePreparator(mail);
    }
}
