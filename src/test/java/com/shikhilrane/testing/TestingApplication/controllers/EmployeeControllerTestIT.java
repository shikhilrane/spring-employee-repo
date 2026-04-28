package com.shikhilrane.testing.TestingApplication.controllers;

import com.shikhilrane.testing.TestingApplication.dto.EmployeeDto;
import com.shikhilrane.testing.TestingApplication.entities.Employee;
import com.shikhilrane.testing.TestingApplication.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeControllerTestIT extends AbstractIntegrationTest {
    @Autowired
    private EmployeeRepository employeeRepository;  // Used to save or delete employee data in the database before running the API tests

    @BeforeEach
    void setUp() {  // Runs before every test and clears the database so that each test starts with a clean state and previous test data does not affect other tests
        employeeRepository.deleteAll();
    }

    // 1. Test to getEmployeeById, when success
    @Test
    void testGetEmployeeById_success() {
        Employee savedEmployee = employeeRepository.save(testEmployee);                 // First we save a test employee in the database so that the API has data to fetch
        webTestClient.get()                                                             // Send a GET request using WebTestClient
                .uri("/employees/getSingleEmployee/{id}", savedEmployee.getId())   // Call the API endpoint with the saved employee id
                .exchange()                                                             // Execute the HTTP request
                .expectStatus().isOk()                                                  // Verify that the API response status is 200 OK
                // .expectBody(EmployeeDto.class).isEqualTo(testEmployeeDto)            // We can either use this or below method
                .expectBody()                                                           // Start validating the response body returned by the API
                .jsonPath("$.id").isEqualTo(savedEmployee.getId())           // Check that the id returned in the response matches the saved employee id
                .jsonPath("$.email").isEqualTo(savedEmployee.getEmail());    // Check that the email returned in the response matches the saved employee email
    }

    // 2. Test to getEmployeeById, when fails
    @Test
    void testGetEmployeeById_failure(){
        webTestClient.get()                                                             // Send a GET request using WebTestClient
                .uri("/employees/getSingleEmployee/1")                              // Call the API endpoint with an id which does not exist in database
                .exchange()                                                             // Execute the HTTP request
                .expectStatus().isNotFound();                                           // Verify that the API returns 404 NOT FOUND status
    }

    // 3. Test to createNewEmployee, when employee does not exist and creation is successful
    @Test
    void testCreateNewEmployee_whenEmployeeDoesNotExists_thenCreateEmployee(){
        webTestClient.post()                                                               // Send a POST request using WebTestClient
                .uri("/employees/createNewEmployee")                                  // Call the API endpoint to create a new employee
                .bodyValue(testEmployeeDto)                                               // Send EmployeeDto as request body (data to create employee)
                .exchange()                                                               // Execute the HTTP request
                .expectStatus().isCreated()                                               // Verify that the API returns 201 CREATED status
                .expectBody()                                                             // Start validating the response body returned by the API
                .jsonPath("$.name").isEqualTo(testEmployeeDto.getName())       // Verify that the name in response matches the name sent in request
                .jsonPath("$.email").isEqualTo(testEmployeeDto.getEmail());    // Verify that the email in response matches the email sent in request
    }

    // 4. Test to createNewEmployee, when employee already exists (failure case)
    @Test
    void testCreateNewEmployee_whenEmployeeAlreadyExists_thenThrowException(){
        Employee savedEmployee = employeeRepository.save(testEmployee);   // First save an employee in the database so that the same email already exists
        webTestClient.post()                                              // Send a POST request using WebTestClient
                .uri("/employees/createNewEmployee")                  // Call the API endpoint to create a new employee
                .bodyValue(testEmployeeDto)                               // Send EmployeeDto in the request body (same email as existing employee)
                .exchange()                                               // Execute the HTTP request
                .expectStatus().is5xxServerError();                       // Verify that the API returns server error because employee with same email already exists
    }

    // 5. Test to updateEmployee, when employee exists and update is successful
    @Test
    void testUpdateEmployee_whenEmployeeIsValid_thenUpdateEmployee(){
        Employee savedEmployee = employeeRepository.save(testEmployee);              // First save an employee in the database so that there is an existing employee to update

        testEmployeeDto.setId(savedEmployee.getId());                                   // Set the same id in DTO so that the API knows which employee needs to be updated
        testEmployeeDto.setName("Random");                                              // Modify the name to simulate an update request
        testEmployeeDto.setSalary(400L);                                                // Modify the salary to simulate an update request

        webTestClient.put()                                                             // Send a PUT request using WebTestClient
                .uri("/employees/updateEmployee/{id}", savedEmployee.getId())       // Call the update API with the employee id
                .bodyValue(testEmployeeDto)                                             // Send the updated EmployeeDto as request body
                .exchange()                                                             // Execute the HTTP request
                .expectStatus().isOk()                                                   // Verify that the API returns 200 OK status
                .expectBody()                                                            // Start validating the response body returned by the API
                .jsonPath("$.name").isEqualTo(testEmployeeDto.getName())      // Verify that the updated name in response matches the new name sent in request
                .jsonPath("$.salary").isEqualTo(testEmployeeDto.getSalary()); // Verify that the updated salary in response matches the new salary sent in request
    }

    // 6.1. Test to updateEmployee, when employee does not exist and update fails
    @Test
    void testUpdateEmployee_whenEmployeeDoesNotExists_thenThrowException() {
        webTestClient.put()                                           // Send a PUT request using WebTestClient
                .uri("/employees/updateEmployee/999")             // Call the update API with an id that does not exist in the database
                .bodyValue(testEmployeeDto)                           // Send EmployeeDto as request body (update data)
                .exchange()                                           // Execute the HTTP request
                .expectStatus().isNotFound();                         // Verify that the API returns 404 NOT FOUND because employee does not exist
    }

    // 6.2. Test to updateEmployee, when trying to update email and update fails
    @Test
    void testUpdateEmployee_whenAttemptingToUpdateTheEmail_thenThrowException(){
        Employee savedEmployee = employeeRepository.save(testEmployee);             // First save an employee in the database so that there is an existing employee
        testEmployeeDto.setName("Random Name");                                     // Modify the name to simulate an update request
        testEmployeeDto.setEmail("random@gmail.com");                               // Change the email to simulate an invalid update (email update is not allowed)

        webTestClient.put()                                                         // Send a PUT request using WebTestClient
                .uri("/employees/updateEmployee/{id}", savedEmployee.getId())   // Call the update API with the existing employee id
                .bodyValue(testEmployeeDto)                                         // Send the modified EmployeeDto as request body
                .exchange()                                                         // Execute the HTTP request
                .expectStatus().is5xxServerError();                                 // Verify that the API returns server error because email update is not allowed
    }

    // 7. Test to deleteEmployee, when employee exists and deletion is successful
    @Test
    void testDeleteEmployee_whenEmployeeExists_thenDeleteEmployee(){
        Employee savedEmployee = employeeRepository.save(testEmployee);             // First save an employee in the database so that there is an employee to delete
        webTestClient.delete()                                                      // Send a DELETE request using WebTestClient
                .uri("/employees/deleteEmployee/{id}", savedEmployee.getId())   // Call the delete API with the saved employee id
                .exchange()                                                         // Execute the HTTP request
                .expectStatus().isNoContent()                                       // Verify that the API returns 204 NO CONTENT meaning deletion was successful
                .expectBody(Void.class);                                            // Verify that the response body is empty after deletion

        webTestClient.delete()                                                      // Send DELETE request again for the same employee
                .uri("/employees/deleteEmployee/{id}", savedEmployee.getId())   // Call the delete API again with the same id
                .exchange()                                                         // Execute the HTTP request
                .expectStatus().isNotFound();                                       // Verify that API returns 404 NOT FOUND because employee is already deleted
    }

    // 8. Test to deleteEmployee, when employee does not exist and deletion fails
    @Test
    void testDeleteEmployee_whenEmployeeDoesNotExists_thenThrowException(){
        webTestClient.delete()                                           // Send a DELETE request using WebTestClient
                .uri("/employees/deleteEmployee/1")                  // Call the delete API with an id that does not exist in the database
                .exchange()                                              // Execute the HTTP request
                .expectStatus().isNotFound();                            // Verify that the API returns 404 NOT FOUND because employee does not exist
    }
}