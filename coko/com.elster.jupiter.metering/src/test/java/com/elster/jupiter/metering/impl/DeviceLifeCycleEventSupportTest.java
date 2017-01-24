package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;

import java.util.Arrays;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceLifeCycleEventSupport} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (11:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceLifeCycleEventSupportTest {

    private static final long FINITE_STATE_MACHINE_ID = 11L;
    private static final long STATE_ID = 121L;
    private static final long METER_ID = 212L;
    private static final String STATE_NAME = "TheOneAndOnly";

    @Mock
    private MeteringService meteringService;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private State state;
    @Mock
    private FiniteStateMachine otherFiniteStateMachine;
    @Mock
    private State otherState;
    @Mock
    private EndDevice endDevice = mock(EndDevice.class);
    @Mock
    private Meter meter = mock(Meter.class);

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
        when(this.endDevice.getId()).thenReturn(METER_ID);
        when(this.endDevice.getState()).thenReturn(Optional.of(this.state));
        when(this.meteringService.findEndDeviceById(METER_ID)).thenReturn(Optional.of(this.endDevice));
        when(this.endDevice.getAmrId()).thenReturn(String.valueOf(METER_ID));
        when(this.meter.getId()).thenReturn(METER_ID);
        when(this.meter.getState()).thenReturn(Optional.of(this.state));
        when(this.meter.getAmrId()).thenReturn(String.valueOf(METER_ID));
        when(this.meteringService.findMeterById(METER_ID)).thenReturn(Optional.of(this.meter));
    }

    @Test
    public void isCandidateForAnUnknownEventType() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("isCandidateForAnUnknownEventType");

        // Business method
        boolean isCandidate = this.getTestInstance().isCandidate(eventType);

        // Asserts
        assertThat(isCandidate).isFalse();
    }

    @Test
    public void isCandidateForAnKnownEventType() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_CREATED.topic());

        // Business method
        boolean isCandidate = this.getTestInstance().isCandidate(eventType);

        // Asserts
        assertThat(isCandidate).isTrue();
    }

    @Test
    public void extractFromAnUnknownEventType() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("extractFromAnUnknownEventType");
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromMeterCreatedEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(METER_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromMeterCreatedEventWithoutLifeCycle() {
        when(this.endDevice.getState()).thenReturn(Optional.empty());
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromMeterCreatedEventWithOtherLifeCycle() {
        when(this.endDevice.getState()).thenReturn(Optional.of(this.otherState));
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromMeterUpdatedEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(METER_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromMeterUpdatedEventWithoutLifeCycle() {
        when(this.endDevice.getState()).thenReturn(Optional.empty());
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromMeterUpdatedEventWithOtherLifeCycle() {
        when(this.endDevice.getState()).thenReturn(Optional.of(this.otherState));
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_UPDATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromMeterDeletedEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(METER_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromMeterDeletedEventWithoutLifeCycle() {
        when(this.endDevice.getState()).thenReturn(Optional.empty());
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromMeterDeletedEventWithOtherLifeCycle() {
        when(this.endDevice.getState()).thenReturn(Optional.of(this.otherState));
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.endDevice);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromReadingCreatedEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METERREADING_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        MeterReadingStorer.EventSource eventSource = mock(MeterReadingStorer.EventSource.class);
        when(eventSource.getMeterId()).thenReturn(METER_ID);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        verify(this.meteringService).findMeterById(METER_ID);
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(METER_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromReadingCreatedEventWithoutLifeCycle() {
        when(this.meter.getState()).thenReturn(Optional.empty());
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METERREADING_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        MeterReadingStorer.EventSource eventSource = mock(MeterReadingStorer.EventSource.class);
        when(eventSource.getMeterId()).thenReturn(METER_ID);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        verify(this.meteringService).findMeterById(METER_ID);
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromReadingCreatedEventWithOtherLifeCycle() {
        when(this.meter.getState()).thenReturn(Optional.of(this.otherState));
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METERREADING_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        MeterReadingStorer.EventSource eventSource = mock(MeterReadingStorer.EventSource.class);
        when(eventSource.getMeterId()).thenReturn(METER_ID);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        verify(this.meteringService).findMeterById(METER_ID);
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromReadingCreatedEventWhenMeterNoLongerExists() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METERREADING_CREATED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        MeterReadingStorer.EventSource eventSource = mock(MeterReadingStorer.EventSource.class);
        when(eventSource.getMeterId()).thenReturn(METER_ID);
        when(localEvent.getSource()).thenReturn(eventSource);
        when(this.meteringService.findMeterById(anyLong())).thenReturn(Optional.empty());

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromReadingDeletedEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(this.meter));
        Channel.ReadingsDeletedEvent eventSource = mock(Channel.ReadingsDeletedEvent.class);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(eventSource.getChannel()).thenReturn(channel);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isTrue();
        CurrentStateExtractor.CurrentState currentState = extracted.get();
        assertThat(currentState.sourceId).isEqualTo(String.valueOf(METER_ID));
        assertThat(currentState.sourceType).isNotEmpty();
        assertThat(currentState.name).isEqualTo(STATE_NAME);
    }

    @Test
    public void extractFromReadingDeletedEventWithoutLifeCycle() {
        when(this.meter.getState()).thenReturn(Optional.empty());
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(this.meter));
        Channel.ReadingsDeletedEvent eventSource = mock(Channel.ReadingsDeletedEvent.class);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(eventSource.getChannel()).thenReturn(channel);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromReadingDeletedEventWithOtherLifeCycle() {
        when(this.meter.getState()).thenReturn(Optional.of(this.otherState));
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(this.meter));
        Channel.ReadingsDeletedEvent eventSource = mock(Channel.ReadingsDeletedEvent.class);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(eventSource.getChannel()).thenReturn(channel);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    @Test
    public void extractFromReadingDeletedEventForNonMDCEnvironment() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_DELETED.topic());
        LocalEvent localEvent = mock(LocalEvent.class);
        when(localEvent.getType()).thenReturn(eventType);
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        Channel.ReadingsDeletedEvent eventSource = mock(Channel.ReadingsDeletedEvent.class);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(eventSource.getChannel()).thenReturn(channel);
        when(localEvent.getSource()).thenReturn(eventSource);

        // Business method
        Optional<CurrentStateExtractor.CurrentState> extracted = this.getTestInstance().extractFrom(localEvent, this.finiteStateMachine);

        // Asserts
        assertThat(extracted.isPresent()).isFalse();
    }

    private DeviceLifeCycleEventSupport getTestInstance() {
        return new DeviceLifeCycleEventSupport(this.meteringService);
    }

}
