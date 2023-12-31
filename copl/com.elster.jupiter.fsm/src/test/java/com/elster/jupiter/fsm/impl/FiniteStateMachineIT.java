/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.*;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.base.Strings;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link FiniteStateMachineImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (17:12)
 */
public class FiniteStateMachineIT {

    private static InMemoryPersistence inMemoryPersistence;
    private static BpmProcessDefinition onEntry;
    private static BpmProcessDefinition onExit;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(FiniteStateMachineIT.class.getSimpleName());
        try (TransactionContext context = getTransactionService().getContext()) {
            onEntry = inMemoryPersistence.getBpmService()
                    .newProcessBuilder()
                    .setProcessName("oneEntry")
                    .setAssociation("device")
                    .setVersion("1.0")
                    .setAppKey("MDC")
                    .setStatus("ACTIVE")
                    .create();
            onExit = inMemoryPersistence.getBpmService()
                    .newProcessBuilder()
                    .setProcessName("onExit")
                    .setAssociation("device")
                    .setVersion("1.0")
                    .setAppKey("MDC")
                    .setStatus("ACTIVE")
                    .create();
            context.commit();
        }
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void stateMachineCannotHaveANullName() {
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(null);

        // Business method
        builder.complete(builder.newCustomState("Just one").complete());

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void stateMachineCannotHaveAnEmptyName() {
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine("");

        // Business method
        builder.complete(builder.newCustomState("Just one").complete());

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void stateMachineCannotHaveAnExtremelyLongName() {
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(Strings.repeat("Too long", 100));

        // Business method
        builder.complete(builder.newCustomState("Just one").complete());

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_STATE + "}", strict = false)
    @Test
    public void stateMachineMustHaveAtLeastOneState() {
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine("stateMachineMustHaveExactlyOnInitialState");
        State state = builder.newCustomState("SingleButNotInitial").complete();

        FiniteStateMachine finiteStateMachine = builder.complete(state);

        finiteStateMachine.startUpdate()
                .removeState(state)
                .complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.EXACTLY_ONE_INITIAL_STATE + "}", strict = false)
    @Test
    public void stateMachineMustHaveExactlyOnInitialState() {
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine("stateMachineMustHaveExactlyOnInitialState");
        State initialState = builder.newCustomState("Initial").complete();
        State finalState = builder.newCustomState("Final").complete();

        FiniteStateMachine finiteStateMachine = builder
                .complete(initialState);

        finiteStateMachine.startUpdate()
                .removeState(initialState)
                .complete();
    }


    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNIQUE_FINITE_STATE_MACHINE_NAME + "}")
    @Test
    public void createDuplicateStateMachine() {
        String expectedName = "notUnique";
        FiniteStateMachineBuilder builder1 = this.getTestService().newFiniteStateMachine(expectedName);
        FiniteStateMachine stateMachine = builder1.complete(builder1.newCustomState("Initial").complete());

        FiniteStateMachineBuilder builder2 = this.getTestService().newFiniteStateMachine(expectedName);

        // Business method
        builder2.complete(builder2.newCustomState("Another initial").complete());

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateButNoProcesses() {
        String expectedName = "stateMachineWithOneStateButNoProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";

        // Business method
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(expectedStateName).complete());

        // Asserts
        assertThat(stateMachine.getId()).isGreaterThan(0);
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getCreationTimestamp()).isNotNull();
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        assertThat(state.isInitial()).isTrue();
        assertThat(stateMachine.getInitialState()).isNotNull();
        assertThat(stateMachine.getInitialState().getId()).isEqualTo(state.getId());
    }

    @Transactional
    @Test
    public void findStateMachineById() {
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine("findStateMachineById");
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Initial").complete());

        // Business method
        Optional<FiniteStateMachine> found = this.getTestService().findFiniteStateMachineById(stateMachine.getId());

        // Asserts
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(stateMachine.getId());
    }

    @Transactional
    @Test
    public void findNonExistingStateMachineById() {
        // Business method
        Optional<FiniteStateMachine> found = this.getTestService().findFiniteStateMachineById(Long.MAX_VALUE);

        // Asserts
        assertThat(found).isEmpty();
    }

