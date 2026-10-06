package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session configuration that backs HTTP sessions with Amazon ElastiCache for Redis.
 *
 * <p>Replaces server-local {@link javax.servlet.http.HttpSession} storage with a
 * centralised, distributed Redis store so that any application instance can serve
 * any request without server affinity (cr-java-0065).
 *
 * <p>Configuration is driven entirely by environment variables so that no credentials
 * or host names are hard-coded in source:
 * <ul>
 *   <li>{@code REDIS_HOST}  – ElastiCache primary endpoint (default: {@code localhost})</li>
 *   <li>{@code REDIS_PORT}  – Redis port (default: {@code 6379})</li>
 *   <li>{@code REDIS_PASSWORD} – Redis AUTH token (optional; leave unset for no-auth clusters)</li>
 *   <li>{@code SESSION_TIMEOUT_SECONDS} – max-inactive interval in seconds (default: {@code 1800})</li>
 * </ul>
 *
 * <p>The {@code springSessionRepositoryFilter} bean registered by
 * {@link EnableRedisHttpSession} is wired into the servlet container via the
 * {@code springSessionRepositoryFilter} {@code <filter>} entry in {@code web.xml}.
 * All existing {@code HttpSession} API calls in the application continue to work
 * unchanged – Spring Session transparently delegates them to Redis.
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = RedisHttpSessionConfig.SESSION_TIMEOUT)
public class RedisHttpSessionConfig {

    /**
     * Default session timeout: 30 minutes.  Override via the
     * {@code SESSION_TIMEOUT_SECONDS} environment variable.
     */
    static final int SESSION_TIMEOUT = 1800;

    /** ElastiCache / Redis host resolved from the environment. */
    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    /** ElastiCache / Redis port resolved from the environment. */
    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    /**
     * Optional Redis AUTH password.  Leave the environment variable unset (or
     * empty) for clusters that do not require authentication.
     */
    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    /**
     * Lettuce-based Redis connection factory pointing at the ElastiCache endpoint.
     *
     * @return a configured {@link LettuceConnectionFactory}
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }
}
