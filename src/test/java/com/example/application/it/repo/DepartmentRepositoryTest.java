package com.example.application.it.repo;

import com.example.application.entity.Department;
import com.example.application.entity.Skill;
import com.example.application.entity.SkillGroup;
import com.example.application.it.testutils.CleanDbExtension;
import com.example.application.repo.DepartmentRepository;
import com.example.application.repo.SkillGroupRepository;
import com.example.application.repo.SkillRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CleanDbExtension.class)
@SpringBootTest
class DepartmentRepositoryTest {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private SkillRepo skillRepo;
    @Autowired
    private SkillGroupRepository skillGroupRepo;

    @Test
    void findAll() {
        List<Department> all = departmentRepository.findAll();
        assertThat(all).isNotNull().isEmpty();
    }

    @Test
    void save() {
        // Arrange
        Department department = new Department();
        department.setName("Some department");

        // Act
        departmentRepository.save(department);

        // Assert
        List<Department> all = departmentRepository.findAll();
        assertThat(all).isNotNull().hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("Some department");
    }

    @Test
    void saveWithSkillGroups() {
        // Arrange
        Skill skill = new Skill();
        skill.setName("Some skill");
        skillRepo.save(skill);

        SkillGroup skillGroup = new SkillGroup();
        skillGroup.setName("Some group");
        skillGroup.setDescription("Some description");
        skillGroup.setSkills(List.of(skill));
        skillGroupRepo.save(skillGroup);

        Department department = new Department();
        department.setName("Some department");
        department.setSkillGroups(List.of(skillGroup));

        // Act
        departmentRepository.save(department);

        // Assert
        List<Department> all = departmentRepository.findAll();
        assertThat(all).isNotNull().hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("Some department");
        assertThat(all.get(0).getSkillGroups()).hasSize(1);
        assertThat(all.get(0).getSkillGroups().get(0).getName()).isEqualTo("Some group");
        assertThat(all.get(0).getSkillGroups().get(0).getSkills()).hasSize(1);
        assertThat(all.get(0).getSkillGroups().get(0).getSkills().get(0).getName()).isEqualTo("Some skill");
    }

    @Test
    void findByName() {
        String departmentName = "Some department";

        // Assumptions
        assertThat(departmentRepository.findByName(departmentName)).isNull();

        // Arrange
        Department department = new Department();
        department.setName(departmentName);
        departmentRepository.save(department);

        // Act
        Department found = departmentRepository.findByName(departmentName);

        // Assert
        assertThat(found).isNotNull();
        assertThat(department.getName()).isEqualTo(departmentName);
    }
}