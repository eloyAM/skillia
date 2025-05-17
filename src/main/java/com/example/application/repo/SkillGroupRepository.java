package com.example.application.repo;

import com.example.application.entity.SkillGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillGroupRepository extends JpaRepository<SkillGroup, Long> {
}