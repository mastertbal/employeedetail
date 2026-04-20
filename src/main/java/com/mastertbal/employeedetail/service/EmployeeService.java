package com.mastertbal.employeedetail.service;

import com.mastertbal.employeedetail.entity.EmployeeRequestDto;
import com.mastertbal.employeedetail.entity.ImportResultDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EmployeeService {

    EmployeeRequestDto saveEmployee(EmployeeRequestDto employeeRequestDto);

    List<EmployeeRequestDto> getAllEmployees();

    List<EmployeeRequestDto> getPaginatedEmployees(Pageable pageable);

    List<EmployeeRequestDto> findBySalaryRange(BigDecimal min, BigDecimal max);

    EmployeeRequestDto getEmployeeById(Long id);

    EmployeeRequestDto updateEmployee(Long id, EmployeeRequestDto employeeRequestDto);

    EmployeeRequestDto updateEmployeePatch(Long id, Map<String, Object> fields);

    void hardDelete(Long id);

    void softDelete(Long id);

    ImportResultDto importExcelData(MultipartFile file, String fileName) throws Exception;

    byte[] downloadEmployeeExcelData() throws IOException;

    void downloadEmployeePdfData(HttpServletResponse response) throws Exception;
}
