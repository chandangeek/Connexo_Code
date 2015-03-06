package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
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
        builder.newState("Just one").complete();
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
        builder.newState("Just one").complete();
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
        builder.newState("Just one").complete();
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
        builder.newState("Just one").complete();
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
        builder.newState("Just one").complete();
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
        builder.newState("Just one").complete();
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
    @Test
    public void createStateMachineWithOneStateButNoProcesses() {
        String expectedName = "stateMachineWithOneStateButNoProcesses";
        String expectedTopic = "test";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String expectedStateName = "Initial";
        builder.newState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getTopic()).isEqualTo(expectedTopic);
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
        builder.newState(expectedStateName).complete();
        FinateStateMachine stateMachine = builder.complete();
        stateMachine.save();

        // Business method
        Optional<State> found = stateMachine.getState(expectedStateName);

        // Asserts
        assertThat(found).isNotNull();
        assertThat(found.isPresent()).isTrue();
    }

    @Transactional
    @Test
    public void findStateThatDoesNotExist() {
        String expectedName = "findStateThatDoesNotExist";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newState("Initial").complete();
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
    public void findCreatedStateMachineWithOneStateButNoProcesses() {
        String expectedName = "stateMachineWithOneStateButNoProcesses";
        String expectedTopic = "test";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, expectedTopic);
        String expectedStateName = "Initial";
        builder.newState(expectedStateName).complete();
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
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_FINATE_STATE_MACHINE_NAME)
    @Test
    public void createDuplicateStateMachine() {
        String expectedName = "notUnique";
        FinateStateMachineBuilder builder1 = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder1.newState("Initial").complete();
        FinateStateMachine stateMachine = builder1.complete();
        stateMachine.save();

        FinateStateMachineBuilder builder2 = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder2.newState("More initial").complete();
        FinateStateMachine duplicate = builder2.complete();

        // Business method
        duplicate.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateAndBothEntryAndExitProcesses() {
        String expectedName = "createStateMachineWithOneStateAndBothEntryAndExitProcesses";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        String expectedStateName = "Initial";
        builder
            .newState(expectedStateName)
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
            .newState(expectedStateName)
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
            .newState(expectedStateName)
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
            .newState(expectedStateName)
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
            .newState(expectedStateName)
            .onEntry("deploymentId", Strings.repeat("onEntry", 100))
            .complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void createStateMachineWithOneStateWithNullName() {
        String expectedName = "createStateMachineWithOneStateWithNullName";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newState(null).complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts: see expected contraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void createStateMachineWithOneStateWithEmptyName() {
        String expectedName = "createStateMachineWithOneStateWithEmptyName";
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        builder.newState("").complete();
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
        builder.newState("State").complete();
        builder.newState("State").complete();
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
        builder.newState("Before completion").complete();
        builder.complete();

        // Business method
        builder.newState("After completion");

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void createStateMachineWithOneStateTransition() {
        String expectedName = "createStateMachineWithOneStateTransition";
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        FinateStateMachineBuilder builder = this.getTestService().newFinateStateMachine(expectedName, "test");
        State commissioned = builder.newState("Commissioned").complete();
        State inStock = builder.newState("InStock").on(commissionedEventType).transitionTo(commissioned).complete();
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
        State deleted = builder.newState("Deleted").complete();
        State decommissioned = builder
                .newState("Decommissioned")
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        FinateStateMachineBuilder.StateBuilder activeBuilder = builder.newState("Active");
        FinateStateMachineBuilder.StateBuilder inactiveBuilder = builder.newState("Inactive");
        State active = activeBuilder
                            .on(decommissionedEventType).transitionTo(decommissioned)
                            .on(deactivated).transitionTo(inactiveBuilder)
                            .complete();
        State inactive = inactiveBuilder
                            .on(activated).transitionTo(active)
                            .on(decommissionedEventType).transitionTo(decommissioned)
                            .complete();
        State commissioned = builder
                .newState("Commissioned")
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .complete();
        State inStock = builder
                .newState("InStock")
                .on(activated).transitionTo(active)
                .on(commissionedEventType).transitionTo(commissioned)
                .complete();
        builder
            .newState("Ordered")
            .on(deliveredToWarehouse).transitionTo(inStock)
            .complete();
        FinateStateMachine stateMachine = builder.complete();

        // Business method
        stateMachine.save();

        // Asserts
        assertThat(stateMachine.getName()).isEqualTo(expectedName);
        assertThat(stateMachine.getStates()).hasSize(7);
        List<StateTransition> transitions = stateMachine.getTransitions();
        assertThat(transitions).hasSize(10);
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