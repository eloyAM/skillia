package com.example.application.persistence.repo;

import com.example.application.persistence.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SkillRepo extends JpaRepository<Skill, Long> {

    @Transactional
    @Modifying
    @Query("update Skill s set s.name = :name where s.id = :id")
    int updateNameById(Long id, String name);

    // Delete from join table due to FK constraint
    @Modifying
    @Query(value = "DELETE FROM skill_group_skills WHERE skill_id = :skillId", nativeQuery = true)
    Integer deleteSkillGroupSkillsBySkillId(Long skillId);
}
