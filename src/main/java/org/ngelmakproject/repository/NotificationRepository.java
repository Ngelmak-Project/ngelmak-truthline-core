package org.ngelmakproject.repository;

import java.time.Instant;
import java.time.Instant;
import java.time.Instant;
import java.util.List;

import org.ngelmakproject.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Notification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  @Query("""
          SELECT n FROM Notification n
          WHERE n.scheduledAt <= :now
            AND n.scheduledAt + (n.expiresAfterHours * INTERVAL) >= :now
          ORDER BY RANDOM()
      """)
  List<Notification> findActiveRandom(@Param("now") Instant now, Pageable pageable);

}
