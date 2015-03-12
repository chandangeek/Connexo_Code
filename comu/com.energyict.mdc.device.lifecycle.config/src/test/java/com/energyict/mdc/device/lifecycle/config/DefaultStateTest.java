package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DefaultState} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:05)
 */
public class DefaultStateTest {

    @Test
    public void testAllDefaultStateHaveUniqueKeys () {
        Set<String> uniqueKeys = new HashSet<>();
        for (DefaultState messageSeed : DefaultState.values()) {
            assertThat(uniqueKeys)
                .as(messageSeed.name() + " does not have a unique key")
                .doesNotContain(messageSeed.getKey());
            uniqueKeys.add(messageSeed.getKey());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit () {
        for (DefaultState messageSeed : DefaultState.values()) {
            assertThat(messageSeed.getKey().length())
                .as(messageSeed.name() + " key is longer than max of 256")
                .isLessThanOrEqualTo(256);
        }
    }

    @Test
    public void fromCustomState() {
        State state = mock(State.class);
        when(state.isCustom()).thenReturn(true);

        // Business method
        Optional<DefaultState> defaultState = DefaultState.from(state);

        // Asserts
        assertThat(defaultState.isPresent()).isFalse();
    }

    @Test
    public void fromStandardStateWithNonStandardSymbolicName() {
        State state = mock(State.class);
        when(state.isCustom()).thenReturn(false);
        when(state.getName()).thenReturn("fromCustomStateWithNonStandardSymbolicName");

        // Business method
        Optional<DefaultState> defaultState = DefaultState.from(state);

        // Asserts
        assertThat(defaultState.isPresent()).isFalse();
    }

    @Test
    public void fromDefaultState() {
        for (DefaultState defaultState : DefaultState.values()) {
            State state = mock(State.class);
            when(state.isCustom()).thenReturn(false);
            when(state.getName()).thenReturn(defaultState.getKey());

            // Business method
            Optional<DefaultState> defaultStateOptional = DefaultState.from(state);

            // Asserts
            assertThat(defaultStateOptional.isPresent()).as(defaultState.name() + " not found by DefaultState#from(State)").isTrue();
            assertThat(defaultStateOptional.get()).isEqualTo(defaultState);
        }

    }

}