package com.example.application.persistence.repo;

import com.example.application.dto.main.AcquiredSkillDto;
import com.example.application.dto.skillperson.PersonWithLevelDto;
import com.example.application.dto.stats.SkillStatValue;
import com.example.application.persistence.entity.PersonSkill;
import com.example.application.persistence.entity.PersonSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonSkillRepo extends JpaRepository<PersonSkill, PersonSkillId> {
    @Query("select new com.example.application.dto.main.AcquiredSkillDto("
        + "p.personSkillId.skillId, p.skill.name, p.level)"
        + " from PersonSkill p where p.personSkillId.personId = :personId")
    List<AcquiredSkillDto> findAllAcquiredSkillByPersonId(String personId);

    @Query("select new com.example.application.dto.skillperson.PersonWithLevelDto(p.personSkillId.personId"
        + ", p.person.fullName, p.person.email, p.person.title, p.person.department"
        + ", p.level)"
        + " from PersonSkill p where p.personSkillId.skillId = :skillId")
    List<PersonWithLevelDto> findAllPersonWithLevelBySkillId(Long skillId);

    Optional<PersonSkill> findByPersonSkillId_PersonIdAndPersonSkillId_SkillId(String personId, Long skillId);

    @Query("""
        select new com.example.application.dto.stats.SkillStatValue(
            p.personSkillId.skillId,
            new com.example.application.dto.stats.StatValue(count(*), min(p.level), max(p.level), avg(p.level))
        )
        from PersonSkill p group by p.personSkillId.skillId""")
    List<SkillStatValue> calculateAllStatValue();

    @Query("""
        select ps from PersonSkill ps
        where ps.person.department = :departmentName""")
    List<PersonSkill> findPersonWithSkillsByDepartment(String departmentName);
}
