package com.example.application.controller;

import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.dto.PersonWithLevelDto;
import com.example.application.dto.SkillDto;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.example.application.service.SkillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO remove or convert into real restful controller
@RestController
@RequestMapping("/dbController")
public class DbController {
    private final PersonService personService;
    private final SkillService skillService;
    private final PersonSkillService personSkillService;

    public DbController(
        PersonService personService,
        SkillService skillService,
        PersonSkillService personSkillService
    ) {
        this.personService = personService;
        this.skillService = skillService;
        this.personSkillService = personSkillService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/void")
    public void doNothing() {
    }

    @GetMapping("/person")
    public List<PersonDto> getAllPerson() {
        return personService.findAllPerson();
    }

    @GetMapping("/skill")
    public List<SkillDto> getAllSkill() {
        return skillService.getAllSkill();
    }

    @GetMapping("/personSkill")
    public List<PersonSkillBasicDto> getAllPersonSkill() {
        return personSkillService.getAllPersonSkillBasic();
    }

    @GetMapping("/personWithLevel/{skillId}")
    public List<PersonWithLevelDto> getAllPersonAndLevelWithGivenSkillName(
        @PathVariable Long skillId) {
        return personSkillService.findAllPersonWithLevelBySkillId(skillId);
    }

}
