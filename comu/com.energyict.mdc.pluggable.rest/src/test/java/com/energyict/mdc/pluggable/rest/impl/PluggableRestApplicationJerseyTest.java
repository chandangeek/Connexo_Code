package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.util.exception.MessageSeed;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class PluggableRestApplicationJerseyTest extends com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest {
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    ProtocolPluggableService protocolPluggableService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        MdcPluggableRestApplication application = new MdcPluggableRestApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setPropertySpecService(propertySpecService);
        application.setProtocolPluggableService(protocolPluggableService);
        return application;
    }
}
