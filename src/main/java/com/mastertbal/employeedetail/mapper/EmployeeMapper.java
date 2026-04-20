package com.mastertbal.employeedetail.mapper;

import com.mastertbal.employeedetail.entity.Employee;
import com.mastertbal.employeedetail.entity.EmployeeRequestDto;

import java.util.List;

public class EmployeeMapper {

    public static Employee toEmployee(EmployeeRequestDto employeeRequestDto) {
        return Employee.builder()
                .firstName(employeeRequestDto.getFirstName())
                .lastName(employeeRequestDto.getLastName())
                .email(employeeRequestDto.getEmail())
                .department(employeeRequestDto.getDepartment())
                .salary(employeeRequestDto.getSalary())
                .dateOfJoining(employeeRequestDto.getDateOfJoining())
                .active(employeeRequestDto.getActive())
                .build();
    }

    public static EmployeeRequestDto toEmployeeRequestDto(Employee employee) {
        return new EmployeeRequestDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getSalary(),
                employee.getDateOfJoining(),
                employee.getActive(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    public static List<EmployeeRequestDto> toEmpRequestDtoList(List<Employee> employees) {
        return employees.stream()
                .map(emp -> toEmployeeRequestDto(emp))
                .toList();
    }
}
