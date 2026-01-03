package com.hoolistem.hoolistem;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

/**
 * HooliSTEM Platform - Main Application Entry Point
 * 
 * An educational platform for courses, workshops, and e-commerce.
 */
@SpringBootApplication
@EnableJpaAuditing
public class HoolistemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoolistemApplication.class, args);
    }

    /**
     * Set default timezone to Indian Standard Time (IST)
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
}
