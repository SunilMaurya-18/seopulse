package com.seopulse.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seopulse.website.job.AuditJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, AuditJob> auditRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {

        RedisTemplate<String, AuditJob> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(
                new StringRedisSerializer()
        );

        template.setValueSerializer(
                new GenericJackson2JsonRedisSerializer(
                        objectMapper
                )
        );

        template.afterPropertiesSet();

        return template;
    }
}