package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * SpringHttpSessionConfig - cz-java-0069 fix
 *
 * Replaces in-memory HttpSession with Spring Session backed by Amazon ElastiCache
 * for Redis, deployed as a Kubernetes workload on EKS.
 *
 * The SessionRepositoryFilter (registered in web.xml via DelegatingFilterProxy)
 * transparently intercepts every request and stores session data in Redis so that:
 *   - Sessions survive container restarts
 *   - Sessions are shared across all horizontally-scaled pod instances
 *
 * Redis connection parameters are read from environment variables:
 *   REDIS_HOST     - ElastiCache primary endpoint (default: localhost)
 *   REDIS_PORT     - Redis port                   (default: 6379)
 *   REDIS_PASSWORD - Redis AUTH token / password  (default: empty)
 *
 * Session TTL is controlled by maxInactiveIntervalInSeconds (default: 1800 s / 30 min).
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
     * Lettuce-based Redis connection factory.
     * Lettuce is thread-safe and non-blocking, making it well-suited for
     * high-concurrency servlet workloads on EKS.
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }
}
