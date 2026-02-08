package org.ngelmakproject.service;

import java.time.Instant;
import java.util.List;

import org.ngelmakproject.domain.Notification;
import org.ngelmakproject.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Create a new scheduled notification. * If scheduledAt is null, default to
     * now.
     */
    public Notification schedule(Notification notification) {
        log.debug("Request to schedule a new Notification : {}", notification);
        if (notification.getScheduledAt() == null) {
            notification.setScheduledAt(Instant.now());
        }
        return notificationRepository.save(notification);
    }

    /**
     * Returns notifications that are currently active.
     * Active = now is between scheduledAt and scheduledAt + expiresAfter.
     */
    @Transactional(readOnly = true)
    public List<Notification> getTop10ActiveNotifications() {
        log.debug("Request to get top 10 active Notifications");
        return notificationRepository.findActiveRandom(Instant.now(), PageRequest.of(0, 10));
    }
}