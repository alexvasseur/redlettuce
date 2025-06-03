package com.example.redlettuce;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(3)) //TODO this overrides the application.properties
            .shutdownTimeout(Duration.ZERO)
            .clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .build())
            .clientResources(ClientResources.builder()
                .reconnectDelay(Delay.constant(Duration.ofMillis(200))) //TODO this could be configurable
                .build())
            .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName("localhost"); //TODO this overrides the application.properties
        serverConfig.setPort(6379); //TODO this overrides the application.properties
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
