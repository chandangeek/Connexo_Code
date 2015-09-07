package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedStandardTransitionAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionType;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
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

    public static final String CUSTOM_EVENT_SYMBOL = "forTestingPurposesOnly";
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
        String expectedName = "createNewEmptyDeviceLifeCycle";
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(expectedName, stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts
        assertThat(deviceLifeCycle.getFiniteStateMachine()).isNotNull();
        assertThat(deviceLifeCycle.getFiniteStateMachine().getId()).isEqualTo(stateMachine.getId());
        assertThat(deviceLifeCycle.getId()).isGreaterThan(0);
        assertThat(deviceLifeCycle.getName()).isEqualTo(expectedName);
        assertThat(deviceLifeCycle.getCreationTimestamp()).isNotNull();
        assertThat(deviceLifeCycle.getVersion()).isNotNull();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void createDeviceLifeCycleWithEmptyName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing("", stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void createDeviceLifeCycleWithNullName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(null, stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void createDeviceLifeCycleWithTooLongName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(Strings.repeat("Too long", 100), stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNIQUE_DEVICE_LIFE_CYCLE_NAME + "}")
    @Test
    public void createDeviceLifeCycleWithDuplicateName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String duplicateName = "createDeviceLifeCycleWithDuplicateName";
        this.getTestService().newDeviceLifeCycleUsing(duplicateName, stateMachine).complete().save();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(duplicateName, stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "maximumFutureEffectiveTimeShift")
    @Test
    public void createDeviceLifeCycleWithoutMaximumFutureEffectiveTimeShift() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "createDeviceLifeCycleWithoutMaximumFutureEffectiveTimeShift";
        this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumFutureEffectiveTimeShift(null)
                .complete().save();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(name, stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "maximumPastEffectiveTimeShift")
    @Test
    public void createDeviceLifeCycleWithoutMaximumPastEffectiveTimeShift() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "createDeviceLifeCycleWithoutMaximumPastEffectiveTimeShift";
        this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumPastEffectiveTimeShift(null)
                .complete().save();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(name, stateMachine).complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void createDeviceLifeCycleWithMaximumFutureEffectiveTimeShift() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "createDeviceLifeCycleWithMaximumFutureEffectiveTimeShift";
        TimeDuration expectedMaximumFutureEffectiveTimeShift = TimeDuration.days(1);

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumFutureEffectiveTimeShift(expectedMaximumFutureEffectiveTimeShift)
                .complete();
        deviceLifeCycle.save();

        // Asserts
        assertThat(deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).isNotNull();
        assertThat(deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).isEqualTo(expectedMaximumFutureEffectiveTimeShift);
    }

    @Transactional
    @Test
    public void createDeviceLifeCycleWithMaximumPastEffectiveTimeShift() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "createDeviceLifeCycleWithMaximumPastEffectiveTimeShift";
        TimeDuration expectedMaximumPastEffectiveTimeShift = TimeDuration.days(1);

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumPastEffectiveTimeShift(expectedMaximumPastEffectiveTimeShift)
                .complete();
        deviceLifeCycle.save();

        // Asserts
        assertThat(deviceLifeCycle.getMaximumPastEffectiveTimeShift()).isNotNull();
        assertThat(deviceLifeCycle.getMaximumPastEffectiveTimeShift()).isEqualTo(expectedMaximumPastEffectiveTimeShift);
    }

    @Transactional
    @Test
    public void maximumFutureEffectiveTimeShiftIsCorrectAfterReload() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "maximumFutureEffectiveTimeShiftIsCorrectAfterReload";
        TimeDuration expectedMaximumFutureEffectiveTimeShift = TimeDuration.days(1);
        DeviceLifeCycle deviceLifeCycle = this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumFutureEffectiveTimeShift(expectedMaximumFutureEffectiveTimeShift)
                .complete();
        deviceLifeCycle.save();
        DeviceLifeCycle reloaded = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId()).get();

        // Business method
        TimeDuration timeShift = reloaded.getMaximumFutureEffectiveTimeShift();

        // Asserts
        assertThat(timeShift).isNotNull();
        assertThat(timeShift).isEqualTo(expectedMaximumFutureEffectiveTimeShift);
    }

    @Transactional
    @Test
    public void maximumPastEffectiveTimeShiftIsCorrectAfterReload() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "maximumPastEffectiveTimeShiftIsCorrectAfterReload";
        TimeDuration expectedMaximumPastEffectiveTimeShift = TimeDuration.days(1);
        DeviceLifeCycle deviceLifeCycle = this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumPastEffectiveTimeShift(expectedMaximumPastEffectiveTimeShift)
                .complete();
        deviceLifeCycle.save();
        DeviceLifeCycle reloaded = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId()).get();

        // Business method
        TimeDuration timeShift = reloaded.getMaximumPastEffectiveTimeShift();

        // Asserts
        assertThat(timeShift).isNotNull();
        assertThat(timeShift).isEqualTo(expectedMaximumPastEffectiveTimeShift);
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MAXIMUM_FUTURE_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE + "}", property = "maximumFutureEffectiveTimeShift")
    @Test
    public void createDeviceLifeCycleWithTooBigMaximumFutureEffectiveTimeShift() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "createDeviceLifeCycleWithTooBigMaximumFutureEffectiveTimeShift";

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumFutureEffectiveTimeShift(TimeDuration.days(365))
                .complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MAXIMUM_PAST_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE + "}", property = "maximumPastEffectiveTimeShift")
    @Test
    public void createDeviceLifeCycleWithTooBigMaximumPastEffectiveTimeShift() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String name = "createDeviceLifeCycleWithTooBigMaximumPastEffectiveTimeShift";

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService()
                .newDeviceLifeCycleUsing(name, stateMachine)
                .maximumPastEffectiveTimeShift(TimeDuration.days(365))
                .complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void findNewEmptyDeviceLifeCycle() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        String expectedName = "findNewEmptyDeviceLifeCycle";
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(expectedName, stateMachine).complete();
        deviceLifeCycle.save();
        long id = deviceLifeCycle.getId();

        // Business method
        Optional<DeviceLifeCycle> found = this.getTestService().findDeviceLifeCycle(id);

        // Asserts
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getName()).isEqualTo(expectedName);
    }

    @Transactional
    @Test
    public void findDeviceLifeCycleThatDoesNotExist() {
        // Business method
        Optional<DeviceLifeCycle> found = this.getTestService().findDeviceLifeCycle(Long.MAX_VALUE);

        // Asserts
        assertThat(found.isPresent()).isFalse();
    }

    @Transactional
    @Test
    public void addBusinessProcessAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);

        // Business method
        String expectedActionName = "addBusinessProcessAction";
        builder
            .newCustomAction(state, expectedActionName, process)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(state.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedBusinessProcessAction.class);
        AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) authorizedAction;
        assertThat(businessProcessAction.getName()).isEqualTo(expectedActionName);
        assertThat(businessProcessAction.getTransitionBusinessProcess().getId()).isEqualTo(process.getId());
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].name")
    @Test
    public void addBusinessProcessActionWithNullName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);

        // Business method
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(state, null, process)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].name")
    @Test
    public void addBusinessProcessActionWithEmptyName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);

        // Business method
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(state, "", process)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "actions[0].name")
    @Test
    public void addBusinessProcessActionWithTooLongName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);

        // Business method
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(state, Strings.repeat("Too long", 100), process)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void findAuthorizedActionsForState() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(state, "custom", process)
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
        assertThat(businessProcessAction.getTransitionBusinessProcess().getId()).isEqualTo(process.getId());
    }

    @Transactional
    @Test
    public void findAuthorizedActionsForStateWithoutAuthorizations() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State active = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        State inactive = stateMachine.getState(DefaultState.INACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(active, "custom", process)
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "actions[0].process")
    @Test
    public void addBusinessProcessActionWithNullProcess() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleConfigurationService testService = this.getTestService();

        // Business method
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(state, "custom", null)
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
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
    }

    @Transactional
    @Test
    public void addStandardTransitionActionWithoutChecks() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
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
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
    }

    @Transactional
    @Test
    public void addStandardTransitionActionWithoutActions() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
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
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(transitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void addTransitionActionForStandardEventWithoutActions() {
        FiniteStateMachine stateMachine = this.createFiniteStateMachineWithStandardEventTypes();
        StateTransition stateTransition =
                stateMachine
                        .getTransitions()
                        .stream()
                        .filter(t -> t.getEventType() instanceof StandardStateTransitionEventType)
                        .findAny().get();

        // Business method
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) authorizedAction;
        assertThat(transitionAction.isStandard()).isFalse();
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
    }

    @Transactional
    @Test
    public void cloningDeviceLifeCycleAlsoClonesFiniteStateMachine() {
        DeviceLifeCycleConfigurationService service = this.getTestService();
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        DeviceLifeCycle deviceLifeCycle = service.newDeviceLifeCycleUsing("createNewEmptyDeviceLifeCycle", stateMachine).complete();
        deviceLifeCycle.save();

        // Business method
        String expectedName = "Cloned";
        DeviceLifeCycle cloned = service.cloneDeviceLifeCycle(deviceLifeCycle, expectedName);

        // Asserts
        assertThat(cloned.getId()).isGreaterThan(0);
        assertThat(cloned.getFiniteStateMachine().getId()).isNotEqualTo(deviceLifeCycle.getFiniteStateMachine().getId());
        assertThat(cloned.getName()).isEqualTo(expectedName);
        assertThat(cloned.getCreationTimestamp()).isNotNull();
        assertThat(cloned.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void cloneWithBusinessProcessAction() {
        DeviceLifeCycleConfigurationService testService = this.getTestService();
        String expectedName = "name1";
        String expectedDeploymentId = "deploymentId1";
        String expectedProcessId = "processId";
        TransitionBusinessProcess process = testService.enableAsTransitionBusinessProcess(expectedName, expectedDeploymentId, expectedProcessId);
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        State state = stateMachine.getState(DefaultState.ACTIVE.getKey()).get();
        DeviceLifeCycleBuilder builder = testService.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newCustomAction(state, "custom", process)
            .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR)
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycle cloned = testService.cloneDeviceLifeCycle(deviceLifeCycle, "Cloned");

        // Asserts
        FiniteStateMachine clonedFiniteStateMachine = cloned.getFiniteStateMachine();
        List<AuthorizedAction> authorizedActions = cloned.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(cloned.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(clonedFiniteStateMachine.getState(state.getName()).get().getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.FOUR);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedBusinessProcessAction.class);
        AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) authorizedAction;
        assertThat(businessProcessAction.getTransitionBusinessProcess().getId()).isEqualTo(process.getId());
    }

    @Transactional
    @Test
    public void cloneWithStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleConfigurationService service = this.getTestService();
        DeviceLifeCycleBuilder builder = service.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAllActions(EnumSet.of(MicroAction.ENABLE_VALIDATION, MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS))
            .addAllChecks(EnumSet.of(MicroCheck.DEFAULT_CONNECTION_AVAILABLE, MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE))
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycle cloned = service.cloneDeviceLifeCycle(deviceLifeCycle, "Cloned");

        // Asserts
        List<AuthorizedAction> authorizedActions = cloned.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(cloned.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE, MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION, MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS);
    }

    @Transactional
    @Test
    public void cloneWithCustomTransitionActionWithoutActions() {
        DeviceLifeCycleConfigurationService service = this.getTestService();
        FiniteStateMachine stateMachine = this.createFiniteStateMachineWithCustomTransitions();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = service.newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycle cloned = service.cloneDeviceLifeCycle(deviceLifeCycle, "Cloned");

        // Asserts
        List<AuthorizedAction> authorizedActions = cloned.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getId()).isGreaterThan(0);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(cloned.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) authorizedAction;
        assertThat(transitionAction.isStandard()).isFalse();
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
    }

    @Transactional
    @Test
    public void newFromDefaultTemplate() {
        DeviceLifeCycleConfigurationService service = this.getTestService();

        // Business method
        DeviceLifeCycle anotherDefault = service.newDefaultDeviceLifeCycle("Second default");

        // Asserts
        List<AuthorizedAction> authorizedActions = anotherDefault.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(11);
        assertThat(authorizedActions).are(new Condition<AuthorizedAction>() {
            @Override
            public boolean matches(AuthorizedAction action) {
                return action instanceof AuthorizedStandardTransitionAction;
            }
        });
        Set<TransitionType> transitionTypes = EnumSet.noneOf(TransitionType.class);
        authorizedActions
                .stream()
                .map(AuthorizedStandardTransitionAction.class::cast)
                .map(AuthorizedStandardTransitionAction::getType)
                .forEach(transitionTypes::add);
        assertThat(transitionTypes).containsOnly(TransitionType.values());
    }

    @Transactional
    @Test
    public void clearLevelsOfStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater.transitionAction(stateTransition).clearLevels().complete();
        deviceLifeCycleUpdater.complete().save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getLevels()).isEmpty();
        assertThat(authorizedAction.getModifiedTimestamp()).isNotNull();
        assertThat(authorizedAction.getModifiedTimestamp()).isNotEqualTo(authorizedAction.getCreationTimestamp());
        // Assert that all other attributes have not changed
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
    }

    @Transactional
    @Test
    public void updateLevelsOfStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater
                .transitionAction(stateTransition)
                .clearLevels()
                .addLevel(AuthorizedAction.Level.THREE, AuthorizedAction.Level.FOUR)
                .complete();
        deviceLifeCycleUpdater.complete().save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.THREE, AuthorizedAction.Level.FOUR);
        assertThat(authorizedAction.getModifiedTimestamp()).isNotNull();
        assertThat(authorizedAction.getModifiedTimestamp()).isNotEqualTo(authorizedAction.getCreationTimestamp());
        // Assert that all other attributes have not changed
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getVersion()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
    }

    @Transactional
    @Test
    public void updateChecksOfStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater
                .transitionAction(stateTransition)
                .clearChecks()
                .addCheck(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED)
                .complete();
        deviceLifeCycleUpdater.complete().save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getModifiedTimestamp()).isNotNull();
        assertThat(authorizedAction.getModifiedTimestamp()).isNotEqualTo(authorizedAction.getCreationTimestamp());
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED);
        // Assert that all other attributes have not changed
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void clearActionsOfStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater.transitionAction(stateTransition).clearActions().complete();
        deviceLifeCycleUpdater.complete().save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getModifiedTimestamp()).isNotNull();
        assertThat(authorizedAction.getModifiedTimestamp()).isNotEqualTo(authorizedAction.getCreationTimestamp());
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getActions()).isEmpty();
        // Assert that all other attributes have not changed
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void updateActionsOfStandardTransitionAction() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater
                .transitionAction(stateTransition)
                .clearActions()
                .addAction(MicroAction.DISABLE_VALIDATION)
                .complete();
        deviceLifeCycleUpdater.complete().save();

        // Asserts
        List<AuthorizedAction> authorizedActions = deviceLifeCycle.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getModifiedTimestamp()).isNotNull();
        assertThat(authorizedAction.getModifiedTimestamp()).isNotEqualTo(authorizedAction.getCreationTimestamp());
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.DISABLE_VALIDATION);
        // Assert that all other attributes have not changed
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void setDeviceLifeCycleName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        String newName = "Updated";
        deviceLifeCycleUpdater.setName(newName).complete().save();
        DeviceLifeCycle updated = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId()).get();

        // Asserts
        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getModifiedTimestamp()).isNotNull();
        assertThat(updated.getModifiedTimestamp()).isNotEqualTo(updated.getCreationTimestamp());
        // Assert that all other attributes have not changed
        List<AuthorizedAction> authorizedActions = updated.getAuthorizedActions();
        assertThat(authorizedActions).hasSize(1);
        AuthorizedAction authorizedAction = authorizedActions.get(0);
        assertThat(authorizedAction.getModifiedTimestamp()).isNotNull();
        assertThat(authorizedAction).isInstanceOf(AuthorizedStandardTransitionAction.class);
        AuthorizedStandardTransitionAction transitionAction = (AuthorizedStandardTransitionAction) authorizedAction;
        assertThat(transitionAction.getType()).isEqualTo(TransitionType.from(stateTransition).get());
        assertThat(transitionAction.getActions()).containsOnly(MicroAction.ENABLE_VALIDATION);
        assertThat(transitionAction.getChecks()).containsOnly(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(authorizedAction.getDeviceLifeCycle().getId()).isEqualTo(deviceLifeCycle.getId());
        assertThat(authorizedAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(authorizedAction.getCreationTimestamp()).isNotNull();
        assertThat(authorizedAction.getState().getId()).isEqualTo(stateTransition.getFrom().getId());
        assertThat(authorizedAction.getVersion()).isNotNull();
    }

    @Transactional
    @Test
    public void removeTransitionActionName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater.removeTransitionAction(stateTransition).complete().save();
        DeviceLifeCycle updated = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId()).get();

        // Asserts
        List<AuthorizedAction> authorizedActions = updated.getAuthorizedActions();
        assertThat(authorizedActions).isEmpty();
    }

    @Transactional
    @Test
    public void obsoleteDeviceLifeCycle() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        deviceLifeCycle.makeObsolete();

        // Asserts
        assertThat(deviceLifeCycle.isObsolete()).isTrue();
        assertThat(deviceLifeCycle.getObsoleteTimestamp()).isNotNull();
    }

    @Transactional
    @Test
    public void obsoleteDeviceLifeCycleWithReload() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        deviceLifeCycle.makeObsolete();

        // Asserts
        DeviceLifeCycle reloaded = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId()).get();
        assertThat(reloaded.isObsolete()).isTrue();
        assertThat(reloaded.getObsoleteTimestamp()).isNotNull();
    }

    @Transactional
    @Test
    public void obsoleteDeviceLifeCycleShowsUpInFindById() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        deviceLifeCycle.makeObsolete();

        // Asserts
        Optional<DeviceLifeCycle> shouldBePresent = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId());
        assertThat(shouldBePresent).isPresent();
    }

    @Transactional
    @Test
    public void obsoleteDeviceLifeCycleDoesNotShowUpInFindByName() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        deviceLifeCycle.makeObsolete();

        // Asserts
        Optional<DeviceLifeCycle> shouldNotBePresent = this.getTestService().findDeviceLifeCycleByName(deviceLifeCycle.getName());
        assertThat(shouldNotBePresent).isEmpty();
    }

    @Transactional
    @Test
    public void obsoleteDeviceLifeCycleDoesNotShowUpInFindAll() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        deviceLifeCycle.makeObsolete();

        // Asserts
        List<Long> deviceLifeCycleIds =
                this.getTestService()
                        .findAllDeviceLifeCycles()
                        .find()
                        .stream()
                        .map(DeviceLifeCycle::getId)
                        .collect(Collectors.toList());
        assertThat(deviceLifeCycleIds).doesNotContain(deviceLifeCycle.getId());
    }

    @Transactional
    @Test
    public void deleteDeviceLifeCycle() {
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        StateTransition stateTransition = stateMachine.getTransitions().get(0);
        DeviceLifeCycleBuilder builder = this.getTestService().newDeviceLifeCycleUsing("Test", stateMachine);
        builder
            .newTransitionAction(stateTransition)
            .addAction(MicroAction.ENABLE_VALIDATION)
            .addCheck(MicroCheck.DEFAULT_CONNECTION_AVAILABLE)
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO))
            .complete();
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();

        // Business method
        deviceLifeCycle.delete();
        Optional<DeviceLifeCycle> shouldNotBePresent = this.getTestService().findDeviceLifeCycle(deviceLifeCycle.getId());

        // Asserts
        assertThat(shouldNotBePresent.isPresent()).isFalse();
        assertThat(this.findDefaultFiniteStateMachine()).isNotNull();
    }

    private FiniteStateMachine findDefaultFiniteStateMachine() {
        return inMemoryPersistence
                .getService(FiniteStateMachineService.class)
                .findFiniteStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finite state machine"));
    }

    private FiniteStateMachine createFiniteStateMachineWithCustomTransitions() {
        FiniteStateMachineService finiteStateMachineService = inMemoryPersistence.getService(FiniteStateMachineService.class);
        CustomStateTransitionEventType eventType = finiteStateMachineService.newCustomStateTransitionEventType(CUSTOM_EVENT_SYMBOL);
        eventType.save();
        FiniteStateMachineBuilder stateMachineBuilder = finiteStateMachineService.newFiniteStateMachine("For Testing Purposes Only");
        State b = stateMachineBuilder.newCustomState("B").complete();
        State a = stateMachineBuilder.newCustomState("A").on(eventType).transitionTo(b).complete();
        FiniteStateMachine stateMachine = stateMachineBuilder.complete(a);
        stateMachine.save();
        return stateMachine;
    }

    private FiniteStateMachine createFiniteStateMachineWithStandardEventTypes() {
        com.elster.jupiter.events.EventType jupiterEventType = inMemoryPersistence.getService(EventService.class).getEventType(EventType.START_BPM.topic()).get();
        FiniteStateMachineService finiteStateMachineService = inMemoryPersistence.getService(FiniteStateMachineService.class);
        StandardStateTransitionEventType eventType = finiteStateMachineService.newStandardStateTransitionEventType(jupiterEventType);
        eventType.save();
        FiniteStateMachineBuilder stateMachineBuilder = finiteStateMachineService.newFiniteStateMachine("For Testing Purposes Only");
        State b = stateMachineBuilder.newCustomState("B").complete();
        State a = stateMachineBuilder.newCustomState("A").on(eventType).transitionTo(b).complete();
        FiniteStateMachine stateMachine = stateMachineBuilder.complete(a);
        stateMachine.save();
        return stateMachine;
    }

    private DeviceLifeCycleConfigurationService getTestService() {
        return inMemoryPersistence.getDeviceLifeCycleConfigurationService();
    }

}