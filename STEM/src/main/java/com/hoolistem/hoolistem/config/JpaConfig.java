package com.hoolistem.hoolistem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA Configuration for auditing and entity management.
 * 
 * Enables automatic population of created_at and updated_at fields
 * in entities extending BaseAuditEntity.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    /**
     * Provides the current auditor (user) for auditing purposes.
     * 
     * In a real application, this would return the currently authenticated user.
     * For now, returns "system" as a placeholder.
     * 
     * @return AuditorAware instance
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // TODO: Integrate with Spring Security to get current user
        // return () -> Optional.ofNullable(SecurityContextHolder.getContext())
        //         .map(SecurityContext::getAuthentication)
        //         .filter(Authentication::isAuthenticated)
        //         .map(Authentication::getName);
        return () -> Optional.of("system");
    }
}
