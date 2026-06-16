package com.example.application.persistence.repo;

import com.example.application.persistence.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);

    boolean existsByName(String name);
}