/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceLifeCycleEventSupport} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (16:36)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceLifeCycleEventSupportTest {

    private static final long FINITE_STATE_MACHINE_ID = 11L;
    private static final long STATE_ID = 121L;
    private static final long DEVICE_ID = 700L;
    private static final String DEVICE_NAME = "Name";
    private static final String STATE_NAME = "TheOneAndOnly";

    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private State state;
    @Mock
    private FiniteStateMachine otherFiniteStateMachine;
    @Mock
    private State otherState;
    @Mock
    private Device device;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private ScheduledConnectionTask connectionTask;
    @Mock
    private ProtocolDialectProperties protocolDialectProperties;
    @Mock
    private DeviceMessage<Device> deviceMessage;

    @Before
    public void initializeMocks() {
        when(this.finiteStateMachine.getId()).thenReturn(FINITE_STATE_MACHINE_ID);
        when(this.state.getId()).thenReturn(STATE_ID);
        when(this.state.getFiniteStateMachine()).thenReturn(this.finiteStateMachine);
        when(this.state.getName()).thenReturn(STATE_NAME);
        when(this.finiteStateMachine.getInitialState()).thenReturn(this.state);
        when(this.finiteStateMachine.getStates()).thenReturn(Arrays.asList(this.state));
        when(this.finiteStateMachine.getState(anyString())).thenReturn(Optional.empty());
        when(this.finiteStateMachine.getState(STATE_NAME)).thenReturn(Optional.of(this.state));
        when(this.otherFiniteStateMachine.getId()).thenReturn(FINITE_STATE_MACHINE_ID + 1);
        when(this.otherState.getId()).thenReturn(STATE_ID + 1);
        when(this.otherState.getFiniteStateMachine()).thenReturn(this.otherFiniteStateMachine);
        when(this.otherState.getName()).thenReturn(STATE_NAME);
        when(this.otherFiniteStateMachine.getInitialState()).thenReturn(this.otherState);
        when(this.otherFiniteStateMachine.getStates()).thenReturn(Arrays.asList(this.otherState));
        when(this.otherFiniteStateMachine.getState(anyString())).thenReturn(Optional.empty());
        when(this.otherFiniteStateMachine.getState(STATE_NAME)).thenReturn(Optional.of(this.otherState));
        when(this.device.getName()).thenReturn(DEVICE_NAME);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.device.getState()).thenReturn(this.state);
        when(this.comTaskExecution.getDevice()).thenReturn(this.device);
        when(this.protocolDialectProperties.getDevice()).thenReturn(this.device);
        when(this.deviceMessage.getDevice()).thenReturn(this.device);
        when(this.connectionTask.getDevice()).thenReturn(this.device);
        when(this.protocolDialectProperties.getDevice()).thenReturn(this.device);
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
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_CREATED.topic());

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
    public void extractFromComTaskExecutionCreatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskExecution);

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
    public void extractFromComTaskExecutionCreatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskExecution);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromComTaskExecutionUpdatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskExecution);

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
    public void extractFromComTaskExecutionUpdatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskExecution);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromComTaskExecutionDeletedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskExecution);

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
    public void extractFromComTaskExecutionDeletedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.COMTASKEXECUTION_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskExecution);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromConnectionTaskCreatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.CONNECTIONTASK_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.connectionTask);

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
    public void extractFromConnectionTaskCreatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.CONNECTIONTASK_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.connectionTask);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromConnectionTaskUpdatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.CONNECTIONTASK_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.connectionTask);

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
    public void extractFromConnectionTaskUpdatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.CONNECTIONTASK_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.connectionTask);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromConnectionTaskDeletedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.CONNECTIONTASK_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.connectionTask);

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
    public void extractFromConnectionTaskDeletedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.CONNECTIONTASK_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.connectionTask);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceMessageCreatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICEMESSAGE_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.deviceMessage);

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
    public void extractFromDeviceMessageCreatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICEMESSAGE_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.deviceMessage);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceMessageUpdatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICEMESSAGE_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.deviceMessage);

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
    public void extractFromDeviceMessageUpdatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICEMESSAGE_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.deviceMessage);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceMessageDeletedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICEMESSAGE_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.deviceMessage);

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
    public void extractFromDeviceMessageDeletedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICEMESSAGE_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.deviceMessage);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceCreatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.device);

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
    public void extractFromDeviceCreatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.device);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceUpdatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.device);

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
    public void extractFromDeviceUpdatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.device);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromDeviceDeletedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.device);

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
    public void extractFromDeviceDeletedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.DEVICE_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.device);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromProtocoldialectpropertiesCreatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PROTOCOLDIALECTPROPERTIES_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.protocolDialectProperties);

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
    public void extractFromProtocoldialectpropertiesCreatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PROTOCOLDIALECTPROPERTIES_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.protocolDialectProperties);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromProtocoldialectpropertiesUpdatedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PROTOCOLDIALECTPROPERTIES_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.protocolDialectProperties);

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
    public void extractFromProtocoldialectpropertiesUpdatedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PROTOCOLDIALECTPROPERTIES_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.protocolDialectProperties);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromProtocoldialectpropertiesDeletedEvent() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PROTOCOLDIALECTPROPERTIES_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.protocolDialectProperties);

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
    public void extractFromProtocoldialectpropertiesDeletedEventWithOtherLifeCycle() {
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PROTOCOLDIALECTPROPERTIES_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.protocolDialectProperties);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.otherFiniteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    private DeviceLifeCycleEventSupport getTestInstance() {
        return new DeviceLifeCycleEventSupport();
    }


}