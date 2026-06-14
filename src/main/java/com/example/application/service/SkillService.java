package com.example.application.service;

import com.example.application.dto.main.SkillDto;
import com.example.application.dto.main.SkillGroupDto;
import com.example.application.dto.main.SkillTagDto;
import com.example.application.entity.Skill;
import com.example.application.entity.SkillGroup;
import com.example.application.entity.SkillTag;
import com.example.application.mapper.IDtoEntityMapper;
import com.example.application.repo.SkillGroupRepository;
import com.example.application.repo.SkillRepo;
import com.example.application.repo.SkillTagRepository;
import com.example.application.service.utils.FunctionalUtils;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SkillService {
    private final IDtoEntityMapper dtoEntityMapper;

    private final SkillTagRepository skillTagRepo;
    private final SkillGroupRepository skillGroupRepository;
    private final SkillRepo skillRepo;

    public SkillService(
        SkillRepo skillRepo,
        SkillGroupRepository skillGroupRepository,
        SkillTagRepository skillTagRepo,
        IDtoEntityMapper dtoEntityMapper
    ) {
        this.skillRepo = skillRepo;
        this.skillGroupRepository = skillGroupRepository;
        this.skillTagRepo = skillTagRepo;
        this.dtoEntityMapper = dtoEntityMapper;
    }

    public Optional<SkillDto> saveSkill(SkillDto skill) {
        Skill skillEntity = dtoEntityMapper.toSkill(skill);
        try {
            Skill savedSkill = skillRepo.save(skillEntity);
            return Optional.of(savedSkill).map(dtoEntityMapper::toSkillDto);
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public List<SkillDto> saveSkill(Iterable<SkillDto> dtoS) {
        Iterable<Skill> entities = FunctionalUtils.streamToIterable(
            FunctionalUtils.iterableToStream(dtoS)
                .map(dtoEntityMapper::toSkill));
        final List<Skill> savedEntities;
        try {
            savedEntities = skillRepo.saveAll(entities);
        } catch (DataIntegrityViolationException e) {
            return Collections.emptyList();
        }
        return savedEntities.stream()
            .map(dtoEntityMapper::toSkillDto)
            .toList();
    }

    public List<SkillDto> getAllSkill() {
        return skillRepo.findAll().stream()
            .map(dtoEntityMapper::toSkillDto)
            .toList();
    }

    public Optional<SkillDto> updateSkill(Long id, String skillName) {  // TODO rename to express that this only updates the name, or modify to update tags as well
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

    @Transactional
    public void deleteSkillById(Long id) {
        // Each delete silently fails if there's no entity with the given id
        skillRepo.deleteSkillGroupSkillsBySkillId(id);
        skillRepo.deleteById(id);
    }

    public Optional<SkillDto> getSkillById(Long id) {
        return skillRepo.findById(id)
            .map(dtoEntityMapper::toSkillDto);
    }

    public Optional<SkillGroupDto> saveGroup(SkillGroupDto group) {
        SkillGroup entity = dtoEntityMapper.toEntity(group);
        try {
            var saved = skillGroupRepository.save(entity);
            return Optional.of(saved).map(dtoEntityMapper::toDto);
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteGroupById(Long id) {
        skillGroupRepository.deleteDepartmentSkillGroupByGroupId(id);
        skillGroupRepository.deleteById(id);
    }

    public List<SkillGroupDto> getAllGroups() {
        return skillGroupRepository.findAll()
            .stream()
            .map(dtoEntityMapper::toDto)
            .toList();
    }

    public Optional<SkillTagDto> saveSkillTag(SkillTagDto dto) {
        SkillTag entity = dtoEntityMapper.toSkillTag(dto);
        try {
            SkillTag savedEntity = skillTagRepo.save(entity);
            return Optional.of(savedEntity).map(dtoEntityMapper::toSkillTagDto);
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public List<SkillTagDto> getAllSkillTag() {
        return skillTagRepo.findAll()
            .stream()
            .map(dtoEntityMapper::toSkillTagDto)
            .toList();
    }

    public List<SkillTagDto> getAllSkillTagInUse() {
        return skillTagRepo.findAllUsedOnSkillTagging()
            .stream()
            .map(dtoEntityMapper::toSkillTagDto)
            .toList();
    }

    public Optional<SkillTagDto> updateSkillTag(String newName, Long id) {
        try {
            int rowsUpdated = skillTagRepo.updateNameById(newName, id);
            if (rowsUpdated == 0)
                return Optional.empty();
            return Optional.of(new SkillTagDto(id, newName));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteSkillTagById(Long id) {
        skillTagRepo.deleteSkillTaggingByTagId(id);
        skillTagRepo.deleteById(id);
    }

    public Optional<SkillTagDto> getSkillTagById(Long id) {
        return skillTagRepo.findById(id).map(dtoEntityMapper::toSkillTagDto);
    }

    public Optional<SkillGroupDto> getGroupById(Long id) {
        return skillGroupRepository.findById(id).map(dtoEntityMapper::toDto);
    }
}
