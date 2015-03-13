package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.*;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;

import java.sql.SQLException;
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
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts
        assertThat(deviceLifeCycle.getFinateStateMachine()).isNotNull();
        assertThat(deviceLifeCycle.getFinateStateMachine().getId()).isEqualTo(stateMachine.getId());
        assertThat(deviceLifeCycle.getId()).isGreaterThan(0);
        assertThat(deviceLifeCycle.getName()).isNotNull();
        assertThat(deviceLifeCycle.getCreationTimestamp()).isNotNull();
        assertThat(deviceLifeCycle.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void findNewEmptyDeviceLifeCycle() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
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
    public void addAuthorizedBusinessProcessAction() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        builder
            .newCustomAction(state, expectedDeploymentId, expectedProcessId)
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_LEVEL + "}")
    @Test
    public void addAuthorizedBusinessProcessActionWithoutLevels() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
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
    public void addAuthorizedBusinessProcessActionWithNullDeploymentId() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, null, "processId")
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].deploymentId")
    @Test
    public void addAuthorizedBusinessProcessActionWithEmptyDeploymentId() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "", "processId")
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "actions[0].deploymentId")
    @Test
    public void addAuthorizedBusinessProcessActionWithTooLongDeploymentId() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, Strings.repeat("Too long", 100), "processId")
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].processId")
    @Test
    public void addAuthorizedBusinessProcessActionWithNullProcessId() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId", null)
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].processId")
    @Test
    public void addAuthorizedBusinessProcessActionWithEmptyProcessId() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId", "")
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "actions[0].processId")
    @Test
    public void addAuthorizedBusinessProcessActionWithTooLongProcessId() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        builder
            .newCustomAction(state, "deploymentId", Strings.repeat("Too long", 100))
            .add(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    private FinateStateMachine findDefaultFinateStateMachine() {
        return inMemoryPersistence
                .getService(FinateStateMachineService.class)
                .findFinateStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_FINATE_STATE_MACHINE_NAME.getDefaultFormat())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finate state machine"));
    }

    private DeviceLifeCycleConfigurationService getTestService() {
        return inMemoryPersistence.getDeviceLifeCycleConfigurationService();
    }

}