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

@Service
@RequiredArgsConstructor
public class JwtService {

    private final ObjectMapper objectMapper;

    public Map<String, Object> decodeJwt(String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            String[] parts = token.trim().split("\\.");
            if (parts.length < 2 || parts.length > 3) {
                result.put("error", "Invalid JWT format. Expected 2 or 3 parts separated by dots.");
                return result;
            }

            // Decode header
            String headerJson = decodeBase64Url(parts[0]);
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            result.put("header", formatJson(header));
            result.put("headerRaw", headerJson);

            // Decode payload
            String payloadJson = decodeBase64Url(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);
            result.put("payload", formatJson(payload));
            result.put("payloadRaw", payloadJson);

            // Check expiration
            if (payload.containsKey("exp")) {
                long exp = ((Number) payload.get("exp")).longValue();
                Instant expInstant = Instant.ofEpochSecond(exp);
                boolean isExpired = Instant.now().isAfter(expInstant);
                result.put("isExpired", isExpired);
                result.put("expFormatted", formatTimestamp(exp));
            }

            // Format issued at
            if (payload.containsKey("iat")) {
                long iat = ((Number) payload.get("iat")).longValue();
                result.put("iatFormatted", formatTimestamp(iat));
            }

            // Format not before
            if (payload.containsKey("nbf")) {
                long nbf = ((Number) payload.get("nbf")).longValue();
                result.put("nbfFormatted", formatTimestamp(nbf));
            }

            // Signature (just show it exists)
            if (parts.length == 3) {
                result.put("signature", parts[2]);
                result.put("hasSignature", true);
            } else {
                result.put("hasSignature", false);
            }

            result.put("success", true);

        } catch (Exception e) {
            result.put("error", "Failed to decode JWT: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    public Map<String, Object> verifyJwt(String token, String secret) {
        Map<String, Object> result = new HashMap<>();

        try {
            String[] parts = token.trim().split("\\.");
            if (parts.length != 3) {
                result.put("error", "JWT must have 3 parts for signature verification");
                result.put("valid", false);
                return result;
            }

            // Get algorithm from header
            String headerJson = decodeBase64Url(parts[0]);
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String algorithm = (String) header.get("alg");

            if (algorithm == null) {
                result.put("error", "No algorithm specified in header");
                result.put("valid", false);
                return result;
            }

            // Calculate expected signature
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

    private String decodeBase64Url(String input) {
        // Add padding if necessary
        String padded = input;
        int padding = 4 - (input.length() % 4);
        if (padding != 4) {
            padded = input + "=".repeat(padding);
        }

        byte[] decoded = Base64.getUrlDecoder().decode(padded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String formatJson(Map<String, Object> map) throws JsonProcessingException {
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return prettyMapper.writeValueAsString(map);
    }

    private String formatTimestamp(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    private String calculateSignature(String data, String secret, String algorithm) throws Exception {
        javax.crypto.Mac mac;

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

        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), mac.getAlgorithm());
        mac.init(keySpec);

        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}
