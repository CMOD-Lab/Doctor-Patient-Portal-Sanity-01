package com.hms.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Spring Session Web Initializer - cz-java-0069
 *
 * Registers the Spring Session {@code springSessionRepositoryFilter} as the
 * first servlet filter so that every {@link javax.servlet.http.HttpSession}
 * interaction is transparently delegated to the Redis-backed session store
 * configured in {@link RedisSessionConfig}.
 *
 * This resolves the in-memory session storage blocker (cz-java-0069) by
 * ensuring all session data is persisted in Amazon ElastiCache (Redis),
 * surviving container restarts and enabling horizontal scaling on EKS.
 *
 * This replaces the need to manually declare the filter in web.xml.
 */
public class SpringSessionInitializer extends AbstractHttpSessionApplicationInitializer {

    public SpringSessionInitializer() {
        super(RedisSessionConfig.class);
    }
}
