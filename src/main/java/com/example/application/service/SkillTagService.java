package com.example.application.service;

import com.example.application.dto.SkillTagDto;
import com.example.application.entity.SkillTag;
import com.example.application.mapper.DtoEntityMapping;
import com.example.application.repo.SkillTagRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SkillTagService {

    private final SkillTagRepository skillTagRepo;

    public SkillTagService(SkillTagRepository skillTagRepo) {
        this.skillTagRepo = skillTagRepo;
    }

    public Optional<SkillTagDto> saveSkillTag(SkillTagDto dto) {
        SkillTag entity = DtoEntityMapping.mapSkillTagDtoToSkillTagEntity(dto);
        try {
            SkillTag savedEntity = skillTagRepo.save(entity);
            return Optional.of(DtoEntityMapping.mapSkillTagEntityToSkillTagDto(savedEntity));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    public List<SkillTagDto> getAllSkillTag() {
        return skillTagRepo.findAll()
                .stream()
                .map(DtoEntityMapping::mapSkillTagEntityToSkillTagDto)
                .toList();
    }

    public List<SkillTagDto> getAllSkillTagInUse() {
        return skillTagRepo.findAllUsedOnSkillTagging()
            .stream()
            .map(DtoEntityMapping::mapSkillTagEntityToSkillTagDto)
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

    public void deleteSkillTagById(Long id) {
        skillTagRepo.deleteById(id);
    }
}
