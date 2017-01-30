package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.Range;

import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests getting the List of {@link DeviceLifeCycleChangeEvent}s from a {@link DeviceImpl}.
 * @see {@link DeviceImpl#getDeviceLifeCycleChangeEvents()}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (14:14)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceLifeCycleChangeEventsTest {

    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DataModel dataModel;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private EventService eventService;
    @Mock
    private IssueService issueService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private SecurityPropertyService securityPropertyService;
    @Mock
    private DeviceLifeCycle deviceLifeCycle;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private MeterBuilder meterBuilder;
    @Mock
    private LifecycleDates lifecycleDates;
    @Mock
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    @Mock
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    @Mock
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Provider<ComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private MdcReadingTypeUtilService readingTypeUtilService;
    @Mock
    private User user;
    @Mock
    private AmrSystem mdcAmrSystem;
    @Mock
    private Meter meter;
    @Mock
    private StateTimeline stateTimeline;
    @Mock
    private MultiplierType multiplierType;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserPreferencesService userPreferencesService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private LockService lockService;

    @Before
    public void initializeMocks() {
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(this.mdcAmrSystem));
        when(deviceService.findDefaultMultiplierType()).thenReturn(multiplierType);
        when(multiplierType.getName()).thenReturn("Default");
        when(this.mdcAmrSystem.findMeter("0")).thenReturn(Optional.of(this.meter));
        when(this.meter.getStateTimeline()).thenReturn(Optional.of(this.stateTimeline));
        when(this.stateTimeline.getSlices()).thenReturn(Collections.emptyList());
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.getId()).thenReturn(633L);
        when(mdcAmrSystem.newMeter(anyString(), anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setAmrId(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setMRID(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setSerialNumber(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setManufacturer(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setModelNumber(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setModelVersion(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setStateMachine(any(FiniteStateMachine.class))).thenReturn(meterBuilder);
        when(meterBuilder.setReceivedDate(any(Instant.class))).thenReturn(meterBuilder);
        when(meterBuilder.create()).thenReturn(meter);
        when(deviceLifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(Instant.MIN);
        when(deviceLifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(Instant.MAX);
        when(meter.getMeterActivations()).thenReturn(Collections.emptyList());
        when(meter.getMeterActivation(any(Instant.class))).thenReturn(Optional.empty());
        when(meter.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(meter.getLifecycleDates()).thenReturn(lifecycleDates);
        when(meter.getConfiguration(any(Instant.class))).thenReturn(Optional.empty());
    }

    @Test
    public void noDeviceLifeCycleChangesAndNoStateChanges() {
        Instant deviceTypeCreationTimestamp = Instant.ofEpochMilli(10000L);
        Instant deviceCreationTimestamp = Instant.ofEpochMilli(20000L);
        when(this.clock.instant()).thenReturn(deviceCreationTimestamp);

        // Mock the device life cycle change events at the device type level
        com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent initial = mock(com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent.class);
        when(initial.getTimestamp()).thenReturn(deviceTypeCreationTimestamp);
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(initial.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(initial.getUser()).thenReturn(Optional.of(this.user));
        when(this.deviceType.getDeviceLifeCycleChangeEvents()).thenReturn(Collections.singletonList(initial));

        // Mock the state time line of the Kore meter
        StateTimeSlice initialState = mock(StateTimeSlice.class);
        when(initialState.getPeriod()).thenReturn(Range.atLeast(deviceCreationTimestamp));
        State state = mock(State.class);
        when(initialState.getState()).thenReturn(state);
        when(initialState.getUser()).thenReturn(Optional.of(this.user));
        when(this.stateTimeline.getSlices()).thenReturn(Collections.singletonList(initialState));

        DeviceImpl device = this.getTestInstance();

        // Business method
        List<DeviceLifeCycleChangeEvent> changeEvents = device.getDeviceLifeCycleChangeEvents();

        // Asserts
        assertThat(changeEvents).hasSize(1);
        DeviceLifeCycleChangeEvent changeEvent = changeEvents.get(0);
        assertThat(changeEvent.getTimestamp()).isEqualTo(deviceCreationTimestamp);
        assertThat(changeEvent.getUser()).contains(this.user);
        assertThat(changeEvent.getType()).isEqualTo(DeviceLifeCycleChangeEvent.Type.STATE);
        assertThat(changeEvent.getState()).isEqualTo(state);
    }

    @Test
    public void deviceCreationAndLifeCycleChange() {
        Instant deviceTypeCreationTimestamp = Instant.ofEpochMilli(10000L);
        Instant deviceCreationTimestamp = Instant.ofEpochMilli(20000L);
        when(this.clock.instant()).thenReturn(deviceCreationTimestamp);
        Instant changeDLCTimestamp = Instant.ofEpochMilli(30000L);

        // Mock the device life cycle change events at the device type level
        com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent createDeviceType = mock(com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent.class);
        when(createDeviceType.getTimestamp()).thenReturn(deviceTypeCreationTimestamp);
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(createDeviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(createDeviceType.getUser()).thenReturn(Optional.of(this.user));
        com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent changeDeviceLifeCycleOnDeviceType = mock(com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent.class);
        when(changeDeviceLifeCycleOnDeviceType.getTimestamp()).thenReturn(changeDLCTimestamp);
        DeviceLifeCycle newDeviceLifeCycle = mock(DeviceLifeCycle.class);
        when(changeDeviceLifeCycleOnDeviceType.getDeviceLifeCycle()).thenReturn(newDeviceLifeCycle);
        when(changeDeviceLifeCycleOnDeviceType.getUser()).thenReturn(Optional.of(this.user));
        when(this.deviceType.getDeviceLifeCycleChangeEvents()).thenReturn(Arrays.asList(createDeviceType, changeDeviceLifeCycleOnDeviceType));

        // Mock the state time line of the Kore meter
        StateTimeSlice initialStateTimeSlice = mock(StateTimeSlice.class);
        when(initialStateTimeSlice.getPeriod()).thenReturn(Range.closedOpen(deviceCreationTimestamp, changeDLCTimestamp));
        State initialState = mock(State.class);
        when(initialStateTimeSlice.getState()).thenReturn(initialState);
        when(initialStateTimeSlice.getUser()).thenReturn(Optional.of(this.user));
        StateTimeSlice administrativeChangeState = mock(StateTimeSlice.class);  // The state change that relates to the device life cycle change
        when(administrativeChangeState.getPeriod()).thenReturn(Range.atLeast(changeDLCTimestamp));
        State mappedState = mock(State.class);
        when(administrativeChangeState.getState()).thenReturn(mappedState);
        when(administrativeChangeState.getUser()).thenReturn(Optional.of(this.user));
        when(this.stateTimeline.getSlices()).thenReturn(Arrays.asList(initialStateTimeSlice, administrativeChangeState));

        DeviceImpl device = this.getTestInstance();

        // Business method
        List<DeviceLifeCycleChangeEvent> changeEvents = device.getDeviceLifeCycleChangeEvents();

        // Asserts
        assertThat(changeEvents).hasSize(2);
        DeviceLifeCycleChangeEvent deviceCreationEvent = changeEvents.get(0);
        assertThat(deviceCreationEvent.getTimestamp()).isEqualTo(deviceCreationTimestamp);
        assertThat(deviceCreationEvent.getUser()).contains(this.user);
        assertThat(deviceCreationEvent.getType()).isEqualTo(DeviceLifeCycleChangeEvent.Type.STATE);
        assertThat(deviceCreationEvent.getState()).isEqualTo(initialState);
        DeviceLifeCycleChangeEvent changeDLCEvent = changeEvents.get(1);
        assertThat(changeDLCEvent.getTimestamp()).isEqualTo(changeDLCTimestamp);
        assertThat(changeDLCEvent.getUser()).contains(this.user);
        assertThat(changeDLCEvent.getType()).isEqualTo(DeviceLifeCycleChangeEvent.Type.LIFE_CYCLE);
        assertThat(changeDLCEvent.getDeviceLifeCycle()).isEqualTo(newDeviceLifeCycle);
    }

    @Test
    public void deviceCreationLifeCycleChangeAndStateChangeEvent() {
        Instant deviceTypeCreationTimestamp = Instant.ofEpochMilli(10000L);
        Instant deviceCreationTimestamp = Instant.ofEpochMilli(20000L);
        when(this.clock.instant()).thenReturn(deviceCreationTimestamp);
        Instant changeDLCTimestamp = Instant.ofEpochMilli(30000L);
        Instant changeStateTimestamp = Instant.ofEpochMilli(40000L);

        // Mock the device life cycle change events at the device type level
        com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent createDeviceType = mock(com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent.class);
        when(createDeviceType.getTimestamp()).thenReturn(deviceTypeCreationTimestamp);
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(createDeviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(createDeviceType.getUser()).thenReturn(Optional.of(this.user));
        com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent changeDeviceLifeCycleOnDeviceType = mock(com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent.class);
        when(changeDeviceLifeCycleOnDeviceType.getTimestamp()).thenReturn(changeDLCTimestamp);
        DeviceLifeCycle newDeviceLifeCycle = mock(DeviceLifeCycle.class);
        when(changeDeviceLifeCycleOnDeviceType.getDeviceLifeCycle()).thenReturn(newDeviceLifeCycle);
        when(changeDeviceLifeCycleOnDeviceType.getUser()).thenReturn(Optional.of(this.user));
        when(this.deviceType.getDeviceLifeCycleChangeEvents()).thenReturn(Arrays.asList(createDeviceType, changeDeviceLifeCycleOnDeviceType));

        // Mock the state time line of the Kore meter
        StateTimeSlice initialStateTimeSlice = mock(StateTimeSlice.class);
        when(initialStateTimeSlice.getPeriod()).thenReturn(Range.atLeast(deviceCreationTimestamp));
        State initialState = mock(State.class);
        when(initialStateTimeSlice.getState()).thenReturn(initialState);
        when(initialStateTimeSlice.getUser()).thenReturn(Optional.of(this.user));
        StateTimeSlice administrativeChangeState = mock(StateTimeSlice.class);  // The state change that relates to the device life cycle change
        when(administrativeChangeState.getPeriod()).thenReturn(Range.atLeast(changeDLCTimestamp));
        State mappedState = mock(State.class);
        when(administrativeChangeState.getState()).thenReturn(mappedState);
        when(administrativeChangeState.getUser()).thenReturn(Optional.of(this.user));
        StateTimeSlice changeState = mock(StateTimeSlice.class);
        when(changeState.getPeriod()).thenReturn(Range.atLeast(changeStateTimestamp));
        State newState = mock(State.class);
        when(changeState.getState()).thenReturn(newState);
        when(changeState.getUser()).thenReturn(Optional.of(this.user));
        when(this.stateTimeline.getSlices()).thenReturn(Arrays.asList(initialStateTimeSlice, changeState));

        DeviceImpl device = this.getTestInstance();

        // Business method
        List<DeviceLifeCycleChangeEvent> changeEvents = device.getDeviceLifeCycleChangeEvents();

        // Asserts
        assertThat(changeEvents).hasSize(3);
        DeviceLifeCycleChangeEvent deviceCreationEvent = changeEvents.get(0);
        assertThat(deviceCreationEvent.getTimestamp()).isEqualTo(deviceCreationTimestamp);
        assertThat(deviceCreationEvent.getUser()).contains(this.user);
        assertThat(deviceCreationEvent.getType()).isEqualTo(DeviceLifeCycleChangeEvent.Type.STATE);
        assertThat(deviceCreationEvent.getState()).isEqualTo(initialState);
        DeviceLifeCycleChangeEvent changeDLCEvent = changeEvents.get(1);
        assertThat(changeDLCEvent.getTimestamp()).isEqualTo(changeDLCTimestamp);
        assertThat(changeDLCEvent.getUser()).contains(this.user);
        assertThat(changeDLCEvent.getType()).isEqualTo(DeviceLifeCycleChangeEvent.Type.LIFE_CYCLE);
        assertThat(changeDLCEvent.getDeviceLifeCycle()).isEqualTo(newDeviceLifeCycle);
        DeviceLifeCycleChangeEvent changeStateEvent = changeEvents.get(2);
        assertThat(changeStateEvent.getTimestamp()).isEqualTo(changeStateTimestamp);
        assertThat(changeStateEvent.getUser()).contains(this.user);
        assertThat(changeStateEvent.getType()).isEqualTo(DeviceLifeCycleChangeEvent.Type.STATE);
        assertThat(changeStateEvent.getState()).isEqualTo(newState);
    }

    private DeviceImpl getTestInstance() {
        DeviceImpl device =  new DeviceImpl(
                this.dataModel,
                this.eventService,
                this.issueService,
                this.thesaurus,
                this.clock,
                this.meteringService,
                this.validationService,
                this.securityPropertyService,
                this.scheduledConnectionTaskProvider,
                this.inboundConnectionTaskProvider,
                this.connectionInitiationTaskProvider,
                this.scheduledComTaskExecutionProvider,
                this.meteringGroupsService,
                customPropertySetService,
                this.readingTypeUtilService,
                this.threadPrincipalService,
                this.userPreferencesService,
                this.deviceConfigurationService, deviceService, lockService)
                .initialize(this.deviceConfiguration, "Hello world", Instant.now());
        device.save();
        return device;
    }

}
