package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointLifeCycleIT extends BaseTestIT {

    @Test
    @Transactional
    public void testCanCreateSimpleLifeCycle() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test");

        assertThat(lifeCycle.getId()).isGreaterThan(0L);
        assertThat(lifeCycle.getName()).isEqualTo("Test");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{finite.state.machine.unique.name}")
    public void testCanNotCreateLifeCycleWithTheSameNameTwice() {
        get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test");
        get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{CanNotBeEmpty}")
    public void testCanNotCreateLifeCycleWithEmptyName() {
        get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{CanNotBeEmpty}")
    public void testCanNotCreateLifeCycleWithNullName() {
        get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle(null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{FieldTooLong}")
    public void testCanNotCreateLifeCycleWithLongName() {
        String longName = IntStream.range(0, 80).mapToObj(i -> "a").collect(Collectors.joining(""));
        get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle(longName);
    }

    @Test
    @Transactional
    public void testCanCreateNewState() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test");
        String stateName = "StateTest";
        lifeCycle.newState(stateName).setStage(UsagePointStage.Key.OPERATIONAL).complete();
        UsagePointState usagePointState = get(UsagePointLifeCycleConfigurationService.class).findUsagePointLifeCycle(lifeCycle.getId()).get()
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
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        lifeCycle.remove();

        assertThat(service.findUsagePointLifeCycle(lifeCycle.getId())).isEmpty();
    }

    @Test(expected = UsagePointLifeCycleRemoveException.class)
    @Transactional
    public void testCanNotRemoveDefaultLifeCycle() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        lifeCycle.markAsDefault();
        lifeCycle.remove();
    }

    @Test
    @Transactional
    public void testCanCreateTransition() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        UsagePointState from = lifeCycle.newState("From").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        UsagePointState to = lifeCycle.newState("To").setStage(UsagePointStage.Key.OPERATIONAL).complete();

        UsagePointTransition transition = lifeCycle.newTransition("tr1", from, to)
                .withLevels(EnumSet.of(UsagePointTransition.Level.ONE, UsagePointTransition.Level.TWO))
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .complete();

        transition = service.findUsagePointTransition(transition.getId()).get();
        assertThat(transition.getName()).isEqualTo("tr1");
        assertThat(transition.getFrom()).isEqualTo(from);
        assertThat(transition.getTo()).isEqualTo(to);
        assertThat(transition.getTriggeredBy()).isEmpty();
        assertThat(transition.getLevels()).containsExactly(UsagePointTransition.Level.ONE, UsagePointTransition.Level.TWO);
        Set<MicroCheck> microChecks = transition.getChecks();
        assertThat(microChecks).hasSize(1);
        assertThat(microChecks.iterator().next().getKey()).isEqualTo(TestMicroCheck.class.getSimpleName());
        Set<MicroAction> microActions = transition.getActions();
        assertThat(microActions).hasSize(1);
        assertThat(microActions.iterator().next().getKey()).isEqualTo(TestMicroAction.class.getSimpleName());
    }

    @Test
    @Transactional
    public void testCanCloneLifeCycle() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycleImpl source = (UsagePointLifeCycleImpl) service.newUsagePointLifeCycle("Test");
        UsagePointState state1 = source.newState("State 1").setStage(UsagePointStage.Key.OPERATIONAL).setInitial().complete();
        UsagePointState state2 = source.newState("State 2").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        source.newTransition("tr1", state1, state2).complete();

        UsagePointLifeCycleImpl clone = (UsagePointLifeCycleImpl) service.cloneUsagePointLifeCycle("Clone", source);
        assertThat(clone.getName()).isEqualTo("Clone");
        assertThat(clone.getStates().size()).isEqualTo(source.getStates().size());
        assertThat(clone.getTransitions().size()).isEqualTo(source.getTransitions().size());
        assertThat(clone.getStateMachine()).isNotEqualTo(source.getStateMachine());
        assertThat(clone.getStateMachine().getName()).isEqualTo("Clone");
        assertThat(clone.getStateMachine().getStates().size()).isEqualTo(source.getStateMachine().getStates().size());
        assertThat(clone.getStateMachine().getTransitions().size()).isEqualTo(source.getStateMachine().getTransitions().size());
    }

    @Test
    @Transactional
    public void testCanMarkLifeCycleAsDefault() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        assertThat(lifeCycle.isDefault()).isFalse();
        lifeCycle.markAsDefault();

        lifeCycle = service.findUsagePointLifeCycle(lifeCycle.getId()).get();
        assertThat(lifeCycle.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testCanMarkLifeCycleAsDefaultWhenAlreadyHaveMarked() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        lifeCycle.markAsDefault();
        assertThat(lifeCycle.isDefault()).isTrue();

        UsagePointLifeCycle lifeCycle2 = service.newUsagePointLifeCycle("Test 2");
        lifeCycle2.markAsDefault();

        lifeCycle = service.findUsagePointLifeCycle(lifeCycle.getId()).get();
        assertThat(lifeCycle.isDefault()).isFalse();
        lifeCycle2 = service.findUsagePointLifeCycle(lifeCycle2.getId()).get();
        assertThat(lifeCycle2.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testCanCreateLifeCycleWithNameOfDeletedLifeCycle() {
        UsagePointLifeCycleConfigurationService service = get(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = service.newUsagePointLifeCycle("Test");
        lifeCycle.remove();
        long id = lifeCycle.getId();

        lifeCycle = service.newUsagePointLifeCycle("Test");

        // assert no exception
        assertThat(lifeCycle.getId()).isNotEqualTo(id);
    }
}
