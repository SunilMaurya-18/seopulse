package com.seopulse.website.job;

import com.seopulse.website.entity.Audit;
import com.seopulse.website.entity.AuditStatus;
import com.seopulse.website.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditWorker {
    private static final String QUEUE_KEY = "seopulse:audit:queue";
    private final RedisTemplate<String, AuditJob> redisTemplate;
    private final AuditRepository auditRepository;

    public void processNextJob() {
        AuditJob job = redisTemplate
                .opsForList()
                .leftPop(
                        QUEUE_KEY,
                        Duration.ofSeconds(5)
                );
        if (job == null) {
            return;
        }
        processJob(job);

    }

    private void processJob(AuditJob job) {
        log.info("Processing audit job: auditId={}", job.auditId());

        Audit audit = auditRepository.findById(job.auditId())
                .orElse(null);

        if (audit == null) {
            log.warn("Audit not found for auditId={}", job.auditId());
            return;
        }
        if (audit.getStatus() != AuditStatus.QUEUED) {
            log.warn(
                    "Skipping audit {} because statsus is {}",
                    audit.getId(),
                    audit.getStatus()
            );
            return;
        }
        audit.setStatus(AuditStatus.CRAWLING);
        audit.setStartedAt(java.time.Instant.now());
        auditRepository.save(audit);

        log.info("Audit {} moved to CRAWLING", audit.getId());
    }

}
