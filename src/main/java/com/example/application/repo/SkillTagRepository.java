package com.example.application.repo;

import com.example.application.entity.SkillTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SkillTagRepository extends JpaRepository<SkillTag, Long> {
    @Transactional
    @Modifying
    @Query("update SkillTag s set s.name = ?1 where s.id = ?2")
    int updateNameById(String name, Long id);
}