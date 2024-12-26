package com.example.application.repo;

import com.example.application.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SkillRepo extends JpaRepository<Skill, Long> {

    @Transactional
    @Modifying
    @Query("update Skill s set s.name = :name where s.id = :id")
    int updateNameById(Long id, String name);

}
