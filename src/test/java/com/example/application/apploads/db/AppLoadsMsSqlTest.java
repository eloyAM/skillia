package com.example.application.apploads.db;

import com.example.application.data.service.CrmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("mssql")
@SpringBootTest
public class AppLoadsMsSqlTest extends AppLoadsTestBase {

    @Autowired
    public AppLoadsMsSqlTest(CrmService crmService) {
        super(crmService);
    }

    @Test
    public void contextLoads() {
        super.contextLoads();
    }
}
