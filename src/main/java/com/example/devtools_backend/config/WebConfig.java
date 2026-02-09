package com.example.devtools_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for static resources and JSON serialization.
 *
 * <p>This configuration class provides:
 * <ul>
 *   <li>Static resource handler mapping for CSS, JS, and image files</li>
 *   <li>Centralized ObjectMapper bean for consistent JSON processing across services</li>
 * </ul>
 *
 * <p><b>Design Decision:</b> ObjectMapper is provided as a bean (rather than relying
 * on Spring Boot auto-configuration) to ensure explicit control over JSON serialization
 * settings and to satisfy dependency injection requirements in service classes.
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures static resource handlers for serving CSS, JavaScript, and image files.
     *
     * <p><b>Security Note:</b> Static resources are served without authentication.
     * Ensure no sensitive data is placed in the /static directory.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /static/** URLs to classpath:/static/ for serving frontend assets
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Provides a centralized ObjectMapper for JSON serialization/deserialization.
     *
     * <p>This bean is used by:
     * <ul>
     *   <li>JwtService - For parsing and formatting JWT header/payload JSON</li>
     *   <li>Future services requiring JSON processing</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> ObjectMapper is thread-safe for read operations
     * (serialization/deserialization) after configuration.
     *
     * @return configured ObjectMapper instance with default settings
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
