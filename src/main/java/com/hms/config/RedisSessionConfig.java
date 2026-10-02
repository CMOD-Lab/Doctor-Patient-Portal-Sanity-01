package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session configuration that stores all HTTP session data in
 * Amazon ElastiCache for Redis.
 *
 * <p>This replaces server-local {@link javax.servlet.http.HttpSession} storage,
 * enabling stateless application instances that can be horizontally scaled
 * behind a load balancer without server affinity (sticky sessions).
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code REDIS_HOST}    – ElastiCache primary endpoint (default: localhost)</li>
 *   <li>{@code REDIS_PORT}    – Redis port (default: 6379)</li>
 *   <li>{@code REDIS_PASSWORD}– Redis AUTH token / ElastiCache auth token (optional)</li>
 * </ul>
 *
 * <p>The {@code maxInactiveIntervalInSeconds} attribute controls the session
 * TTL stored in Redis (default: 1800 s = 30 min).
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {

    /** ElastiCache primary endpoint, injected from the REDIS_HOST env variable. */
    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    /** Redis port, injected from the REDIS_PORT env variable. */
    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    /** Optional Redis AUTH token, injected from the REDIS_PASSWORD env variable. */
    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    /**
     * Creates a Lettuce-based Redis connection factory pointing at the
     * ElastiCache cluster endpoint resolved from environment variables.
     *
     * @return a configured {@link LettuceConnectionFactory}
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
