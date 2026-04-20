package com.mastertbal.employeedetail.entity;

import lombok.Data;

import java.util.List;

@Data
public class ImportResultDto {
    int successfulCount;
    int failureCount;
    List<String> errors;
}
