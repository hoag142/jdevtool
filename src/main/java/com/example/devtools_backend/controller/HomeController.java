package com.example.devtools_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

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

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tools", TOOLS);
        model.addAttribute("activeTool", "home");
        return "index";
    }
}
