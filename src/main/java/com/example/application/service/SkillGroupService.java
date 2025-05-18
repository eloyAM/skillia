package com.example.application.service;

import com.example.application.dto.SkillGroupDto;
import com.example.application.entity.SkillGroup;
import com.example.application.mapper.IDtoEntityMapper;
import com.example.application.repo.SkillGroupRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SkillGroupService {
    private final IDtoEntityMapper dtoEntityMapper;
    private final SkillGroupRepository skillGroupRepository;

    public SkillGroupService(
        IDtoEntityMapper dtoEntityMapper,
        SkillGroupRepository skillGroupRepository
    ) {
        this.dtoEntityMapper = dtoEntityMapper;
        this.skillGroupRepository = skillGroupRepository;
    }

    public Optional<SkillGroupDto> saveGroup(SkillGroupDto group) {
        SkillGroup entity = dtoEntityMapper.toEntity(group);
        try {
            var saved = skillGroupRepository.save(entity);
            return Optional.ofNullable(dtoEntityMapper.toDto(saved));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public void deleteGroupById(Long id) {
        skillGroupRepository.deleteById(id);
    }

    public List<SkillGroupDto> getAllGroups() {
        return skillGroupRepository.findAll()
            .stream()
            .map(dtoEntityMapper::toDto)
            .collect(Collectors.toList());
    }
}
