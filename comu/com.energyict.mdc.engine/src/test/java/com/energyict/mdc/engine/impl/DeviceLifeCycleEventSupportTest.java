/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.events.DeviceTopologyChangedEvent;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceLifeCycleEventSupport} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-18 (10:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceLifeCycleEventSupportTest {

    private static final long FINITE_STATE_MACHINE_ID = 11L;
    private static final long STATE_ID = FINITE_STATE_MACHINE_ID + 1;
    private static final long DEVICE_ID = STATE_ID + 1;
    private static final long CONNECTION_TASK_ID = DEVICE_ID + 1;
    private static final long COMSERVER_ID = CONNECTION_TASK_ID + 1;
    private static final long COMPORT_ID = COMSERVER_ID + 1;
    private static final long COMSESSION_ID = COMPORT_ID + 1;
    private static final String STATE_NAME = "TheOneAndOnly";

    @Mock
    private DeviceService deviceService;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private State state;
    @Mock
    private FiniteStateMachine otherFiniteStateMachine;
    @Mock
    private State otherState;
    @Mock
    private Device device = mock(Device.class);
    @Mock
    private DeviceIdentifier<Device> deviceIdentifier;
    @Mock
    private ScheduledConnectionTask connectionTask;
    @Mock
    private ComPort comPort;
    @Mock
    private ComServer comServer;
    @Mock
    private ComSession comSession;

    @Before
    public void initializeMocks() {
        when(this.finiteStateMachine.getId()).thenReturn(FINITE_STATE_MACHINE_ID);
        when(this.state.getId()).thenReturn(STATE_ID);
        when(this.state.getFiniteStateMachine()).thenReturn(this.finiteStateMachine);
        when(this.state.getName()).thenReturn(STATE_NAME);
        when(this.finiteStateMachine.getInitialState()).thenReturn(this.state);
        when(this.finiteStateMachine.getStates()).thenReturn(Collections.singletonList(this.state));
        when(this.finiteStateMachine.getState(anyString())).thenReturn(Optional.empty());
        when(this.finiteStateMachine.getState(STATE_NAME)).thenReturn(Optional.of(this.state));
        when(this.otherFiniteStateMachine.getId()).thenReturn(FINITE_STATE_MACHINE_ID + 1);
        when(this.otherState.getId()).thenReturn(STATE_ID + 1);
        when(this.otherState.getFiniteStateMachine()).thenReturn(this.otherFiniteStateMachine);
        when(this.otherState.getName()).thenReturn(STATE_NAME);
        when(this.otherFiniteStateMachine.getInitialState()).thenReturn(this.otherState);
        when(this.otherFiniteStateMachine.getStates()).thenReturn(Collections.singletonList(this.otherState));
        when(this.otherFiniteStateMachine.getState(anyString())).thenReturn(Optional.empty());
        when(this.otherFiniteStateMachine.getState(STATE_NAME)).thenReturn(Optional.of(this.otherState));
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.device.getState()).thenReturn(this.state);
        when(this.deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.of(this.device));
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comServer.getName()).thenReturn("ComServer");
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.comPort.getName()).thenReturn("ComPort");
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        when(this.connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(this.connectionTask.getDevice()).thenReturn(this.device);
        when(this.comSession.getId()).thenReturn(COMSESSION_ID);
    }

    @Test
    public void isCandidateForAnUnknownEventType() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn("isCandidateForAnUnknownEventType");

        // Business method
        boolean isCandidate = this.getTestInstance().isCandidate(eventType);

        // Asserts
        assertThat(isCandidate).isFalse();
    }

    @Test
    public void isCandidateForAnKnownEventType() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_FAILURE.topic());

        // Business method
        boolean isCandidate = this.getTestInstance().isCandidate(eventType);

        // Asserts
        assertThat(isCandidate).isTrue();
    }

    @Test
    public void extractFromAnUnknownEventType() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn("extractFromAnUnknownEventType");
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceConnectionFailureEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_FAILURE.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ConnectionTaskCompletionEventInfo eventInfo =
                ConnectionTaskCompletionEventInfo
                        .forFailure(
                                this.connectionTask,
                                this.comPort,
                                this.comSession,
                                Collections.emptyList());
        when(localEvent.getSource()).thenReturn(eventInfo);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(DEVICE_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromDeviceConnectionFailureEventWithDeletedDevice() {
        when(this.deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_FAILURE.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ConnectionTaskCompletionEventInfo eventInfo =
                ConnectionTaskCompletionEventInfo
                        .forFailure(
                                this.connectionTask,
                                this.comPort,
                                this.comSession,
                                Collections.emptyList());
        when(localEvent.getSource()).thenReturn(eventInfo);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceConnectionFailureEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_FAILURE.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ConnectionTaskCompletionEventInfo eventInfo =
                ConnectionTaskCompletionEventInfo
                        .forFailure(
                                this.connectionTask,
                                this.comPort,
                                this.comSession,
                                Collections.emptyList());
        when(localEvent.getSource()).thenReturn(eventInfo);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceConnectionCompletionEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_COMPLETION.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ConnectionTaskCompletionEventInfo eventInfo =
                ConnectionTaskCompletionEventInfo
                        .forCompletion(
                                this.connectionTask,
                                this.comPort,
                                this.comSession,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList());
        when(localEvent.getSource()).thenReturn(eventInfo);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(DEVICE_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromDeviceConnectionCompletionEventWithDeletedDevice() {
        when(this.deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_COMPLETION.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ConnectionTaskCompletionEventInfo eventInfo =
                ConnectionTaskCompletionEventInfo
                        .forCompletion(
                                this.connectionTask,
                                this.comPort,
                                this.comSession,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList());
        when(localEvent.getSource()).thenReturn(eventInfo);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceConnectionCompletionEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CONNECTION_COMPLETION.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ConnectionTaskCompletionEventInfo eventInfo =
                ConnectionTaskCompletionEventInfo
                        .forCompletion(
                                this.connectionTask,
                                this.comPort,
                                this.comSession,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList());
        when(localEvent.getSource()).thenReturn(eventInfo);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceTopologyChangedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_TOPOLOGY_CHANGED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        DeviceTopologyChangedEvent topologyChangedEvent = new DeviceTopologyChangedEvent(this.deviceIdentifier, Collections.emptyList());
        when(localEvent.getSource()).thenReturn(topologyChangedEvent);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(DEVICE_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromDeviceTopologyChangedEventWithDeletedDevice() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_TOPOLOGY_CHANGED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(this.deviceIdentifier.findDevice()).thenReturn(null);
        DeviceTopologyChangedEvent topologyChangedEvent = new DeviceTopologyChangedEvent(this.deviceIdentifier, Collections.emptyList());
        when(localEvent.getSource()).thenReturn(topologyChangedEvent);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceTopologyChangedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_TOPOLOGY_CHANGED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        DeviceTopologyChangedEvent topologyChangedEvent = new DeviceTopologyChangedEvent(this.deviceIdentifier, Collections.emptyList());
        when(localEvent.getSource()).thenReturn(topologyChangedEvent);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    private DeviceLifeCycleEventSupport getTestInstance() {
        return new DeviceLifeCycleEventSupport(this.deviceService);
    }

}