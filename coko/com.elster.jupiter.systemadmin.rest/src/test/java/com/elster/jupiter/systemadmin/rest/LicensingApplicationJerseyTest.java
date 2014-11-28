package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.systemadmin.rest.resource.MessageSeeds;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

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

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
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
        return app;
    }
}
