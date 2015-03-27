package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.response.AuthorizedActionInfo;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCyclePrivilegeInfo;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link AuthorizedActionEditorFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-27 (11:28)
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorizedActionEditorFactoryIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private Thesaurus thesaurus;
    private ResourceHelper resourceHelper;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(AuthorizedActionEditorFactoryIT.class.getSimpleName());
        TransactionService transactionService = inMemoryPersistence.getService(TransactionService.class);
        try (TransactionContext context = transactionService.getContext()) {
            createDeviceLifeCycle(createFiniteStateMachine());
            context.commit();
        }
    }

    private static FiniteStateMachine createFiniteStateMachine() {
        FiniteStateMachineService service = inMemoryPersistence.getService(FiniteStateMachineService.class);
        CustomStateTransitionEventType eventType = service.newCustomStateTransitionEventType("#whatever");
        eventType.save();
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine("ABC");
        State b = builder.newCustomState("B").complete();
        State a = builder.newCustomState("A").on(eventType).transitionTo(b).complete();
        builder.newCustomState("C").complete();
        FiniteStateMachine stateMachine = builder.complete(a);
        stateMachine.save();
        return stateMachine;
    }

    private static DeviceLifeCycle createDeviceLifeCycle(FiniteStateMachine stateMachine) {
        DeviceLifeCycleBuilder builder = getDeviceLifeCycleConfigurationService().newDeviceLifeCycleUsing("ABC", stateMachine);
        stateMachine.getTransitions()
                .stream()
                .forEach(t ->
                        builder
                                .newTransitionAction(t)
                                .addLevel(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO)
                                .complete());
        DeviceLifeCycle deviceLifeCycle = builder.complete();
        deviceLifeCycle.save();
        return deviceLifeCycle;
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void setupResourceHelper() {
        this.resourceHelper =
                new ResourceHelper(
                        getDeviceLifeCycleConfigurationService(),
                        new ExceptionFactory(this.thesaurus));
    }

    @Transactional
    @Test
    public void changeToState() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = new AuthorizedActionInfo(this.thesaurus, deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateAID = finiteStateMachine.getState("A").get().getId();
        long stateBID = finiteStateMachine.getState("B").get().getId();

        // Business method: change the to state of the transition action and save changes
        actionInfo.toState.id = stateBID;
        AuthorizedActionEditor editor = this.getTestInstance().from(deviceLifeCycle, actionInfo);
        AuthorizedAction updatedAction = editor.saveChanges();

        // Asserts
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateAID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateBID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void changeFromState() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = new AuthorizedActionInfo(this.thesaurus, deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateBID = finiteStateMachine.getState("B").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change the to state of the transition action and save changes
        actionInfo.fromState.id = stateCID;
        AuthorizedActionEditor editor = this.getTestInstance().from(deviceLifeCycle, actionInfo);
        AuthorizedAction updatedAction = editor.saveChanges();

        // Asserts
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateCID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateBID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void changeFromAndToState() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = new AuthorizedActionInfo(this.thesaurus, deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateBID = finiteStateMachine.getState("B").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change the to state of the transition action and save changes
        actionInfo.fromState.id = stateBID;
        actionInfo.toState.id = stateCID;
        AuthorizedActionEditor editor = this.getTestInstance().from(deviceLifeCycle, actionInfo);
        AuthorizedAction updatedAction = editor.saveChanges();

        // Asserts
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateBID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateCID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void changeEverything() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = new AuthorizedActionInfo(this.thesaurus, deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateBID = finiteStateMachine.getState("B").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change all aspects and save changes
        actionInfo.fromState.id = stateBID;
        actionInfo.toState.id = stateCID;
        actionInfo.privileges = Arrays.asList(
                new DeviceLifeCyclePrivilegeInfo(this.thesaurus, AuthorizedAction.Level.THREE),
                new DeviceLifeCyclePrivilegeInfo(this.thesaurus, AuthorizedAction.Level.FOUR));
        AuthorizedActionEditor editor = this.getTestInstance().from(deviceLifeCycle, actionInfo);
        AuthorizedAction updatedAction = editor.saveChanges();

        // Asserts
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateBID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateCID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.THREE, AuthorizedAction.Level.FOUR);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    private static DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return inMemoryPersistence.getService(DeviceLifeCycleConfigurationService.class);
    }

    private AuthorizedActionEditorFactory getTestInstance() {
        return new AuthorizedActionEditorFactory(this.resourceHelper);
    }

}