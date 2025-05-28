package com.example.application.service;

import com.example.application.dto.DepartmentDto;
import com.example.application.mapper.IDtoEntityMapper;
import com.example.application.repo.DepartmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final IDtoEntityMapper iDtoEntityMapper;

    public DepartmentService(
        DepartmentRepository departmentRepository,
        IDtoEntityMapper iDtoEntityMapper
    ) {
        this.departmentRepository = departmentRepository;
        this.iDtoEntityMapper = iDtoEntityMapper;
    }


    public List<DepartmentDto> findAllDepartment() {
        return departmentRepository.findAll().stream()
            .map(iDtoEntityMapper::toDto).toList();
    }

    public Optional<DepartmentDto> saveDepartment(DepartmentDto departmentDto) {
        var entity = iDtoEntityMapper.toEntity(departmentDto);
        try {
            var saved = departmentRepository.save(entity);
            return Optional.ofNullable(iDtoEntityMapper.toDto(saved));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public Optional<DepartmentDto> findDepartmentById(Long id) {
        if (id == null)
            return Optional.empty();
        return departmentRepository.findById(id)
            .map(iDtoEntityMapper::toDto);
    }

    public Optional<DepartmentDto> findDepartmentByName(String name) {
        if (name == null)
            return Optional.empty();
        return Optional.ofNullable(departmentRepository.findByName(name))
            .map(iDtoEntityMapper::toDto);
    }
}
