package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * cr-java-0065: HTTP Session State Storage
 *
 * Configures Spring Session to store all HTTP session data in Amazon ElastiCache
 * for Redis instead of in-memory server-side session storage.
 *
 * This enables:
 *  - Stateless application instances (no server affinity / sticky sessions)
 *  - Horizontal scaling across multiple instances behind a load balancer
 *  - Session persistence across instance restarts and deployments
 *  - Centralized, distributed session management via ElastiCache
 *
 * The @EnableRedisHttpSession annotation replaces the default HttpSession
 * implementation with a Redis-backed implementation transparently. All existing
 * code that uses HttpSession (getAttribute / setAttribute / removeAttribute)
 * continues to work without modification — sessions are now stored in Redis.
 *
 * Environment variables required:
 *   REDIS_HOST  - ElastiCache cluster endpoint (e.g., my-cluster.abc123.ng.0001.use1.cache.amazonaws.com)
 *   REDIS_PORT  - Redis port (default: 6379)
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisHttpSessionConfig {

    /** ElastiCache Redis endpoint — injected from REDIS_HOST environment variable */
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    /** ElastiCache Redis port — injected from REDIS_PORT environment variable */
    @Value("${spring.redis.port:6379}")
    private int redisPort;

    /**
     * Creates a Lettuce-based Redis connection factory pointing at the
     * Amazon ElastiCache cluster endpoint.
     *
     * @return RedisConnectionFactory backed by Amazon ElastiCache for Redis
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }
}
