package com.example.devtools_backend.controller;

import com.example.devtools_backend.service.UuidService;
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
 * Controller for UUID generation and parsing tool.
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>Rendering the UUID tool page</li>
 *   <li>Generating random UUIDs (version 4)</li>
 *   <li>Generating time-ordered UUIDs (version 7)</li>
 *   <li>Parsing UUID strings to extract metadata</li>
 * </ul>
 *
 * <p><b>URL Pattern:</b>
 * <ul>
 *   <li>{@code GET /tools/uuid} - Full page render</li>
 *   <li>{@code POST /tools/uuid/generate-v4} - HTMX partial update (v4 UUIDs)</li>
 *   <li>{@code POST /tools/uuid/generate-v7} - HTMX partial update (v7 UUIDs)</li>
 *   <li>{@code POST /tools/uuid/parse} - HTMX partial update (UUID parser)</li>
 * </ul>
 *
 * <p><b>HTMX Integration:</b> POST endpoints return Thymeleaf fragments (not full pages)
 * for dynamic content updates without page reload. Results displayed inline with
 * copy-to-clipboard functionality.
 *
 * <p><b>Use Cases:</b>
 * <ul>
 *   <li>Generate UUIDs for database primary keys</li>
 *   <li>Create correlation IDs for distributed tracing</li>
 *   <li>Debug UUID structure and version information</li>
 *   <li>Compare UUID v4 vs v7 for application needs</li>
 * </ul>
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 * @see UuidService
 */
@Controller
@RequestMapping("/tools/uuid")
@RequiredArgsConstructor
public class UuidController {

    private final UuidService uuidService;

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
     * Renders the UUID generator and parser tool page.
     *
     * @param model Spring MVC model for template data
     * @return template name "tools/uuid" (resolves to templates/tools/uuid.html)
     */
    @GetMapping
    public String uuidPage(Model model) {
        model.addAttribute("tools", TOOLS);
        model.addAttribute("activeTool", "uuid");
        model.addAttribute("pageTitle", "UUID Generator");
        return "tools/uuid";
    }

    /**
     * Generates random UUIDs (version 4) and returns result fragment for HTMX swap.
     *
     * <p><b>Default Behavior:</b> Generates 1 UUID if count parameter omitted.
     *
     * <p><b>Validation:</b> Count must be between 1-100 (enforced by service layer).
     * Invalid counts return error fragment displayed as alert in UI.
     *
     * <p><b>UI Features:</b>
     * <ul>
     *   <li>Individual copy buttons for each UUID</li>
     *   <li>Bulk copy button for all UUIDs (newline-separated)</li>
     *   <li>Version badge indicating UUID v4</li>
     *   <li>Generation timestamp for audit trail</li>
     * </ul>
     *
     * @param count number of UUIDs to generate (default: 1, max: 100)
     * @param model Spring MVC model for result data
     * @return Thymeleaf fragment "tools/uuid :: result" for HTMX target replacement
     * @see UuidService#generateUuidV4(int)
     */
    @PostMapping("/generate-v4")
    public String generateV4(@RequestParam(defaultValue = "1") int count, Model model) {
        Map<String, Object> result = uuidService.generateUuidV4(count);
        model.addAttribute("result", result);
        return "tools/uuid :: result";
    }

    /**
     * Generates time-ordered UUIDs (version 7) and returns result fragment for HTMX swap.
     *
     * <p><b>Advantages over v4:</b>
     * <ul>
     *   <li>Lexicographically sortable by creation time</li>
     *   <li>Better database index performance (sequential vs random inserts)</li>
     *   <li>Enables time-range queries on UUID primary keys</li>
     * </ul>
     *
     * <p><b>Default Behavior:</b> Generates 1 UUID if count parameter omitted.
     *
     * <p><b>Validation:</b> Count must be between 1-100 (enforced by service layer).
     *
     * <p><b>Timestamp Precision:</b> UUIDs generated in same batch maintain
     * chronological order via monotonic clock sequence.
     *
     * @param count number of UUIDs to generate (default: 1, max: 100)
     * @param model Spring MVC model for result data
     * @return Thymeleaf fragment "tools/uuid :: result" for HTMX target replacement
     * @see UuidService#generateUuidV7(int)
     */
    @PostMapping("/generate-v7")
    public String generateV7(@RequestParam(defaultValue = "1") int count, Model model) {
        Map<String, Object> result = uuidService.generateUuidV7(count);
        model.addAttribute("result", result);
        return "tools/uuid :: result";
    }

    /**
     * Parses a UUID string and displays detailed metadata.
     *
     * <p>Displays:
     * <ul>
     *   <li>UUID canonical format (lowercase with hyphens)</li>
     *   <li>Version and variant numbers</li>
     *   <li>Human-readable type description</li>
     *   <li>Most/least significant bits (hexadecimal)</li>
     *   <li>Embedded timestamp (for time-based UUIDs)</li>
     * </ul>
     *
     * <p><b>Error Handling:</b> Invalid UUID formats return error message
     * with parse failure details (e.g., incorrect hyphen placement, invalid characters).
     *
     * <p><b>Input Flexibility:</b> Accepts uppercase/lowercase, leading/trailing whitespace.
     *
     * @param uuid UUID string to parse (format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
     * @param model Spring MVC model for parse result
     * @return Thymeleaf fragment "tools/uuid :: parseResult" for HTMX target replacement
     * @see UuidService#parseUuid(String)
     */
    @PostMapping("/parse")
    public String parseUuid(@RequestParam String uuid, Model model) {
        Map<String, Object> result = uuidService.parseUuid(uuid);
        model.addAttribute("parseResult", result);
        return "tools/uuid :: parseResult";
    }
}
