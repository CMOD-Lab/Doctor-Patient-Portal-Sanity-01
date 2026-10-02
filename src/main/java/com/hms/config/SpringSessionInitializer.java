package com.hms.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Registers Spring Session's {@code springSessionRepositoryFilter} as the
 * first {@link javax.servlet.Filter} in the servlet container.
 *
 * <p>Every incoming request is intercepted by this filter, which transparently
 * replaces the container-managed {@link javax.servlet.http.HttpSession} with a
 * Redis-backed session stored in Amazon ElastiCache.  No changes to existing
 * servlet code are required — calls to {@code request.getSession()} continue
 * to work exactly as before, but session data is now stored in Redis rather
 * than in server-local memory.
 */
public class SpringSessionInitializer
        extends AbstractHttpSessionApplicationInitializer {

    /**
     * Passes {@link RedisSessionConfig} to the parent constructor so that
     * Spring creates an {@code AnnotationConfigWebApplicationContext} with
     * the Redis session configuration loaded.
     */
    public SpringSessionInitializer() {
        super(RedisSessionConfig.class);
    }
}
