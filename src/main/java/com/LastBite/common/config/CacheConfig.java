package com.LastBite.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Cấu hình Redis cache với TTL riêng cho từng cache.
 * <p>
 * Tên cache:
 * <ul>
 *   <li>{@code user-profile} — 30 min TTL (changes infrequently)</li>
 *   <li>{@code user-addresses} — 30 min TTL</li>
 *   <li>{@code store-detail} — 15 min TTL (includes schedules)</li>
 *   <li>{@code store-by-slug} — 15 min TTL</li>
 *   <li>{@code store-list} — 5 min TTL (search results change often)</li>
 *   <li>{@code bag-discovery} — 60 sec TTL</li>
 *   <li>{@code bag-detail} — 60 sec TTL</li>
 *   <li>{@code store-bags} — 60 sec TTL</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.json()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(15));

        Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
                "user-profile", defaults.entryTtl(Duration.ofMinutes(30)),
                "user-addresses", defaults.entryTtl(Duration.ofMinutes(30)),
                "store-detail", defaults.entryTtl(Duration.ofMinutes(15)),
                "store-by-slug", defaults.entryTtl(Duration.ofMinutes(15)),
                "store-list", defaults.entryTtl(Duration.ofMinutes(5)),
                "bag-discovery", defaults.entryTtl(Duration.ofSeconds(60)),
                "bag-detail", defaults.entryTtl(Duration.ofSeconds(60)),
                "store-bags", defaults.entryTtl(Duration.ofSeconds(60))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCacheConfig)
                .transactionAware()
                .build();
    }
}
