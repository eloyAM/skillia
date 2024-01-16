package com.example.application.service;

import com.example.application.dto.*;
import com.example.application.entity.PersonSkill;
import com.example.application.entity.PersonSkillId;
import com.example.application.mapper.DtoEntityMapping;
import com.example.application.repo.PersonSkillRepo;
import com.example.application.utils.FunctionalUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonSkillService {
    private final PersonSkillRepo personSkillRepo;

    public PersonSkillService(PersonSkillRepo personSkillRepo) {
        this.personSkillRepo = personSkillRepo;
    }

    @Nullable
    public PersonSkillBasicDto savePersonSkill(PersonSkillBasicDto dto) {
        return Optional.of(dto)
            .map(DtoEntityMapping::mapPersonSkillDtoToPersonSkillEntity)
            .map(personSkillRepo::save)
            .map(DtoEntityMapping::mapPersonSkillEntityToPersonSkillDto)
            .orElse(null);
    }

    public List<PersonSkillBasicDto> savePersonSkill(Iterable<PersonSkillBasicDto> dtoIterable) {
        Iterable<PersonSkill> entitiesFromDtos = FunctionalUtils.streamToIterable(
            FunctionalUtils.iterableToStream(dtoIterable)
                .map(DtoEntityMapping::mapPersonSkillDtoToPersonSkillEntity)
        );
        return personSkillRepo.saveAll(entitiesFromDtos).stream()
            .map(DtoEntityMapping::mapPersonSkillEntityToPersonSkillDto)
            .toList();
    }

    public List<PersonSkillBasicDto> getAllPersonSkillBasic() {
        return personSkillRepo.findAllBy();
    }

    public List<PersonWithSkillsDto> getAllPersonSkill() {
        List<PersonSkill> personSkillOriginal = personSkillRepo.findAll();
        return DtoEntityMapping.mapPersonSkillEntityToPersonWithSkillsDto(personSkillOriginal);
    }

    public List<PersonWithLevelDto> findAllPersonWithLevelBySkillId(Long skillId) {
        return personSkillRepo.findAllPersonWithLevelBySkillId(skillId);
    }

    public List<AcquiredSkillDto> findAllAcquiredSkillByPersonId(String personId) {
        return personSkillRepo.findAllAcquiredSkillByPersonId(personId);
    }


    // Static utilities

    @NonNull
    public static String getLevelName(@Nullable Integer level) {
        if (level == null) {
            return "Unknown";
        }
        return switch (level) {
            case 1 -> "Novice";
            case 2 -> "Beginner";
            case 3 -> "Intermediate";
            case 4 -> "Advanced";
            case 5 -> "Expert";
            default -> "Unknown";
        };
    }

    @NonNull
    public static List<Integer> getLevels() {
        return List.of(1, 2, 3, 4, 5);
    }

    public void deletePersonSkillById(PersonSkillIdDto idDto) {
        PersonSkillId entityId = DtoEntityMapping.mapPersonSkillIdDtoToPersonSkillIdEntity(idDto);
        personSkillRepo.deleteById(entityId);
    }
}
