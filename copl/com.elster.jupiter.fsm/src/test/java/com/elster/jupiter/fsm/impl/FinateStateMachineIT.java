package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
import com.elster.jupiter.fsm.FinateStateMachineUpdater;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.UnknownProcessReferenceException;
import com.elster.jupiter.fsm.UnknownStateException;
import com.elster.jupiter.fsm.UnsupportedStateTransitionException;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link FinateStateMachineImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (17:12)
 */
public class FinateStateMachineIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(FinateStateMachineIT.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void stateMachineCannotHaveANullName() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(null, "test");
        builder.newCustomState("Just one").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void stateMachineCannotHaveAnEmptyName() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("", "test");
        builder.newCustomState("Just one").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void stateMachineCannotHaveAnExtremelyLongName() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(Strings.repeat("Too long", 100), "test");
        builder.newCustomState("Just one").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "topic")
    @Test
    public void stateMachineCannotHaveANullTopic() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("stateMachineCannotHaveANullTopic", null);
        builder.newCustomState("Just one").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "topic")
    @Test
    public void stateMachineCannotHaveAnEmptyTopic() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("stateMachineCannotHaveANullTopic", "");
        builder.newCustomState("Just one").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "topic")
    @Test
    public void stateMachineCannotHaveAnExtremelyLongTopic() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("stateMachineCannotHaveANullTopic", Strings.repeat("Too long", 100));
        builder.newCustomState("Just one").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.AT_LEAST_ONE_STATE)
    @Test
    public void stateMachineMustHaveAtLeastOneState() {
        FinateStateMachine stateMachine = this.getTestService().newFinateStateMachine("stateMachineMustHaveAtLeastOneState", "test").complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_FINATE_STATE_MACHINE_NAME)
    @Test
    public void createDuplicateStateMachine() {
        String expectedName = "notUnique";
        FinateStateMachineBuilder builder1 = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder1.newCustomState("Initial").complete();
        FinateStateMachine stateMachine = builder1.complete();
        stateMachine.save();

        FinateStateMachineBuilder builder2 = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder2.newCustomState("More initial").complete();
        FinateStateMachine duplicate = builder2.complete();

        // Business method
        duplicate.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateButNoProcesses() {
        String expectedName = "stateMachineWithOneStateButNoProcesses";
        String expectedTopic = "test";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
        assertThat(stateMachine.getCreationTimestamp()).isNotNull();
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
    }

    @Transactional
    @Test
    public void findStateThatExists() {
        String expectedName = "findStateThatExists";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

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
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newCustomState("Initial").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

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
        String expectedTopic = "test";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachine reloaded = getTestService().findFinateStateMachineByName(stateMachine.getName()).get();

        // Asserts
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
        assertThat(reloaded.getStates()).hasSize(1);
        State state = reloaded.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        assertThat(state.isCustom()).isTrue();
    }

    @Transactional
    @Test
    public void findCreatedStateMachineWithOneStandardStateButNoProcesses() {
        String expectedName = "findCreatedStateMachineWithOneStandardStateButNoProcesses";
        String expectedTopic = "test";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String expectedStateName = "Initial";
        builder.newStandardState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachine reloaded = getTestService().findFinateStateMachineByName(stateMachine.getName()).get();

        // Asserts
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
        assertThat(reloaded.getStates()).hasSize(1);
        State state = reloaded.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        assertThat(state.isCustom()).isFalse();
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateAndBothEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndBothEntryAndExitProcesses";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder
            .newCustomState(expectedStateName)
            .onEntry("onEntryDepId", "onEntry")
            .onExit("onExitDepId", "onExit")
            .complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry");
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit");
    }

    @Transactional
    @Test
    public void findStateMachineWithOneStateAndBothEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndBothEntryAndExitProcesses";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder
            .newCustomState(expectedStateName)
            .onEntry("onEntryDepId", "onEntry")
            .onExit("onExitDepId", "onExit")
            .complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(stateMachine.getName()).get();

        // Asserts
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(1);
        State state = reloaded.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry");
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit");
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateAndMultipleEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndMultipleEntryAndExitProcesses";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder
            .newCustomState(expectedStateName)
            .onEntry("onEntryDepId", "onEntry1")
            .onEntry("onEntryDepId", "onEntry2")
            .onExit("onExitDepId", "onExit1")
            .onExit("onExitDepId", "onExit2")
            .complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(2);
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry1");
        assertThat(onEntryProcesses.get(1).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(1).getProcessId()).isEqualTo("onEntry2");
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(2);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit1");
        assertThat(onExitProcesses.get(1).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(1).getProcessId()).isEqualTo("onExit2");
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @Test
    public void createStateMachineWithOneStateAndTooBigDeploymentId() {
        String expectedName = "createStateMachineWithOneStateAndTooBigDeploymentId";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder
            .newCustomState(expectedStateName)
            .onEntry(Strings.repeat("deploymentId", 100), "onEntry")
            .complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @Test
    public void createStateMachineWithOneStateAndTooBigProcessId() {
        String expectedName = "createStateMachineWithOneStateAndTooBigProcessId";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder
            .newCustomState(expectedStateName)
            .onEntry("deploymentId", Strings.repeat("onEntry", 100))
            .complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "states[0].name")
    @Test
    public void createStateMachineWithOneStateWithNullName() {
        String expectedName = "createStateMachineWithOneStateWithNullName";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newCustomState(null).complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "states[0].name")
    @Test
    public void createStateMachineWithOneStateWithEmptyName() {
        String expectedName = "createStateMachineWithOneStateWithEmptyName";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newCustomState("").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_STATE_NAME, strict = false)
    @Test
    public void createStateMachineWithTwiceTheSameStateName() {
        String expectedName = "createStateMachineWithTwiceTheSameStateName";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newCustomState("State").complete();
        builder.newCustomState("State").complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @Test(expected = IllegalStateException.class)
    public void addStateAfterCompletion() {
        String expectedName = "completeTwice";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newCustomState("Before completion").complete();
        builder.complete();

        // Business method
        builder.newCustomState("After completion");

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateTransition() {
        String expectedName = "createStateMachineWithOneStateTransition";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

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
    public void findFinateStateMachinesUsing() {
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType otherEventType = this.createNewStateTransitionEventType("#other");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("UsingCommissioned", "test");
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachineUsingCommissioned = builder.complete();
        stateMachineUsingCommissioned.save();

        FinateStateMachineBuilder otherBuilder = this.getTestService().newFinateStateMachine("Other", "elsewhere");
        FinateStateMachineBuilder.StateBuilder aStateBuilder = otherBuilder.newCustomState("A");
        FinateStateMachineBuilder.StateBuilder bStateBuilder = otherBuilder.newCustomState("B");
        State a = aStateBuilder.on(otherEventType).transitionTo(bStateBuilder).complete();
        bStateBuilder.on(otherEventType).transitionTo(a).complete();
        otherBuilder.complete().save();

        // Business method
        List<FinateStateMachine> finateStateMachines = this.getTestService().findFinateStateMachinesUsing(commissionedEventType);

        // Asserts
        assertThat(finateStateMachines).hasSize(1);
        assertThat(finateStateMachines.get(0).getId()).isEqualTo(stateMachineUsingCommissioned.getId());
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
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        // Create default States
        State deleted = builder.newCustomState("Deleted").complete();
        State decommissioned = builder
                .newCustomState("Decommissioned")
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        FinateStateMachineBuilder.StateBuilder activeBuilder = builder.newCustomState("Active");
        FinateStateMachineBuilder.StateBuilder inactiveBuilder = builder.newCustomState("Inactive");
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
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(7);
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
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(initialName, "test");
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();
        long initialVersion = stateMachine.getVersion();

        // Business method
        String newName = "renamed";
        stateMachine.update().setName(newName).complete();

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
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_FINATE_STATE_MACHINE_NAME)
    @Test
    public void renameStateMachineToOneThatAlreadyExists() {
        String duplicateName = "alreadyExists";
        FinateStateMachineBuilder builder1 = this.getTestService().newFinateStateMachine(duplicateName, "test-topic-1");
        builder1.newCustomState("Initial").complete();
        builder1.complete().save();

        String initialName = "initialName";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(initialName, "test-topic-2");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setName(duplicateName).complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void setNameToEmptyString() {
        String initialName = "setNameToEmptyString";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(initialName, "test-topic");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setName("").complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void setNameToNull() {
        String initialName = "setNameToNull";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(initialName, "test-topic");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setName(null).complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void setNameToExtremelyLongString() {
        String initialName = "setNameToExtremelyLongString";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(initialName, "test-topic");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setName(Strings.repeat("Too long", 100)).complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void changeTopic() {
        String expectedName = "changeTopic";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        State commissioned = builder.newCustomState("Commissioned").complete();
        State inStock = builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        String updatedTopic = "changed-topic";
        stateMachine.update().setTopic(updatedTopic).complete();

        // Asserts
        assertThat(stateMachine.getTopic()).isEqualTo(updatedTopic);
        // Check that there was no effect on the name
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        // Check that there was no effect on the States and transitions
        assertThat(stateMachine.getStates()).hasSize(2);
        assertThat(stateMachine.getTransitions()).hasSize(1);
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(commissionedEventType.getId());
        assertThat(stateTransition.getFrom().getId()).isEqualTo(inStock.getId());
        assertThat(stateTransition.getTo().getId()).isEqualTo(commissioned.getId());
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "topic")
    @Test
    public void changeTopicToEmptyString() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("changeTopicToEmptyString", "test-topic");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setTopic("").complete();

        // Asserts: see constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "topic")
    @Test
    public void changeTopicToNull() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("changeTopicToNull", "test-topic");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setTopic(null).complete();

        // Asserts: see constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "topic")
    @Test
    public void changeTopicToExtremelyLongName() {
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("changeTopicToExtremelyLongName", "test-topic");
        builder.newCustomState("Commissioned").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().setTopic(Strings.repeat("Too long", 100)).complete();

        // Asserts: see constraint violation rule
    }

    @Transactional
    @Test
    public void removeState() {
        String expectedName = "removeState";
        String expectedTopic = "test-topic";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().removeState(commissioned).complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
        assertThat(stateMachine.getStates()).hasSize(1);
        assertThat(stateMachine.getStates().get(0).getName()).isEqualTo("InStock");
        assertThat(stateMachine.getTransitions()).isEmpty();
    }

    @Transactional
    @Test
    public void stateRemovedAfterFind() {
        String expectedName = "stateRemovedAfterFind";
        String expectedTopic = "test-topic";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().removeState(commissioned).complete();

        // Asserts
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getTopic()).isEqualTo(expectedTopic);
        assertThat(reloaded.getStates()).hasSize(1);
        assertThat(reloaded.getStates().get(0).getName()).isEqualTo("InStock");
        assertThat(reloaded.getTransitions()).isEmpty();
    }

    @Transactional
    @Test
    public void removeStateByName() {
        String expectedName = "removeStateByName";
        String expectedTopic = "test-topic";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().removeState("InStock").complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
        assertThat(stateMachine.getStates()).hasSize(1);
        assertThat(stateMachine.getStates().get(0).getName()).isEqualTo("Commissioned");
        assertThat(stateMachine.getTransitions()).isEmpty();
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void removeStateThatDoesNotExist() {
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder targetBuilder = this.getTestService().newFinateStateMachine("removeStateThatDoesNotExist", "test-topic-1");
        State commissioned = targetBuilder.newCustomState("Commissioned").complete();
        targetBuilder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = targetBuilder.complete();
        stateMachine.save();

        FinateStateMachineBuilder otherBuilder = this.getTestService().newFinateStateMachine("Other", "test-topic-2");
        State whatever = otherBuilder.newCustomState("Whatever").complete();
        FinateStateMachine other = otherBuilder.complete();
        other.save();

        // Business method
        stateMachine.update().removeState(whatever).complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void removeStateByNameThatDoesNotExist() {
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine("removeStateThatDoesNotExist", "test-topic-1");
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().removeState("does not exist").complete();

        // Asserts: see expected exception rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.AT_LEAST_ONE_STATE)
    @Test
    public void cannotRemoveLastState() {
        String expectedName = "cannotRemoveLastState";
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        builder.newCustomState("Single").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        stateMachine.update().removeState("Single").complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void renameStateByName() {
        String expectedName = "renameState";
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String initialName = "Single";
        builder.newCustomState(initialName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        String newName = "renamed";
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state(initialName).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getState(newName).isPresent()).isTrue();
        assertThat(stateMachine.getState(initialName).isPresent()).isFalse();
    }

    @Transactional
    @Test
    public void findAfterRenameStateByName() {
        String expectedName = "findAfterRenameStateByName";
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String initialName = "Single";
        long initialStateVersion = builder.newCustomState(initialName).complete().getVersion();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        String newName = "renamed";
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state(initialName).setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(expectedName).get();
        assertThat(reloaded.getState(newName).isPresent()).isTrue();
        assertThat(reloaded.getState(initialName).isPresent()).isFalse();
        State updatedState = reloaded.getState(newName).get();
        assertThat(updatedState.getVersion()).isGreaterThan(initialStateVersion);
        assertThat(updatedState.getModifiedTimestamp()).isNotNull();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_STATE_NAME, strict = false)
    @Test
    public void renameStateToOneThatAlreadyExists() {
        String expectedName = "renameStateToOneThatAlreadyExists";
        String expectedTopic = "test-topic";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        String newName = "InStock";
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("Commissioned").setName(newName).complete();
        stateMachineUpdater.complete();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test(expected = UnknownStateException.class)
    public void renameStateThatDoesNotExist() {
        String expectedName = "renameStateThatDoesNotExist";
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String initialName = "Single";
        builder.newCustomState(initialName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
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
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        FinateStateMachineBuilder.StateBuilder inStock = builder.newCustomState("InStock");
        builder.newCustomState("Initial").on(deliveredEventType).transitionTo(inStock).complete();
        inStock.on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("InStock").prohibit(commissionedEventType).complete();
        stateMachineUpdater.complete();

        // Asserts
        // Check that there was no effect on the name and the topic
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
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
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("InStock").prohibit(deliveredEventType).complete();

        // Asserts: see expected exception rul
    }

    @Transactional
    @Test
    public void removeTransitionAndReload() {
        String expectedName = "removeTransitionAndReload";
        StateTransitionEventType deliveredEventType = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        String expectedTopic = "test-topic";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        State commissioned = builder.newCustomState("Commissioned").complete();
        FinateStateMachineBuilder.StateBuilder inStock = builder.newCustomState("InStock");
        builder.newCustomState("Initial").on(deliveredEventType).transitionTo(inStock).complete();
        inStock.on(commissionedEventType).transitionTo(commissioned).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("InStock").prohibit(commissionedEventType).complete();
        stateMachineUpdater.complete();

        // Asserts
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(expectedName).get();
        assertThat(reloaded.getStates()).hasSize(3);
        assertThat(reloaded.getTransitions()).hasSize(1);
        StateTransition stateTransition = reloaded.getTransitions().get(0);
        assertThat(stateTransition.getEventType().getId()).isEqualTo(deliveredEventType.getId());
    }

    @Transactional
    @Test
    public void addBothEntryAndExitProcessesToExistingState() {
        String expectedName = "addBothEntryAndExitProcessesToExistingState";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("Initial").onEntry("onEntryDepId", "onEntry").onExit("onExitDepId", "onExit").complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry");
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit");
    }

    @Transactional
    @Test
    public void addMultipleEntryAndExitProcessesToExistingState() {
        String expectedName = "addBothEntryAndExitProcessesToExistingState";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("Initial")
                .onEntry("onEntryDepId", "onEntry1")
                .onEntry("onEntryDepId", "onEntry2")
                .onExit("onExitDepId", "onExit1")
                .onExit("onExitDepId", "onExit2")
                .complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(2);
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry1");
        assertThat(onEntryProcesses.get(1).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(1).getProcessId()).isEqualTo("onEntry2");
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(2);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit1");
        assertThat(onExitProcesses.get(1).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(1).getProcessId()).isEqualTo("onExit2");
    }

    /**
     * Builds an incomplete version of the default life cycle
     * with missing {@link State}s and transitions and then
     * completes that via the {@link FinateStateMachineUpdater} interface.
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
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
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
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.removeState(temporary);
        FinateStateMachineBuilder.StateBuilder activeBuilder = stateMachineUpdater.newStandardState("Active");
        FinateStateMachineBuilder.StateBuilder inactiveBuilder = stateMachineUpdater.newStandardState("Inactive");
        State active = activeBuilder
            .on(decommissionedEventType).transitionTo(decommissioned)
            .on(deactivated).transitionTo(inactiveBuilder)
            .onEntry("onEntryDepId", "onEntry")
            .onExit("onExitDepId", "onExit")
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
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(expectedName).get();
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
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry");
        List<ProcessReference> onExitProcesses = reloaded.getState(active.getName()).get().getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit");
    }

    @Transactional
    @Test
    public void testAddTransitionByStateName() {
        String expectedName = "testAddTransitionByStateName";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        builder.newCustomState("Commissioned").complete();
        builder.newCustomState("InStock").complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business methods
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("InStock").on(commissionedEventType).transitionTo("Commissioned");

        // Asserts
        FinateStateMachine reloaded = this.getTestService().findFinateStateMachineByName(expectedName).get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getStates()).hasSize(2);
        List<StateTransition> transitions = reloaded.getTransitions();
        assertThat(transitions).hasSize(1);
    }

    @Transactional
    @Test
    public void removeEntryAndExitProcesses() {
        String expectedName = "removeEntryAndExitProcesses";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName)
                .onEntry("onEntryDepId", "onEntry1")
                .onEntry("onEntryDepId", "onEntry2")
                .onExit("onExitDepId", "onExit1")
                .onExit("onExitDepId", "onExit2")
                .complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("Initial")
                .removeOnEntry("onEntryDepId", "onEntry1")
                .removeOnExit("onExitDepId", "onExit2")
                .complete();
        stateMachineUpdater.complete();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(1);
        State state = stateMachine.getStates().get(0);
        assertThat(state.getName()).isEqualTo(expectedStateName);
        List<ProcessReference> onEntryProcesses = state.getOnEntryProcesses();
        assertThat(onEntryProcesses).hasSize(1);
        assertThat(onEntryProcesses.get(0).getDeploymentId()).isEqualTo("onEntryDepId");
        assertThat(onEntryProcesses.get(0).getProcessId()).isEqualTo("onEntry2");
        List<ProcessReference> onExitProcesses = state.getOnExitProcesses();
        assertThat(onExitProcesses).hasSize(1);
        assertThat(onExitProcesses.get(0).getDeploymentId()).isEqualTo("onExitDepId");
        assertThat(onExitProcesses.get(0).getProcessId()).isEqualTo("onExit1");
    }

    @Transactional
    @Test
    public void removeAllEntryAndExitProcesses() {
        String expectedName = "removeEntryAndExitProcesses";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName)
                .onEntry("onEntryDepId", "onEntry1")
                .onEntry("onEntryDepId", "onEntry2")
                .onExit("onExitDepId", "onExit1")
                .onExit("onExitDepId", "onExit2")
                .complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("Initial")
                .removeOnEntry("onEntryDepId", "onEntry1")
                .removeOnEntry("onEntryDepId", "onEntry2")
                .removeOnExit("onExitDepId", "onExit1")
                .removeOnExit("onExitDepId", "onExit2")
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
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test-topic");
        String expectedStateName = "Initial";
        builder.newCustomState(expectedStateName)
                .onEntry("onEntryDepId", "onEntry")
                .onExit("onExitDepId", "onExit")
                .complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        FinateStateMachineUpdater stateMachineUpdater = stateMachine.update();
        stateMachineUpdater.state("Initial")
                .removeOnEntry("onEntryDepId", "does not exist")
                .complete();    // Not expecting to get this far in fact

        // Asserts: see expected exception rule
    }

    private StateTransitionEventType createNewStateTransitionEventType(String symbol) {
        StateTransitionEventType commissionedEventType = this.getTestService().newCustomStateTransitionEventType(symbol);
        commissionedEventType.save();
        return commissionedEventType;
    }

    private FinateStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFinateStateMachineService();
    }

}