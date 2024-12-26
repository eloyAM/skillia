package com.example.application.service;

import com.example.application.dto.SkillDto;
import com.example.application.entity.Skill;
import com.example.application.mapper.DtoEntityMapping;
import com.example.application.repo.SkillRepo;
import com.example.application.utils.FunctionalUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SkillService {
    private final SkillRepo skillRepo;

    public SkillService(SkillRepo personRepo) {
        this.skillRepo = personRepo;
    }

    public Optional<SkillDto> saveSkill(SkillDto person) {
        Skill skillEntity = DtoEntityMapping.mapSkillDtoToSkillEntity(person);
        try {
            Skill savedSkill = skillRepo.save(skillEntity);
            return Optional.ofNullable(DtoEntityMapping.mapSkillEntityToSkillDto(savedSkill));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public List<SkillDto> saveSkill(Iterable<SkillDto> dtoS) {
        Iterable<Skill> entities = FunctionalUtils.streamToIterable(
            FunctionalUtils.iterableToStream(dtoS)
                .map(DtoEntityMapping::mapSkillDtoToSkillEntity));
        final List<Skill> savedEntities;
        try {
            savedEntities = skillRepo.saveAll(entities);
        } catch (DataIntegrityViolationException e) {
            return Collections.emptyList();
        }
        return savedEntities.stream()
            .map(DtoEntityMapping::mapSkillEntityToSkillDto)
            .toList();
    }

    public List<SkillDto> getAllSkill() {
        return skillRepo.findAll().stream()
            .map(DtoEntityMapping::mapSkillEntityToSkillDto)
            .toList();
    }

    public Optional<SkillDto> updateSkill(Long id, String skillName) {
        try {
            int rowsUpdated = skillRepo.updateNameById(id, skillName);
            if (rowsUpdated == 0) {
                return Optional.empty();
            }
            return getSkillById(id);
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public void deleteSkillById(Long id) {
        // This silently fails if there's no entity with the given id
        skillRepo.deleteById(id);
    }

    public Optional<SkillDto> getSkillById(Long id) {
        return skillRepo.findById(id)
                .map(DtoEntityMapping::mapSkillEntityToSkillDto);
    }
}
