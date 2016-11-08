package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UsagePointLifeCycleServiceImplIT extends BaseTestIT {

    @Test
    public void testDefaultLifeCycleExists() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).getUsagePointLifeCycles().find().get(0);

        assertThat(lifeCycle.getName()).isEqualTo(TranslationKeys.LIFE_CYCLE_NAME.getDefaultFormat());

        Optional<UsagePointState> underConstruction = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.UNDER_CONSTRUCTION)).findFirst();
        Optional<UsagePointState> active = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.ACTIVE)).findFirst();
        Optional<UsagePointState> inactive = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.INACTIVE)).findFirst();
        Optional<UsagePointState> demolished = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.DEMOLISHED)).findFirst();
        assertThat(underConstruction).isPresent();
        assertThat(underConstruction.get().isInitial()).isTrue();
        assertThat(active).isPresent();
        assertThat(inactive).isPresent();
        assertThat(demolished).isPresent();

        Optional<UsagePointTransition> transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_INSTALL_ACTIVE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(underConstruction.get());
        assertThat(transition.get().getTo()).isEqualTo(active.get());

        transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_INSTALL_INACTIVE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(underConstruction.get());
        assertThat(transition.get().getTo()).isEqualTo(inactive.get());

        transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_DEACTIVATE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(active.get());
        assertThat(transition.get().getTo()).isEqualTo(inactive.get());

        transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_ACTIVATE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(inactive.get());
        assertThat(transition.get().getTo()).isEqualTo(active.get());

        transition = lifeCycle.getTransitions().stream()
                .filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_DEMOLISH_FROM_ACTIVE.getDefaultFormat()))
                .filter(tr -> tr.getFrom().equals(active.get()))
                .findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(active.get());

        transition = lifeCycle.getTransitions().stream()
                .filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_DEMOLISH_FROM_INACTIVE.getDefaultFormat()))
                .filter(tr -> tr.getFrom().equals(inactive.get()))
                .findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getTo()).isEqualTo(demolished.get());
    }
}
