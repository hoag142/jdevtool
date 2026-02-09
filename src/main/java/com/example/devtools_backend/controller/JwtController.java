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

/**
 * Controller for JWT decoding and signature verification tool.
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>Rendering the JWT tool page</li>
 *   <li>Decoding JWT tokens (header, payload, signature extraction)</li>
 *   <li>Verifying JWT signatures using HMAC algorithms (HS256, HS384, HS512)</li>
 * </ul>
 *
 * <p><b>URL Pattern:</b>
 * <ul>
 *   <li>{@code GET /tools/jwt} - Full page render</li>
 *   <li>{@code POST /tools/jwt/decode} - HTMX partial update (returns result fragment)</li>
 *   <li>{@code POST /tools/jwt/verify} - HTMX partial update (returns verification fragment)</li>
 * </ul>
 *
 * <p><b>HTMX Integration:</b> POST endpoints return Thymeleaf fragments (not full pages)
 * for dynamic content updates without page reload.
 *
 * <p><b>Security Note:</b> Secret keys are transmitted in plain text over HTTP.
 * This tool is intended for development/testing only, not production token verification.
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 * @see JwtService
 */
@Controller
@RequestMapping("/tools/jwt")
@RequiredArgsConstructor
public class JwtController {

    private final JwtService jwtService;

    /**
     * Tool navigation list - duplicated from HomeController for controller independence.
     * See {@link HomeController#TOOLS} for structure details.
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
     * Renders the JWT decoder tool page.
     *
     * @param model Spring MVC model for template data
     * @return template name "tools/jwt" (resolves to templates/tools/jwt.html)
     */
    @GetMapping
    public String jwtPage(Model model) {
        model.addAttribute("tools", TOOLS);
        model.addAttribute("activeTool", "jwt");
        model.addAttribute("pageTitle", "JWT Decoder");
        return "tools/jwt";
    }

    /**
     * Decodes a JWT token and returns the result fragment for HTMX swap.
     *
     * <p>Extracts and displays:
     * <ul>
     *   <li>Header - Algorithm and token type</li>
     *   <li>Payload - Claims with formatted timestamps (iat, exp, nbf)</li>
     *   <li>Signature - Present/absent indicator</li>
     *   <li>Expiration status - Whether token is currently expired</li>
     * </ul>
     *
     * <p><b>Error Handling:</b> Invalid tokens return error message in result map,
     * displayed as error alert in UI.
     *
     * @param token JWT token string (whitespace trimmed by service)
     * @param model Spring MVC model for result data
     * @return Thymeleaf fragment "tools/jwt :: result" for HTMX target replacement
     * @see JwtService#decodeJwt(String)
     */
    @PostMapping("/decode")
    public String decodeJwt(@RequestParam String token, Model model) {
        Map<String, Object> result = jwtService.decodeJwt(token);
        model.addAttribute("result", result);
        model.addAttribute("token", token);
        return "tools/jwt :: result";
    }

    /**
     * Verifies JWT signature using provided secret key.
     *
     * <p>Supported algorithms: HS256, HS384, HS512 (HMAC-based)
     *
     * <p><b>Algorithm Detection:</b> Algorithm is extracted from JWT header,
     * not user input, to prevent algorithm substitution attacks.
     *
     * <p><b>Limitations:</b>
     * <ul>
     *   <li>RSA/EC algorithms not supported (require public key, not secret)</li>
     *   <li>No token expiration check during verification (use decode endpoint for expiry)</li>
     *   <li>Constant-time comparison not implemented (acceptable for dev tool)</li>
     * </ul>
     *
     * @param token JWT token string
     * @param secret HMAC secret key for signature verification
     * @param model Spring MVC model for verification result
     * @return Thymeleaf fragment "tools/jwt :: verifyResult" for HTMX target replacement
     * @see JwtService#verifyJwt(String, String)
     */
    @PostMapping("/verify")
    public String verifyJwt(@RequestParam String token,
                           @RequestParam String secret,
                           Model model) {
        Map<String, Object> verifyResult = jwtService.verifyJwt(token, secret);
        model.addAttribute("verifyResult", verifyResult);
        return "tools/jwt :: verifyResult";
    }
}
