package com.energyict.mdc.rest.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class ComserverCoreApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;

    @Override
    protected Application getApplication() {
        MdcApplication application = new MdcApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setEngineConfigurationService(engineConfigurationService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setProtocolPluggableService(protocolPluggableService);
        return application;
    }
}
