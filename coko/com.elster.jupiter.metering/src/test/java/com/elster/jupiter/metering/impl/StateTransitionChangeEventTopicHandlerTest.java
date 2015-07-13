package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StateTransitionChangeEventTopicHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (08:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionChangeEventTopicHandlerTest {

    private static final long END_DEVICE_ID = 121;
    private static final String END_DEVICE_MRID = "MasterResourceIdentifier";
    private static final String MISSING_END_DEVICE_MRID = "DoesNotExist";

    @Mock
    private FiniteStateMachineService stateMachineService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private StateTransitionChangeEvent event;
    @Mock
    private State state;
    @Mock
    private ServerEndDevice endDevice;
    @Mock
    private Clock clock;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getSource()).thenReturn(this.event);
        when(this.event.getSourceId()).thenReturn(MISSING_END_DEVICE_MRID);
        when(this.event.getNewState()).thenReturn(this.state);
        when(this.meteringService.findEndDevice(MISSING_END_DEVICE_MRID)).thenReturn(Optional.<EndDevice>empty());
        when(this.meteringService.findEndDevice(END_DEVICE_MRID)).thenReturn(Optional.of(this.endDevice));
        when(this.endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(this.endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
    }

    @Test
    public void handlerAttemptsToFindTheEndDevice() {
        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
    }

    @Test
    public void handlerDelegatesToTheEndDeviceWithEffectiveTimestampFromEvent() {
        when(this.event.getSourceId()).thenReturn(END_DEVICE_MRID);
        Instant effective = Instant.ofEpochSecond(1000L);
        when(this.event.getEffectiveTimestamp()).thenReturn(effective);

        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.endDevice).changeState(this.state, effective);
        verify(this.event).getEffectiveTimestamp();
        verify(this.clock, never()).instant();
    }

    @Test
    public void handlerDelegatesToTheEndDeviceWithEffectiveTimestampFromClock() {
        Instant effective = Instant.ofEpochSecond(2000L);
        when(this.clock.instant()).thenReturn(effective);
        when(this.event.getSourceId()).thenReturn(END_DEVICE_MRID);

        // Business method
        this.getTestInstance().handle(this.localEvent);

        // Asserts
        verify(this.endDevice).changeState(this.state, effective);
        verify(this.event).getEffectiveTimestamp();
        verify(this.clock).instant();
    }

    private StateTransitionChangeEventTopicHandler getTestInstance() {
        return new StateTransitionChangeEventTopicHandler(this.clock, this.stateMachineService, this.meteringService);
    }

}