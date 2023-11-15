package com.example.application.data.controller;

import com.example.application.data.entity.Company;
import com.example.application.data.entity.Contact;
import com.example.application.data.entity.Status;
import com.example.application.data.service.CrmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SuperController {

    private final CrmService crmService;

    public SuperController(CrmService crmService) {
        this.crmService = crmService;
    }

    @GetMapping("/status")
    public List<Status> getStatusAll() {
        return crmService.findAllStatuses();
    }

    @GetMapping("/company")
    public List<Company> getCompanyAll() {
        return crmService.findAllCompanies();
    }

    @GetMapping("/contact")
    public List<Contact> getContactAll() {
        return crmService.findAllContacts(null);
    }
}
