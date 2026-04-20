package com.mastertbal.employeedetail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String department;

    private BigDecimal salary;

    private LocalDate dateOfJoining;

    private Boolean active;

    @Column(updatable = false)
    private LocalDate createdAt;

    private LocalDate updatedAt;


    @PrePersist
    public void beforePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = LocalDate.now();
    }

}
