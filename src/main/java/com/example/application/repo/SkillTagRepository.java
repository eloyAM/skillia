package com.example.application.repo;

import com.example.application.entity.SkillTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SkillTagRepository extends JpaRepository<SkillTag, Long> {
    @Transactional
    @Modifying
    @Query("update SkillTag s set s.name = ?1 where s.id = ?2")
    int updateNameById(String name, Long id);

    @Query("select distinct skill.tags from Skill skill")
    List<SkillTag> findAllUsedOnSkillTagging();

    // Delete from join table due to FK constraint
    @Modifying
    @Query(value = "delete from skill_tagging where tag_id = ?1", nativeQuery = true)
    void deleteSkillTaggingByTagId(Long tagId);
}