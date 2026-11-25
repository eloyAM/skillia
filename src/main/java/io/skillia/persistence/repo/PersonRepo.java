package io.skillia.persistence.repo;

import io.skillia.dto.main.PersonDto;
import io.skillia.persistence.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepo extends JpaRepository<Person, String> {
    @Query("""
        select new io.skillia.dto.main.PersonDto(
            p.username, p.fullName, p.email, p.title, p.department)
        from Person p""")
    List<PersonDto> findAllPerson();

    @Query("""
        select new io.skillia.dto.main.PersonDto(
            p.username, p.fullName, p.email, p.title, p.department)
        from Person p
        where p.department = :departmentName""")
    List<PersonDto> findAllByDepartment(String departmentName);

    @Query("""
        select distinct p.department
        from Person p order by p.department asc""")
    List<String> findDistinctDepartments();
}
