package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class UsagePointTransitionImplIT extends BaseTestIT {
    private UsagePointLifeCycle lifeCycle;
    private UsagePointState state1;
    private UsagePointState state2;
    private UsagePointState state3;

    @Before
    public void before() {
        this.lifeCycle = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
        this.state1 = this.lifeCycle.newState("State1").complete();
        this.state2 = this.lifeCycle.newState("State2").complete();
        this.state3 = this.lifeCycle.newState("State3").complete();
    }

    @Test
    @Transactional
    public void testCanCreateTransitionWithoutLevelsChecksAndActions() {
        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2).complete();

        assertThat(transition.getName()).isEqualTo("tr1");
        assertThat(transition.getFrom()).isEqualTo(this.state1);
        assertThat(transition.getTo()).isEqualTo(this.state2);
        assertThat(transition.getTriggeredBy()).isEmpty();
        assertThat(transition.getLevels().iterator().hasNext()).isFalse();
        assertThat(transition.getChecks().iterator().hasNext()).isFalse();
        assertThat(transition.getActions().iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE + "}", property = "name")
    public void testCanNotCreateWithTheSameNameAndFrom() {
        this.lifeCycle.newTransition("tr1", this.state1, this.state2).complete();
        this.lifeCycle.newTransition("tr1", this.state1, this.state3).complete();
    }

    @Test
    @Transactional
    public void testCanCreateWithTheSameName() {
        this.lifeCycle.newTransition("tr1", this.state1, this.state2).complete();
        this.lifeCycle.newTransition("tr1", this.state2, this.state3).complete();

        // assert no exception
    }

    @Test
    @Transactional
    public void testCanCreateWithTheSameFrom() {
        this.lifeCycle.newTransition("tr1", this.state1, this.state2).complete();
        this.lifeCycle.newTransition("tr2", this.state1, this.state3).complete();

        // assert no exception
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TRANSITION_FROM_AND_TO_ARE_THE_SAME + "}", property = "toState")
    public void testCanNotCreateWithTheSameFromAndTo() {
        this.lifeCycle.newTransition("tr1", this.state1, this.state1).complete();
    }

    @Test
    @Transactional
    public void testCanCreateJustWithChecks() {
        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withActions(null)
                .withLevels(null)
                .withChecks(EnumSet.of(MicroCheck.Key.ALL_DATA_VALID))
                .complete();

        assertThat(transition.getId()).isGreaterThan(0L);
        assertThat(transition.getActions().isEmpty()).isTrue();
        assertThat(transition.getChecks().iterator().next().getKey()).isEqualTo(MicroCheck.Key.ALL_DATA_VALID);
        assertThat(transition.getLevels().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void testCanCreateJustWithActions() {
        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withActions(EnumSet.of(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS))
                .withLevels(null)
                .withChecks(null)
                .complete();

        assertThat(transition.getId()).isGreaterThan(0L);
        assertThat(transition.getChecks().isEmpty()).isTrue();
        assertThat(transition.getActions().iterator().next().getKey()).isEqualTo(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS);
        assertThat(transition.getLevels().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void testCanCreateWithTriggeredBy() {
        com.elster.jupiter.events.EventType eventType = get(EventService.class).buildEventTypeWithTopic("usage/point/lifecycle/test")
                .name("TEST")
                .component(UsagePointLifeCycleService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldPublish()
                .enableForUseInStateMachines()
                .create();
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);

        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .triggeredBy(triggeredBy)
                .complete();
        transition = get(UsagePointLifeCycleService.class).findAndLockUsagePointTransitionByIdAndVersion(transition.getId(), transition.getVersion()).get();
        assertThat(transition.getTriggeredBy()).isPresent();
        assertThat(transition.getTriggeredBy().get().getEventType()).isEqualTo(eventType);
    }

    @Test
    @Transactional
    public void testCanCloneTransitionWithTriggeredBy() {
        com.elster.jupiter.events.EventType eventType = get(EventService.class).buildEventTypeWithTopic("usage/point/lifecycle/test")
                .name("TEST")
                .component(UsagePointLifeCycleService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldPublish()
                .enableForUseInStateMachines()
                .create();
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withLevels(EnumSet.of(UsagePointTransition.Level.TWO, UsagePointTransition.Level.FOUR))
                .withChecks(EnumSet.of(MicroCheck.Key.ALL_DATA_VALID))
                .withActions(EnumSet.of(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS))
                .triggeredBy(triggeredBy)
                .complete();

        UsagePointLifeCycle clonedLifeCycle = get(UsagePointLifeCycleService.class).cloneUsagePointLifeCycle("Cloned", this.lifeCycle);
        UsagePointTransition cloned = clonedLifeCycle.getTransitions().stream().filter(candidate -> candidate.getName().equals(transition.getName())).findFirst().get();
        assertThat(cloned.getId()).isNotEqualTo(transition.getId());
        assertThat(cloned.getLevels()).containsExactlyElementsOf(transition.getLevels());
        assertThat(cloned.getActions()).containsExactlyElementsOf(transition.getActions());
        assertThat(cloned.getChecks()).containsExactlyElementsOf(transition.getChecks());
        assertThat(cloned.getFrom().getName()).isEqualTo(transition.getFrom().getName());
        assertThat(cloned.getTo().getName()).isEqualTo(transition.getTo().getName());
        assertThat(cloned.getTriggeredBy()).isEqualTo(transition.getTriggeredBy());
    }

    @Test
    @Transactional
    public void testCanCloneCustomTransition() {
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withLevels(EnumSet.of(UsagePointTransition.Level.TWO, UsagePointTransition.Level.FOUR))
                .withChecks(EnumSet.of(MicroCheck.Key.ALL_DATA_VALID))
                .withActions(EnumSet.of(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS))
                .complete();

        UsagePointLifeCycle clonedLifeCycle = get(UsagePointLifeCycleService.class).cloneUsagePointLifeCycle("Cloned", this.lifeCycle);
        UsagePointTransitionImpl cloned = (UsagePointTransitionImpl) clonedLifeCycle.getTransitions().stream()
                .filter(candidate -> candidate.getName().equals(transition.getName())).findFirst().get();
        assertThat(cloned.getId()).isNotEqualTo(transition.getId());
        assertThat(cloned.getLevels()).containsExactlyElementsOf(transition.getLevels());
        assertThat(cloned.getActions()).containsExactlyElementsOf(transition.getActions());
        assertThat(cloned.getChecks()).containsExactlyElementsOf(transition.getChecks());
        assertThat(cloned.getFrom().getName()).isEqualTo(transition.getFrom().getName());
        assertThat(cloned.getTo().getName()).isEqualTo(transition.getTo().getName());
        assertThat(cloned.getTriggeredBy()).isEqualTo(transition.getTriggeredBy());
        assertThat(cloned.getFsmTransition().getId()).isNotEqualTo(transition.getFsmTransition().getId());
        assertThat(cloned.getFsmTransition().getEventType().getSymbol()).isNotEqualTo(transition.getFsmTransition().getEventType().getSymbol());

    }
}
