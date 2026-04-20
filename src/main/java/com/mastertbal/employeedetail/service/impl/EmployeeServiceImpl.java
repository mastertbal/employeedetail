package com.mastertbal.employeedetail.service.impl;

import com.mastertbal.employeedetail.entity.Employee;
import com.mastertbal.employeedetail.entity.EmployeeRequestDto;
import com.mastertbal.employeedetail.entity.ImportResultDto;
import com.mastertbal.employeedetail.exception.DuplicateEmailException;
import com.mastertbal.employeedetail.exception.EmployeeNotFoundException;
import com.mastertbal.employeedetail.exception.InvalidFileFormatException;
import com.mastertbal.employeedetail.exception.SalaryException;
import com.mastertbal.employeedetail.mapper.EmployeeMapper;
import com.mastertbal.employeedetail.repository.EmployeeRepository;
import com.mastertbal.employeedetail.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeRequestDto saveEmployee(EmployeeRequestDto employeeRequestDto) {
        Optional<Employee> employee = employeeRepository.findByEmail(employeeRequestDto.getEmail());
        if (employee.isPresent()) throw new DuplicateEmailException("Employee with email " + employeeRequestDto.getEmail() + " already exist");

        if (employeeRequestDto.getDepartment().equalsIgnoreCase("intern")) {
            if ( employeeRequestDto.getSalary().compareTo( BigDecimal.valueOf(15000) ) == -1 ) {
                throw new SalaryException("Minimum salary of intern should be 15,000");
            }
        }

        if (!employeeRequestDto.getDepartment().equalsIgnoreCase("intern")) {
            if (employeeRequestDto.getSalary().compareTo(BigDecimal.valueOf(30000)) == -1) {
                throw new SalaryException("Minimum salary of " + employeeRequestDto.getDepartment() + " employee should be 30,000");
            }
        }

        Employee newEmployee = EmployeeMapper.toEmployee(employeeRequestDto);
        Employee savedEmployee = employeeRepository.save(newEmployee);
        return EmployeeMapper.toEmployeeRequestDto(savedEmployee);
    }

    @Override
    public List<EmployeeRequestDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.size() <= 0) {
            throw new EmployeeNotFoundException("No employee information found");
        }

        List<EmployeeRequestDto> employeeRequestDtos = EmployeeMapper.toEmpRequestDtoList(employees);
        return employeeRequestDtos;
    }

    @Override
    public List<EmployeeRequestDto> getPaginatedEmployees(Pageable pageable) {
        List<Employee> employees = employeeRepository.findAll(pageable).getContent();
        if (employees.size() <= 0) throw new EmployeeNotFoundException("Employees information not found");
        return EmployeeMapper.toEmpRequestDtoList(employees);
    }

    @Override
    public List<EmployeeRequestDto> findBySalaryRange(BigDecimal min, BigDecimal max) {
        List<Employee> employees = employeeRepository.findBySalaryRange(min, max);
        return EmployeeMapper.toEmpRequestDtoList(employees);
    }

    @Override
    public EmployeeRequestDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + id + " not found"));
        return EmployeeMapper.toEmployeeRequestDto(employee);
    }

    @Transactional
    @Override
    public EmployeeRequestDto updateEmployee(Long id, EmployeeRequestDto employeeRequestDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + id + " not found"));

        if (employeeRequestDto.getFirstName() != null) employee.setFirstName(employeeRequestDto.getFirstName());
        if (employeeRequestDto.getLastName() != null) employee.setLastName(employeeRequestDto.getLastName());
        if (employeeRequestDto.getEmail() != null) employee.setEmail(employeeRequestDto.getEmail());
        if (employeeRequestDto.getDepartment() != null) employee.setDepartment(employeeRequestDto.getDepartment());
        if (employeeRequestDto.getSalary() != null) employee.setSalary(employeeRequestDto.getSalary());

        Employee savedEmp = employeeRepository.save(employee);

        return EmployeeMapper.toEmployeeRequestDto(savedEmp);
    }

    @Transactional
    @Override
    public EmployeeRequestDto updateEmployeePatch(Long id, Map<String, Object> fields) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + id + " not found"));
        fields.forEach( (key, value) -> {
            Field field = null;
            if (value instanceof Integer || value instanceof Double) {
                String sVal = String.valueOf(value);
                Double dVal = Double.valueOf(sVal);
                BigDecimal newVal = BigDecimal.valueOf(dVal);
                field = ReflectionUtils.findField(Employee.class, key);
                field.setAccessible(true);
                ReflectionUtils.setField(field, employee, newVal);
            } else {
                field = ReflectionUtils.findField(Employee.class, key);
                field.setAccessible(true);
                ReflectionUtils.setField(field, employee, value);
            }
        });

        Employee savedEmp = employeeRepository.save(employee);
        return EmployeeMapper.toEmployeeRequestDto(savedEmp);
    }

    @Transactional
    @Override
    public void hardDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + id + " not found"));
        employeeRepository.delete(employee);
    }

    @Transactional
    @Override
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + id + " not found"));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    @Transactional
    @Override
    public ImportResultDto importExcelData(MultipartFile file, String fileName) throws Exception {
        if (file == null) throw new InvalidFileFormatException("File not available");
        int index = file.getOriginalFilename().indexOf('.');
        String extension = file.getOriginalFilename().substring(index + 1);
        if (!extension.equalsIgnoreCase("xlsx")) throw new InvalidFileFormatException("File format of " + extension + " not supported");
        return saveExcelEntries(file);
    }

    @Override
    public byte[] downloadEmployeeExcelData() throws IOException {

        List<Employee> employees = employeeRepository.findAll();
        if (employees.size() <= 0) throw new EmployeeNotFoundException("No employee Data");
        List<EmployeeRequestDto> employeeRequestDtos = EmployeeMapper.toEmpRequestDtoList(employees);

        return writeDataToExcel(employeeRequestDtos);
    }

    @Override
    public void downloadEmployeePdfData(HttpServletResponse response) throws Exception {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.size() <= 0) throw new EmployeeNotFoundException("No employee Data");

        List<EmployeeRequestDto> employeeRequestDtos = EmployeeMapper.toEmpRequestDtoList(employees);
        writeDataToPdf(employeeRequestDtos, response);
    }

    private void writeDataToPdf(List<EmployeeRequestDto> employeeRequestDtos, HttpServletResponse response)
            throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // write table header
        createHeader(employeeRequestDtos, document);

        // write table header
        PdfPTable table = new PdfPTable(7);
        table.setSpacingBefore(30);
        table.setWidthPercentage(100);
        createTableHeader(table);
        document.add(table);


        // write table data
        PdfPTable dataTable = new PdfPTable(7);
        dataTable.setWidthPercentage(100);
        writeTableData(dataTable, employeeRequestDtos);
        document.add(dataTable);

        response.setHeader("Content-Disposition", "attachment; filename=users.pdf");
        document.close();
    }

    private void writeTableData(PdfPTable table, List<EmployeeRequestDto> employeeRequestDtos) {
        for (int i = 0; i < employeeRequestDtos.size(); i++) {
                EmployeeRequestDto empDto = employeeRequestDtos.get(i);
                PdfPCell cell = new PdfPCell();


                if (!empDto.getActive()) {
                    Font strikethroughFont = FontFactory.getFont(FontFactory.HELVETICA);
                    strikethroughFont.setStyle(Font.STRIKETHRU);

                    if (i % 2 == 0) strikethroughFont.setColor(new Color(130, 202, 255));

                    cell.setPhrase(new Phrase( String.valueOf(empDto.getId()), strikethroughFont));
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getFirstName() + " " + empDto.getLastName(), strikethroughFont));
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getEmail(), strikethroughFont));
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getDepartment(), strikethroughFont));
                    table.addCell(cell);


                    cell.setPhrase(new Phrase('\u20a6' + String.valueOf(empDto.getSalary()), strikethroughFont));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getDateOfJoining().format(DateTimeFormatter.ISO_DATE), strikethroughFont));
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getActive().toString().toUpperCase(), strikethroughFont));
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                } else {
                    if (i % 2 == 0) cell.setBackgroundColor(new Color(70, 130, 180));
                    cell.setPhrase(new Phrase( String.valueOf(empDto.getId())));
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getFirstName() + " " + empDto.getLastName()));
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getEmail()));
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getDepartment()));
                    table.addCell(cell);


                    cell.setPhrase(new Phrase('\u20a6' + String.valueOf(empDto.getSalary())));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getDateOfJoining().format(DateTimeFormatter.ISO_DATE)));
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);

                    cell.setPhrase(new Phrase(empDto.getActive().toString().toUpperCase()));
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }
                writeCellData(cell, table, empDto);
        }
    }

    private void writeCellData(PdfPCell cell, PdfPTable table, EmployeeRequestDto empDto) {
    }

    private void createTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.DARK_GRAY);
        cell.setPadding(5);

        Font font = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.WHITE);

        cell.setPhrase(new Phrase("ID", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Full Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Email", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Department", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Salary", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Date of Joining", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Status", font));
        table.addCell(cell);
    }

    private static void createHeader(List<EmployeeRequestDto> employeeRequestDtos, Document document) {

        Font font = new Font(Font.HELVETICA, 18, Font.BOLD, Color.GRAY);
        document.add(new Paragraph("Company Name: NaijaJUG Inc.", font));
        document.add(new Paragraph("Report Title: Employee Data", font));
        document.add(
                new Paragraph(
                        "Date and Time: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + " " +  LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())),
                        font
                )
        );
        document.add(new Paragraph("Total Record(s): " + employeeRequestDtos.size(), font));
    }

    private byte[] writeDataToExcel(List<EmployeeRequestDto> employeeRequestDtos) throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Employees");

        XSSFRow row = sheet.createRow(0);

        for (int i = 0; i < 10; i++) {
            switch (i) {
                case 0 -> row.createCell(i).setCellValue("id");
                case 1 -> row.createCell(i).setCellValue("firstName");
                case 2 -> row.createCell(i).setCellValue("lastName");
                case 3 -> row.createCell(i).setCellValue("email");
                case 4 -> row.createCell(i).setCellValue("department");
                case 5 -> row.createCell(i).setCellValue("salary");
                case 6 -> row.createCell(i).setCellValue("dateOfJoining");
                case 7 -> row.createCell(i).setCellValue("active");
                case 8 -> row.createCell(i).setCellValue("createdAt");
                case 9 -> row.createCell(i).setCellValue("updatedAt");
            }
        }

        for (int r = 0; r < employeeRequestDtos.size(); r++) {
            XSSFRow currentRow = sheet.createRow(r+1);
            XSSFCellStyle style = workbook.createCellStyle();
            EmployeeRequestDto e = employeeRequestDtos.get(r);
            System.out.println(e.toString());
            for (int c = 0; c < 10; c++) {
                switch (c) {
                    case 0 -> {
                        XSSFCell cell = currentRow.createCell(0);
                        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getId());
                    }
                    case 1 -> {
                        XSSFCell cell = currentRow.createCell(1);
                        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getFirstName());
                    }
                    case 2 -> {
                        XSSFCell cell = currentRow.createCell(2);
                        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getLastName());
                    }
                    case 3 -> {
                        XSSFCell cell = currentRow.createCell(3);
                        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getEmail());
                    }
                    case 4 -> {

                        XSSFCell cell = currentRow.createCell(4);
                        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getDepartment());
                    }
                    case 5 -> {
                        XSSFCell cell = currentRow.createCell(5);
                        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getSalary().toString());
                    }
                    case 6 -> {
                        XSSFCell cell = currentRow.createCell(6);
                        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getDateOfJoining().format(DateTimeFormatter.ISO_DATE));
                    }
                    case 7 ->{
                        XSSFCell cell = currentRow.createCell(7);
                        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getActive().toString().toUpperCase());
                    }
                    case 8 -> {
                        XSSFCell cell = currentRow.createCell(8);
                        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getCreatedAt().format(DateTimeFormatter.ISO_DATE));
                    }
                    case 9 -> {
                        XSSFCell cell = currentRow.createCell(8);
                        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                        cell.setCellStyle(style);
                        cell.setCellValue(e.getUpdatedAt().format(DateTimeFormatter.ISO_DATE));
                    }
                }
            }
            sheet.autoSizeColumn(1);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        return outputStream.toByteArray();
    }

    private ImportResultDto saveExcelEntries(MultipartFile excepFile) throws Exception {
        ImportResultDto ird = new ImportResultDto();

        int successfulCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        try(XSSFWorkbook workbook = new XSSFWorkbook(excepFile.getInputStream())) {

            XSSFSheet sheet = workbook.getSheetAt(0);

            int rows = sheet.getLastRowNum(); // number of rows
            int cols = sheet.getRow(1).getLastCellNum(); // number of columns
            EmployeeRequestDto employeeRequestDto = null;

            for (int r = 1; r <= rows; r++) {
                // get row
                XSSFRow currentRow = sheet.getRow(r);
                employeeRequestDto = new EmployeeRequestDto();

                for (int c = 0; c < cols; c++) {
                    XSSFCell currentCell = currentRow.getCell(c);
                    try {
                        switch (currentCell.getCellType()) {
                            case STRING -> {
                                if (c == 0) employeeRequestDto.setFirstName(currentCell.getStringCellValue());
                                if (c == 1) employeeRequestDto.setLastName(currentCell.getStringCellValue());
                                if (c == 2) employeeRequestDto.setEmail(currentCell.getStringCellValue());
                                if (c == 3) employeeRequestDto.setDepartment(currentCell.getStringCellValue());
                            }
                            case NUMERIC -> {
                                if (c == 4)
                                    employeeRequestDto.setSalary(BigDecimal.valueOf(currentCell.getNumericCellValue()));
                                if (c == 5)
                                    employeeRequestDto.setDateOfJoining(currentCell.getLocalDateTimeCellValue().toLocalDate());
                            }
                            case BOOLEAN -> employeeRequestDto.setActive(currentCell.getBooleanCellValue());
                        }
                    } catch (Exception e) {
                        failureCount++;
                        errors.add(e.getMessage());
                    }
                }

                Employee employeeToSave = EmployeeMapper.toEmployee(employeeRequestDto);
                employeeRepository.save(employeeToSave);
                successfulCount++;
            }
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        ird.setSuccessfulCount(successfulCount);
        ird.setFailureCount(failureCount);
        ird.setErrors(errors);
        return ird;
    }
}
