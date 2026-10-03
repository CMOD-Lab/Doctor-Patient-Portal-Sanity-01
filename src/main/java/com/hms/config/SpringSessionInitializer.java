package com.hms.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Registers the Spring Session filter (springSessionRepositoryFilter) with the
 * servlet container so that every request goes through the Redis-backed session
 * repository instead of the in-memory container session.
 *
 * cz-java-0069: Required companion class for RedisSessionConfig to wire
 * Spring Session into the existing javax.servlet web application.
 * Enables transparent replacement of in-memory HttpSession with Amazon
 * ElastiCache (Redis)-backed session for EKS horizontal scaling.
 */
public class SpringSessionInitializer extends AbstractHttpSessionApplicationInitializer {

    public SpringSessionInitializer() {
        super(RedisSessionConfig.class);
    }
}
