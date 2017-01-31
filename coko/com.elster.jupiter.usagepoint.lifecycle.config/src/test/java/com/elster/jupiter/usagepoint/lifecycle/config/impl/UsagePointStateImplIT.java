/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointStateImplIT extends BaseTestIT {

    private UsagePointLifeCycle getTestLifeCycle() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        String lifeCycleName = "Test";
        return service.findUsagePointLifeCycleByName(lifeCycleName).orElseGet(() -> service.newUsagePointLifeCycle(lifeCycleName));
    }

    @Test
    @Transactional
    public void testCanCreateNewInitialState() {
        UsagePointLifeCycle lifeCycle = getTestLifeCycle();
        String stateName = "New state";
        UsagePointState state = lifeCycle.newState(stateName).setStage(UsagePointStage.Key.OPERATIONAL).setInitial().complete();

        assertThat(state.getName()).isEqualTo(stateName);
        assertThat(state.isInitial()).isTrue();
    }

    @Test
    @Transactional
    public void testCanChangeDefaultStateName() {
        String stateName = "New state name";
        UsagePointState state = getTestLifeCycle().getStates().stream().filter(candidate -> candidate.isDefault(DefaultState.UNDER_CONSTRUCTION)).findFirst().get();
        state.startUpdate().setName(stateName).setInitial().complete();

        assertThat(state.getName()).isEqualTo(stateName);
        assertThat(state.isInitial()).isTrue();
        assertThat(state.isDefault(DefaultState.UNDER_CONSTRUCTION)).isFalse();
    }

    @Test
    @Transactional
    public void testCanChangeCustomStateName() {
        String stateName = "New state name";
        UsagePointState state = getTestLifeCycle().newState("Custom state").setStage(UsagePointStage.Key.OPERATIONAL).setInitial().complete();
        state.startUpdate().setName(stateName).complete();

        state = getTestLifeCycle().getStates().stream().filter(state::equals).findFirst().get();
        assertThat(state.getName()).isEqualTo(stateName);
        assertThat(state.isInitial()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "state.unique.name")
    public void testCanNotCreateStatesWithTheSameName() {
        getTestLifeCycle().newState("TestState").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        getTestLifeCycle().newState("TestState").setStage(UsagePointStage.Key.OPERATIONAL).complete();
    }

    @Test
    @Transactional
    public void testCanRemoveState() {
        UsagePointLifeCycle lifeCycle = getTestLifeCycle();
        int stateCount = lifeCycle.getStates().size();

        UsagePointState testState = lifeCycle.newState("TestState").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        assertThat(lifeCycle.getStates().size()).isEqualTo(stateCount + 1);
        testState = get(UsagePointLifeCycleConfigurationService.class).findAndLockUsagePointStateByIdAndVersion(testState.getId(), testState.getVersion()).get();

        testState.remove();
        assertThat(testState.getLifeCycle().getStates().size()).isEqualTo(stateCount);
    }

    @Test(expected = UsagePointStateRemoveException.class)
    @Transactional
    public void testCanNotRemoveInitialState() {
        UsagePointLifeCycle lifeCycle = getTestLifeCycle();
        int stateCount = lifeCycle.getStates().size();

        UsagePointState testState = lifeCycle.newState("TestState").setStage(UsagePointStage.Key.OPERATIONAL).setInitial().complete();
        assertThat(lifeCycle.getStates().size()).isEqualTo(stateCount + 1);

        testState.remove();
    }

    @Test
    @Transactional
    public void testAfterCloneLifeCycleWeHaveTheSameState() {
        UsagePointLifeCycle source = getTestLifeCycle();
        UsagePointState state = source.newState("State 1").setStage(UsagePointStage.Key.OPERATIONAL).setInitial().complete();

        UsagePointLifeCycle cloned = get(UsagePointLifeCycleConfigurationService.class).cloneUsagePointLifeCycle("Cloned", source);

        assertThat(cloned.getStates().size()).isEqualTo(source.getStates().size());
        Optional<UsagePointState> cloned1 = cloned.getStates().stream().filter(candidate -> candidate.getName().equals(state.getName())).findFirst();
        assertThat(cloned1).isPresent();
        assertThat(cloned1.get().getId()).isNotEqualTo(state.getId());
        assertThat(cloned1.get().isInitial()).isEqualTo(state.isInitial());
        assertThat(cloned1.get().getDefaultState()).isEqualTo(state.getDefaultState());
        assertThat(cloned1.get().getOnEntryProcesses()).containsExactlyElementsOf(state.getOnEntryProcesses());
        assertThat(cloned1.get().getOnExitProcesses()).containsExactlyElementsOf(state.getOnExitProcesses());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "stage", messageId = "{CanNotBeEmpty}")
    public void testCanNotCreateStatesWithoutStage() {
        getTestLifeCycle().newState("TestState").complete();
    }
}
