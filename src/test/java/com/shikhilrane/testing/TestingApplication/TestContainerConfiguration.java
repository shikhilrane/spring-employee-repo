package com.shikhilrane.testing.TestingApplication;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration                                                     // Used to define configuration specifically for test classes
public class TestContainerConfiguration {
    @Bean                                                              // Creates a Spring bean so that the container can be managed by Spring
    @ServiceConnection                                                 // Automatically connects this container to Spring Boot datasource configuration
    PostgreSQLContainer<?> postgresContainer() {                       // Creates and starts a PostgreSQL Docker container using the latest postgres image
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest")); // Creates a PostgreSQL Docker container using the latest postgres image which will be used as a test database
    }
}

// Basically it is saying that, to run the latest docker image