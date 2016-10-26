package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointStateImplTest {
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private State delegate;
    @Mock
    private ProcessReference process;

    private UsagePointState getTestInstance() {
        return new UsagePointStateImpl(this.thesaurus).init(this.delegate);
    }

    @Test
    public void testIsInitialWhenDelegateIsInitial() {
        when(this.delegate.isInitial()).thenReturn(true);
        assertThat(getTestInstance().isInitial()).isTrue();
    }

    @Test
    public void testIsInitialWhenDelegateIsNotInitial() {
        when(this.delegate.isInitial()).thenReturn(false);
        assertThat(getTestInstance().isInitial()).isFalse();
    }

    @Test
    public void testCustomStateIsNotDefault() {
        when(this.delegate.isCustom()).thenReturn(true);
        assertThat(getTestInstance().getDefaultState()).isEmpty();
    }

    @Test
    public void testRenamedStateIsNotDefault() {
        when(this.delegate.isCustom()).thenReturn(false);
        when(this.delegate.getName()).thenReturn("Renamed");
        assertThat(getTestInstance().getDefaultState()).isEmpty();
    }

    @Test
    public void testStateIsDefault() {
        when(this.delegate.isCustom()).thenReturn(false);
        when(this.delegate.getName()).thenReturn(DefaultState.DEMOLISHED.getKey());
        UsagePointState state = getTestInstance();
        assertThat(state.getDefaultState()).isPresent();
        assertThat(state.getDefaultState().get().getKey()).isEqualTo(DefaultState.DEMOLISHED.getKey());
        assertThat(state.isDefault(DefaultState.DEMOLISHED)).isTrue();
    }

    @Test
    public void testGetVersion() {
        when(this.delegate.getVersion()).thenReturn(123L);
        assertThat(getTestInstance().getVersion()).isEqualTo(123L);
    }

    @Test
    public void testGetProcessesOnEntry() {
        when(this.delegate.getOnEntryProcesses()).thenReturn(Collections.singletonList(this.process));
        assertThat(getTestInstance().getOnEntryProcesses()).containsExactly(this.process);
    }

    @Test
    public void testGetProcessesOnExit() {
        when(this.delegate.getOnEntryProcesses()).thenReturn(Collections.singletonList(this.process));
        assertThat(getTestInstance().getOnEntryProcesses()).containsExactly(this.process);
    }
}
