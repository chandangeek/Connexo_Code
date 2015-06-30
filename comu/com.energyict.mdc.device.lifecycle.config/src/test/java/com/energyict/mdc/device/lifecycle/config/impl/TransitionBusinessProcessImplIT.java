package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcessInUseException;
import com.energyict.mdc.device.lifecycle.config.UnknownTransitionBusinessProcessException;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link TransitionBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (10:15)
 */
public class TransitionBusinessProcessImplIT {

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
    public void createWithoutConstraint() {
        String expectedDeploymentId = "deploymentId";
        String expectedProcessId = "processId";

        // Business method
        TransitionBusinessProcess process = this.getTestService().enableAsTransitionBusinessProcess(expectedDeploymentId, expectedProcessId);

        // Asserts
        assertThat(process).isNotNull();
        assertThat(process.getId()).isNotZero();
        assertThat(process.getDeploymentId()).isEqualTo(expectedDeploymentId);
        assertThat(process.getProcessId()).isEqualTo(expectedProcessId);
    }

    @Transactional
    @Test
    public void findAfterCreate() {
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedDeploymentId = "deploymentId";
        String expectedProcessId = "processId";
        testService.enableAsTransitionBusinessProcess(expectedDeploymentId, expectedProcessId);

        // Business method
        List<TransitionBusinessProcess> businessProcesses = testService.findTransitionBusinessProcesses();

        // Asserts
        Optional<String> deploymentId = businessProcesses
                .stream()
                .map(TransitionBusinessProcess::getDeploymentId)
                .filter(ip -> ip.equals(expectedDeploymentId))
                .findAny();
        assertThat(deploymentId).contains(expectedDeploymentId);
        Optional<String> processId = businessProcesses
                .stream()
                .map(TransitionBusinessProcess::getProcessId)
                .filter(ip -> ip.equals(expectedProcessId))
                .findAny();
        assertThat(processId).contains(expectedProcessId);
    }

    @Transactional
    @Test
    public void findAllWhenNoneExists() {
        // Business method
        List<TransitionBusinessProcess> businessProcesses = this.getTestService().findTransitionBusinessProcesses();

        // Asserts
        assertThat(businessProcesses).isEmpty();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "deploymentId")
    @Test
    public void addWithNullDeploymentId() {
        // Business method
        this.getTestService().enableAsTransitionBusinessProcess(null, "processId");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "deploymentId")
    @Test
    public void addWithEmptyDeploymentId() {
        // Business method
        this.getTestService().enableAsTransitionBusinessProcess("", "processId");
        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "deploymentId")
    @Test
    public void addWithTooLongDeploymentId() {
        // Business method
        this.getTestService().enableAsTransitionBusinessProcess(Strings.repeat("Too long", 100), "processId");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "processId")
    @Test
    public void addWithNullProcessId() {
        // Business method
        this.getTestService().enableAsTransitionBusinessProcess("deploymentId", null);

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "processId")
    @Test
    public void addWithEmptyProcessId() {
        // Business method
        this.getTestService().enableAsTransitionBusinessProcess("deploymentId", "");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "processId")
    @Test
    public void addWithTooLongProcessId() {
        // Business method
        this.getTestService().enableAsTransitionBusinessProcess("deploymentId", Strings.repeat("Too long", 100));

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void disableWhenNotUsed() {
        String deploymentId = "deploymentId";
        String processId = "processId";
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        testService.enableAsTransitionBusinessProcess(deploymentId, processId);

        // Business method
        testService.disableAsTransitionBusinessProcess(deploymentId, processId);

        // Asserts
        List<TransitionBusinessProcess> businessProcesses = testService.findTransitionBusinessProcesses();
        assertThat(businessProcesses
                .stream()
                .map(TransitionBusinessProcess::getDeploymentId)
                .filter(ip -> ip.equals(deploymentId))
                .findAny())
            .isEmpty();
        assertThat(businessProcesses
                .stream()
                .map(TransitionBusinessProcess::getProcessId)
                .filter(ip -> ip.equals(processId))
                .findAny())
            .isEmpty();
    }

    @Transactional
    @Test(expected = TransitionBusinessProcessInUseException.class)
    public void disableWhenUsed() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String deploymentId = "deploymentId1";
        String processId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(deploymentId, processId);
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        String expectedActionName = "addBusinessProcessAction";
        builder
            .newCustomAction(state, expectedActionName, process)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        testService.disableAsTransitionBusinessProcess(deploymentId, processId);

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test(expected = UnknownTransitionBusinessProcessException.class)
    public void disableWhenNotEnabled() {
        DeviceLifeCycleConfigurationService testService = this.getTestService();

        // Business method
        testService.disableAsTransitionBusinessProcess("disableWhenNotEnabled", "disableWhenNotEnabled");

        // Asserts: see expected exception rule
    }

    private DeviceLifeCycleConfigurationService getTestService() {
        return inMemoryPersistence.getDeviceLifeCycleConfigurationService();
    }

    private FiniteStateMachine findDefaultFiniteStateMachine() {
        return inMemoryPersistence
                .getService(FiniteStateMachineService.class)
                .findFiniteStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finite state machine"));
    }

}