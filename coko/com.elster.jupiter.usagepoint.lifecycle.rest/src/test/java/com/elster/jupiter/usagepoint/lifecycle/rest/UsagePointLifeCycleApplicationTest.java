package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.rest.impl.UsagePointLifeCycleApplication;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class UsagePointLifeCycleApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected PropertyValueInfoService propertyValueInfoService;
    @Mock
    protected UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    @Mock
    protected UsagePointLifeCycleService usagePointLifeCycleService;
    @Mock
    protected FiniteStateMachineService finiteStateMachineService;

    @Override
    public void setupMocks() {
        super.setupMocks();
    }

    @Override
    protected Application getApplication() {
        UsagePointLifeCycleApplication app = new UsagePointLifeCycleApplication();
        app.setNlsService(this.nlsService);
        app.setPropertyValueInfoService(this.propertyValueInfoService);
        app.setUsagePointLifeCycleConfigurationService(this.usagePointLifeCycleConfigurationService);
        app.setUsagePointLifeCycleService(this.usagePointLifeCycleService);
        app.setFiniteStateMachineService(this.finiteStateMachineService);
        return app;
    }
}
