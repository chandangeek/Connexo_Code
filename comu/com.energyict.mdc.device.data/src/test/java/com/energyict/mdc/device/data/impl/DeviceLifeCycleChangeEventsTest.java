package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.FirmwareComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;

import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
    private ValidationService validationService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private SecurityPropertyService securityPropertyService;
    @Mock
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    @Mock
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    @Mock
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    @Mock
    private Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private User user;
    @Mock
    private AmrSystem mdcAmrSystem;
    @Mock
    private Meter meter;
    @Mock
    private StateTimeline stateTimeline;

    @Before
    public void initializeMocks() {
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(this.mdcAmrSystem));
        when(this.mdcAmrSystem.findMeter("0")).thenReturn(Optional.of(this.meter));
        when(this.meter.getStateTimeline()).thenReturn(Optional.of(this.stateTimeline));
        when(this.stateTimeline.getSlices()).thenReturn(Collections.<StateTimeSlice>emptyList());
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
        when(this.deviceType.getDeviceLifeCycleChangeEvents()).thenReturn(Arrays.asList(initial));

        // Mock the state time line of the Kore meter
        StateTimeSlice initialState = mock(StateTimeSlice.class);
        when(initialState.getPeriod()).thenReturn(Range.atLeast(deviceCreationTimestamp));
        State state = mock(State.class);
        when(initialState.getState()).thenReturn(state);
        when(initialState.getUser()).thenReturn(Optional.of(this.user));
        when(this.stateTimeline.getSlices()).thenReturn(Arrays.<StateTimeSlice>asList(initialState));

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
        when(this.stateTimeline.getSlices()).thenReturn(Arrays.<StateTimeSlice>asList(initialStateTimeSlice, administrativeChangeState));

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
        when(this.stateTimeline.getSlices()).thenReturn(Arrays.<StateTimeSlice>asList(initialStateTimeSlice, changeState));

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
        return new DeviceImpl(
                this.dataModel,
                this.eventService,
                this.issueService,
                this.thesaurus,
                this.clock,
                this.meteringService,
                this.validationService,
                this.connectionTaskService,
                this.communicationTaskService,
                this.securityPropertyService,
                this.scheduledConnectionTaskProvider,
                this.inboundConnectionTaskProvider,
                this.connectionInitiationTaskProvider,
                this.scheduledComTaskExecutionProvider,
                this.protocolPluggableService,
                this.manuallyScheduledComTaskExecutionProvider,
                this.firmwareComTaskExecutionProvider,
                this.meteringGroupsService)
            .initialize(this.deviceConfiguration, "Hello world", "mRID");
    }

}