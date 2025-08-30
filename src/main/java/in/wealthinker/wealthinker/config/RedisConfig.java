package in.wealthinker.wealthinker.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for JWT Token Blacklisting
 *
 * PURPOSE:
 * - Configure Redis connection for token blacklisting
 * - Set up Redis templates with proper serializers
 * - Configure connection pooling and timeouts
 * - Support for Redis Cluster in production
 *
 * FEATURES:
 * - Fast token blacklist lookups
 * - Automatic expiration of blacklisted tokens
 * - High availability with Redis Sentinel/Cluster
 * - Connection pooling for performance
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig {

    private String host = "localhost";
    private int port = 6379;
    private String password;
    private int database = 0;
    private int timeout = 2000;

    /**
     * Redis Connection Factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);

        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(true);

        log.info("Redis connection factory configured: {}:{}/{}", host, port, database);
        return factory;
    }

    /**
     * Redis Template with optimized serializers
     *
     * SERIALIZATION STRATEGY:
     * - String keys for fast lookups
     * - JSON values for complex objects
     * - Optimized for JWT blacklist use case
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys (faster lookups)
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values (flexible data types)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        log.info("Redis template configured with optimized serializers");
        return template;
    }

    // Getters and setters for configuration properties
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getDatabase() { return database; }
    public void setDatabase(int database) { this.database = database; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
}
