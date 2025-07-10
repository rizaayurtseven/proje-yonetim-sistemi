package com.stajprojesi.backend.repository;

import com.stajprojesi.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
} 