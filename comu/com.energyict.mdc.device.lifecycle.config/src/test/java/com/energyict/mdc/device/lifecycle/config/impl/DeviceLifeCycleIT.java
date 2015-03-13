package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedStandardTransitionAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.TransitionType;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.*;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link DeviceLifeCycleImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (12:01)
 */
public class DeviceLifeCycleIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(DeviceLifeCycleIT.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Transactional
    @Test
    public void createNewEmptyDeviceLifeCycle() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts
        assertThat(deviceLifeCycle.getFiniteStateMachine()).isNotNull();
        assertThat(deviceLifeCycle.getFiniteStateMachine().getId()).isEqualTo(stateMachine.getId());
        assertThat(deviceLifeCycle.getId()).isGreaterThan(0);
        assertThat(deviceLifeCycle.getName()).isNotNull();
        assertThat(deviceLifeCycle.getCreationTimestamp()).isNotNull();
        assertThat(deviceLifeCycle.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void findNewEmptyDeviceLifeCycle() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(stateMachine).complete();
        deviceLifeCycle.save();
        long id = deviceLifeCycle.getId();

        // Business method
        Optional<DeviceLifeCycle> found = this.getTestService().findDeviceLifeCycle(id);

        // Asserts
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Transactional
    @Test
    public void findDeviceLifeCycleThatDoesNotExist() {
        // Business method
        Optional<DeviceLifeCycle> found = this.getTestService().findDeviceLifeCycle(1);

        // Asserts
        assertThat(found.isPresent()).isFalse();
    }

    @Transactional
    @Test
    public void addBusinessProcessAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        builder
            .newCustomAction(state, expectedDeploymentId, expectedProcessId)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(state.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedBusinessProcessAction.class);
        AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) authorizedAction;
        assertThat(businessProcessAction.getDeploymentId()).isEqualTo(expectedDeploymentId);
        assertThat(businessProcessAction.getProcessId()).isEqualTo(expectedProcessId);
    }

    @Transactional
    @Test
    public void findAuthorizedActionsForState() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        builder
            .newCustomAction(state, expectedDeploymentId, expectedProcessId)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions(state);

        // Asserts
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(state.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedBusinessProcessAction.class);
        AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) authorizedAction;
        assertThat(businessProcessAction.getDeploymentId()).isEqualTo(expectedDeploymentId);
        assertThat(businessProcessAction.getProcessId()).isEqualTo(expectedProcessId);
    }

    @Transactional
    @Test
    public void findAuthorizedActionsForStateWithoutAuthorizations() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State active = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        State inactive = stateMachine.getState(DefaultState.INACTIVE.getKey()).get();
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        builder
            .newCustomAction(active, expectedDeploymentId, expectedProcessId)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions(inactive);

        // Asserts
        assertThat(authorizedActions).isEmpty();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_LEVEL + "}")
    @Test
    public void addBusinessProcessActionWithoutLevels() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId1", "processId")
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].deploymentId")
    @Test
    public void addBusinessProcessActionWithNullDeploymentId() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, null, "processId")
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].deploymentId")
    @Test
    public void addBusinessProcessActionWithEmptyDeploymentId() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "", "processId")
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "actions[0].deploymentId")
    @Test
    public void addBusinessProcessActionWithTooLongDeploymentId() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, Strings.repeat("Too long", 100), "processId")
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].processId")
    @Test
    public void addBusinessProcessActionWithNullProcessId() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId", null)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].processId")
    @Test
    public void addBusinessProcessActionWithEmptyProcessId() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId", "")
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "actions[0].processId")
    @Test
    public void addBusinessProcessActionWithTooLongProcessId() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId", Strings.repeat("Too long", 100))
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void addStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.EXAMPLE)
            .addCheck(MicroCheck.EXAMPLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.EXAMPLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.EXAMPLE);
    }

    @Transactional
    @Test
    public void addStandardTransitionActionWithoutChecks() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.EXAMPLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).isEmpty();
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.EXAMPLE);
    }

    @Transactional
    @Test
    public void addStandardTransitionActionWithoutActions() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addCheck(MicroCheck.EXAMPLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.isStandard()).isTrue();
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.EXAMPLE);
        assertThat(transitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void addCustomTransitionActionWithoutActions() {
        FiniteStateMachine stateMachine = this.createFiniteStateMachineWithCustomTransitions();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAllChecks(EnumSet.allOf(MicroCheck.class))
            .addAllActions(EnumSet.allOf(MicroAction.class))
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) authorizedAction;
        assertThat(transitionAction.isStandard()).isFalse();
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.EXAMPLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.EXAMPLE);
    }

    private FiniteStateMachine findDefaultFiniteStateMachine() {
        return inMemoryPersistence
                .getService(FiniteStateMachineService.class)
                .findFiniteStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_FINITE_STATE_MACHINE_NAME.getDefaultFormat())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finite state machine"));
    }

    private FiniteStateMachine createFiniteStateMachineWithCustomTransitions() {
        FiniteStateMachineService finiteStateMachineService = inMemoryPersistence.getService(FiniteStateMachineService.class);
        CustomStateTransitionEventType eventType = finiteStateMachineService.newCustomStateTransitionEventType("forTestingPurposesOnly");
        eventType.save();
        FiniteStateMachineBuilder stateMachineBuilder = finiteStateMachineService.newFiniteStateMachine("For Testing Purposes Only");
        State b = stateMachineBuilder.newCustomState("B").complete();
        stateMachineBuilder.newCustomState("A").on(eventType).transitionTo(b).complete();
        FiniteStateMachine stateMachine = stateMachineBuilder.complete();
        stateMachine.save();
        return stateMachine;
    }

    private DeviceLifeCycleConfigurationService getTestService() {
        return inMemoryPersistence.getDeviceLifeCycleConfigurationService();
    }

}