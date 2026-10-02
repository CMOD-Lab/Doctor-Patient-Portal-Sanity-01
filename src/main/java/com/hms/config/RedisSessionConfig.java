package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session configuration that externalizes HttpSession storage to
 * Amazon ElastiCache (Redis) via Lettuce client.
 *
 * This configuration replaces in-memory server-side sessions with a
 * Redis-backed session store, enabling horizontal scaling across multiple
 * container instances on EKS without session loss on container restart.
 *
 * Required environment variables:
 *   REDIS_HOST  - Amazon ElastiCache Redis endpoint (default: localhost)
 *   REDIS_PORT  - Redis port (default: 6379)
 *
 * Rule: cz-java-0063 - Server-side Sessions
 */
@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    /**
     * Creates a Lettuce-based Redis connection factory pointing to
     * Amazon ElastiCache. Connection parameters are read from environment
     * variables REDIS_HOST and REDIS_PORT so no credentials are hardcoded.
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }
}
