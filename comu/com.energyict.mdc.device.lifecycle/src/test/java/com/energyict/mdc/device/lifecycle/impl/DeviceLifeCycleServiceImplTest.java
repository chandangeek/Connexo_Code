package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionDoesNotRelateToDeviceStateException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.EffectiveTimestampNotAfterLastStateChangeException;
import com.energyict.mdc.device.lifecycle.EffectiveTimestampNotInRangeException;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.RequiredMicroActionPropertiesException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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

    private static final long DEVICE_LIFE_CYCLE_ID = 1L;
    private static final long EVENT_TYPE_ID = 11L;
    private static final long STATE_ID = 97L;
    private static final long DEVICE_ID = 111L;
    private static final String DEVICE_MRID = "MasterResourceId";
    private static final long STANDARD_SECONDS_IN_DAY = 86400L;

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
    private AuthorizedTransitionAction action;
    @Mock
    private User user;
    @Mock
    private ThreadPrincipalService threadPrincipleService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    private UserService userService;
    @Mock
    private UserPreferencesService userPreferencesService;
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
        when(this.thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new NoTranslation((TranslationKey) invocationOnMock.getArguments()[0]));
        when(this.thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new NoTranslation((MessageSeed) invocationOnMock.getArguments()[0]));
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        when(this.lifeCycle.getId()).thenReturn(DEVICE_LIFE_CYCLE_ID);
        when(this.lifeCycle.getFiniteStateMachine()).thenReturn(this.finiteStateMachine);
        when(this.lifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(Instant.now().plusSeconds(STANDARD_SECONDS_IN_DAY));
        when(this.lifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(Instant.now().minusSeconds(STANDARD_SECONDS_IN_DAY));
        when(this.state.getId()).thenReturn(STATE_ID);
        when(this.deviceType.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.device.getmRID()).thenReturn(DEVICE_MRID);
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getState()).thenReturn(this.state);
        when(this.action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        when(this.action.getState()).thenReturn(this.state);
        when(this.action.getStateTransition()).thenReturn(this.stateTransition);
        StateTimeline stateTimeline = mock(StateTimeline.class);
        StateTimeSlice timeSlice = mock(StateTimeSlice.class);
        when(timeSlice.getPeriod()).thenReturn(Range.atLeast(Instant.EPOCH));
        when(stateTimeline.getSlices()).thenReturn(Collections.singletonList(timeSlice));
        when(this.device.getStateTimeline()).thenReturn(stateTimeline);
        when(this.stateTransition.getEventType()).thenReturn(this.eventType);
        when(this.user.getName()).thenReturn(DeviceLifeCycleServiceImplTest.class.getSimpleName());
        when(user.getLocale()).thenReturn(Optional.empty());
        when(this.threadPrincipleService.getPrincipal()).thenReturn(this.user);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(anyString())).thenReturn(Optional.of(this.privilege));
        when(this.eventType.newInstance(any(FiniteStateMachine.class), anyString(), anyString(), anyString(), any(Instant.class), anyMap())).thenReturn(this.event);
        when(this.eventType.getId()).thenReturn(EVENT_TYPE_ID);
        for (MicroCheck microCheck : MicroCheck.values()) {
            ServerMicroCheck serverMicroCheck = mock(ServerMicroCheck.class);
            when(serverMicroCheck.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
            when(this.microCheckFactory.from(microCheck)).thenReturn(serverMicroCheck);
        }
        for (MicroAction microAction : MicroAction.values()) {
            ServerMicroAction serverMicroAction = mock(ServerMicroAction.class);
            when(this.microActionFactory.from(microAction)).thenReturn(serverMicroAction);
        }
        when(userService.getUserPreferencesService()).thenReturn(userPreferencesService);
        when(userPreferencesService.getPreferenceByKey(any(User.class), any(FormatKey.class))).thenReturn(Optional.empty());
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
        when(eventType.getSymbol()).thenReturn("executeTransitionActionThatDoesNotRelateToDeviceState");
        StateTransition stateTransition = mock(StateTransition.class);
        when(stateTransition.getEventType()).thenReturn(eventType);
        when(action.getStateTransition()).thenReturn(stateTransition);

        try {
            // Business method
            service.execute(action, this.device, Instant.now(), Collections.emptyList());
        } catch (ActionDoesNotRelateToDeviceStateException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
            throw e;
        }
    }

    @Test(expected = ActionDoesNotRelateToDeviceStateException.class)
    public void executeBpmActionThatDoesNotRelateToDeviceState() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        TransitionBusinessProcess businessProcess = mock(TransitionBusinessProcess.class);
        when(businessProcess.getDeploymentId()).thenReturn("deploymentId");
        when(businessProcess.getProcessId()).thenReturn("processId");
        AuthorizedBusinessProcessAction action = mock(AuthorizedBusinessProcessAction.class);
        when(action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        State state = mock(State.class);
        when(state.getId()).thenReturn(STATE_ID + 1);
        when(action.getState()).thenReturn(state);
        when(action.getTransitionBusinessProcess()).thenReturn(businessProcess);


        try {
            // Business method
            service.execute(action, this.device, Instant.now());

        } catch (ActionDoesNotRelateToDeviceStateException e) {
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
            service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

            // Asserts: see expected exception rule
        } catch (SecurityException e) {
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
            service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

            // Asserts: see expected exception rule
        } catch (SecurityException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.NOT_ALLOWED_2_EXECUTE);
            throw e;
        }
    }

    @Test(expected = SecurityException.class)
    public void executeActionThatCurrentUserIsNotAllowedToExecute() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(false);

        // Business method
        try {
            service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

        } catch (SecurityException e) {
            // Asserts: see also expected exception rule
            assertThat(e.getMessage()).isEqualTo(MessageSeeds.Keys.NOT_ALLOWED_2_EXECUTE);
            throw e;
        }
    }

    @Test
    public void executeCallsFactoryForEveryCheck() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        when(this.action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));

        // Business method
        service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

        // Asserts
        for (MicroCheck microCheck : MicroCheck.values()) {
            verify(this.microCheckFactory).from(microCheck);
        }
    }

    @Test
    public void allChecksAreEvaluatedAgainstTheDevice() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        MicroCheck microCheck1 = MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED;
        MicroCheck microCheck2 = MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE;
        ServerMicroCheck serverMicroCheck1 = mock(ServerMicroCheck.class);
        when(serverMicroCheck1.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        ServerMicroCheck serverMicroCheck2 = mock(ServerMicroCheck.class);
        when(serverMicroCheck2.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(this.microCheckFactory.from(microCheck1)).thenReturn(serverMicroCheck1);
        when(this.microCheckFactory.from(microCheck2)).thenReturn(serverMicroCheck2);
        when(this.action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(microCheck1, microCheck2)));

        // Business method
        service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(serverMicroCheck1).evaluate(eq(this.device), any(Instant.class));
        verify(serverMicroCheck2).evaluate(eq(this.device), any(Instant.class));
    }

    @Test(expected = MultipleMicroCheckViolationsException.class)
    public void allFailingChecksAreReported() {
        reset(this.thesaurus);
        when(this.thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new TranslationArgumentsOnly((TranslationKey) invocationOnMock.getArguments()[0]));
        when(this.thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new TranslationArgumentsOnly((MessageSeed) invocationOnMock.getArguments()[0]));
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        MicroCheck microCheck1 = MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED;
        MicroCheck microCheck2 = MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE;
        MicroCheck microCheck3 = MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID;
        MicroCheck microCheck4 = MicroCheck.DEFAULT_CONNECTION_AVAILABLE;
        ServerMicroCheck serverMicroCheck1 = mock(ServerMicroCheck.class);
        when(serverMicroCheck1.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        ServerMicroCheck serverMicroCheck2 = mock(ServerMicroCheck.class);
        when(serverMicroCheck2.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        ServerMicroCheck failingServerMicroCheck1 = mock(ServerMicroCheck.class);
        DeviceLifeCycleActionViolation violation1 = mock(DeviceLifeCycleActionViolation.class);
        when(violation1.getCheck()).thenReturn(microCheck3);
        when(violation1.getLocalizedMessage()).thenReturn("Violation 1");
        when(failingServerMicroCheck1.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.of(violation1));
        ServerMicroCheck failingServerMicroCheck2 = mock(ServerMicroCheck.class);
        DeviceLifeCycleActionViolation violation2 = mock(DeviceLifeCycleActionViolation.class);
        when(violation2.getCheck()).thenReturn(microCheck4);
        when(violation2.getLocalizedMessage()).thenReturn("Violation 2");
        when(failingServerMicroCheck2.evaluate(any(Device.class), any(Instant.class))).thenReturn(Optional.of(violation2));
        when(this.microCheckFactory.from(microCheck1)).thenReturn(serverMicroCheck1);
        when(this.microCheckFactory.from(microCheck2)).thenReturn(serverMicroCheck2);
        when(this.microCheckFactory.from(microCheck3)).thenReturn(failingServerMicroCheck1);
        when(this.microCheckFactory.from(microCheck4)).thenReturn(failingServerMicroCheck2);
        when(this.action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(microCheck1, microCheck3, microCheck2, microCheck4)));

        try {
            // Business method
            service.execute(this.action, this.device, Instant.now(), Collections.emptyList());
        } catch (MultipleMicroCheckViolationsException e) {
            // Asserts
            assertThat(e.getLocalizedMessage()).contains("Violation 1");
            assertThat(e.getLocalizedMessage()).contains("Violation 2");
            throw e;
        }
    }

    @Test
    public void executeCallsFactoryForEveryAction() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        when(this.action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));

        // Business method
        service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

        // Asserts
        for (MicroAction microAction : MicroAction.values()) {
            verify(this.microActionFactory, atLeastOnce()).from(microAction);
        }
    }

    @Test
    public void executeTriggersEvent() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        when(this.action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));

        // Business method
        service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(this.eventType).newInstance(eq(this.finiteStateMachine), eq(String.valueOf(DEVICE_ID)), anyString(), anyString(), any(Instant.class), anyMap());
    }

    @Test
    public void executeForwardsPropertiesToActions() {
        Instant now = Instant.now();
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.ENABLE_VALIDATION)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        PropertySpec validationStartDatePropertySpec = mock(PropertySpec.class);
        when(validationStartDatePropertySpec.getName()).thenReturn("validationStartDate");
        when(validationStartDatePropertySpec.isRequired()).thenReturn(true);
        ExecutableActionProperty validationStartDate = mock(ExecutableActionProperty.class);
        when(validationStartDate.getPropertySpec()).thenReturn(validationStartDatePropertySpec);
        when(validationStartDate.getValue()).thenReturn(now);
        ServerMicroAction enableValidation = mock(ServerMicroAction.class);
        when(this.microActionFactory.from(MicroAction.ENABLE_VALIDATION)).thenReturn(enableValidation);
        List<ExecutableActionProperty> expectedProperties = Collections.singletonList(validationStartDate);

        // Business method
        service.execute(this.action, this.device, now, expectedProperties);

        // Asserts
        verify(enableValidation).execute(this.device, now, expectedProperties);
    }

    @Test(expected = RequiredMicroActionPropertiesException.class)
    public void executeWithMissingRequiredProperties() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.ENABLE_VALIDATION)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        PropertySpec validationStartDatePropertySpec = mock(PropertySpec.class);
        when(validationStartDatePropertySpec.getName()).thenReturn("validationStartDate");
        when(validationStartDatePropertySpec.isRequired()).thenReturn(true);
        ExecutableActionProperty validationStartDate = mock(ExecutableActionProperty.class);
        when(validationStartDate.getPropertySpec()).thenReturn(validationStartDatePropertySpec);
        when(validationStartDate.getValue()).thenReturn(Instant.now());
        ServerMicroAction enableValidation = mock(ServerMicroAction.class);
        when(enableValidation.getPropertySpecs(any(PropertySpecService.class))).thenReturn(Collections.singletonList(validationStartDatePropertySpec));
        when(this.microActionFactory.from(MicroAction.ENABLE_VALIDATION)).thenReturn(enableValidation);

        // Business method
        service.execute(this.action, this.device, Instant.now(), Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = EffectiveTimestampNotInRangeException.class)
    public void executeWithEffectiveTimestampTooFarInThePast() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.SET_LAST_READING)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        ServerMicroAction setLastReading = mock(ServerMicroAction.class);
        when(this.microActionFactory.from(MicroAction.SET_LAST_READING)).thenReturn(setLastReading);
        Instant effectiveTimeStamp = Instant.ofEpochMilli(123456789);
        when(this.lifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(effectiveTimeStamp);
        when(this.lifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(effectiveTimeStamp);
        StateTimeline stateTimeline = mock(StateTimeline.class);
        StateTimeSlice timeSlice = mock(StateTimeSlice.class);
        Instant timeSliceStart = effectiveTimeStamp.minus(8, ChronoUnit.DAYS);
        when(timeSlice.getPeriod()).thenReturn(Range.atLeast(timeSliceStart));
        when(stateTimeline.getSlices()).thenReturn(Collections.singletonList(timeSlice));
        when(this.device.getStateTimeline()).thenReturn(stateTimeline);

        // Business method
        service.execute(this.action, this.device, effectiveTimeStamp.minus(7, ChronoUnit.DAYS), Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = EffectiveTimestampNotInRangeException.class)
    public void executeWithEffectiveTimestampTooFarInTheFuture() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.SET_LAST_READING)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        ServerMicroAction setLastReading = mock(ServerMicroAction.class);
        when(this.microActionFactory.from(MicroAction.SET_LAST_READING)).thenReturn(setLastReading);
        Instant effectiveTimeStamp = Instant.ofEpochMilli(123456789);
        when(this.lifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(effectiveTimeStamp);
        when(this.lifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(effectiveTimeStamp);

        // Business method
        service.execute(this.action, this.device, effectiveTimeStamp.plus(7, ChronoUnit.DAYS), Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = EffectiveTimestampNotAfterLastStateChangeException.class)
    public void executeWithEffectiveTimestampBeforeLastStateChange() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.SET_LAST_READING)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        ServerMicroAction setLastReading = mock(ServerMicroAction.class);
        when(this.microActionFactory.from(MicroAction.SET_LAST_READING)).thenReturn(setLastReading);
        when(this.lifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(Instant.ofEpochMilli(100000L));
        when(this.lifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(Instant.ofEpochMilli(10000L));
        StateTimeline stateTimeline = mock(StateTimeline.class);
        StateTimeSlice timeSlice = mock(StateTimeSlice.class);
        Instant timeSliceStart = Instant.ofEpochMilli(60000L);
        when(timeSlice.getPeriod()).thenReturn(Range.atLeast(timeSliceStart));
        when(stateTimeline.getSlices()).thenReturn(Collections.singletonList(timeSlice));
        when(this.device.getStateTimeline()).thenReturn(stateTimeline);

        // Business method
        service.execute(this.action, this.device, timeSliceStart.minusMillis(1000L), Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = EffectiveTimestampNotAfterLastStateChangeException.class)
    public void executeWithEffectiveTimestampExactlyOnLastStateChange() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.SET_LAST_READING)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        ServerMicroAction setLastReading = mock(ServerMicroAction.class);
        when(this.microActionFactory.from(MicroAction.SET_LAST_READING)).thenReturn(setLastReading);
        when(this.lifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(Instant.ofEpochMilli(100000L));
        when(this.lifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(Instant.ofEpochMilli(10000L));
        StateTimeline stateTimeline = mock(StateTimeline.class);
        StateTimeSlice timeSlice = mock(StateTimeSlice.class);
        Instant timeSliceStart = Instant.ofEpochMilli(60000L);
        when(timeSlice.getPeriod()).thenReturn(Range.atLeast(timeSliceStart));
        when(stateTimeline.getSlices()).thenReturn(Collections.singletonList(timeSlice));
        when(this.device.getStateTimeline()).thenReturn(stateTimeline);

        // Business method
        service.execute(this.action, this.device, timeSliceStart, Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = EffectiveTimestampNotInRangeException.class)
    public void executeWithEffectiveTimestampAfterLastStateChangeButNotInRange() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getActions()).thenReturn(new HashSet<>(Collections.singletonList(MicroAction.SET_LAST_READING)));
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);
        ServerMicroAction setLastReading = mock(ServerMicroAction.class);
        when(this.microActionFactory.from(MicroAction.SET_LAST_READING)).thenReturn(setLastReading);

        Instant lastStateChange = Instant.ofEpochMilli(100000L);
        StateTimeline stateTimeline = mock(StateTimeline.class);
        StateTimeSlice timeSlice = mock(StateTimeSlice.class);
        when(timeSlice.getPeriod()).thenReturn(Range.atLeast(lastStateChange));
        when(stateTimeline.getSlices()).thenReturn(Collections.singletonList(timeSlice));
        when(this.device.getStateTimeline()).thenReturn(stateTimeline);

        when(this.lifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(lastStateChange.plus(1, ChronoUnit.DAYS));
        when(this.lifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(lastStateChange.minus(1, ChronoUnit.DAYS));

        // Business method
        service.execute(this.action, this.device, lastStateChange.plus(2, ChronoUnit.DAYS), Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test
    public void executeDelegatesToBpmService() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        TransitionBusinessProcess businessProcess = mock(TransitionBusinessProcess.class);
        String deploymentId = "deploymentId";
        String processId = "processId";
        when(businessProcess.getDeploymentId()).thenReturn(deploymentId);
        when(businessProcess.getProcessId()).thenReturn(processId);
        AuthorizedBusinessProcessAction action = mock(AuthorizedBusinessProcessAction.class);
        when(action.getDeviceLifeCycle()).thenReturn(this.lifeCycle);
        when(action.getTransitionBusinessProcess()).thenReturn(businessProcess);
        when(action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(action.getState()).thenReturn(this.state);
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);

        // Business method
        service.execute(action, this.device, Instant.now());

        // Asserts
        verify(businessProcess).executeOn(eq(this.device.getId()), eq(this.state));
    }

    @Test
    public void getExecutableActionsWithoutAuthorizedActions() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Collections.emptyList());

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).isEmpty();
    }

    @Test
    public void getExecutableActionsForUserWithAllWrongPrivileges() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        AuthorizedBusinessProcessAction businessProcessAction = mock(AuthorizedBusinessProcessAction.class);
        when(businessProcessAction.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Arrays.asList(this.action, businessProcessAction));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(false);

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).isEmpty();
    }

    @Test
    public void getExecutableActionsForUserWithSomeWrongPrivileges() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.THREE));
        AuthorizedBusinessProcessAction businessProcessAction = mock(AuthorizedBusinessProcessAction.class);
        when(businessProcessAction.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.FOUR.getPrivilege())).thenReturn(Optional.of(this.privilege));
        Privilege allowed = mock(Privilege.class);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.THREE.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Arrays.asList(this.action, businessProcessAction));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(false);
        when(this.user.hasPrivilege("MDC", allowed)).thenReturn(true);

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).hasSize(1);
        ExecutableAction executableAction = executableActions.get(0);
        assertThat(executableAction.getAction()).isEqualTo(this.action);
    }

    @Test
    public void getExecutableActionsForUserWithAllPrivileges() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.THREE));
        AuthorizedBusinessProcessAction businessProcessAction = mock(AuthorizedBusinessProcessAction.class);
        when(businessProcessAction.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        Privilege allowed = mock(Privilege.class);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.FOUR.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.THREE.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Arrays.asList(this.action, businessProcessAction));
        when(this.user.hasPrivilege("MDC", allowed)).thenReturn(true);

        // Business method
        List<ExecutableAction> executableActions = service.getExecutableActions(this.device);

        // Asserts
        assertThat(executableActions).hasSize(2);
        List<AuthorizedAction> actions = executableActions.stream().map(ExecutableAction::getAction).collect(Collectors.toList());
        assertThat(actions).containsOnly(this.action, businessProcessAction);
    }

    @Test
    public void getExecutableActionsForEventTypeWithoutAuthorizedActions() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(Collections.emptyList());

        // Business method
        Optional<ExecutableAction> executableAction = service.getExecutableActions(this.device, mock(StateTransitionEventType.class));

        // Asserts
        assertThat(executableAction.isPresent()).isFalse();
    }

    @Test
    public void getExecutableActionsForEventTypeWithOnlyBusinessProcessActions() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        Privilege allowed = mock(Privilege.class);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.FOUR.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.THREE.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.user.hasPrivilege("MDC", allowed)).thenReturn(true);
        AuthorizedBusinessProcessAction action = mock(AuthorizedBusinessProcessAction.class);
        when(action.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        List<AuthorizedAction> actions = Collections.singletonList(action);
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(actions);

        // Business method
        Optional<ExecutableAction> executableAction = service.getExecutableActions(this.device, mock(StateTransitionEventType.class));

        // Asserts
        assertThat(executableAction.isPresent()).isFalse();
    }

    @Test
    public void getExecutableActionsForEventTypeWithNoMatchingEventType() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        Privilege allowed = mock(Privilege.class);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.FOUR.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.THREE.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.user.hasPrivilege("MDC", allowed)).thenReturn(true);
        StateTransition stateTransition1 = mock(StateTransition.class);
        StateTransitionEventType eventType1 = mock(StateTransitionEventType.class);
        when(eventType1.getId()).thenReturn(1L);
        when(stateTransition1.getEventType()).thenReturn(eventType1);
        AuthorizedTransitionAction action1 = mock(AuthorizedTransitionAction.class);
        when(action1.getStateTransition()).thenReturn(stateTransition1);
        when(action1.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        StateTransition stateTransition2 = mock(StateTransition.class);
        StateTransitionEventType eventType2 = mock(StateTransitionEventType.class);
        when(eventType2.getId()).thenReturn(2L);
        when(stateTransition2.getEventType()).thenReturn(eventType2);
        AuthorizedTransitionAction action2 = mock(AuthorizedTransitionAction.class);
        when(action2.getStateTransition()).thenReturn(stateTransition2);
        when(action2.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        List<AuthorizedAction> actions = Arrays.asList(action1, action2);
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(actions);

        // Business method
        Optional<ExecutableAction> executableAction = service.getExecutableActions(this.device, this.eventType);

        // Asserts
        assertThat(executableAction.isPresent()).isFalse();
    }

    @Test
    public void getExecutableActionsForEventTypeWithMatchingEventType() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        Privilege allowed = mock(Privilege.class);
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.FOUR.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(AuthorizedAction.Level.THREE.getPrivilege())).thenReturn(Optional.of(allowed));
        when(this.user.hasPrivilege("MDC", allowed)).thenReturn(true);
        StateTransition stateTransition1 = mock(StateTransition.class);
        StateTransitionEventType eventType1 = mock(StateTransitionEventType.class);
        when(eventType1.getId()).thenReturn(1L);
        when(stateTransition1.getEventType()).thenReturn(eventType1);
        AuthorizedTransitionAction action1 = mock(AuthorizedTransitionAction.class);
        when(action1.getStateTransition()).thenReturn(stateTransition1);
        when(action1.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        StateTransition stateTransition2 = mock(StateTransition.class);
        when(stateTransition2.getEventType()).thenReturn(this.eventType);
        AuthorizedTransitionAction action2 = mock(AuthorizedTransitionAction.class);
        when(action2.getStateTransition()).thenReturn(stateTransition2);
        when(action2.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.FOUR));
        List<AuthorizedAction> actions = Arrays.asList(action1, action2);
        when(this.lifeCycle.getAuthorizedActions(any(State.class))).thenReturn(actions);

        // Business method
        Optional<ExecutableAction> executableAction = service.getExecutableActions(this.device, this.eventType);

        // Asserts
        assertThat(executableAction.isPresent()).isTrue();
    }

    @Test
    public void getPropertySpecsDoesNotReturnNull() {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();

        for (MicroAction action : MicroAction.values()) {
            // Business method
            List<PropertySpec> propertySpecs = service.getPropertySpecsFor(action);

            // Asserts
            assertThat(propertySpecs).as("Service returns null for MicroAction#" + action.name()).isNotNull();
        }
    }

    @Test
    public void toExecutableActionPropertyValidatesValueWithThePropertySpec() throws InvalidValueException {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        PropertySpec propertySpec = mock(PropertySpec.class);

        // Business method
        String value = "toExecutableActionPropertyThrowsExceptionForIncompatibleValue";
        service.toExecutableActionProperty(value, propertySpec);

        // Asserts
        verify(propertySpec).validateValueIgnoreRequired(value);
    }

    @Test(expected = InvalidValueException.class)
    public void toExecutableActionPropertyThrowsExceptionForIncompatibleValue() throws InvalidValueException {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        PropertySpec propertySpec = mock(PropertySpec.class);
        doThrow(InvalidValueException.class).when(propertySpec).validateValueIgnoreRequired(anyString());

        // Business method
        service.toExecutableActionProperty("toExecutableActionPropertyThrowsExceptionForIncompatibleValue", propertySpec);

        // Asserts: exception is mocked on the PropertySpec, verify that it is effectively thrown and not eaten
    }

    @Test
    public void toExecutableActionProperty() throws InvalidValueException {
        DeviceLifeCycleServiceImpl service = this.getTestInstance();
        PropertySpec propertySpec = mock(PropertySpec.class);

        // Business method
        BigDecimal value = BigDecimal.TEN;
        ExecutableActionProperty executableActionProperty = service.toExecutableActionProperty(value, propertySpec);

        // Asserts: see expected exception rule
        assertThat(executableActionProperty).isNotNull();
        assertThat(executableActionProperty.getPropertySpec()).isEqualTo(propertySpec);
        assertThat(executableActionProperty.getValue()).isEqualTo(value);
    }

    @Test
    public void testTransitionsWithStandardEventTypesAreNotAvailableForExecution() {
        CustomStateTransitionEventType customEventType = mock(CustomStateTransitionEventType.class);
        StateTransition customStateTransition = mock(StateTransition.class);
        when(customStateTransition.getEventType()).thenReturn(customEventType);
        AuthorizedTransitionAction customTransition = mock(AuthorizedTransitionAction.class);
        when(customTransition.getStateTransition()).thenReturn(customStateTransition);
        when(customTransition.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.ONE));

        StandardStateTransitionEventType standardEventType = mock(StandardStateTransitionEventType.class);
        StateTransition standardStateTransition = mock(StateTransition.class);
        when(standardStateTransition.getEventType()).thenReturn(standardEventType);
        AuthorizedTransitionAction standardTransition = mock(AuthorizedTransitionAction.class);
        when(standardTransition.getStateTransition()).thenReturn(standardStateTransition);
        when(standardTransition.getLevels()).thenReturn(EnumSet.of(AuthorizedAction.Level.ONE));

        when(lifeCycle.getAuthorizedActions(state)).thenReturn(Arrays.asList(standardTransition, customTransition));
        when(this.user.hasPrivilege("MDC", this.privilege)).thenReturn(true);

        // Business method
        List<ExecutableAction> executableActions = getTestInstance().getExecutableActions(this.device);

        // Assertion
        assertThat(executableActions.size()).isEqualTo(1);
        assertThat(executableActions.get(0).getAction()).isEqualTo(customTransition);
    }

    private DeviceLifeCycleServiceImpl getTestInstance() {
        return new DeviceLifeCycleServiceImpl(this.nlsService, this.threadPrincipleService, this.propertySpecService, this.microCheckFactory, this.microActionFactory, this.deviceLifeCycleConfigurationService, this.userService, Clock
                .systemDefaultZone());
    }

    public static class NoTranslation implements NlsMessageFormat {
        private final String defaultFormat;

        NoTranslation(TranslationKey translationKey) {
            this.defaultFormat = translationKey.getKey();
        }

        NoTranslation(MessageSeed messageSeed) {
            this.defaultFormat = messageSeed.getKey();
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format(this.defaultFormat, args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return MessageFormat.format(this.defaultFormat, args);
        }

    }

    public static class TranslationArgumentsOnly implements NlsMessageFormat {

        TranslationArgumentsOnly(TranslationKey translationKey) {
        }

        TranslationArgumentsOnly(MessageSeed messageSeed) {
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format("{0}", args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return this.format(args);
        }
    }
}