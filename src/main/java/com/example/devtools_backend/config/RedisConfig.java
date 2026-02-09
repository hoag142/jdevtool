package com.example.devtools_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for caching and data storage.
 *
 * <p>Configures RedisTemplate with appropriate serializers for storing:
 * <ul>
 *   <li>User history - Recent tool usage (TTL: 7 days, max 50 items)</li>
 *   <li>Shared snippets - Shareable code/data snippets (TTL: 24 hours)</li>
 *   <li>Usage statistics - Tool usage metrics for analytics</li>
 * </ul>
 *
 * <p><b>Data Structures:</b>
 * <ul>
 *   <li>{@code history:{userId}} - List for chronological history</li>
 *   <li>{@code snippet:{shortId}} - Hash for snippet metadata and content</li>
 *   <li>{@code stats:tools} - Sorted set for usage counters</li>
 * </ul>
 *
 * <p><b>Performance Considerations:</b>
 * <ul>
 *   <li>Connection pooling configured automatically by Spring Boot</li>
 *   <li>JSON serialization overhead acceptable for dev tools use case</li>
 *   <li>No Redis transaction requirements (atomic operations sufficient)</li>
 * </ul>
 *
 * <p><b>Failure Handling:</b> Application continues to function without Redis
 * (graceful degradation), but history and snippet sharing will be unavailable.
 *
 * @author DevTools Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RedisConfig {

    /**
     * Configures RedisTemplate with JSON serialization for values and String serialization for keys.
     *
     * <p><b>Serialization Strategy:</b>
     * <ul>
     *   <li>Keys: StringRedisSerializer - Human-readable keys in Redis CLI</li>
     *   <li>Values: GenericJackson2JsonRedisSerializer - Preserves Java type information</li>
     *   <li>Hash Keys: StringRedisSerializer - For map-like structures</li>
     *   <li>Hash Values: GenericJackson2JsonRedisSerializer - For nested objects</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> RedisTemplate is thread-safe and can be shared across services.
     *
     * @param connectionFactory auto-configured by Spring Boot based on application properties
     * @return configured RedisTemplate ready for injection
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys ensures Redis CLI compatibility
        template.setKeySerializer(new StringRedisSerializer());

        // JSON serializer for values preserves type information for deserialization
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash operations use same serializers for consistency
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Initialize serializers
        template.afterPropertiesSet();
        return template;
    }
}
