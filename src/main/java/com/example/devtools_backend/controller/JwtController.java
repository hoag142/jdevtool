package com.example.devtools_backend.controller;

import com.example.devtools_backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tools/jwt")
@RequiredArgsConstructor
public class JwtController {

    private final JwtService jwtService;

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

    @GetMapping
    public String jwtPage(Model model) {
        model.addAttribute("tools", TOOLS);
        model.addAttribute("activeTool", "jwt");
        model.addAttribute("pageTitle", "JWT Decoder");
        return "tools/jwt";
    }

    @PostMapping("/decode")
    public String decodeJwt(@RequestParam String token, Model model) {
        Map<String, Object> result = jwtService.decodeJwt(token);
        model.addAttribute("result", result);
        model.addAttribute("token", token);
        return "tools/jwt :: result";
    }

    @PostMapping("/verify")
    public String verifyJwt(@RequestParam String token,
                           @RequestParam String secret,
                           Model model) {
        Map<String, Object> verifyResult = jwtService.verifyJwt(token, secret);
        model.addAttribute("verifyResult", verifyResult);
        return "tools/jwt :: verifyResult";
    }
}
