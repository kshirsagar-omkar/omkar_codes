package com.hoolistem.hoolistem;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

/**
 * STEM Platform - Main Application Entry Point
 * 
 * An educational platform for courses, workshops, and e-commerce.
 * 
 * Features:
 * - Course management with modules, lessons, and quizzes
 * - Instructor and student management
 * - E-commerce for courses and products
 * - Workshop registration and management
 * - Certificate issuance
 * 
 * @author STEM Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class StemPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(StemPlatformApplication.class, args);
    }

    /**
     * Set default timezone to Indian Standard Time (IST)
     * This ensures consistent timestamp handling across the application.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
}
