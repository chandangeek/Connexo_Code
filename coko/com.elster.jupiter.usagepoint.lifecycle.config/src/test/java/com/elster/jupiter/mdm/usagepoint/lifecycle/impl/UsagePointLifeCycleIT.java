package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;

import java.util.EnumSet;
import java.util.Set;
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

    @Test
    @Transactional
    public void testCanRemoveLifeCycle() {
        UsagePointLifeCycleService service = get(UsagePointLifeCycleService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        lifeCycle.remove();

        assertThat(service.findUsagePointLifeCycle(lifeCycle.getId())).isEmpty();
    }

    @Test
    @Transactional
    public void testCanCreateTransition() {
        UsagePointLifeCycleService service = get(UsagePointLifeCycleService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        UsagePointState from = lifeCycle.newState("From").complete();
        UsagePointState to = lifeCycle.newState("To").complete();

        UsagePointTransition transition = lifeCycle.newTransition("tr1", from, to)
                .withLevels(EnumSet.of(UsagePointTransition.Level.ONE, UsagePointTransition.Level.TWO))
                .withChecks(EnumSet.of(MicroCheck.Key.ALL_DATA_VALID))
                .withActions(EnumSet.of(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS))
                .complete();

        transition = service.findUsagePointTransition(transition.getId()).get();
        assertThat(transition.getName()).isEqualTo("tr1");
        assertThat(transition.getFrom()).isEqualTo(from);
        assertThat(transition.getTo()).isEqualTo(to);
        assertThat(transition.getTriggeredBy()).isEmpty();
        assertThat(transition.getLevels()).containsExactly(UsagePointTransition.Level.ONE, UsagePointTransition.Level.TWO);
        Set<MicroCheck> microChecks = transition.getChecks();
        assertThat(microChecks).hasSize(1);
        assertThat(microChecks.iterator().next().getKey()).isEqualTo(MicroCheck.Key.ALL_DATA_VALID);
        Set<MicroAction> microActions = transition.getActions();
        assertThat(microActions).hasSize(1);
        assertThat(microActions.iterator().next().getKey()).isEqualTo(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS);
    }
}
