package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionDoesNotRelateToDeviceStateException;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedStandardTransitionAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    public static final long STATE_ID = 97L;
    public static final long DEVICE_ID = 111L;

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerMicroCheckFactory microCheckFactory;
    @Mock
    private ServerMicroActionFactory microActionFactory;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private DeviceLifeCycle lifeCycle;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;
    @Mock
    private AuthorizedStandardTransitionAction action;
    @Mock
    private User user;
    @Mock
    private ThreadPrincipalService threadPrincipleService;
    @Mock
    private BpmService bpmService;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    private Privilege privilege;
    @Mock
    private StateTransition stateTransition;
    @Mock
    private CustomStateTransitionEventType eventType;
    @Mock
    private State state;
    @Mock
    private StateTransitionTriggerEvent event;

    @Before
    public void initializeMocks() {
        // Mock thesaurus such that it returns the key as the translation
        when(this.thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        when(this.lifeCycle.getId()).thenReturn(DEVICE_LIFE_CYCLE_ID);
        when(this.lifeCycle.getFiniteStateMachine()).thenReturn(this.finiteStateMachine);
        when(this.state.getId()).thenReturn(STATE_ID);
        when(this.deviceType.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getState()).thenReturn(this.state);
        when(this.action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        when(this.action.getState()).thenReturn(this.state);
        when(this.action.getStateTransition()).thenReturn(this.stateTransition);
        when(this.stateTransition.getEventType()).thenReturn(this.eventType);
        when(this.user.getName()).thenReturn(DeviceLifeCycleServiceImplTest.class.getSimpleName());
        when(this.threadPrincipleService.getPrincipal()).thenReturn(this.user);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(anyString())).thenReturn(Optional.of(this.privilege));
        when(this.eventType.newInstance(any(FiniteStateMachine.class), anyString(), anyString(), anyMap())).thenReturn(this.event);
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

    @Test(expected = ActionDoesNotRelateToDeviceStateException.class)
    public void executeTransitionActionThatDoesNotRelateToDeviceState() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        State state = mock(State.class);
        when(state.getId()).thenReturn(STATE_ID + 1);
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getState()).thenReturn(state);
        StateTransitionEventType eventType = mock(StateTransitionEventType.class);
        when(eventType.getSymbol()).thenReturn("executeTransitionActionThatIsNotPartOfTheDeviceLifeCycle");
        StateTransition stateTransition = mock(StateTransition.class);
        when(stateTransition.getEventType()).thenReturn(eventType);
        when(action.getStateTransition()).thenReturn(stateTransition);


        try {
            // Business method
            service.execute(action, this.device);
        }
        catch (ActionDoesNotRelateToDeviceStateException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
            throw e;
        }
    }

    @Test(expected = ActionDoesNotRelateToDeviceStateException.class)
    public void executeBpmActionThatDoesNotRelateToDeviceState() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        AuthorizedBusinessProcessAction action = mock(AuthorizedBusinessProcessAction.class);
        when(action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        State state = mock(State.class);
        when(state.getId()).thenReturn(STATE_ID + 1);
        when(action.getState()).thenReturn(state);
        when(action.getDeploymentId()).thenReturn("deploymentId");
        when(action.getProcessId()).thenReturn("processId");


        try {
            // Business method
            service.execute(action, this.device);

        }
        catch (ActionDoesNotRelateToDeviceStateException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
            throw e;
        }
    }

    @Test(expected = SecurityException.class)
    public void executeActionForNonUserPrincipal() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(Collections.emptySet());
        Principal principal = mock(Principal.class);
        when(this.threadPrincipleService.getPrincipal()).thenReturn(principal);

        try {
            // Business method
            service.execute(this.action, this.device);

            // Asserts: see expected exception rule
        }
        catch (SecurityException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.NOT_ALLOWED_2_EXECUTE);
            throw e;
        }
    }

    @Test(expected = SecurityException.class)
    public void executeActionThatNobodyIsAllowedToExecute() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(Collections.emptySet());

        try {
            // Business method
            service.execute(this.action, this.device);

            // Asserts: see expected exception rule
        }
        catch (SecurityException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.NOT_ALLOWED_2_EXECUTE);
            throw e;
        }
    }

    @Test(expected = SecurityException.class)
    public void executeActionThatCurrentUserIsNotAllowedToExecute() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege(this.privilege)).thenReturn(false);

        // Business method
        try {
            service.execute(this.action, this.device);

        }
        catch (SecurityException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.NOT_ALLOWED_2_EXECUTE);
            throw e;
        }
    }

    @Test
    public void executeCallsFactoryForEveryCheck() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege(this.privilege)).thenReturn(true);
        when(this.action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));

        // Business method
        service.execute(this.action, this.device);

        // Asserts
        for (MicroCheck microCheck : MicroCheck.values()) {
            verify(this.microCheckFactory).from(microCheck);
        }
    }

    @Test
    public void executeCallsFactoryForEveryAction() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege(this.privilege)).thenReturn(true);
        when(this.action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));

        // Business method
        service.execute(this.action, this.device);

        // Asserts
        for (MicroAction microAction : MicroAction.values()) {
            verify(this.microActionFactory).from(microAction);
        }
    }

    @Test
    public void executeTriggersEvent() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege(this.privilege)).thenReturn(true);
        when(this.action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));

        // Business method
        service.execute(this.action, this.device);

        // Asserts
        verify(this.eventType).newInstance(eq(this.finiteStateMachine), eq(String.valueOf(DEVICE_ID)), anyString(), anyMap());
    }

    @Test
    public void executeDelegatesToBpmService() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        AuthorizedBusinessProcessAction action = mock(AuthorizedBusinessProcessAction.class);
        when(action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        String deploymentId = "deploymentId";
        when(action.getDeploymentId()).thenReturn(deploymentId);
        String processId = "processId";
        when(action.getProcessId()).thenReturn(processId);
        when(action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(action.getState()).thenReturn(this.state);
        when(this.user.hasPrivilege(this.privilege)).thenReturn(true);

        // Business method
        service.execute(action, this.device);

        // Asserts
        ArgumentCaptor<Map> processParameterCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.bpmService).startProcess(eq(deploymentId), eq(processId), processParameterCaptor.capture());
        Map processParameters = processParameterCaptor.getValue();
        assertThat(processParameters.get(AuthorizedBusinessProcessAction.ProcessParameterKey.DEVICE.getName())).isEqualTo(this.device.getId());
    }

    @Test
    public void getExecutableStatesWithoutAuthorizedActions() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Collections.emptyList());

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).isEmpty();
    }

    @Test
    public void getExecutableStatesForUserWithAllWrongPrivileges() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        AuthorizedBusinessProcessAction businessProcessAction = mock(AuthorizedBusinessProcessAction.class);
        when(businessProcessAction.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Arrays.asList(this.action, businessProcessAction));
        when(this.user.hasPrivilege(this.privilege)).thenReturn(false);

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).isEmpty();
    }

    @Test
    public void getExecutableStatesForUserWithSomeWrongPrivileges() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        AuthorizedBusinessProcessAction businessProcessAction = mock(AuthorizedBusinessProcessAction.class);
        when(businessProcessAction.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.THREE));
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.FOUR.getPrivilege())).thenReturn(Optional.of(this.privilege));
        Privilege allowed = mock(Privilege.class);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.THREE.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Arrays.asList(this.action, businessProcessAction));
        when(this.user.hasPrivilege(this.privilege)).thenReturn(false);
        when(this.user.hasPrivilege(allowed)).thenReturn(true);

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).hasSize(1);
        ExecutableAction executableAction = executableActions.get(0);
        assertThat(executableAction.getAction()).isEqualTo(businessProcessAction);
    }

    private DeviceLifeCycleServiceImpl getTestInstance() {
        return new DeviceLifeCycleServiceImpl(this.nlsService, this.threadPrincipleService, this.bpmService, this.microCheckFactory, this.microActionFactory, this.deviceLifeCycleConfigurationService);
    }

}