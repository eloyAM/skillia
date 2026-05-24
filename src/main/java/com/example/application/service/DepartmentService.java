package com.example.application.service;

import com.example.application.dto.DepartmentDto;
import com.example.application.entity.Department;
import com.example.application.mapper.IDtoEntityMapper;
import com.example.application.repo.DepartmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final IDtoEntityMapper dtoEntityMapper;

    public DepartmentService(
        DepartmentRepository departmentRepository,
        IDtoEntityMapper dtoEntityMapper
    ) {
        this.departmentRepository = departmentRepository;
        this.dtoEntityMapper = dtoEntityMapper;
    }


    public List<DepartmentDto> findAllDepartment() {
        return departmentRepository.findAll().stream()
            .map(dtoEntityMapper::toDto).toList();
    }

    public Optional<DepartmentDto> saveDepartment(DepartmentDto departmentDto) {
        var entity = dtoEntityMapper.toEntity(departmentDto);
        try {
            var saved = departmentRepository.save(entity);
            return Optional.ofNullable(dtoEntityMapper.toDto(saved));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public DepartmentDto saveByNameIfDoesntExist(String name) {
        if (name == null || departmentRepository.existsByName(name))
            return null;
        var entity = new Department();
        entity.setName(name);
        var saved = departmentRepository.save(entity);
        return dtoEntityMapper.toDto(saved);
    }

    public Optional<DepartmentDto> findDepartmentById(Long id) {
        if (id == null)
            return Optional.empty();
        return departmentRepository.findById(id)
            .map(dtoEntityMapper::toDto);
    }

    public Optional<DepartmentDto> findDepartmentByName(String name) {
        if (name == null)
            return Optional.empty();
        return Optional.ofNullable(departmentRepository.findByName(name))
            .map(dtoEntityMapper::toDto);
    }
}
