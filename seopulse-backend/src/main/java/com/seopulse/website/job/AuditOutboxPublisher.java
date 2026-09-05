package com.seopulse.website.job;

import com.seopulse.website.entity.AuditOutbox;
import com.seopulse.website.repository.AuditOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditOutboxPublisher {

    private final AuditOutboxRepository auditOutboxRepository;
    private final AuditQueue auditQueue;

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {

        List<AuditOutbox> events =
                auditOutboxRepository
                        .findTop50ByPublishedFalseOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        for (AuditOutbox event : events) {
            publish(event);
        }
    }

    @Transactional
    protected void publish(AuditOutbox event) {

        try {

            Long auditId = event.getAudit().getId();

            String recordId =
                    auditQueue.enqueue(auditId);

            event.setPublished(true);
            event.setPublishedAt(Instant.now());

            auditOutboxRepository.save(event);

            log.info(
                    "Published audit outbox event: " +
                            "outboxId={}, auditId={}, recordId={}",
                    event.getId(),
                    auditId,
                    recordId
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to publish audit outbox event: " +
                            "outboxId={}, auditId={}",
                    event.getId(),
                    event.getAudit().getId(),
                    ex
            );
        }
    }
}