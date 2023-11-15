package com.example.application.apploads.db;

import com.example.application.data.service.CrmService;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AppLoadsTestBase {

    protected CrmService crmService;

    public AppLoadsTestBase(CrmService crmService) {
        this.crmService = crmService;
    }

    public void contextLoads() {
        // assert db initialized and the data can be loaded from the service
        assertThat(crmService.findAllStatuses()).isNotEmpty();
        assertThat(crmService.findAllCompanies()).isNotEmpty();
        assertThat(crmService.findAllContacts(null)).isNotEmpty();
    }
}
