package io.skillia.persistence.repo;

import io.skillia.persistence.entity.SkillGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SkillGroupRepository extends JpaRepository<SkillGroup, Long> {
    // Delete from join table due to FK constraint
    @Modifying
    @Query(value = "DELETE FROM department_skill_groups WHERE group_id = :groupId", nativeQuery = true)
    void deleteDepartmentSkillGroupByGroupId(Long groupId);
}