package com.example.application.service;

import com.example.application.dto.SkillStatValue;
import com.example.application.dto.StatValue;
import com.example.application.repo.PersonSkillRepo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final PersonSkillRepo personSkillRepo;

    public StatsService(PersonSkillRepo personSkillRepo) {
        this.personSkillRepo = personSkillRepo;
    }

    public Map<Long, StatValue> getAllStats() {
        var listStats = personSkillRepo.calculateAllStatValue();
        return listStats.stream().collect(Collectors.toMap(
            SkillStatValue::getSkillId, SkillStatValue::getStatValue)
        );
    }

}
