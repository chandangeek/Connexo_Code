package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;

import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TransitionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (11:25)
 */
public class TransitionTypeTest {

    @Test
    public void allTransitionTypesHaveAFrom() {
        for (TransitionType transitionType : TransitionType.values()) {
            assertThat(transitionType.getFrom()).as(transitionType.name() + " does not have a from").isNotNull();
        }
    }

    @Test
    public void allTransitionTypesHaveATo() {
        for (TransitionType transitionType : TransitionType.values()) {
            assertThat(transitionType.getTo()).as(transitionType.name() + " does not have a to").isNotNull();
        }
    }

    @Test
    public void testWithCustomStateTransition() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(true);
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(true);
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithCustomFromAndStandardTo() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(true);
        when(from.getName()).thenReturn("testWithCustomFromAndStandardTo");
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(false);
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithStandardFromAndCustomTo() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(false);
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(true);
        when(to.getName()).thenReturn("testWithStandardFromAndCustomTo");
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithStandardTransition() {
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.COMMISSIONING, TransitionType.COMMISSION);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.ACTIVE, TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.INACTIVE, TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING);
        this.testStandardTransition(DefaultState.COMMISSIONING, DefaultState.ACTIVE, TransitionType.INSTALL_AND_ACTIVATE);
        this.testStandardTransition(DefaultState.COMMISSIONING, DefaultState.INACTIVE, TransitionType.INSTALL_INACTIVE);
        this.testStandardTransition(DefaultState.ACTIVE, DefaultState.INACTIVE, TransitionType.DEACTIVATE);
        this.testStandardTransition(DefaultState.INACTIVE, DefaultState.ACTIVE, TransitionType.ACTIVATE);
        this.testStandardTransition(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED, TransitionType.DECOMMISSION);
        this.testStandardTransition(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED, TransitionType.DEACTIVATE_AND_DECOMMISSION);
        this.testStandardTransition(DefaultState.DECOMMISSIONED, DefaultState.DELETED, TransitionType.DELETE_FROM_DECOMMISSIONED);
        this.testStandardTransition(DefaultState.DECOMMISSIONED, DefaultState.IN_STOCK, TransitionType.RECYCLE);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.DELETED, TransitionType.DELETE_FROM_IN_STOCK);
    }

    private void testStandardTransition(DefaultState defaultFrom, DefaultState defaultTo, TransitionType expectedTransitionType) {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(false);
        when(from.getName()).thenReturn(defaultFrom.getKey());
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(false);
        when(to.getName()).thenReturn(defaultTo.getKey());
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent())
                .as("TransitionType#from for default state " + defaultFrom + " and " + defaultTo + " was not found")
                .isTrue();
        assertThat(transitionType.get()).isEqualTo(expectedTransitionType);
    }

}