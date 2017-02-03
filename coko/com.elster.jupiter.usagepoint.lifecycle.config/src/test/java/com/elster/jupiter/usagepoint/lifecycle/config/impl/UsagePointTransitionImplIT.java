/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

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

    private com.elster.jupiter.events.EventType createSystemEventType(String eventTypeSymbol) {
        return get(EventService.class).buildEventTypeWithTopic("usage/point/lifecycle/" + eventTypeSymbol)
                .name(eventTypeSymbol)
                .component(UsagePointLifeCycleConfigurationService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldPublish()
                .enableForUseInStateMachines()
                .create();
    }

    @Before
    public void before() {
        this.lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test");
        this.state1 = this.lifeCycle.newState("State1").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        this.state2 = this.lifeCycle.newState("State2").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        this.state3 = this.lifeCycle.newState("State3").setStage(UsagePointStage.Key.OPERATIONAL).complete();
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
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .complete();

        assertThat(transition.getId()).isGreaterThan(0L);
        assertThat(transition.getActions().isEmpty()).isTrue();
        assertThat(transition.getChecks().iterator().next().getKey()).isEqualTo(TestMicroCheck.class.getSimpleName());
        assertThat(transition.getLevels().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void testCanCreateJustWithActions() {
        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .withLevels(null)
                .withChecks(null)
                .complete();

        assertThat(transition.getId()).isGreaterThan(0L);
        assertThat(transition.getChecks().isEmpty()).isTrue();
        assertThat(transition.getActions().iterator().next().getKey()).isEqualTo(TestMicroAction.class.getSimpleName());
        assertThat(transition.getLevels().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void testCanCreateWithTriggeredBy() {
        com.elster.jupiter.events.EventType eventType = createSystemEventType("TEST");
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);

        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .triggeredBy(triggeredBy)
                .complete();
        transition = get(UsagePointLifeCycleConfigurationService.class).findAndLockUsagePointTransitionByIdAndVersion(transition.getId(), transition.getVersion()).get();
        assertThat(transition.getTriggeredBy()).isPresent();
        assertThat(transition.getTriggeredBy().get().getEventType()).isEqualTo(eventType);
    }

    @Test
    @Transactional
    public void testCanCloneTransitionWithTriggeredBy() {
        com.elster.jupiter.events.EventType eventType = createSystemEventType("TEST");
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        UsagePointTransition transition = this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withLevels(EnumSet.of(UsagePointTransition.Level.TWO, UsagePointTransition.Level.FOUR))
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .triggeredBy(triggeredBy)
                .complete();

        UsagePointLifeCycle clonedLifeCycle = get(UsagePointLifeCycleConfigurationService.class).cloneUsagePointLifeCycle("Cloned", this.lifeCycle);
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
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .complete();

        UsagePointLifeCycle clonedLifeCycle = get(UsagePointLifeCycleConfigurationService.class).cloneUsagePointLifeCycle("Cloned", this.lifeCycle);
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

    @Test
    @Transactional
    public void testCanRemoveTransition() {
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .withLevels(EnumSet.of(UsagePointTransition.Level.FOUR))
                .complete();
        FiniteStateMachine stateMachine = ((UsagePointLifeCycleImpl) this.lifeCycle).getStateMachine();
        int transitionSize = stateMachine.getTransitions().size();
        StateTransitionEventType eventType = transition.getFsmTransition().getEventType();

        transition.remove();

        assertThat(stateMachine.getTransitions().size()).isEqualTo(transitionSize - 1);
        assertThat(this.lifeCycle.getTransitions().size()).isEqualTo(transitionSize - 1);
        assertThat(this.lifeCycle.getTransitions()).doesNotContain(transition);
        assertThat(get(FiniteStateMachineService.class).findCustomStateTransitionEventType(eventType.getSymbol())).isEmpty();
    }

    @Test
    @Transactional
    public void testCanRemoveTransitionWithTriggeredBy() {
        com.elster.jupiter.events.EventType eventType = createSystemEventType("TEST");
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withLevels(EnumSet.of(UsagePointTransition.Level.TWO, UsagePointTransition.Level.FOUR))
                .triggeredBy(triggeredBy)
                .complete();
        FiniteStateMachine stateMachine = ((UsagePointLifeCycleImpl) this.lifeCycle).getStateMachine();
        int transitionSize = stateMachine.getTransitions().size();

        transition.remove();

        assertThat(stateMachine.getTransitions().size()).isEqualTo(transitionSize - 1);
        assertThat(this.lifeCycle.getTransitions().size()).isEqualTo(transitionSize - 1);
        assertThat(this.lifeCycle.getTransitions()).doesNotContain(transition);
        assertThat(get(FiniteStateMachineService.class).findStandardStateTransitionEventType(eventType)).isPresent();
    }

    @Test
    @Transactional
    public void testCanSwitchFromStandardEventToCustom() {
        com.elster.jupiter.events.EventType eventType = createSystemEventType("TEST");
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .triggeredBy(triggeredBy)
                .complete();

        transition.startUpdate().triggeredBy(null).complete();

        assertThat(transition.getTriggeredBy()).isEmpty();
    }

    @Test
    @Transactional
    public void testCanSwitchFromCustomEventToStandard() {
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .complete();
        String symbol = transition.getFsmTransition().getEventType().getSymbol();

        com.elster.jupiter.events.EventType eventType = createSystemEventType("TEST");
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        transition.startUpdate().triggeredBy(triggeredBy).complete();

        assertThat(transition.getTriggeredBy()).isPresent();
        assertThat(get(FiniteStateMachineService.class).findCustomStateTransitionEventType(symbol)).isEmpty();
    }

    @Test
    @Transactional
    public void testCanSwitchFromStandardEventToAnotherStandard() {
        com.elster.jupiter.events.EventType eventType = createSystemEventType("TEST");
        StandardStateTransitionEventType triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .triggeredBy(triggeredBy)
                .complete();

        eventType = createSystemEventType("TEST2");
        triggeredBy = get(FiniteStateMachineService.class).newStandardStateTransitionEventType(eventType);
        transition.startUpdate().triggeredBy(triggeredBy).complete();

        assertThat(transition.getTriggeredBy()).isPresent();
        assertThat(transition.getTriggeredBy().get()).isEqualTo(triggeredBy);
    }

    @Test
    @Transactional
    public void testCanChangeTransitionAndPreserveEventType() {
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withLevels(EnumSet.of(UsagePointTransition.Level.THREE))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .complete();
        StateTransitionEventType eventType = transition.getFsmTransition().getEventType();

        transition.startUpdate()
                .withName("tr2")
                .to(this.state3)
                .withLevels(EnumSet.of(UsagePointTransition.Level.FOUR))
                .withActions(null)
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .complete();

        assertThat(transition.getName()).isEqualTo("tr2");
        assertThat(transition.getFrom()).isEqualTo(this.state1);
        assertThat(transition.getTo()).isEqualTo(this.state3);
        assertThat(transition.getLevels()).containsExactly(UsagePointTransition.Level.FOUR);
        assertThat(transition.getActions()).isEmpty();
        assertThat(transition.getChecks().stream().map(MicroCheck::getKey).collect(Collectors.toList())).containsExactly(TestMicroCheck.class.getSimpleName());
        assertThat(transition.getFsmTransition().getEventType()).isEqualTo(eventType);
    }

    @Test
    @Transactional
    public void testCanChangeTransitionFromState() {
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .complete();
        StateTransitionEventType eventType = transition.getFsmTransition().getEventType();

        transition.startUpdate().from(this.state3).complete();

        assertThat(transition.getFrom()).isEqualTo(this.state3);
        assertThat(transition.getFsmTransition().getEventType()).isNotEqualTo(eventType);
        assertThat(get(FiniteStateMachineService.class).findCustomStateTransitionEventType(eventType.getSymbol())).isEmpty();
        assertThat(this.lifeCycle.getTransitions().size()).isEqualTo(transition.getFsmTransition().getFrom().getFiniteStateMachine().getTransitions().size());
    }

    @Test
    @Transactional
    public void testTransitionUpdateDoesNotClearName() {
        UsagePointTransitionImpl transition = (UsagePointTransitionImpl) this.lifeCycle.newTransition("tr1", this.state1, this.state2)
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .complete();

        transition.startUpdate().complete();

        assertThat(transition.getName()).isEqualTo("tr1");
    }
}
