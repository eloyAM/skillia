package io.skillia.service;

import io.skillia.dto.PersonSkillBasicDto;
import io.skillia.dto.PersonSkillIdDto;
import io.skillia.dto.main.AcquiredSkillDto;
import io.skillia.dto.main.PersonWithSkillsDto;
import io.skillia.dto.skillperson.PersonWithLevelDto;
import io.skillia.dto.stats.SkillStatValue;
import io.skillia.dto.stats.StatValue;
import io.skillia.mapper.IDtoEntityMapper;
import io.skillia.persistence.entity.PersonSkill;
import io.skillia.persistence.entity.PersonSkillId;
import io.skillia.persistence.repo.PersonSkillRepo;
import io.skillia.service.utils.FunctionalUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonSkillService {
    private final PersonSkillRepo personSkillRepo;
    private final IDtoEntityMapper dtoEntityMapper;

    public PersonSkillService(PersonSkillRepo personSkillRepo, IDtoEntityMapper dtoEntityMapper) {
        this.personSkillRepo = personSkillRepo;
        this.dtoEntityMapper = dtoEntityMapper;
    }

    @Nullable
    public PersonSkillBasicDto savePersonSkill(PersonSkillBasicDto dto) {
        try {
            return Optional.of(dto)
                .map(dtoEntityMapper::toPersonSkill)
                // NOTE: not using method reference to avoid ArchUnit misunderstanding
                .map(e -> personSkillRepo.save(e))
                .map(dtoEntityMapper::toPersonSkillBasicDto)
                .orElse(null);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<PersonSkillBasicDto> savePersonSkill(Iterable<PersonSkillBasicDto> dtoIterable) {
        Iterable<PersonSkill> entitiesFromDtos = FunctionalUtils.streamToIterable(
            FunctionalUtils.iterableToStream(dtoIterable)
                .map(dtoEntityMapper::toPersonSkill)
        );
        try {
            return personSkillRepo.saveAll(entitiesFromDtos).stream()
                .map(dtoEntityMapper::toPersonSkillBasicDto)
                .toList();
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<PersonWithSkillsDto> getAllPersonSkill() {
        List<PersonSkill> personSkillOriginal = personSkillRepo.findAll();
        return dtoEntityMapper.toListPersonWithSkillsDto(personSkillOriginal);
    }

    public List<PersonWithSkillsDto> getAllPersonSkillForDepartment(String departmentName) {
        List<PersonSkill> personSkillOriginal = personSkillRepo.findPersonWithSkillsByDepartment(departmentName);
        return dtoEntityMapper.toListPersonWithSkillsDto(personSkillOriginal);
    }

    public List<PersonWithLevelDto> findAllPersonWithLevelBySkillId(Long skillId) {
        return personSkillRepo.findAllPersonWithLevelBySkillId(skillId);
    }

    public List<AcquiredSkillDto> findAllAcquiredSkillByPersonId(String personId) {
        return personSkillRepo.findAllAcquiredSkillByPersonId(personId);
    }

    public Optional<AcquiredSkillDto> findPersonSkillBasicByPersonIdAndSkillId(PersonSkillIdDto personSkillIdD) {
        return personSkillRepo.findByPersonSkillId_PersonIdAndPersonSkillId_SkillId(
                        personSkillIdD.getPersonId(), personSkillIdD.getSkillId()
                )
                .map(dtoEntityMapper::toAcquiredSkillDto);
    }

    // Stats data by skill id
    public Map<Long, StatValue> getAllStats() {
        var listStats = personSkillRepo.calculateAllStatValue();
        return listStats.stream().collect(Collectors.toMap(
            SkillStatValue::getSkillId, SkillStatValue::getStatValue)
        );
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
        PersonSkillId entityId = dtoEntityMapper.toPersonSkillId(idDto);
        personSkillRepo.deleteById(entityId);
    }
}
