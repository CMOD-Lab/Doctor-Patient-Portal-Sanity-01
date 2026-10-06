package com.hms.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * cz-java-0063 [Server-side Sessions] - Registers the Spring Session
 * {@code springSessionRepositoryFilter} as a Servlet filter so that every
 * request passes through it and the standard {@code HttpSession} API is
 * transparently backed by Amazon ElastiCache (Redis).
 *
 * This initializer is detected automatically by the Servlet 3.0+ container
 * (Tomcat) via {@link javax.servlet.ServletContainerInitializer} – no
 * additional web.xml entry is required beyond the DelegatingFilterProxy
 * already declared there.
 *
 * Rule: cz-java-0063 - Server-side Sessions
 */
public class SpringSessionInitializer extends AbstractHttpSessionApplicationInitializer {

    public SpringSessionInitializer() {
        super(RedisHttpSessionConfig.class);
    }
}
