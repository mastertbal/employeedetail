package com.mastertbal.employeedetail.controller;

import com.mastertbal.employeedetail.entity.EmployeeRequestDto;
import com.mastertbal.employeedetail.entity.ImportResultDto;
import com.mastertbal.employeedetail.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    public final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeRequestDto>> getPaginatedEmployee(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "2") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ) {
        Sort sort = null;
        if (sortDirection.equalsIgnoreCase("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        List<EmployeeRequestDto> employeeRequestDtos =
                employeeService.getPaginatedEmployees(PageRequest.of(pageNumber, pageSize, sort));
        return ResponseEntity.status(HttpStatus.OK.value()).body(employeeRequestDtos);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeRequestDto>> getAllEmployees(){
        List<EmployeeRequestDto> employeeRequestDtos =  employeeService.getAllEmployees();
        return ResponseEntity.status(HttpStatus.OK).body(employeeRequestDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeRequestDto> getEmployeeById(@PathVariable Long id) {
        EmployeeRequestDto employeeRequestDto = employeeService.getEmployeeById(id);
        return ResponseEntity.status(HttpStatus.OK.value()).body(employeeRequestDto);
    }

    @GetMapping("/salary-range")
    public ResponseEntity<List<EmployeeRequestDto>> getSalaryRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<EmployeeRequestDto> employeeRequestDtos =  employeeService.findBySalaryRange(min, max);
        return ResponseEntity.status(HttpStatus.OK).body(employeeRequestDtos);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> downloadEmployeeExcelData() throws IOException {
        byte[] excelByteData = employeeService.downloadEmployeeExcelData();
        long currentTime = System.currentTimeMillis();
        String contentDisposition = "attachment; filename_" + currentTime + ".xlsx";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees_" + currentTime + ".xlsx" )
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelByteData);
    }

    @GetMapping("/export/pdf")
    public void downloadEmployeePdfData(HttpServletResponse response) throws Exception {
        employeeService.downloadEmployeePdfData(response);
    }

    @PostMapping
    public ResponseEntity<EmployeeRequestDto> saveEmployee(@Valid @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeRequestDto savedEmp = employeeService.saveEmployee(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(savedEmp);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importExcelData(MultipartFile file, String fileName)
            throws Exception {
        ImportResultDto importResultDto = employeeService.importExcelData(file, fileName);
        return ResponseEntity.status(HttpStatus.OK).body(importResultDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeRequestDto> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequestDto employeeRequestDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body( employeeService.updateEmployee(id, employeeRequestDto) );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeRequestDto> patchUpdate(@PathVariable Long id, @RequestBody Map<String, Object> fields){
        EmployeeRequestDto employeeRequestDto = employeeService.updateEmployeePatch(id, fields);
        return ResponseEntity.status(HttpStatus.OK).body(employeeRequestDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> invalidateEmployee(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT.value())
                .body("Employee invalidated");
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.hardDelete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT.value())
                .body("Employee deleted successfully");
    }

}
