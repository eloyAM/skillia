package com.example.application.repo;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.dto.PersonWithLevelDto;
import com.example.application.entity.PersonSkill;
import com.example.application.entity.PersonSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonSkillRepo extends JpaRepository<PersonSkill, PersonSkillId> {
    /*
    // SKILL and PERSON can be related by PERSON_SKILL this way:
    SELECT
       s.name,
       ps.skill_id,
       ps.person_id,
       ps.level,
    FROM PERSON_SKILL ps
    LEFT JOIN SKILL s ON s.id=ps.skill_id
    WHERE ps.person_id = 'eloy.abellan'
     */
    // This fills the person and the skill objects
    List<PersonSkill> findPersonSkillByPersonSkillIdPersonId(String personSkillId);

    @Query("select new com.example.application.dto.PersonSkillBasicDto("
        + "p.personSkillId.personId, p.personSkillId.skillId, p.level)"
        + " from PersonSkill p")
    List<PersonSkillBasicDto> findAllBy();

    @Query("select new com.example.application.dto.AcquiredSkillDto("
        + "p.personSkillId.skillId, p.skill.name, p.level)"
        + " from PersonSkill p where p.personSkillId.personId = :personId")
    List<AcquiredSkillDto> findAllAcquiredSkillByPersonId(String personId);

    @Query("select new com.example.application.dto.PersonWithLevelDto(p.personSkillId.personId"
        + ", p.person.displayName, p.person.email, p.person.title, p.person.department"
        + ", p.level)"
        + " from PersonSkill p where p.personSkillId.skillId = :skillId")
    List<PersonWithLevelDto> findAllPersonWithLevelBySkillId(Long skillId);

}
