package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MessageSeeds;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceLifecycleConfigApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    RestQueryService restQueryService;
    @Mock
    UserService userService;
    @Mock
    DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Override
    protected Application getApplication() {
        DeviceLifecycleConfigApplication application = new DeviceLifecycleConfigApplication();
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setUserService(userService);
        application.setNlsService(nlsService);
        application.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        when(nlsService.getThesaurus(DeviceLifecycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT, Layer.REST)).thenReturn(thesaurus);
        return application;
    }

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    // Common mocks for device lifecycle configuration
    public DeviceLifeCycle mockSimpleDeviceLifeCycle(Long id, String name){
        DeviceLifeCycle dlc = mock(DeviceLifeCycle.class);
        when(dlc.getId()).thenReturn(id);
        when(dlc.getName()).thenReturn(name);
        return  dlc;
    }
}
