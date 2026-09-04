package com.seopulse.website.job;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditQueue {

    private static final String QUEUE_KEY =
            "seopulse:audit:queue";

    private final RedisTemplate<String, AuditJob> redisTemplate;

    public void enqueue(Long auditId) {

        AuditJob job = new AuditJob(auditId);

        redisTemplate
                .opsForList()
                .rightPush(QUEUE_KEY, job);
    }
}