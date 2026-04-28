package com.shikhilrane.testing.TestingApplication.repositories;

import com.shikhilrane.testing.TestingApplication.TestContainerConfiguration;
import com.shikhilrane.testing.TestingApplication.entities.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest                                             // 3. Give this annotation for configuring testing in the whole class | 5.2.
@Import(TestContainerConfiguration.class)                      // 8.1 Import the docker configuration file
@DataJpaTest                  // 5.1. this will only scan within repository level and also use test db according to scope from pom.xml
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)     // 4. Replace Real DB with Test DB | 5.3.
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // 8.2 To not get this db use along with Docker DB
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;          // 2. Autowire the repository for testing

    // 6.2 Get the Employee entity reference so that we can create employee to test
    private Employee employee;

    // 6.3 To test the employee for every case, we will use @BeforeEach so that this object we create here can be used in every test case
    @BeforeEach
    void setUp(){
        employee = Employee.builder()
                .name("Shikhil")
                .email("shikhil@gmail.com")
                .salary(100L)
                .build();
    }

    @Test
    void testFindByEmail_whenEmailIsPresent_thenReturnEmployee() {        // 1. Give proper name to test method
        // 6.1 There are 3 step to create test case, Arrange, Act and Assert.
        // Arrange (Given) - Arrange the test case or prepare the test
        employeeRepository.save(employee);      // 6.4 Save the employee in the test database so that it can be searched later

        // Act (When) - Do method call (like I want to pass an employee and when I want to get this employee by findByEmail())
        List<Employee> findEmployeeByEmail = employeeRepository.findByEmail(employee.getEmail());   // 6.5 Call the repository method findByEmail() to fetch employee using email

        // Assert (Then) - Verify the result (then I should get that employee)
        assertThat(findEmployeeByEmail).isNotNull();   // 6.6 Check that the returned list is not null
        assertThat(findEmployeeByEmail).isNotEmpty();   // 6.7 Check that the returned list is not empty (employee should be present)
        assertThat(findEmployeeByEmail.getFirst().getEmail()).isEqualTo(employee.getEmail());   // 6.8 Verify that the email of the fetched employee matches the expected email
    }

    @Test
    void testFindByEmail_whenEmailIsNotFound_thenReturnEmptyEmployeeList(){
        // Arrange
        String email = "notPresent.123@gmail.com";  // 7.1 Create an email which is not present in the database

        // Act
        List<Employee> byEmail = employeeRepository.findByEmail(email); // 7.2 Call the repository method findByEmail() using the email that does not exist

        // Assert
        assertThat(byEmail).isNotNull();    // 7.3 Verify that the returned list is not null
        assertThat(byEmail).isEmpty();      // 7.4 Verify that the returned list is empty because no employee exists with this email
    }
}