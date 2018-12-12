/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.systemadmin.rest.imp.SystemApplication;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.framework.BundleContext;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SystemApplicationJerseyTest extends FelixRestApplicationJerseyTest {
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
    @Mock
    BundleContext bundleContext;
    @Mock
    SubsystemService subsystemService;

    @Override
    public void setupMocks() {
        super.setupMocks();
        this.setupThesaurus();
    }

    protected void setupThesaurus() {
        NlsMessageFormat notSupported = mock(NlsMessageFormat.class);
        when(notSupported.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        doReturn(notSupported).when(thesaurus).getFormat(any(MessageSeed.class));
        doReturn(notSupported).when(thesaurus).getFormat(any(TranslationKey.class));
    }

    @Override
    protected Application getApplication() {
        SystemApplication app = new SystemApplication();
        app.setTransactionService(transactionService);
        app.setRestQueryService(restQueryService);
        app.setUserService(userService);
        app.setLicenseService(licenseService);
        app.setNlsService(nlsService);
        app.setLifeCycleService(lifeCycleService);
        app.setTaskService(taskService);
        app.setJsonService(jsonService);
        app.setSubsystemService(subsystemService);
        app.setClock(Clock.systemDefaultZone());
        app.activate(bundleContext);
        return app;
    }
}
