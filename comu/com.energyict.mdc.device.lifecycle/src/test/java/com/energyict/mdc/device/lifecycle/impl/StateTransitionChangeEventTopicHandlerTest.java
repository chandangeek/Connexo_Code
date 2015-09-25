package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link StateTransitionChangeEventTopicHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-11 (11:34)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionChangeEventTopicHandlerTest {

    private static final long END_DEVICE_ID = 97L;
    private static final String END_DEVICE_MRID = "Master Resource Identifier";

    @Mock
    private FiniteStateMachineService stateMachineService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private Clock clock;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private StateTransitionChangeEvent event;
    @Mock
    private State oldState;
    @Mock
    private State newState;
    @Mock
    private EndDevice endDevice;
    @Mock
    private LifecycleDates lifecycleDates;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getSource()).thenReturn(this.event);
        when(this.event.getOldState()).thenReturn(this.oldState);
        when(this.event.getNewState()).thenReturn(this.newState);
        when(this.event.getSourceId()).thenReturn(END_DEVICE_MRID);
        when(this.meteringService.findEndDevice(END_DEVICE_MRID)).thenReturn(Optional.of(this.endDevice));
        when(this.endDevice.getLifecycleDates()).thenReturn(this.lifecycleDates);
    }

    @Test
    public void transitionToCustomStateIgnoresEvent() {
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(true);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService, never()).findEndDevice(anyString());
    }

    @Test
    public void transitionToActiveSetsInstalledDate() {
        Instant expectedInstalledDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.newState.getName()).thenReturn(DefaultState.ACTIVE.getKey());
        when(this.clock.instant()).thenReturn(expectedInstalledDate);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice).getLifecycleDates();
        verify(this.lifecycleDates).setInstalledDate(expectedInstalledDate);
        verify(this.endDevice).update();
    }

    @Test
    public void transitionToActiveForUnknownDeviceIgnoresEvent() {
        Instant expectedInstalledDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.newState.getName()).thenReturn(DefaultState.ACTIVE.getKey());
        when(this.clock.instant()).thenReturn(expectedInstalledDate);
        when(this.meteringService.findEndDevice(anyString())).thenReturn(Optional.empty());

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice, never()).getLifecycleDates();
        verifyNoMoreInteractions(this.lifecycleDates);
        verify(this.endDevice, never()).update();
    }

    @Test
    public void transitionFromActiveToInactiveSetsRemovedDate() {
        Instant expectedRemovedDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.oldState.getName()).thenReturn(DefaultState.ACTIVE.getKey());
        when(this.newState.getName()).thenReturn(DefaultState.INACTIVE.getKey());
        when(this.clock.instant()).thenReturn(expectedRemovedDate);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice).getLifecycleDates();
        verify(this.lifecycleDates).setRemovedDate(expectedRemovedDate);
        verify(this.endDevice).update();
    }

    @Test
    public void transitionFromCommissioningToInactiveIgnoresEvent() {
        Instant expectedRemovedDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.oldState.getName()).thenReturn(DefaultState.COMMISSIONING.getKey());
        when(this.newState.getName()).thenReturn(DefaultState.INACTIVE.getKey());
        when(this.clock.instant()).thenReturn(expectedRemovedDate);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice).getLifecycleDates();
        verify(this.lifecycleDates, never()).setRemovedDate(expectedRemovedDate);
        verify(this.endDevice, never()).update();
    }

    @Test
    public void transitionToInactiveForUnknownDeviceIgnoresEvent() {
        Instant expectedRemovedDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.newState.getName()).thenReturn(DefaultState.INACTIVE.getKey());
        when(this.clock.instant()).thenReturn(expectedRemovedDate);
        when(this.meteringService.findEndDevice(anyString())).thenReturn(Optional.empty());

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice, never()).getLifecycleDates();
        verifyNoMoreInteractions(this.lifecycleDates);
        verify(this.endDevice, never()).update();
    }

    @Test
    public void transitionToDecommissionedSetsRemovedDate() {
        Instant expectedRetiredDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.newState.getName()).thenReturn(DefaultState.DECOMMISSIONED.getKey());
        when(this.clock.instant()).thenReturn(expectedRetiredDate);

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice).getLifecycleDates();
        verify(this.lifecycleDates).setRetiredDate(expectedRetiredDate);
        verify(this.endDevice).update();
    }

    @Test
    public void transitionToDecommissionedForUnknownDeviceIgnoresEvent() {
        Instant expectedRemovedDate = Instant.ofEpochMilli(10000L);
        StateTransitionChangeEventTopicHandler handler = getTestInstance();
        when(this.newState.isCustom()).thenReturn(false);
        when(this.newState.getName()).thenReturn(DefaultState.DECOMMISSIONED.getKey());
        when(this.clock.instant()).thenReturn(expectedRemovedDate);
        when(this.meteringService.findEndDevice(anyString())).thenReturn(Optional.empty());

        // Business method
        handler.handle(this.localEvent);

        // Asserts
        verify(this.meteringService).findEndDevice(anyString());
        verify(this.endDevice, never()).getLifecycleDates();
        verifyNoMoreInteractions(this.lifecycleDates);
        verify(this.endDevice, never()).update();
    }

    private StateTransitionChangeEventTopicHandler getTestInstance() {
        return new StateTransitionChangeEventTopicHandler(this.stateMachineService, this.meteringService, this.clock);
    }

}