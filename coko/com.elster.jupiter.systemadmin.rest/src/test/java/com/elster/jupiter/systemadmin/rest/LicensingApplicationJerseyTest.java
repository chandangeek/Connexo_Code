package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.systemadmin.rest.imp.LicensingApplication;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class LicensingApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    RestQueryService restQueryService;
    @Mock
    UserService userService;
    @Mock
    LicenseService licenseService;
    @Mock
    LifeCycleService lifeCycleService;
    @Mock
    TaskService taskService;
    @Mock
    JsonService jsonService;

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(thesaurus.getStringBeyondComponent(anyString(), anyString())).thenAnswer(invocationOnMock -> (String) invocationOnMock.getArguments()[1]);
    }

    @Override
    protected Application getApplication() {
        LicensingApplication app = new LicensingApplication();
        app.setTransactionService(transactionService);
        app.setRestQueryService(restQueryService);
        app.setUserService(userService);
        app.setLicenseService(licenseService);
        app.setNlsService(nlsService);
        app.setLifeCycleService(lifeCycleService);
        app.setTaskService(taskService);
        app.setJsonService(jsonService);
        return app;
    }
}
