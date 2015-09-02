package com.energyict.mdc.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.rest.impl.comserver.MessageSeeds;
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
    protected MessageSeed[] getMessageSeeds() {
        return new MessageSeed[0];
    }

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
