package com.example.devtools_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for decoding and verifying JSON Web Tokens (JWT).
 *
 * <p>Provides utilities for:
 * <ul>
 *   <li>Decoding JWT structure (header, payload, signature)</li>
 *   <li>Extracting and formatting standard JWT claims (iat, exp, nbf)</li>
 *   <li>Verifying HMAC-based signatures (HS256, HS384, HS512)</li>
 * </ul>
 *
 * <p><b>Supported Standards:</b> RFC 7519 (JWT), RFC 7515 (JWS)
 *
 * <p><b>Limitations:</b>
 * <ul>
 *   <li>RSA/EC algorithms not supported (require asymmetric keys)</li>
 *   <li>JWE (encrypted tokens) not supported</li>
 *   <li>No validation of registered claims beyond expiration check</li>
 *   <li>Intended for development/debugging, not production use</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This service is stateless and thread-safe.
 * ObjectMapper is thread-safe for serialization operations.
 *
 * <p><b>Error Handling Strategy:</b> All errors return Map with "error" key
 * rather than throwing exceptions, allowing graceful error display in UI.
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final ObjectMapper objectMapper;

    /**
     * Decodes a JWT token and extracts header, payload, and signature information.
     *
     * <p>Performs the following operations:
     * <ol>
     *   <li>Splits token by '.' delimiter (expects 2-3 parts)</li>
     *   <li>Base64URL decodes header and payload</li>
     *   <li>Parses JSON and formats for display</li>
     *   <li>Checks token expiration against current system time</li>
     *   <li>Formats Unix timestamps to human-readable dates</li>
     * </ol>
     *
     * <p><b>Timestamp Handling:</b> All timestamps formatted to system default timezone.
     * For production use, consider UTC or user-specific timezone.
     *
     * <p><b>Return Map Structure:</b>
     * <ul>
     *   <li>success (boolean) - Operation success indicator</li>
     *   <li>header (String) - Formatted JSON header</li>
     *   <li>payload (String) - Formatted JSON payload</li>
     *   <li>signature (String) - Base64URL encoded signature (if present)</li>
     *   <li>hasSignature (boolean) - Whether token includes signature</li>
     *   <li>isExpired (boolean) - Token expiration status (if exp claim present)</li>
     *   <li>expFormatted (String) - Human-readable expiration time (if present)</li>
     *   <li>iatFormatted (String) - Human-readable issued-at time (if present)</li>
     *   <li>nbfFormatted (String) - Human-readable not-before time (if present)</li>
     *   <li>error (String) - Error message (if operation failed)</li>
     * </ul>
     *
     * @param token JWT token string (whitespace is trimmed)
     * @return Map containing decoded token information or error details
     */
    public Map<String, Object> decodeJwt(String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            String[] parts = token.trim().split("\\.");
            if (parts.length < 2 || parts.length > 3) {
                result.put("error", "Invalid JWT format. Expected 2 or 3 parts separated by dots.");
                return result;
            }

            // Decode header - contains algorithm and token type
            String headerJson = decodeBase64Url(parts[0]);
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            result.put("header", formatJson(header));
            result.put("headerRaw", headerJson);

            // Decode payload - contains claims and user data
            String payloadJson = decodeBase64Url(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);
            result.put("payload", formatJson(payload));
            result.put("payloadRaw", payloadJson);

            // Check expiration status - critical for token validity
            if (payload.containsKey("exp")) {
                long exp = ((Number) payload.get("exp")).longValue();
                Instant expInstant = Instant.ofEpochSecond(exp);
                boolean isExpired = Instant.now().isAfter(expInstant);
                result.put("isExpired", isExpired);
                result.put("expFormatted", formatTimestamp(exp));
            }

            // Format issued-at timestamp for display
            if (payload.containsKey("iat")) {
                long iat = ((Number) payload.get("iat")).longValue();
                result.put("iatFormatted", formatTimestamp(iat));
            }

            // Format not-before timestamp for display
            if (payload.containsKey("nbf")) {
                long nbf = ((Number) payload.get("nbf")).longValue();
                result.put("nbfFormatted", formatTimestamp(nbf));
            }

            // Signature presence indicator - verification requires separate endpoint
            if (parts.length == 3) {
                result.put("signature", parts[2]);
                result.put("hasSignature", true);
            } else {
                result.put("hasSignature", false);
            }

            result.put("success", true);

        } catch (Exception e) {
            // Catch-all for Base64 decode errors, JSON parse errors, etc.
            result.put("error", "Failed to decode JWT: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    /**
     * Verifies JWT signature using HMAC algorithm and provided secret key.
     *
     * <p><b>Security Considerations:</b>
     * <ul>
     *   <li>Algorithm extracted from header (not user input) to prevent substitution attacks</li>
     *   <li>Only HMAC algorithms supported (symmetric key)</li>
     *   <li>No constant-time comparison (timing attacks possible but acceptable for dev tool)</li>
     *   <li>Secret transmitted/stored in plaintext (use HTTPS in production scenarios)</li>
     * </ul>
     *
     * <p><b>Verification Process:</b>
     * <ol>
     *   <li>Validate token has 3 parts (header.payload.signature)</li>
     *   <li>Extract algorithm from header</li>
     *   <li>Calculate expected signature using algorithm and secret</li>
     *   <li>Compare calculated signature with provided signature</li>
     * </ol>
     *
     * <p><b>Algorithm Support:</b> HS256, HS384, HS512 only. RSA/EC algorithms
     * require public key infrastructure not suitable for symmetric dev tool.
     *
     * <p><b>Return Map Structure:</b>
     * <ul>
     *   <li>valid (boolean) - Signature verification result</li>
     *   <li>algorithm (String) - Algorithm used (from header)</li>
     *   <li>message (String) - Human-readable result message</li>
     *   <li>error (String) - Error message (if operation failed)</li>
     * </ul>
     *
     * @param token JWT token string with signature (3 parts required)
     * @param secret HMAC secret key for signature verification
     * @return Map containing verification result or error details
     */
    public Map<String, Object> verifyJwt(String token, String secret) {
        Map<String, Object> result = new HashMap<>();

        try {
            String[] parts = token.trim().split("\\.");
            if (parts.length != 3) {
                result.put("error", "JWT must have 3 parts for signature verification");
                result.put("valid", false);
                return result;
            }

            // Extract algorithm from header to prevent algorithm substitution attacks
            String headerJson = decodeBase64Url(parts[0]);
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String algorithm = (String) header.get("alg");

            if (algorithm == null) {
                result.put("error", "No algorithm specified in header");
                result.put("valid", false);
                return result;
            }

            // Calculate expected signature using header-specified algorithm
            String data = parts[0] + "." + parts[1];
            String expectedSignature = calculateSignature(data, secret, algorithm);

            boolean isValid = parts[2].equals(expectedSignature);
            result.put("valid", isValid);
            result.put("algorithm", algorithm);

            if (isValid) {
                result.put("message", "Signature is valid!");
            } else {
                result.put("message", "Signature verification failed. The secret key may be incorrect.");
            }

        } catch (Exception e) {
            result.put("error", "Verification failed: " + e.getMessage());
            result.put("valid", false);
        }

        return result;
    }

    /**
     * Decodes Base64URL encoded string (JWT standard encoding).
     *
     * <p><b>Base64URL vs Standard Base64:</b> JWT uses URL-safe variant without padding.
     * This method adds padding as needed for Java's Base64 decoder.
     *
     * <p><b>Padding Calculation:</b> Base64 requires length divisible by 4.
     * Missing padding is calculated and appended (1-3 '=' characters).
     *
     * @param input Base64URL encoded string (without padding)
     * @return decoded UTF-8 string
     */
    private String decodeBase64Url(String input) {
        // Calculate required padding - Base64 strings must be divisible by 4
        String padded = input;
        int padding = 4 - (input.length() % 4);
        if (padding != 4) {
            padded = input + "=".repeat(padding);
        }

        byte[] decoded = Base64.getUrlDecoder().decode(padded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Formats Map as pretty-printed JSON for UI display.
     *
     * <p><b>Performance Note:</b> Creates new ObjectMapper instance per call
     * to avoid modifying shared instance. Acceptable for low-throughput dev tool.
     *
     * @param map Map to format
     * @return formatted JSON string with indentation
     * @throws JsonProcessingException if map contains non-serializable values
     */
    private String formatJson(Map<String, Object> map) throws JsonProcessingException {
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return prettyMapper.writeValueAsString(map);
    }

    /**
     * Converts Unix epoch seconds to formatted datetime string.
     *
     * <p><b>Timezone Handling:</b> Uses system default timezone. For production,
     * consider UTC or user-configurable timezone preference.
     *
     * <p><b>Format:</b> yyyy-MM-dd HH:mm:ss z (e.g., "2025-02-10 15:30:45 PST")
     *
     * @param epochSeconds Unix timestamp in seconds (JWT standard)
     * @return formatted datetime string in system timezone
     */
    private String formatTimestamp(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    /**
     * Calculates HMAC signature for JWT verification.
     *
     * <p><b>Algorithm Mapping:</b>
     * <ul>
     *   <li>HS256 → HmacSHA256</li>
     *   <li>HS384 → HmacSHA384</li>
     *   <li>HS512 → HmacSHA512</li>
     * </ul>
     *
     * <p><b>Security Note:</b> Secret key bytes derived from UTF-8 encoding.
     * Production systems should use properly derived keys (e.g., PBKDF2).
     *
     * @param data JWT header + payload (format: "base64header.base64payload")
     * @param secret HMAC secret key
     * @param algorithm JWT algorithm name (HS256, HS384, or HS512)
     * @return Base64URL encoded signature (without padding)
     * @throws Exception if algorithm unsupported or cryptographic operation fails
     */
    private String calculateSignature(String data, String secret, String algorithm) throws Exception {
        javax.crypto.Mac mac;

        // Map JWT algorithm names to Java cryptographic algorithm names
        switch (algorithm.toUpperCase()) {
            case "HS256":
                mac = javax.crypto.Mac.getInstance("HmacSHA256");
                break;
            case "HS384":
                mac = javax.crypto.Mac.getInstance("HmacSHA384");
                break;
            case "HS512":
                mac = javax.crypto.Mac.getInstance("HmacSHA512");
                break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm + ". Only HS256, HS384, HS512 are supported for verification.");
        }

        // Initialize MAC with secret key
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), mac.getAlgorithm());
        mac.init(keySpec);

        // Calculate signature and encode as Base64URL without padding
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}
