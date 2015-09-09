package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class PluggableRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    CodeFactory codeFactory;
    @Mock
    UserFileFactory userFileFactory;
    @Mock
    FirmwareService firmwareService;

    @Override
    protected Application getApplication() {
        MdcPluggableRestApplication application = new MdcPluggableRestApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setPropertySpecService(propertySpecService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setCodeFactory(codeFactory);
        application.setUserFileFactory(userFileFactory);
        application.setFirmwareService(firmwareService);
        return application;
    }
}
