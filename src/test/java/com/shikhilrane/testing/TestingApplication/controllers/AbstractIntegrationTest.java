package com.shikhilrane.testing.TestingApplication.controllers;

import com.shikhilrane.testing.TestingApplication.TestContainerConfiguration;
import com.shikhilrane.testing.TestingApplication.dto.EmployeeDto;
import com.shikhilrane.testing.TestingApplication.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient(timeout = "100000") // Automatically configures WebTestClient for integration testing and sets a maximum wait time of 100000 ms for API responses so the test does not fail if the response takes longer
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Loads the full Spring Boot application context and starts the server on a random port for integration testing
@Import(TestContainerConfiguration.class) // Imports TestContainerConfiguration so that the PostgreSQL Testcontainer is started and used during tests
public class AbstractIntegrationTest {

    @Autowired
    WebTestClient webTestClient;    // Used to send HTTP requests (GET, POST, PUT, DELETE) to the API endpoints during integration tests

    Employee testEmployee = Employee.builder()  // Sample Employee entity used as test data to save or use in database during integration tests
            .email("shikhil@gmail.com")
            .name("Shikhil")
            .salary(200L)
            .build();
    EmployeeDto testEmployeeDto = EmployeeDto.builder() // Sample EmployeeDto used as request body while calling API endpoints in integration tests
            .email("shikhil@gmail.com")
            .name("Shikhil")
            .salary(200L)
            .build();
}
