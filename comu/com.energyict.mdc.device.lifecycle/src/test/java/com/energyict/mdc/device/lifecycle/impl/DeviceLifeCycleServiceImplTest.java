package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionNotPartOfDeviceLifeCycleException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceLifeCycleServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (08:51)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceLifeCycleServiceImplTest {

    public static final long DEVICE_LIFE_CYCLE_ID = 1L;

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerMicroCheckFactory microCheckFactory;
    @Mock
    private ServerMicroActionFactory microActionFactory;
    @Mock
    private DeviceLifeCycle lifeCycle;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;
    @Mock
    private AuthorizedTransitionAction action;

    @Before
    public void initializeMocks() {
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        when(this.lifeCycle.getId()).thenReturn(DEVICE_LIFE_CYCLE_ID);
        when(this.deviceType.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
    }

    @Test
    public void getComponentNameDoesNotReturnNull() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();

        // Business method
        String componentName = service.getComponentName();

        // Asserts
        assertThat(componentName).isNotNull();
    }

    @Test
    public void getLayerDoesNotReturnNull() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();

        // Business method
        Layer layer = service.getLayer();

        // Asserts
        assertThat(layer).isNotNull();
    }

    @Test
    public void geTranslationKeysDoesNotReturnNull() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();

        // Business method
        List<TranslationKey> translationKeys = service.getKeys();

        // Asserts
        assertThat(translationKeys).isNotNull();
    }

    @Test(expected = ActionNotPartOfDeviceLifeCycleException.class)
    public void executeActionThatIsNotPartOfTheDeviceLifeCycle() throws DeviceLifeCycleActionViolationException {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        DeviceLifeCycle otherLifeCycle = mock(DeviceLifeCycle.class);
        when(otherLifeCycle.getId()).thenReturn(DEVICE_LIFE_CYCLE_ID + 1);
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getDeviceLifeCycle()).thenReturn(otherLifeCycle);
        StateTransitionEventType eventType = mock(StateTransitionEventType.class);
        when(eventType.getSymbol()).thenReturn("executeActionThatIsNotPartOfTheDeviceLifeCycle");
        StateTransition stateTransition = mock(StateTransition.class);
        when(stateTransition.getEventType()).thenReturn(eventType);
        when(action.getStateTransition()).thenReturn(stateTransition);

        // Business method
        service.execute(action, this.device);

        // Asserts: see expected exception rule
    }

    @Test
    public void executeCallsFactoryForEveryCheck() throws DeviceLifeCycleActionViolationException {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));

        // Business method
        service.execute(this.action, this.device);

        // Asserts
        for (MicroCheck microCheck : MicroCheck.values()) {
            verify(this.microCheckFactory).from(microCheck);
        }
    }

    @Test
    public void executeCallsFactoryForEveryAction() throws DeviceLifeCycleActionViolationException {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));

        // Business method
        service.execute(this.action, this.device);

        // Asserts
        for (MicroAction microAction : MicroAction.values()) {
            verify(this.microActionFactory).from(microAction);
        }
    }

    private DeviceLifeCycleServiceImpl getTestInstance() {
        return new DeviceLifeCycleServiceImpl(this.nlsService, this.microCheckFactory, this.microActionFactory);
    }

}