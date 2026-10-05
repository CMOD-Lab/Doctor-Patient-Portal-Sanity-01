package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session configuration that externalizes HttpSession storage to
 * Amazon ElastiCache (Redis) for container-safe, horizontally-scalable sessions.
 *
 * cz-java-0069 (In-Memory Session Storage) remediation:
 *   Replaces in-memory HttpSession with Spring Session backed by Redis so that
 *   sessions survive container restarts and are shared across horizontally-scaled
 *   instances on EKS. The @EnableRedisHttpSession annotation installs a Spring
 *   Session filter (springSessionRepositoryFilter) that transparently wraps every
 *   javax.servlet.http.HttpSession with a Redis-backed implementation.
 *   No changes to individual servlet code are required beyond this configuration.
 *
 * Required environment variables:
 *   REDIS_HOST              - ElastiCache Redis primary endpoint (default: localhost)
 *   REDIS_PORT              - ElastiCache Redis port             (default: 6379)
 *   REDIS_PASSWORD          - ElastiCache auth token             (default: empty)
 *   SESSION_TIMEOUT_SECONDS - Session max-inactive interval in seconds (default: 1800)
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class SpringHttpSessionConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    /**
     * Creates a Lettuce-based Redis connection factory pointing to
     * Amazon ElastiCache. The host, port, and password are read from
     * environment variables so no credentials are hard-coded in the image.
     * This supports IRSA-based secure access on EKS.
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(RedisPassword.of(redisPassword));
        }
        return new LettuceConnectionFactory(redisConfig);
    }
}
