package org.ngelmakproject.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails asynchronously.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
    }

    private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
    }

}
