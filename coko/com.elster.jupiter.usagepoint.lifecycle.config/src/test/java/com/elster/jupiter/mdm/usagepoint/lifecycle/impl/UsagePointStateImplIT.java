package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointStateImplIT extends BaseTestIT {

    private UsagePointLifeCycle getTestLifeCycle() {
        UsagePointLifeCycleService service = get(UsagePointLifeCycleService.class);
        String lifeCycleName = "Test";
        return service.findUsagePointLifeCycleByName(lifeCycleName).orElseGet(() -> service.newUsagePointLifeCycle(lifeCycleName));
    }

    @Test
    @Transactional
    public void testCanCreateNewInitialState() {
        UsagePointLifeCycle lifeCycle = getTestLifeCycle();
        String stateName = "New state";
        UsagePointState state = lifeCycle.newState(stateName).setInitial().complete();

        assertThat(state.getName()).isEqualTo(stateName);
        assertThat(state.isInitial()).isTrue();
    }

    @Test
    @Transactional
    public void testCanChangeDefaultStateName() {
        String stateName = "New state name";
        UsagePointState state = getTestLifeCycle().getStates().stream().filter(candidate -> candidate.isDefault(DefaultState.CONNECTED)).findFirst().get();
        state.startUpdate().setName(stateName).setInitial().complete();

        assertThat(state.getName()).isEqualTo(stateName);
        assertThat(state.isInitial()).isTrue();
        assertThat(state.isDefault(DefaultState.CONNECTED)).isFalse();
    }

    @Test
    @Transactional
    public void testCanChangeCustomStateName() {
        String stateName = "New state name";
        UsagePointState state = getTestLifeCycle().newState("Custom state").setInitial().complete();
        state.startUpdate().setName(stateName).complete();

        state = getTestLifeCycle().getStates().stream().filter(state::equals).findFirst().get();
        assertThat(state.getName()).isEqualTo(stateName);
        assertThat(state.isInitial()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "state.unique.name")
    public void testCanNotCreateStatesWithTheSameName() {
        getTestLifeCycle().newState("TestState").complete();
        getTestLifeCycle().newState("TestState").complete();
    }

    @Test
    @Transactional
    public void testCanRemoveState() {
        UsagePointLifeCycle lifeCycle = getTestLifeCycle();
        int stateCount = lifeCycle.getStates().size();

        UsagePointState testState = lifeCycle.newState("TestState").complete();
        assertThat(lifeCycle.getStates().size()).isEqualTo(stateCount + 1);

        testState.remove();
        assertThat(lifeCycle.getStates().size()).isEqualTo(stateCount);
    }

    @Test(expected = UsagePointStateRemoveException.class)
    @Transactional
    public void testCanNotRemoveInitialState() {
        UsagePointLifeCycle lifeCycle = getTestLifeCycle();
        int stateCount = lifeCycle.getStates().size();

        UsagePointState testState = lifeCycle.newState("TestState").setInitial().complete();
        assertThat(lifeCycle.getStates().size()).isEqualTo(stateCount + 1);

        testState.remove();
    }
}
