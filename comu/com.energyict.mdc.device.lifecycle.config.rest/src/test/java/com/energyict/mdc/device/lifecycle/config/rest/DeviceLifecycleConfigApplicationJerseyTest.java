package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MessageSeeds;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.List;

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
    public DeviceLifeCycle mockSimpleDeviceLifeCycle(long id, String name){
        DeviceLifeCycle dlc = mock(DeviceLifeCycle.class);
        when(dlc.getId()).thenReturn(id);
        when(dlc.getName()).thenReturn(name);
        return  dlc;
    }

    public State mockSimpleState(long id, String name){
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        return state;
    }

    public List<State> mockDefaultStates(){
        List<State> states = new ArrayList<>(2);
        states.add(mockSimpleState(2, "Decommisioned"));
        states.add(mockSimpleState(1, "Commisioned"));
        states.add(mockSimpleState(3, "In stock"));
        return states;
    }
}
