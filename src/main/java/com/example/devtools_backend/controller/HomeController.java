package com.example.devtools_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * Controller for the application home page and tool navigation.
 *
 * <p>Provides the main landing page displaying all available developer tools
 * in a card-based layout with sidebar navigation.
 *
 * <p><b>URL Mapping:</b> {@code GET /} - Home page
 *
 * <p><b>Design Pattern:</b> TOOLS constant is duplicated across controllers
 * to avoid shared state and maintain controller independence. Future enhancement
 * could move this to a shared configuration bean if centralized tool management
 * becomes necessary.
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 */
@Controller
public class HomeController {

    /**
     * List of all available tools for sidebar navigation and home page display.
     *
     * <p>Each tool entry contains:
     * <ul>
     *   <li>id - URL path segment (e.g., "jwt" for /tools/jwt)</li>
     *   <li>name - Display name shown in UI</li>
     *   <li>icon - Icon identifier for Tailwind Heroicons</li>
     *   <li>description - Short description for tool cards</li>
     * </ul>
     *
     * <p><b>Immutability:</b> List.of() ensures tools cannot be modified at runtime,
     * preventing accidental state changes from concurrent requests.
     */
    private static final List<Map<String, String>> TOOLS = List.of(
            Map.of("id", "jwt", "name", "JWT Decoder", "icon", "key", "description", "Encode/Decode JWT tokens"),
            Map.of("id", "uuid", "name", "UUID Generator", "icon", "fingerprint", "description", "Generate UUID v4/v7"),
            Map.of("id", "base64", "name", "Base64", "icon", "code", "description", "Encode/Decode Base64"),
            Map.of("id", "json2java", "name", "JSON to Java", "icon", "braces", "description", "Convert JSON to Java classes"),
            Map.of("id", "cron", "name", "Cron Builder", "icon", "clock", "description", "Build and explain cron expressions"),
            Map.of("id", "regex", "name", "Regex Tester", "icon", "search", "description", "Test regular expressions"),
            Map.of("id", "timestamp", "name", "Timestamp", "icon", "calendar", "description", "Convert timestamps"),
            Map.of("id", "hash", "name", "Hash Generator", "icon", "lock", "description", "Generate hashes and passwords"),
            Map.of("id", "sql", "name", "SQL Formatter", "icon", "database", "description", "Format SQL queries")
    );

    /**
     * Renders the home page with tool overview cards.
     *
     * <p>Populates model with tools list and activeTool marker for UI highlighting.
     *
     * @param model Spring MVC model for passing data to Thymeleaf template
     * @return template name "index" (resolves to index.html in templates/)
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tools", TOOLS);
        model.addAttribute("activeTool", "home");
        return "index";
    }
}