    @Transactional
    @Test
    public void findStateMachineByName() {
        String expectedName = "findStateMachineByName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Initial").complete());

        // Business method
        Optional<FiniteStateMachine> found = this.getTestService().findFiniteStateMachineByName(expectedName);

        // Asserts
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(expectedName);
        assertThat(found.get().getId()).isEqualTo(stateMachine.getId());
    }

    @Transactional
    @Test
    public void findNonExistingStateMachineByName() {
        // Business method
        Optional<FiniteStateMachine> found = this.getTestService().findFiniteStateMachineByName("findNonExistingStateMachineByName");

        // Asserts
        assertThat(found).isEmpty();
    }

    @Transactional
    @Test
    public void createTwoStateMachinesWithTheSameStateName() {
        FiniteStateMachineServiceImpl service = this.getTestService();
        String sameStateName = "Initial";
        FiniteStateMachineBuilder builder1 = service.newFiniteStateMachine("First");
        FiniteStateMachineBuilder builder2 = service.newFiniteStateMachine("Second");

        // Business method
        FiniteStateMachine first  = builder1.complete(builder1.newCustomState(sameStateName).complete());
        FiniteStateMachine second = builder2.complete(builder2.newCustomState(sameStateName).complete());

        // Asserts
        assertThat(first.getState(sameStateName).isPresent()).isTrue();
        assertThat(second.getState(sameStateName).isPresent()).isTrue();
    }

    @Transactional
    @Test
    public void findStateThatExists() {
        String expectedName = "findStateThatExists";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(expectedStateName).complete());

        // Business method
        Optional<State> found = stateMachine.getState(expectedStateName);

        // Asserts
        assertThat(found).isNotNull();
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getCreationTimestamp()).isNotNull();
    }

    @Transactional
    @Test
    public void findStateThatDoesNotExist() {
        String expectedName = "findStateThatDoesNotExist";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Initial").complete());

        // Business method
        Optional<State> found = stateMachine.getState("Does not exist");

        // Asserts
        assertThat(found).isNotNull();
        assertThat(found.isPresent()).isFalse();
    }

    @Transactional
    @Test
    public void findCreatedStateMachineWithOneCustomStateButNoProcesses() {
        String expectedName = "findCreatedStateMachineWithOneCustomStateButNoProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(expectedStateName).complete());
        long id = stateMachine.getId();

        // Business method
        FiniteStateMachine reloaded = getTestService().findFiniteStateMachineByName(stateMachine.getName()).get();

        // Asserts
        assertThat(reloaded.getId()).isEqualTo(id);
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(1);
        State state = reloaded.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        assertThat(state.isCustom()).isTrue();
    }

    @Transactional
    @Test
    public void findCreatedStateMachineWithOneStandardStateButNoProcesses() {
        String expectedName = "findCreatedStateMachineWithOneStandardStateButNoProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        FiniteStateMachine stateMachine = builder.complete(builder.newStandardState(expectedStateName).complete());

        // Business method
        FiniteStateMachine reloaded = getTestService().findFiniteStateMachineByName(stateMachine.getName()).get();

        // Asserts
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(1);
        State state = reloaded.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        assertThat(state.isCustom()).isFalse();
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateAndBothEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndBothEntryAndExitProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        State initial = builder
                .newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();

        // Business method
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
    }

    @Transactional
    @Test
    public void findStateMachineWithOneStateAndBothEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndBothEntryAndExitProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        State initial = builder
                .newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(stateMachine.getName()).get();

        // Asserts
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(1);
        State state = reloaded.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateAndMultipleEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndMultipleEntryAndExitProcesses";
        FiniteStateMachineServiceImpl testService = this.getTestService();
        BpmProcessDefinition onEntry2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onEntry2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        BpmProcessDefinition onExit2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onExit2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        FiniteStateMachineBuilder builder = testService.newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        State initial = builder
                .newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onEntry(onEntry2)
                .onExit(onExit)
                .onExit(onExit2)
                .complete();

        // Business method
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(2);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        assertThat(onEntryProcesses.get(1).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry2.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(2);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
        assertThat(onExitProcesses.get(1).getStateChangeBusinessProcess().getId()).isEqualTo(onExit2.getId());
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void createStateMachineWithOneStateWithNullName() {
        String expectedName = "createStateMachineWithOneStateWithNullName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);

        // Business method
        builder.complete(builder.newCustomState(null).complete());

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void createStateMachineWithOneStateWithEmptyName() {
        String expectedName = "createStateMachineWithOneStateWithEmptyName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);

        // Business method
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("").complete());

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_STATE_NAME, strict = false)
    @Test
    public void createStateMachineWithTwiceTheSameStateName() {
        String expectedName = "createStateMachineWithTwiceTheSameStateName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder.newCustomState("Initial").complete();
        String sameName = "State";
        builder.newCustomState(sameName).complete();
        FiniteStateMachine stateMachine = builder.complete(initial);
        FiniteStateMachineUpdater updater = stateMachine.startUpdate();
        updater.newCustomState(sameName).complete();

        // Business method
        stateMachine = updater.complete();

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @Test
    public void addStateWithTheNameOfRemovedState() {
        String expectedName = "addStateWithTheNameOfRemovedState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder.newCustomState("Initial").complete();
        builder.newCustomState("State").complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        stateMachine.startUpdate().removeState("State").complete();
        FiniteStateMachineUpdater updater = stateMachine.startUpdate();
        updater.newCustomState("State").complete();
        updater.complete();

        assertThat(stateMachine.getStates()).hasSize(2);
        assertThat(stateMachine.getStates().get(0).getName()).isEqualTo("Initial");
        assertThat(stateMachine.getStates().get(1).getName()).isEqualTo("State");
    }

    @Transactional
    @Test(expected = IllegalStateException.class)
    public void addStateAfterCompletion() {
        String expectedName = "completeTwice";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State state = builder.newCustomState("Before completion").complete();
        builder.complete(state);

        // Business method
        builder.newCustomState("After completion");

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateTransition() {
        String expectedName = "createStateMachineWithOneStateTransition";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();

        // Business method
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(2);
        List<StateTransition> transitions = stateMachine.getTransitions();
        assertThat(transitions).hasSize(1);
        StateTransition stateTransition = transitions.get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(commissionedEventType.getId());
        assertThat(stateTransition.getFrom().getId()).isEqualTo(inStock.getId());
        assertThat(stateTransition.getTo().getId()).isEqualTo(commissioned.getId());
    }

    @Transactional
    @Test
    public void createStateMachineWithOneNamedStateTransition() {
        String expectedName = "createStateMachineWithOneNamedStateTransition";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        String expectedTransitionName = "Commission";
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned, expectedTransitionName).complete();

        // Business method
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(2);
        List<StateTransition> transitions = stateMachine.getTransitions();
        assertThat(transitions).hasSize(1);
        StateTransition stateTransition = transitions.get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(commissionedEventType.getId());
        assertThat(stateTransition.getName().isPresent()).isTrue();
        assertThat(stateTransition.getName().get()).isEqualTo(expectedTransitionName);
        assertThat(stateTransition.getTranslationKey().isPresent()).isFalse();
        assertThat(stateTransition.getFrom().getId()).isEqualTo(inStock.getId());
        assertThat(stateTransition.getTo().getId()).isEqualTo(commissioned.getId());
    }

    @Transactional
    @Test
    public void createStateMachineWithOneTranslatableNamedStateTransition() {
        String expectedName = "createStateMachineWithOneTranslatableNamedStateTransition";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        Thesaurus thesaurus = mock(Thesaurus.class);
        String translationKeyKey = "expected.translation.key";
        String translation = "Commission (custom translation)";
        when(thesaurus.getString(eq(translationKeyKey), anyString())).thenReturn(translation);
        FiniteStateMachineBuilder builder = this.getTestService(thesaurus).newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        TranslationKey translationKey = mock(TranslationKey.class);
        when(translationKey.getKey()).thenReturn(translationKeyKey);
        when(translationKey.getDefaultFormat()).thenReturn(translation);
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned, translationKey).complete();

        // Business method
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(2);
        List<StateTransition> transitions = stateMachine.getTransitions();
        assertThat(transitions).hasSize(1);
        StateTransition stateTransition = transitions.get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(commissionedEventType.getId());
        assertThat(stateTransition.getName().isPresent()).isFalse();
        assertThat(stateTransition.getTranslationKey().isPresent()).isTrue();
        assertThat(stateTransition.getTranslationKey().get()).isEqualTo(translationKeyKey);
        assertThat(stateTransition.getFrom().getId()).isEqualTo(inStock.getId());
        assertThat(stateTransition.getTo().getId()).isEqualTo(commissioned.getId());
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "transitions[0].name")
    @Test
    public void createStateMachineWithTooLongStateTransitionName() {
        String expectedName = "createStateMachineWithTooLongStateTransitionName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned, Strings.repeat("Too long", 100)).complete();

        // Business method
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void findFiniteStateMachinesUsing() {
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType otherEventType = this.createNewStateTransitionEventType("#other");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine("UsingCommissioned");
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachineUsingCommissioned = builder.complete(inStock);

        FiniteStateMachineBuilder otherBuilder = this.getTestService().newFiniteStateMachine("Other");
        FiniteStateMachineBuilder.StateBuilder aStateBuilder = otherBuilder.newCustomState("A");
        FiniteStateMachineBuilder.StateBuilder bStateBuilder = otherBuilder.newCustomState("B");
        State a = aStateBuilder.on(otherEventType).transitionTo(bStateBuilder).complete();
        bStateBuilder.on(otherEventType).transitionTo(a).complete();
        otherBuilder.complete(a);

        // Business method
        List<FiniteStateMachine> finiteStateMachines = this.getTestService().findFiniteStateMachinesUsing(commissionedEventType);

        // Asserts
        assertThat(finiteStateMachines).hasSize(1);
        assertThat(finiteStateMachines.get(0).getId()).isEqualTo(stateMachineUsingCommissioned.getId());
    }

    @Transactional
    @Test
    public void buildDefaultLifeCycle() {
        String expectedName = "Default life cycle";
        // Create default StateTransitionEventTypes
        StateTransitionEventType deliveredToWarehouse = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType activated = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivated = this.createNewStateTransitionEventType("#deactivated");
        StateTransitionEventType decommissionedEventType = this.createNewStateTransitionEventType("#decommissioned");
        StateTransitionEventType deletedEventType = this.createNewStateTransitionEventType("#deleted");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        // Create default States
        State deleted = builder.newCustomState("Deleted").complete();
        State decommissioned = builder
                .newCustomState("Decommissioned")
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        FiniteStateMachineBuilder.StateBuilder activeBuilder = builder.newCustomState("Active");
        FiniteStateMachineBuilder.StateBuilder inactiveBuilder = builder.newCustomState("Inactive");
        State active = activeBuilder
                .on(decommissionedEventType).transitionTo(decommissioned)
                .on(deactivated).transitionTo(inactiveBuilder)
                .complete();
        State inactive = inactiveBuilder
                .on(activated).transitionTo(active)
                .on(decommissionedEventType).transitionTo(decommissioned)
                .complete();
        State commissioned = builder
                .newCustomState("Commissioned")
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .complete();
        State inStock = builder
                .newCustomState("InStock")
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .on(commissionedEventType).transitionTo(commissioned)
                .complete();
        builder
                .newCustomState("Ordered")
                .on(deliveredToWarehouse).transitionTo(inStock)
                .complete();

        // Business method
        builder.complete(inStock);

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(7);
        assertThat(reloaded.getInitialState()).isNotNull();
        assertThat(reloaded.getInitialState().getId()).isEqualTo(inStock.getId());
        assertThat(inStock.isInitial()).isTrue();
        List<StateTransition> transitions = reloaded.getTransitions();
        assertThat(transitions).hasSize(11);
        Optional<State> reloadedActiveState = reloaded.getState(active.getName());
        assertThat(reloadedActiveState.isPresent()).isTrue();
        List<StateTransition> outgoingActiveStateTransitions = reloadedActiveState.get().getOutgoingStateTransitions();
        assertThat(outgoingActiveStateTransitions).hasSize(2);
        Optional<State> reloadedDeletedState = reloaded.getState(deleted.getName());
        assertThat(reloadedDeletedState.isPresent()).isTrue();
        List<StateTransition> outgoingDeletedStateTransitions = reloadedDeletedState.get().getOutgoingStateTransitions();
        assertThat(outgoingDeletedStateTransitions).isEmpty();
    }

    @Transactional
    @Test
    public void renameStateMachine() {
        String initialName = "renameStateMachine";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(initialName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);
        long initialVersion = stateMachine.getVersion();

        // Business method
        String newName = "renamed";
        stateMachine.startUpdate().setName(newName).complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(newName);
        assertThat(stateMachine.getVersion()).isGreaterThan(initialVersion);
        assertThat(stateMachine.getModifiedTimestamp()).isNotNull();
        // Check that there was no effect on the States and transitions
        assertThat(stateMachine.getStates()).hasSize(2);
        assertThat(stateMachine.getTransitions()).hasSize(1);
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(commissionedEventType.getId());
        assertThat(stateTransition.getFrom().getId()).isEqualTo(inStock.getId());
        assertThat(stateTransition.getTo().getId()).isEqualTo(commissioned.getId());
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNIQUE_FINITE_STATE_MACHINE_NAME + "}")
    @Test
    public void renameStateMachineToOneThatAlreadyExists() {
        String duplicateName = "alreadyExists";
        FiniteStateMachineBuilder builder1 = this.getTestService().newFiniteStateMachine(duplicateName);
        State initial = builder1.newCustomState("Initial").complete();
        builder1.complete(initial);

        String initialName = "initialName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(initialName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Commissioned").complete());

        // Business method
        stateMachine.startUpdate().setName(duplicateName).complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void setNameToEmptyString() {
        String initialName = "setNameToEmptyString";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(initialName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Commissioned").complete());

        // Business method
        stateMachine.startUpdate().setName("").complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void setNameToNull() {
        String initialName = "setNameToNull";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(initialName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Commissioned").complete());

        // Business method
        stateMachine.startUpdate().setName(null).complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void setNameToExtremelyLongString() {
        String initialName = "setNameToExtremelyLongString";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(initialName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Commissioned").complete());

        // Business method
        stateMachine.startUpdate().setName(Strings.repeat("Too long", 100)).complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void removeState() {
        String expectedName = "removeState";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        stateMachine.startUpdate().removeState(commissioned).complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        assertThat(stateMachine.getStates().get(0).getName()).isEqualTo("InStock");
        assertThat(stateMachine.getTransitions()).isEmpty();
    }

    @Transactional
    @Test
    public void stateRemovedAfterFind() {
        String expectedName = "stateRemovedAfterFind";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        stateMachine.startUpdate().removeState(commissioned).complete();

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(1);
        assertThat(reloaded.getStates().get(0).getName()).isEqualTo("InStock");
        assertThat(reloaded.getTransitions()).isEmpty();
    }

    @Transactional
    @Test
    public void removeStateByName() {
        String expectedName = "removeStateByName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(commissioned);

        // Business method
        stateMachine.startUpdate().removeState("InStock").complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        assertThat(stateMachine.getStates().get(0).getName()).isEqualTo("Commissioned");
        assertThat(stateMachine.getTransitions()).isEmpty();
    }

    @Transactional
    @Test
    public void removeStateByNameOnlyObsoletesTheState() {
        String expectedName = "removeStateByNameOnlyObsoletesTheState";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        String nameOfStateToBeRemoved = "InStock";
        builder.newCustomState(nameOfStateToBeRemoved).on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(commissioned);
        long idOfStateToBeRemoved = stateMachine.getState(nameOfStateToBeRemoved).get().getId();

        // Business method
        stateMachine.startUpdate().removeState(nameOfStateToBeRemoved).complete();

        // Asserts
        // The following line uses unsupported API to find an obsolete State by id
        StateImpl obsolete = (StateImpl) stateMachine.startUpdate().state(idOfStateToBeRemoved).complete();
        assertThat(obsolete.isObsolete()).isTrue();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.EXACTLY_ONE_INITIAL_STATE + "}")
    @Test
    public void removeInitialStateByName() {
        String expectedName = "removeInitialStateByName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(commissioned);

        // Business method
        stateMachine.startUpdate().removeState("Commissioned").complete();

        // Asserts: see expected constraint violation
    }

    @Transactional
    @Test
    public void removeInitialStateByNameAndMarkOtherAsInitial() {
        String expectedName = "removeStateByName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(commissioned);

        // Business method
        stateMachine.startUpdate().removeState("Commissioned").complete(inStock);

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        assertThat(stateMachine.getStates().get(0).getName()).isEqualTo("InStock");
        assertThat(stateMachine.getInitialState()).isNotNull();
        assertThat(stateMachine.getInitialState().getId()).isEqualTo(inStock.getId());
        assertThat(inStock.isInitial()).isTrue();
        assertThat(stateMachine.getTransitions()).isEmpty();
    }

    @Transactional
    @Test
    public void changeInitialState() {
        String expectedName = "changeInitialState";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(commissioned);

        // Business method
        stateMachine.startUpdate().complete(inStock);

        // Asserts
        assertThat(stateMachine.getInitialState().getId()).isEqualTo(inStock.getId());
        assertThat(inStock.isInitial()).isTrue();
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void removeStateThatDoesNotExist() {
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder targetBuilder = this.getTestService().newFiniteStateMachine("removeStateThatDoesNotExist");
        State commissioned = targetBuilder.newCustomState("Commissioned").complete();
        State inStock = targetBuilder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = targetBuilder.complete(inStock);

        FiniteStateMachineBuilder otherBuilder = this.getTestService().newFiniteStateMachine("Other");
        State whatever = otherBuilder.newCustomState("Whatever").complete();
        FiniteStateMachine other = otherBuilder.complete(whatever);

        // Business method
        stateMachine.startUpdate().removeState(whatever).complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void removeStateByNameThatDoesNotExist() {
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine("removeStateThatDoesNotExist");
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        stateMachine.startUpdate().removeState("does not exist").complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_STATE + "}", strict = false)
    @Test
    public void cannotRemoveLastState() {
        String expectedName = "cannotRemoveLastState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("Single").complete());

        // Business method
        stateMachine.startUpdate().removeState("Single").complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.EXACTLY_ONE_INITIAL_STATE + "}" )
    @Test
    public void cannotRemoveInitialState() {
        String expectedName = "cannotRemoveInitialState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State first = builder.newCustomState("First").complete();
        State second = builder.newCustomState("Second").complete();
        FiniteStateMachine stateMachine = builder.complete(first);

        // Business method
        stateMachine.startUpdate().removeState("First").complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void renameStateByName() {
        String expectedName = "renameState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(initialName).complete());

        // Business method
        String newName = "renamed";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(initialName).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getState(newName).isPresent()).isTrue();
        assertThat(stateMachine.getState(newName).get().isInitial()).isTrue();
        assertThat(stateMachine.getState(initialName).isPresent()).isFalse();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void renameStateToNullName() {
        String expectedName = "renameStateToNullName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(initialName).complete());

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(initialName).setName(null).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void renameStateToEmptyName() {
        String expectedName = "renameStateToEmptyName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(initialName).complete());

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(initialName).setName("").complete();
        stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void renameStateToNameThatIsTooLong() {
        String expectedName = "renameStateToNameThatIsTooLong";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(initialName).complete());

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(initialName).setName(Strings.repeat("Too long", 100)).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void renameStateByNameThatDoesNotExist() {
        String expectedName = "renameState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(initialName).complete());

        // Business method
        String newName = "renamed";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(newName).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void findAfterRenameStateByName() {
        String expectedName = "findAfterRenameStateByName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        State state = builder.newCustomState(initialName).complete();
        long initialStateVersion = state.getVersion();
        FiniteStateMachine stateMachine = builder.complete(state);

        // Business method
        String newName = "renamed";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(initialName).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getState(newName).isPresent()).isTrue();
        assertThat(stateMachine.getState(newName).get().isInitial()).isTrue();
        assertThat(stateMachine.getInitialState()).isNotNull();
        assertThat(stateMachine.getInitialState().getId()).isEqualTo(stateMachine.getState(newName).get().getId());
        assertThat(stateMachine.getInitialState().isInitial()).isTrue();
        assertThat(reloaded.getState(initialName).isPresent()).isFalse();
        State updatedState = reloaded.getState(newName).get();
        assertThat(updatedState.getVersion()).isGreaterThan(initialStateVersion);
        assertThat(updatedState.getModifiedTimestamp()).isNotNull();
    }

    @Transactional
    @Test
    public void renameStateById() {
        String expectedName = "renameState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        State state = builder.newCustomState(initialName).complete();
        FiniteStateMachine stateMachine = builder.complete(state);

        // Business method
        String newName = "renamed";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(state.getId()).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getState(newName).isPresent()).isTrue();
        assertThat(stateMachine.getState(newName).get().isInitial()).isTrue();
        assertThat(stateMachine.getState(initialName).isPresent()).isFalse();
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void renameStateByIdThatDoesNotExist() {
        String expectedName = "renameState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        State state = builder.newCustomState(initialName).complete();
        FiniteStateMachine stateMachine = builder.complete(state);

        // Business method
        String newName = "renamed";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(Long.MAX_VALUE).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void findAfterRenameStateById() {
        String expectedName = "findAfterRenameStateById";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        State state = builder.newCustomState(initialName).complete();
        long initialStateVersion = state.getVersion();
        FiniteStateMachine stateMachine = builder.complete(state);

        // Business method
        String newName = "renamed";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state(state.getId()).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getState(newName).isPresent()).isTrue();
        assertThat(stateMachine.getState(newName).get().isInitial()).isTrue();
        assertThat(stateMachine.getInitialState()).isNotNull();
        assertThat(stateMachine.getInitialState().getId()).isEqualTo(stateMachine.getState(newName).get().getId());
        assertThat(stateMachine.getInitialState().isInitial()).isTrue();
        assertThat(reloaded.getState(initialName).isPresent()).isFalse();
        State updatedState = reloaded.getState(newName).get();
        assertThat(updatedState.getVersion()).isGreaterThan(initialStateVersion);
        assertThat(updatedState.getModifiedTimestamp()).isNotNull();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_STATE_NAME)
    @Test
    public void addStateWithNameThatAlreadyExists() {
        String expectedName = "addStateWithNameThatAlreadyExists";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String duplicateName = "Commissioned";
        State commissioned = builder.newCustomState(duplicateName).complete();
        FiniteStateMachine stateMachine = builder.complete(commissioned);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.newCustomState(duplicateName).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_STATE_NAME, strict = false)
    @Test
    public void renameStateToOneThatAlreadyExists() {
        String expectedName = "renameStateToOneThatAlreadyExists";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        String newName = "InStock";
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("Commissioned").setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void renameStateThatDoesNotExist() {
        String expectedName = "renameStateThatDoesNotExist";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String initialName = "Single";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(initialName).complete());

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("does not exist").setName("whatever").complete();
        stateMachineUpdater.complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void removeTransition() {
        String expectedName = "removeTransition";
        StateTransitionEventType deliveredEventType = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        FiniteStateMachineBuilder.StateBuilder inStock = builder.newCustomState("InStock");
        State initial = builder.newCustomState("Initial").on(deliveredEventType).transitionTo(inStock).complete();
        inStock.on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").prohibit(commissionedEventType).complete();
        stateMachineUpdater.complete();

        // Asserts
        // Check that there was no effect on the name and the topic
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(3);
        assertThat(stateMachine.getTransitions()).hasSize(1);
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(deliveredEventType.getId());
    }

    @Transactional
    @Test(expected = UnsupportedStateTransitionException.class)
    public void removeTransitionThatDoesNotExist() {
        String expectedName = "removeTransitionThatDoesNotExist";
        StateTransitionEventType deliveredEventType = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").prohibit(deliveredEventType).complete();

        // Asserts: see expected exception rul
    }

    @Transactional
    @Test
    public void removeTransitionAndReload() {
        String expectedName = "removeTransitionAndReload";
        StateTransitionEventType deliveredEventType = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        FiniteStateMachineBuilder.StateBuilder inStock = builder.newCustomState("InStock");
        State initial = builder.newCustomState("Initial").on(deliveredEventType).transitionTo(inStock).complete();
        inStock.on(commissionedEventType).transitionTo(commissioned).complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").prohibit(commissionedEventType).complete();
        stateMachineUpdater.complete();

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getStates()).hasSize(3);
        assertThat(reloaded.getTransitions()).hasSize(1);
        StateTransition stateTransition = reloaded.getTransitions().get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(deliveredEventType.getId());
    }

    @Transactional
    @Test
    public void addBothEntryAndExitProcessesToExistingState() {
        String expectedName = "addBothEntryAndExitProcessesToExistingState";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(expectedStateName).complete());

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("Initial").onEntry(onEntry).onExit(onExit).complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
    }

    @Transactional
    @Test
    public void addMultipleEntryAndExitProcessesToExistingState() {
        String expectedName = "addBothEntryAndExitProcessesToExistingState";
        FiniteStateMachineServiceImpl testService = this.getTestService();
        FiniteStateMachineBuilder builder = testService.newFiniteStateMachine(expectedName);
        BpmProcessDefinition onEntry2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onEntry2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        BpmProcessDefinition onExit2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onExit2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        String expectedStateName = "Initial";
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState(expectedStateName).complete());

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("Initial")
                .onEntry(onEntry)
                .onEntry(onEntry2)
                .onExit(onExit)
                .onExit(onExit2)
                .complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(2);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        assertThat(onEntryProcesses.get(1).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry2.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(2);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
        assertThat(onExitProcesses.get(1).getStateChangeBusinessProcess().getId()).isEqualTo(onExit2.getId());
    }

    /**
     * Builds an incomplete version of the default life cycle
     * with missing {@link State}s and transitions and then
     * completes that via the {@link FiniteStateMachineUpdater} interface.
     * This will test:
     * <ul>
     * <li>adding and remove states</li>
     * <li>adding and remove state transitions</li>
     * </ul>
     *
     * @see #buildDefaultLifeCycle()
     */
    @Transactional
    @Test
    public void completeDefaultLifeCycle() {
        String expectedName = "Default life cycle";
        // Create default StateTransitionEventTypes
        StateTransitionEventType deliveredToWarehouse = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType activated = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivated = this.createNewStateTransitionEventType("#deactivated");
        StateTransitionEventType decommissionedEventType = this.createNewStateTransitionEventType("#decommissioned");
        StateTransitionEventType deletedEventType = this.createNewStateTransitionEventType("#deleted");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        // Create default States
        State deleted = builder.newStandardState("Deleted").complete();
        State temporary = builder.newStandardState("Temporary").on(deletedEventType).transitionTo(deleted).complete();
        State decommissioned = builder
                .newStandardState("Decommissioned")
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        State commissioned = builder
                .newStandardState("Commissioned")
                .on(activated).transitionTo(temporary)
                .complete();
        State inStock = builder
                .newStandardState("InStock")
                .on(activated).transitionTo(temporary)
                .on(commissionedEventType).transitionTo(commissioned)
                .complete();
        builder
                .newStandardState("Ordered")
                .on(deliveredToWarehouse).transitionTo(inStock)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.removeState(temporary);
        FiniteStateMachineBuilder.StateBuilder activeBuilder = stateMachineUpdater.newStandardState("Active");
        FiniteStateMachineBuilder.StateBuilder inactiveBuilder = stateMachineUpdater.newStandardState("Inactive");
        State active = activeBuilder
                .on(decommissionedEventType).transitionTo(decommissioned)
                .on(deactivated).transitionTo(inactiveBuilder)
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        stateMachineUpdater
                .state("Commissioned")
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactiveBuilder)
                .complete();
        State inactive = inactiveBuilder
                .on(activated).transitionTo(active)
                .on(decommissionedEventType).transitionTo(decommissioned)
                .complete();
        stateMachineUpdater
                .state("InStock")
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .complete();
        stateMachineUpdater.complete();

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(7);
        for (State state : reloaded.getStates()) {
            assertThat(state.isCustom())
                    .as("Expecting all states to be standard but found that " + state.getName() + " is not")
                    .isFalse();
        }
        List<StateTransition> transitions = reloaded.getTransitions();
        assertThat(transitions).hasSize(11);
        assertThat(reloaded.getState(active.getName()).isPresent()).isTrue();
        List<ProcessReference> onEntryProcesses = reloaded.getState(active.getName()).get().getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        List<ProcessReference> onExitProcesses = reloaded.getState(active.getName()).get().getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
    }

    @Transactional
    @Test
    public void testAddTransitionByStateName() {
        String expectedName = "testAddTransitionByStateName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").on(commissionedEventType).transitionTo("Commissioned");

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(2);
        List<StateTransition> transitions = reloaded.getTransitions();
        assertThat(transitions).hasSize(1);
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void testAddTransitionByStateNameThatDoesNotExist() {
        String expectedName = "testAddTransitionByStateName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").on(commissionedEventType).transitionTo("Does not exist");

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void testAddTransitionByStateId() {
        String expectedName = "testAddTransitionByStateId";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").on(commissionedEventType).transitionTo(commissioned.getId());

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(2);
        List<StateTransition> transitions = reloaded.getTransitions();
        assertThat(transitions).hasSize(1);
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void testAddTransitionByStateIdThatDoesNotExist() {
        String expectedName = "testAddTransitionByStateId";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("InStock").on(commissionedEventType).transitionTo(Long.MAX_VALUE);

        // Asserts: see expected exception rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_STATE_TRANSITION + "}", strict = false)
    @Test
    public void testCreateWithDuplicateTransition() {
        String expectedName = "testCreateWithDuplicateTransition";
        StateTransitionEventType eventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        FiniteStateMachineBuilder.StateBuilder a = builder.newCustomState("A");
        State b = builder.newCustomState("B").complete();
        State c = builder.newCustomState("C").complete();
        a.on(eventType).transitionTo(b);
        a.on(eventType).transitionTo(c);

        // Business methods
        builder.complete(a.complete());

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_STATE_TRANSITION + "}", strict = false)
    @Test
    public void testAddDuplicateTransition() {
        String expectedName = "testAddDuplicateTransition";
        StateTransitionEventType eventType = this.createNewStateTransitionEventType("#commissioned");
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State b = builder.newCustomState("B").complete();
        State c = builder.newCustomState("C").complete();
        State a = builder.newCustomState("A").on(eventType).transitionTo(b).complete();
        FiniteStateMachine stateMachine = builder.complete(a);

        // Business methods
        FiniteStateMachineUpdater updater = stateMachine.startUpdate();
        updater.state(a.getId()).on(eventType).transitionTo(c);
        updater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void removeEntryAndExitProcesses() {
        String expectedName = "removeEntryAndExitProcesses";
        FiniteStateMachineServiceImpl testService = this.getTestService();
        BpmProcessDefinition onEntry2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onEntry2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        BpmProcessDefinition onExit2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onExit2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        FiniteStateMachineBuilder builder = testService.newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        State initial = builder.newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onEntry(onEntry2)
                .onExit(onExit)
                .onExit(onExit2)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("Initial")
                .removeOnEntry(onEntry)
                .removeOnExit(onExit2)
                .complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry2.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
    }

    @Transactional
    @Test
    public void removeAllEntryAndExitProcesses() {
        String expectedName = "removeEntryAndExitProcesses";
        FiniteStateMachineServiceImpl testService = this.getTestService();
        BpmProcessDefinition onEntry2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onEntry2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        BpmProcessDefinition onExit2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onExit2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        FiniteStateMachineBuilder builder = testService.newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        State initialState = builder.newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onEntry(onEntry2)
                .onExit(onExit)
                .onExit(onExit2)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initialState);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("Initial")
                .removeOnEntry(onEntry)
                .removeOnEntry(onEntry2)
                .removeOnExit(onExit)
                .removeOnExit(onExit2)
                .complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        assertThat(state.getOnEntryProcesses()).isEmpty();
        assertThat(state.getOnExitProcesses()).isEmpty();
    }

    @Transactional
    @Test(expected = UnknownProcessReferenceException.class)
    public void removeNonExistingEntryProcesses() {
        String expectedName = "removeNonExistingEntryProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        State initial = builder.newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

//        StateChangeBusinessProcess mockedStateChageBusinessProcess = mock(StateChangeBusinessProcess.class);
        BpmProcessDefinition mockedStateChageBusinessProcess = mock(BpmProcessDefinition.class);
        when(mockedStateChageBusinessProcess.getId()).thenReturn(Long.MAX_VALUE);

        // Business method
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.state("Initial")
                .removeOnEntry(mockedStateChageBusinessProcess)
                .complete();    // Not expecting to get this far in fact

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void obsoleteStateMachine() {
        String expectedName = "obsoleteStateMachine";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder
                .newCustomState("Initial")
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        stateMachine.makeObsolete();

        // Asserts
        assertThat(stateMachine.isObsolete()).isTrue();
        assertThat(stateMachine.getObsoleteTimestamp()).isNotNull();
    }

    @Transactional
    @Test
    public void obsoleteStateMachineWithReload() {
        String expectedName = "obsoleteStateMachineWithReload";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder
                .newCustomState("Initial")
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        stateMachine.makeObsolete();

        // Asserts
        FiniteStateMachine reloaded = this.getTestService().findFiniteStateMachineById(stateMachine.getId()).get();
        assertThat(reloaded.isObsolete()).isTrue();
        assertThat(reloaded.getObsoleteTimestamp()).isNotNull();
    }

    @Transactional
    @Test
    public void obsoleteStateMachineShowsUpInFindById() {
        String expectedName = "obsoleteStateMachineShowsUpInFindById";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder
                .newCustomState("Initial")
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        stateMachine.makeObsolete();

        // Asserts
        assertThat(this.getTestService().findFiniteStateMachineById(stateMachine.getId())).isPresent();
    }

    @Transactional
    @Test
    public void obsoleteStateMachineDoesNotShowUpInFindByName() {
        String expectedName = "obsoleteStateMachineDoesNotShowUpInFindByName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder
                .newCustomState("Initial")
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        stateMachine.makeObsolete();

        // Asserts
        assertThat(this.getTestService().findFiniteStateMachineByName(expectedName)).isEmpty();
    }

    @Transactional
    @Test
    public void jira_CXO_2128() {
        String expectedName = "CXO2128";
        FiniteStateMachineServiceImpl service = this.getTestService();
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine(expectedName);
        State initial = builder
                .newCustomState("Initial")
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Add state
        FiniteStateMachineUpdater addStateUpdater = stateMachine.startUpdate();
        State toBeRemovedSoon = addStateUpdater.newCustomState("ToBeRemovedSoon").complete();
        addStateUpdater.complete();

        // This is essential to the test and why the OptimisticLockException occurred as described by the jira issue
        State removeMe = service.findAndLockStateByIdAndVersion(toBeRemovedSoon.getId(), toBeRemovedSoon.getVersion()).get();

        // Remove the state we just added
        stateMachine.startUpdate().removeState(removeMe).complete();

        // Business method
        stateMachine.makeObsolete();

        // Asserts
        assertThat(service.findFiniteStateMachineByName(expectedName)).isEmpty();
    }

    @Transactional
    @Test
    public void deleteStateMachineWithOneStateAndBothEntryAndExitProcesses() {
        String expectedName = "deleteStateMachineWithOneStateAndBothEntryAndExitProcesses";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State initial = builder
                .newCustomState("Initial")
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        stateMachine.delete();

        // Asserts
        assertThat(this.getTestService().findFiniteStateMachineByName(expectedName)).isEmpty();
    }

    @Transactional
    @Test
    public void cloneStateMachineWithOneStateAndMultipleEntryAndExitProcesses() {
        FiniteStateMachineServiceImpl testService = this.getTestService();
        BpmProcessDefinition onEntry2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onEntry2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        BpmProcessDefinition onExit2 = inMemoryPersistence.getBpmService().newProcessBuilder()
                .setProcessName("onExit2").setAssociation("device").setVersion("1.0").setAppKey("MDC").setStatus("ACTIVE")
                .create();
        FiniteStateMachineBuilder builder = testService.newFiniteStateMachine("cloneStateMachineWithOneStateAndMultipleEntryAndExitProcesses");
        String expectedStateName = "Initial";
        State initial = builder
                .newCustomState(expectedStateName)
                .onEntry(onEntry)
                .onEntry(onEntry2)
                .onExit(onExit)
                .onExit(onExit2)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(initial);

        // Business method
        String expectedName = "Cloned";
        FiniteStateMachine cloned = testService.cloneFiniteStateMachine(stateMachine, expectedName);

        // Asserts
        assertThat(cloned.getName()).isEqualTo(expectedName);
        assertThat(cloned.getStates()).hasSize(1);
        State state = cloned.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(2);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        assertThat(onEntryProcesses.get(1).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry2.getId());
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(2);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
        assertThat(onExitProcesses.get(1).getStateChangeBusinessProcess().getId()).isEqualTo(onExit2.getId());
    }

    @Transactional
    @Test
    public void cloneStateMachineWithTwoStates() {
        FiniteStateMachineServiceImpl service = this.getTestService();
        StateTransitionEventType activatedEventType = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivatedEventType = this.createNewStateTransitionEventType("#deactivated");
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine("cloneStateMachineWithTwoStates");
        FiniteStateMachineBuilder.StateBuilder inactive = builder.newCustomState("Inactive");
        FiniteStateMachineBuilder.StateBuilder active = builder.newCustomState("Active");
        inactive.on(activatedEventType).transitionTo(active);
        active.on(deactivatedEventType).transitionTo(inactive);
        inactive.complete();
        FiniteStateMachine stateMachine = builder.complete(active.complete());

        String expectedName = "Cloned";
        // Business method
        FiniteStateMachine cloned = service.cloneFiniteStateMachine(stateMachine, expectedName);

        // Asserts
        assertThat(cloned.getName()).isEqualTo(expectedName);
        assertThat(cloned.getStates()).hasSize(2);
        List<StateTransition> transitions = cloned.getTransitions();
        assertThat(transitions).hasSize(2);
    }

    @Transactional
    @Test
    public void cloneStateMachineWithCustomTransitionName() {
        FiniteStateMachineServiceImpl service = this.getTestService();
        StateTransitionEventType activatedEventType = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivatedEventType = this.createNewStateTransitionEventType("#deactivated");
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine("cloneStateMachineWithCustomTransitionName");
        FiniteStateMachineBuilder.StateBuilder inactive = builder.newCustomState("Inactive");
        FiniteStateMachineBuilder.StateBuilder active = builder.newCustomState("Active");
        String activateTransitionName = "Activate";
        String deactivateTransitionName = "Deactivate";
        inactive.on(activatedEventType).transitionTo(active, activateTransitionName);
        active.on(deactivatedEventType).transitionTo(inactive, deactivateTransitionName);
        inactive.complete();
        FiniteStateMachine stateMachine = builder.complete(active.complete());

        String expectedName = "Cloned";
        // Business method
        FiniteStateMachine cloned = service.cloneFiniteStateMachine(stateMachine, expectedName);

        // Asserts
        assertThat(cloned.getName()).isEqualTo(expectedName);
        assertThat(cloned.getStates()).hasSize(2);
        List<StateTransition> transitions = cloned.getTransitions();
        assertThat(transitions).hasSize(2);
        for (StateTransition transition : transitions) {
            switch (transition.getEventType().getSymbol()) {
                case "#activated": {
                    assertThat(transition.getName().isPresent()).isTrue();
                    assertThat(transition.getName().get()).isEqualTo(activateTransitionName);
                    break;
                }
                case "#deactivated": {
                    assertThat(transition.getName().isPresent()).isTrue();
                    assertThat(transition.getName().get()).isEqualTo(deactivateTransitionName);
                    break;
                }
                default: {
                }
            }
        }
    }

    @Transactional
    @Test
    public void cloneStateMachineWithTranslatableName() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        String tk1 = "cloneStateMachineWithTranslatableName.activate";
        String t1 = "Activate (custom translation)";
        when(thesaurus.getString(eq(tk1), anyString())).thenReturn(t1);
        String tk2 = "cloneStateMachineWithTranslatableName.deactivate";
        String t2 = "Deactivate (custom translation)";
        when(thesaurus.getString(eq(tk2), anyString())).thenReturn(t2);
        FiniteStateMachineServiceImpl service = this.getTestService(thesaurus);
        StateTransitionEventType activatedEventType = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivatedEventType = this.createNewStateTransitionEventType("#deactivated");
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine("cloneStateMachineWithCustomTransitionName");
        FiniteStateMachineBuilder.StateBuilder inactive = builder.newCustomState("Inactive");
        FiniteStateMachineBuilder.StateBuilder active = builder.newCustomState("Active");
        TranslationKey activateTranslationKey = mock(TranslationKey.class);
        when(activateTranslationKey.getKey()).thenReturn(tk1);
        when(activateTranslationKey.getDefaultFormat()).thenReturn(t1);
        inactive.on(activatedEventType).transitionTo(active, activateTranslationKey);
        TranslationKey deactivateTranslationKey = mock(TranslationKey.class);
        when(deactivateTranslationKey.getKey()).thenReturn(tk2);
        when(deactivateTranslationKey.getDefaultFormat()).thenReturn(t2);
        active.on(deactivatedEventType).transitionTo(inactive, deactivateTranslationKey);
        inactive.complete();
        FiniteStateMachine stateMachine = builder.complete(active.complete());

        String expectedName = "Cloned";
        // Business method
        FiniteStateMachine cloned = service.cloneFiniteStateMachine(stateMachine, expectedName);

        // Asserts
        assertThat(cloned.getName()).isEqualTo(expectedName);
        assertThat(cloned.getStates()).hasSize(2);
        List<StateTransition> transitions = cloned.getTransitions();
        assertThat(transitions).hasSize(2);
        for (StateTransition transition : transitions) {
            switch (transition.getEventType().getSymbol()) {
                case "#activated": {
                    assertThat(transition.getName().isPresent()).isFalse();
                    assertThat(transition.getTranslationKey().isPresent()).isTrue();
                    assertThat(transition.getTranslationKey().get()).isEqualTo(tk1);
                    break;
                }
                case "#deactivated": {
                    assertThat(transition.getName().isPresent()).isFalse();
                    assertThat(transition.getTranslationKey().isPresent()).isTrue();
                    assertThat(transition.getTranslationKey().get()).isEqualTo(tk2);
                    break;
                }
                default: {
                }
            }
        }
    }

    @Transactional
    @Test
    public void cloneDefaultLifeCycle() {
        FiniteStateMachineServiceImpl service = this.getTestService();
        // Create default StateTransitionEventTypes
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType activated = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivated = this.createNewStateTransitionEventType("#deactivated");
        StateTransitionEventType decommissionedEventType = this.createNewStateTransitionEventType("#decommissioned");
        StateTransitionEventType deletedEventType = this.createNewStateTransitionEventType("#deleted");
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine("Default life cycle");
        // Create default States
        FiniteStateMachineBuilder.StateBuilder activeBuilder = builder.newStandardState("Active");
        FiniteStateMachineBuilder.StateBuilder inactiveBuilder = builder.newStandardState("Inactive");
        State deleted = builder.newStandardState("Deleted").complete();
        State decommissioned = builder
                .newStandardState("Decommissioned")
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        State commissioned = builder
                .newStandardState("Commissioned")
                .on(activated).transitionTo(activeBuilder)
                .on(deactivated).transitionTo(inactiveBuilder)
                .complete();
        State inStock = builder
                .newStandardState("InStock")
                .on(activated).transitionTo(activeBuilder)
                .on(deactivated).transitionTo(inactiveBuilder)
                .on(commissionedEventType).transitionTo(commissioned)
                .complete();
        State active = activeBuilder
                .on(decommissionedEventType).transitionTo(decommissioned)
                .on(deactivated).transitionTo(inactiveBuilder)
                .onEntry(onEntry)
                .onExit(onExit)
                .complete();
        inactiveBuilder
                .on(activated).transitionTo(active)
                .on(decommissionedEventType).transitionTo(decommissioned)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business method
        String expectedName = "Clone of Default life cycle";
        FiniteStateMachine cloned = service.cloneFiniteStateMachine(stateMachine, expectedName);

        // Asserts
        assertThat(cloned.getName()).isEqualTo(expectedName);
        assertThat(cloned.getStates()).hasSize(6);
        for (State state : cloned.getStates()) {
            assertThat(state.isCustom())
                    .as("Expecting all states to be standard but found that " + state.getName() + " is not")
                    .isFalse();
        }
        List<StateTransition> transitions = cloned.getTransitions();
        assertThat(transitions).hasSize(10);
        assertThat(cloned.getState(active.getName()).isPresent()).isTrue();
        List<ProcessReference> onEntryProcesses = cloned.getState(active.getName()).get().getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onEntry.getId());
        List<ProcessReference> onExitProcesses = cloned.getState(active.getName()).get().getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getStateChangeBusinessProcess().getId()).isEqualTo(onExit.getId());
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void addStateWithNullName() {
        String expectedName = "addStateWithNullName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.newCustomState(null).complete();
        FiniteStateMachine updated = stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void addStateWithEmptyName() {
        String expectedName = "addStateWithEmptyName";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.newCustomState("").complete();
        FiniteStateMachine updated = stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @Test
    public void addStateWithNameThatIsTooLong() {
        String expectedName = "addStateWithNameThatIsTooLong";
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        State inStock = builder.newCustomState("InStock").complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);

        // Business methods
        FiniteStateMachineUpdater stateMachineUpdater = stateMachine.startUpdate();
        stateMachineUpdater.newCustomState(Strings.repeat("Too long", 100)).complete();
        FiniteStateMachine updated = stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test(expected = IllegalStateException.class)
    public void createStateMachineWithStageSetWithOneStandardStateWithoutStage() {
        String expectedName = "createStateMachineWithStageSetWithOneStandardStateWithoutStage";
        StageSetBuilder stageSetbuilder = this.getTestService().newStageSet("StageSet");
        StageSet stageSet = stageSetbuilder.stage("Stage").add();
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName, stageSet);
        String expectedStateName = "Initial";
        builder.complete(builder.newStandardState(expectedStateName).complete());
    }

    @Transactional
    @Test(expected = IllegalStateException.class)
    public void createStateMachineWithoutStageSetWithOneStandardStateWithStage() {
        String expectedName = "createStateMachineWithoutStageSetWithOneStandardStateWithStage";
        StageSetBuilder stageSetbuilder = this.getTestService().newStageSet("StageSet");
        StageSet stageSet = stageSetbuilder.stage("Stage").add();
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName);
        String expectedStateName = "Initial";
        builder.complete(builder.newStandardState(expectedStateName, stageSet.getStages().get(0)).complete());
    }

    @Transactional
    @Test
    public void createStateMachineWithStageSetWithOneStandardStateWithStage() {
        String expectedName = "createStateMachineWithStageSetWithOneStandardStateWithStage";
        StageSetBuilder stageSetbuilder = this.getTestService().newStageSet("StageSet");
        StageSet stageSet = stageSetbuilder.stage("Stage").add();
        FiniteStateMachineBuilder builder = this.getTestService().newFiniteStateMachine(expectedName, stageSet);
        String expectedStateName = "Initial";
        FiniteStateMachine fsm = builder.complete(builder.newStandardState(expectedStateName, stageSet.getStages().get(0)).complete());

        assertThat(fsm.getStageSet()).isPresent();
        assertThat(fsm.getInitialState().getStage()).isPresent();

    }

    private StateTransitionEventType createNewStateTransitionEventType(String symbol) {
        return this.getTestService().newCustomStateTransitionEventType(symbol, "CMP");
    }

    private FiniteStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFiniteStateMachineService();
    }

    private FiniteStateMachineServiceImpl getTestService(Thesaurus thesaurus) {
        FiniteStateMachineServiceImpl finiteStateMachineService = getTestService();
        NlsService nlsService = mock(NlsService.class);
        when(nlsService.getThesaurus(anyString(), any())).thenReturn(thesaurus);
        finiteStateMachineService.setNlsService(nlsService);
        return finiteStateMachineService;
    }

}