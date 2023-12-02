package com.example.application.repo;

import com.example.application.dto.PersonDto;
import com.example.application.entity.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepo extends JpaRepository<Person, String> {
    @Query("select new com.example.application.dto.PersonDto("
        + "p.username, p.fullName, p.email, p.title, p.department)"
        + " from Person p")
    List<PersonDto> findBy();

    @Query("select new com.example.application.dto.PersonDto("
        + "p.username, p.fullName, p.email, p.title, p.department)"
        + " from Person p")
    List<PersonDto> findBy(Pageable pageable);
}
