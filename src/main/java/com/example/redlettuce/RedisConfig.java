package com.example.redlettuce;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.commandTimeout}")
    private int commandTimeout;

    @Value("${spring.redis.reconnectDelay}")
    private int reconnectDelay;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(commandTimeout))
            .shutdownTimeout(Duration.ZERO)
            .clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .build())
            .clientResources(ClientResources.builder()
                .reconnectDelay(Delay.constant(Duration.ofMillis(reconnectDelay))) 
                .build())
            .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisHost);
        serverConfig.setPort(redisPort); 
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
