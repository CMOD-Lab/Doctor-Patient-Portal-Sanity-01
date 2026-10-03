package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session Redis Configuration
 *
 * cz-java-0069: Externalizes in-memory HttpSession to Amazon ElastiCache (Redis) via
 * Spring Session, enabling stateless, horizontally-scalable container deployments on EKS.
 *
 * Required environment variables:
 *   REDIS_HOST     - Redis/ElastiCache endpoint (default: localhost)
 *   REDIS_PORT     - Redis port (default: 6379)
 *   REDIS_PASSWORD - Redis AUTH password (optional, leave empty if not set)
 *
 * The @EnableRedisHttpSession annotation transparently replaces the servlet
 * container's in-memory HttpSession with a Redis-backed session, so all
 * existing session.setAttribute / session.getAttribute calls continue to work
 * without any code changes in the servlet layer.
 *
 * Deployed as a Kubernetes-managed workload on EKS with IRSA for secure access
 * to Amazon ElastiCache (Redis).
 */
@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }
}
