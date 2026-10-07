package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session Redis Configuration - cz-java-0069
 *
 * Externalizes HttpSession storage to Amazon ElastiCache (Redis) so that
 * sessions survive container restarts and are shared across horizontally
 * scaled instances on EKS. This resolves the in-memory session storage
 * blocker (cz-java-0069) by replacing volatile in-memory HttpSession with
 * a Redis-backed distributed session store.
 *
 * Required environment variables:
 *   REDIS_HOST  - ElastiCache Redis endpoint (default: localhost)
 *   REDIS_PORT  - ElastiCache Redis port     (default: 6379)
 *   SESSION_TIMEOUT_SECONDS - Session max-inactive interval in seconds (default: 1800)
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    /**
     * Creates a Lettuce-based Redis connection factory pointing at the
     * Amazon ElastiCache endpoint supplied via environment variables.
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }
}
