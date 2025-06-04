package com.example.redlettuce;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.command-timeout}")
    private Duration commandTimeout;

    @Value("${spring.data.redis.reconnect-delay}")
    private Duration reconnectDelay;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(commandTimeout)
            .shutdownTimeout(Duration.ZERO)
            .clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .build())
            .clientResources(ClientResources.builder()
                .reconnectDelay(Delay.constant(reconnectDelay)) 
                .build())
            .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisHost);
        serverConfig.setPort(redisPort); 
        
        if (redisPassword != null && !redisPassword.isBlank()) {
            serverConfig.setPassword(RedisPassword.of(redisPassword));
        } 

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
