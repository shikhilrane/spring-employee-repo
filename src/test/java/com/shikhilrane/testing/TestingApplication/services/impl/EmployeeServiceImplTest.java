package com.shikhilrane.testing.TestingApplication.services.impl;

import com.shikhilrane.testing.TestingApplication.TestContainerConfiguration;
import com.shikhilrane.testing.TestingApplication.dto.EmployeeDto;
import com.shikhilrane.testing.TestingApplication.entities.Employee;
import com.shikhilrane.testing.TestingApplication.exceptions.ResourceNotFoundException;
import com.shikhilrane.testing.TestingApplication.repositories.EmployeeRepository;
import com.shikhilrane.testing.TestingApplication.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.when;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // 3. Tells Spring not to replace the configured database with an in-memory database during tests
@Import(TestContainerConfiguration.class)   // 2. Imports the TestContainerConfiguration class so that the PostgreSQL test container is used during testing
@ExtendWith(MockitoExtension.class)     // 1. Enables Mockito support in JUnit 5 so that @Mock and @InjectMocks annotations work
class EmployeeServiceImplTest {

    @Spy                                            // Using Spy because ModelMapper is already tested somewhere so we just want to use it as Spy
    private ModelMapper modelMapper;                // 7.6 Create a mock ModelMapper so that real mapping logic is not executed during unit testing

    @Mock
    private EmployeeRepository employeeRepository;  // 5. Creates a mock (fake) object of EmployeeRepository so that the real database is not used

    @InjectMocks
    private EmployeeServiceImpl employeeService;    // 4. Creates an instance of EmployeeServiceImpl and injects the mocked repository into it (@Mock -> @InjectMocks)

    private Employee mockEmployee;                  // 7.4 Declare a mock Employee object which will act as test data

    private EmployeeDto mockEmployeeDto;            // 7.5 Declare a mock EmployeeDto object which represents the DTO returned by the service

    @BeforeEach
    void setUp() {                                  // 7.1 This method runs before every test case to prepare common test data
        mockEmployee = Employee.builder()           // 7.2 Create a mock Employee object using builder pattern which will be used in test cases
                .id(1L)
                .email("shikhil@gmail.com")
                .name("Shikhil")
                .salary(200L)
                .build();
        mockEmployeeDto = modelMapper.map(mockEmployee, EmployeeDto.class);     // 7.3 Convert the Employee entity into EmployeeDto using ModelMapper for testing
    }

    // 6. Create a test method
    // Test to getEmployeeById, when success
    @Test
    void testGetEmployeeById_WhenEmployeeIdIsPresent_thenReturnEmployeeDto(){
        // Assign
        // 8.1 Prepare the test input and define behaviour of mocked repository
        Long id = mockEmployee.getId();
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.of(mockEmployee));    // Stubbing
        // Here we stub the repository so that when findById(id) is called, it returns the mockEmployee instead of calling the real database

        // Act
        // 8.2 Call the service method that we want to test
        EmployeeDto employeeDto = employeeService.getEmployeeById(id);  // This executes the actual service logic which internally calls the mocked repository

