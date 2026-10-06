package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * cz-java-0063 [Server-side Sessions] - Spring Session configuration that
 * externalizes HttpSession storage to Amazon ElastiCache (Redis) on EKS.
 *
 * This replaces the default in-memory HttpSession with a Redis-backed
 * distributed session store, enabling:
 *   - Horizontal scaling across multiple container replicas
 *   - Session persistence across container restarts
 *   - Secure access via IRSA (IAM Roles for Service Accounts) on EKS
 *
 * Redis connection parameters are supplied via environment variables so that
 * no credentials or endpoints are hard-coded in the source:
 *   REDIS_HOST  - Amazon ElastiCache Redis primary endpoint (default: localhost)
 *   REDIS_PORT  - Amazon ElastiCache Redis port             (default: 6379)
 *
 * Rule: cz-java-0063 - Server-side Sessions
 */
@Configuration
@EnableRedisHttpSession
public class RedisHttpSessionConfig {

    /**
     * ElastiCache Redis primary endpoint.
     * Injected from the REDIS_HOST environment variable.
     * Falls back to "localhost" for local development.
     */
    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    /**
     * ElastiCache Redis port.
     * Injected from the REDIS_PORT environment variable.
     * Falls back to 6379 for local development.
     */
    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    /**
     * Creates a Lettuce-based Redis connection factory pointing to
     * Amazon ElastiCache. Connection details are supplied via environment
     * variables so no credentials are hard-coded in the source.
     *
     * @return configured {@link LettuceConnectionFactory}
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }
}
