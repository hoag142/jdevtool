package com.example.devtools_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for DevTools Backend application.
 *
 * <p>This application provides a collection of developer utility tools including:
 * <ul>
 *   <li>JWT Decoder - Decode and verify JWT tokens</li>
 *   <li>UUID Generator - Generate and parse UUIDs (v4 and v7)</li>
 *   <li>Base64 Encoder/Decoder</li>
 *   <li>JSON to Java Converter</li>
 *   <li>Cron Expression Builder</li>
 *   <li>Regex Tester</li>
 *   <li>Timestamp Converter</li>
 *   <li>Hash Generator</li>
 *   <li>SQL Formatter</li>
 * </ul>
 *
 * <p><b>Architecture:</b> Spring Boot + Thymeleaf + HTMX for server-side rendering
 * with dynamic partial page updates without JavaScript.
 *
 * <p><b>External Dependencies:</b>
 * <ul>
 *   <li>Redis - For caching history, snippets, and usage statistics (optional but recommended)</li>
 * </ul>
 *
 * <p><b>Configuration:</b> Configure Redis connection via SPRING_REDIS_HOST environment variable.
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class DevtoolsBackendApplication {

	/**
	 * Application entry point.
	 *
	 * <p>Initializes Spring Boot context and starts the embedded web server.
	 * Default port is 8080 unless overridden by server.port property.
	 *
	 * @param args command-line arguments (currently unused)
	 */
	public static void main(String[] args) {
		SpringApplication.run(DevtoolsBackendApplication.class, args);
	}

}