        // Assert
        // 8.3 Verify that the returned result is correct
        assertThat(employeeDto).isNotNull();                                    // Check that the returned EmployeeDto is not null
        assertThat(employeeDto.getId()).isEqualTo(id);                          // Verify that the returned id matches the expected id
        assertThat(employeeDto.getEmail()).isEqualTo(mockEmployee.getEmail());  // Verify that the email matches the mock employee email
        verify(employeeRepository, times(1)).findById(id);                        // Verify that repository.findById(id) was called exactly once
    }

    // Test to getEmployeeById, when fails because id not found
    @Test
    void testGetEmployeeById_WhenEmployeeIdIsPresent_ThenReturnEmployeeDto(){
        // Arrange
        // 10.1 Prepare the test input and stub repository behaviour
        Long id = mockEmployee.getId();             // Get the id from the mock employee which will be used as input for the test
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.empty()); // Stub the repository so that when findById(id) is called, it returns an empty Optional (meaning employee with this id does not exist)

        // Act + Assert (We have to combine Act and Assert )
        // 10.2 Call the service method and expect an exception to be thrown
        assertThatThrownBy(() -> employeeService.getEmployeeById(id))
                .isInstanceOf(ResourceNotFoundException.class)                        // Verify that ResourceNotFoundException is thrown
                .hasMessage("Employee not found with id: " + id);                     // Verify that the exception message matches the expected message

        // 10.3 Verify repository interactions
        verify(employeeRepository).findById(id);                                      // Verify that the repository method was called during execution
    }

    // Test to createEmployee, when success
    @Test
    void createNewEmployee_whenValidEmployee_thenCreateNewEmployee(){
        // Assign
        // 9.1 Prepare the mocked behaviour for repository methods
        Mockito.when(employeeRepository.findByEmail(anyString())).thenReturn(List.of()); // Stub the repository method so that when findByEmail() is called with any email, it returns an empty list (meaning no employee with that email already exists)
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee); // Stub the save() method so that whenever any Employee object is passed to save(), it returns mockEmployee instead of saving in a real database

        // Act
        // 9.2 Call the service method that we want to test
        EmployeeDto employeeDto = employeeService.createNewEmployee(mockEmployeeDto); // This executes the actual service logic which internally checks email using findByEmail() and then calls repository.save() to store the new employee

        // Assert
        // 9.3 Verify that the returned result is correct
        assertThat(employeeDto).isNotNull();                                        // Check that the returned EmployeeDto is not null
        assertThat(employeeDto.getEmail()).isEqualTo(mockEmployeeDto.getEmail());   // Verify that the email in returned DTO matches the email of the input DTO

        // We have saved new entity in repository but we don't know what object was actually passed to save() so we use ArgumentCaptor to capture the Employee object that was passed to save()
        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class); // Create an ArgumentCaptor to capture the Employee object passed to repository.save()
        verify(employeeRepository).save(employeeArgumentCaptor.capture()); // Verify that save() method was called and capture the Employee object that was passed to it

        Employee captureValue = employeeArgumentCaptor.getValue();              // Retrieve the captured Employee object that was passed to save()
        assertThat(captureValue.getEmail()).isEqualTo(mockEmployee.getEmail()); // Verify that the email of the captured Employee matches the expected email
    }

    // Test to createEmployee, when fails email already exists
    @Test
    void testCreateNewEmployee_whenAttemptingToCreateEmployeeWithExistingEmail_thenThrowException(){
        // Assign
        // 11.1 Prepare mocked behaviour for repository method
        Mockito.when(employeeRepository.findByEmail(mockEmployeeDto.getEmail())).thenReturn(List.of(mockEmployee)); // Stub repository so that when findByEmail(email) is called, it returns a list containing an existing employee (meaning email already exists)

        // Act + Assert
        // 11.2 Call the service method and expect an exception
        assertThatThrownBy(() -> employeeService.createNewEmployee(mockEmployeeDto))
                .isInstanceOf(RuntimeException.class)                                            // Verify that RuntimeException is thrown
                .hasMessage("Employee already exists with email: " + mockEmployee.getEmail());   // Verify that the exception message matches the expected message

        // 11.3 Verify repository interactions
        verify(employeeRepository).findByEmail((mockEmployeeDto.getEmail()));   // Verify that repository.findByEmail(email) was called during execution
        verify(employeeRepository, never()).save(any());                        // Verify that save() was never called because employee with same email already exists
    }

    // Test to updateEmployee, when success
    @Test
    void testUpdateEmployee_whenValidEmployee_thenUpdateEmployee(){
        // Assign
        // 12.1 Prepare the mocked behaviour for repository methods
        Mockito.when(employeeRepository.findById(mockEmployeeDto.getId())).thenReturn(Optional.of(mockEmployee)); // Stub repository so that findById(id) returns existing employee
        mockEmployeeDto.setName("Random Name");     // Modify the name in DTO to simulate update request
        mockEmployeeDto.setSalary(400L);            // Modify the salary in DTO to simulate update request

        Employee newEmployee = modelMapper.map(mockEmployeeDto, Employee.class);            // Convert updated DTO to Employee entity using ModelMapper
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee); // Stub save() method so that updated employee is returned instead of saving to real database

        // Act
        // 12.2 Call the service method that we want to test
        EmployeeDto updatedEmployee = employeeService.updateEmployee(mockEmployeeDto.getId(), mockEmployeeDto); // Call the service method to update employee

        // Assert
        // 12.3 Verify repository interactions
        assertThat(updatedEmployee).isEqualTo(mockEmployeeDto);     // Verify that returned DTO matches the updated DTO values
        verify(employeeRepository).findById(1L);                    // Verify that repository.findById(id) was called to fetch existing employee
        verify(employeeRepository).save(any());                     // Verify that repository.save() was called to save the updated employee
    }

    // Test to updateEmployee, when fails because id not found
    @Test
    void testUpdateEmployee_whenEmployeeDoesNotExists_thenThrowException(){
        // Assign
        // 13.1 Prepare mocked behaviour so repository returns empty result
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.empty()); // Stub repository so that findById(1L) returns empty Optional meaning employee does not exist

        // Act + Assert
        // 13.2 Call the service method and expect exception to be thrown
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, mockEmployeeDto)) // Execute service method which should fail because employee is not found
                .isInstanceOf(ResourceNotFoundException.class)      // Verify that ResourceNotFoundException is thrown
                .hasMessage("Employee not found with id: 1");       // Verify that exception message matches expected message

        // 13.3 Verify repository interactions
        verify(employeeRepository).findById(1L);                    // Verify that repository.findById(1L) was called
        verify(employeeRepository, never()).save(any());            // Verify that save() was never called because employee does not exist
    }

    // Test to updateEmployee, when fails because email is treated as a unique identifier for the employee and update is not allowed
    @Test
    void testUpdateEmployee_whenAttemptingToUpdateEmail_thenThrowException(){
        // Assign
        // 14.1 Prepare the mocked behaviour for repository methods
        Mockito.when(employeeRepository.findById(mockEmployeeDto.getId())).thenReturn(Optional.of(mockEmployee)); // Stub repository so that findById(id) returns existing employee
        mockEmployeeDto.setName("Random Name");         // Modify the name in DTO to simulate update request
        mockEmployeeDto.setEmail("random@gmail.com");   // Modify the email in DTO to simulate update request

        // Act + Assert
        // 14.2 Call the service method and expect exception to be thrown
        assertThatThrownBy(() -> employeeService.updateEmployee(mockEmployeeDto.getId(), mockEmployeeDto)) // Execute updateEmployee() which should fail because email update is not allowed
                .isInstanceOf(RuntimeException.class)                         // Verify that RuntimeException is thrown
                .hasMessage("The email of the employee cannot be updated");   // Verify that exception message matches expected message

        // 14.3 Verify repository interactions
        verify(employeeRepository).findById(mockEmployeeDto.getId());   // Verify that repository.findById(id) was called to fetch the existing employee
        verify(employeeRepository, never()).save(any());                // Verify that save() was never called because update operation failed
    }

    // Test to deleteEmployee, when success
    @Test
    void testDeleteEmployee_whenEmployeeIsValid_thenDeleteEmployee(){
        // Arrange
        // 15.1 Prepare the mocked behaviour for repository methods
        Mockito.when(employeeRepository.existsById(1L)).thenReturn(true);   // Stub repository so that existsById(1L) returns true meaning employee exists in database

        // Act + Assert
        // 15.2 Call the service method and verify that no exception is thrown
        assertThatCode(() -> employeeService.deleteEmployee(1L)).doesNotThrowAnyException();    // Execute deleteEmployee() and confirm that it runs successfully without throwing any exception

        // 15.3 Verify repository interactions
        verify(employeeRepository).deleteById(1L);    // Verify that repository.deleteById(1L) was called to delete the employee
    }

    // Test to deleteEmployee, when fails because employee with given id does not exist
    @Test
    void testDeleteEmployee_whenEmployeeDoesNotExists_thenThrowException(){
        // Arrange
        // 16.1 Prepare the mocked behaviour for repository methods
        Mockito.when(employeeRepository.existsById(1L)).thenReturn(false); // Stub repository so that existsById(1L) returns false meaning employee does not exist

        // Act + Assert
        // 16.2 Call the service method and expect exception to be thrown
        assertThatThrownBy(() -> employeeService.deleteEmployee(1L)) // Execute service method which should fail because employee does not exist
                .isInstanceOf(ResourceNotFoundException.class)           // Verify that ResourceNotFoundException is thrown
                .hasMessage("Employee not found with id: " + 1L);        // Verify that exception message matches expected message

        // 16.3 Verify repository interactions
        verify(employeeRepository).existsById(1L);                  // Verify that repository.existsById(1L) was called
        verify(employeeRepository, never()).deleteById(1L);         // Verify that deleteById() was never called because employee does not exist
    }
}