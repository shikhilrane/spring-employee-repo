package com.shikhilrane.testing.TestingApplication.services;

import com.shikhilrane.testing.TestingApplication.dto.EmployeeDto;

public interface EmployeeService {

    EmployeeDto getEmployeeById(Long id);

    EmployeeDto createNewEmployee(EmployeeDto employeeDto);

    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);

    void deleteEmployee(Long id);
}