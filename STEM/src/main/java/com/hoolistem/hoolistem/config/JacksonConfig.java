package com.hoolistem.hoolistem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

/**
 * Jackson ObjectMapper configuration for JSON serialization/deserialization.
 * 
 * Configures proper handling of Java 8 date/time types with IST timezone.
 */
@Configuration
public class JacksonConfig {

    /**
     * Configures Jackson ObjectMapper with:
     * - Java 8 date/time support
     * - IST timezone
     * - ISO 8601 date format
     * 
     * @return Configured ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps (use ISO 8601 strings)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Set timezone to IST
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // Don't fail on empty beans
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        return mapper;
    }
}
