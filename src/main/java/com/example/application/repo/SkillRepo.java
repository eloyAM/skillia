package com.example.application.repo;

import com.example.application.dto.SkillDto;
import com.example.application.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SkillRepo extends JpaRepository<Skill, Long> {

    @Query("select new com.example.application.dto.SkillDto(s.id, s.name)"
        + " from Skill s where s.name = :name")
    SkillDto findByName(String name);

    @Query("select new com.example.application.dto.SkillDto(s.id, s.name) from Skill s")
    List<SkillDto> findAllBy();

    @Transactional
    @Modifying
    @Query("update Skill s set s.name = :name where s.id = :id")
    int updateNameById(Long id, String name);

}
