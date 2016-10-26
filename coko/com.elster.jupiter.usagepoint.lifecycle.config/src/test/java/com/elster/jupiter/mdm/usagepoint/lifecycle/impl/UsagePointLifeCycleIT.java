package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointLifeCycleIT extends BaseTestIT {

    @Test
    @Transactional
    public void testCanCreateSimpleLifeCycle() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");

        assertThat(lifeCycle.getId()).isGreaterThan(0L);
        assertThat(lifeCycle.getName()).isEqualTo("Test");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{finite.state.machine.unique.name}")
    public void testCanNotCreateLifeCycleWithTheSameNameTwice() {
        get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
        get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{CanNotBeEmpty}")
    public void testCanNotCreateLifeCycleWithEmptyName() {
        get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{FieldTooLong}")
    public void testCanNotCreateLifeCycleWithLongName() {
        String longName = IntStream.range(0, 80).mapToObj(i -> "a").collect(Collectors.joining(""));
        get(UsagePointLifeCycleService.class).newUsagePointLifeCycle(longName);
    }

    @Test
    @Transactional
    public void testCanCreateNewState() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
        String stateName = "StateTest";
        lifeCycle.newState(stateName).complete();
        UsagePointState usagePointState = get(UsagePointLifeCycleService.class).findUsagePointLifeCycle(lifeCycle.getId()).get()
                .getStates()
                .stream()
                .filter(state -> state.getName().equals(stateName))
                .findFirst()
                .get();

        assertThat(usagePointState.getName()).isEqualTo(stateName);
        assertThat(usagePointState.isInitial()).isFalse();
        assertThat(usagePointState.getDefaultState()).isEmpty();
        assertThat(usagePointState.getOnEntryProcesses()).isEmpty();
        assertThat(usagePointState.getOnExitProcesses()).isEmpty();
    }
}
