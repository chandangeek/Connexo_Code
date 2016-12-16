package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.impl.UsagePointLifeCycleApplication;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointLifeCycleApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected PropertyValueInfoService propertyValueInfoService;
    @Mock
    protected UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    @Mock
    protected UsagePointLifeCycleService usagePointLifeCycleService;
    @Mock
    protected FiniteStateMachineService finiteStateMachineService;
    @Mock
    protected MeteringService meteringService;
    @Mock
    protected UsagePointMicroActionFactory usagePointMicroActionFactory;

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
        app.setMeteringService(this.meteringService);
        app.setUsagePointMicroActionFactory(this.usagePointMicroActionFactory);
        return app;
    }

    protected StateChangeBusinessProcess mockProcess(long id, String name, String deploymentId, String processId) {
        StateChangeBusinessProcess process = mock(StateChangeBusinessProcess.class);
        when(process.getId()).thenReturn(id);
        when(process.getName()).thenReturn(name);
        when(process.getDeploymentId()).thenReturn(deploymentId);
        when(process.getProcessId()).thenReturn(processId);
        return process;
    }

    protected ProcessReference mockProcessReference(long id, String name, String deploymentId, String processId) {
        ProcessReference reference = mock(ProcessReference.class);
        StateChangeBusinessProcess process = mockProcess(id, name, deploymentId, processId);
        when(reference.getStateChangeBusinessProcess()).thenReturn(process);
        return reference;
    }

}
