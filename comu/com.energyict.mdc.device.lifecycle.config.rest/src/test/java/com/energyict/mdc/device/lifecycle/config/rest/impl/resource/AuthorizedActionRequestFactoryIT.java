/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.impl.TableSpecs;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedActionChangeRequest;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedActionRequestFactory;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedTransitionActionComplexEditRequest;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedTransitionActionCreateRequest;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedTransitionActionDeleteRequest;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCyclePrivilegeInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.StateTransitionEventTypeInfo;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link AuthorizedActionRequestFactory} component
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorizedActionRequestFactoryIT {

    private static final String EVENT_TYPE_1 = "#whatever";
    private static final String EVENT_TYPE_2 = "#more";
    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsService nlsService;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private ResourceHelper resourceHelper;
    private MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory;
    private AuthorizedActionInfoFactory authorizedActionInfoFactory;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    private MeteringTranslationService meteringTranslationService;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(AuthorizedActionRequestFactoryIT.class.getSimpleName());
        TransactionService transactionService = inMemoryPersistence.getService(TransactionService.class);
        try (TransactionContext context = transactionService.getContext()) {
            createDeviceLifeCycle(createFiniteStateMachine());
            context.commit();
        }
    }

    private static FiniteStateMachine createFiniteStateMachine() {
        FiniteStateMachineService service = inMemoryPersistence.getService(FiniteStateMachineService.class);
        CustomStateTransitionEventType eventType = createEventTypes();
        FiniteStateMachineBuilder builder = service.newFiniteStateMachine("ABC");
        State b = builder.newCustomState("B").complete();
        State a = builder.newCustomState("A").on(eventType).transitionTo(b).complete();
        builder.newCustomState("C").complete();
        FiniteStateMachine stateMachine = builder.complete(a);
        return stateMachine;
    }

    private static CustomStateTransitionEventType createEventTypes() {
        FiniteStateMachineService service = inMemoryPersistence.getService(FiniteStateMachineService.class);
        CustomStateTransitionEventType eventType1 = service.newCustomStateTransitionEventType(EVENT_TYPE_1, "context");
        service.newCustomStateTransitionEventType(EVENT_TYPE_2, "context");
        return eventType1;
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
        inMemoryPersistence.getService(OrmService.class).invalidateCache(FiniteStateMachineService.COMPONENT_NAME, TableSpecs.FSM_FINITE_STATE_MACHINE.name());
        when(thesaurus.join(any())).thenReturn(thesaurus);
        when(nlsService.getThesaurus(anyString(), any())).thenReturn(thesaurus);
        this.resourceHelper =
                new ResourceHelper(
                        getDeviceLifeCycleConfigurationService(),
                        deviceConfigurationService,
                        inMemoryPersistence.getService(FiniteStateMachineService.class),
                        new ExceptionFactory(this.thesaurus),
                        inMemoryPersistence.getService(EventService.class),
                        new ConcurrentModificationExceptionFactory(this.thesaurus),
                        this.nlsService);
        microActionAndCheckInfoFactory = new MicroActionAndCheckInfoFactory(deviceLifeCycleService, thesaurus);
        authorizedActionInfoFactory = new AuthorizedActionInfoFactory(thesaurus, deviceLifeCycleConfigurationService, microActionAndCheckInfoFactory,meteringTranslationService);
    }

    @Transactional
    @Test
    public void changeToState() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = authorizedActionInfoFactory.from(deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateAID = finiteStateMachine.getState("A").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change the to state of the transition action and save changes
        actionInfo.toState.id = stateCID;
        AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, actionInfo, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction updatedAction = request.perform();

        // Asserts
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateAID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateCID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void changeFromState() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = authorizedActionInfoFactory.from(deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateBID = finiteStateMachine.getState("B").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change the to state of the transition action and save changes
        actionInfo.fromState.id = stateCID;
        AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, actionInfo, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction updatedAction = request.perform();

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
        AuthorizedActionInfo actionInfo = authorizedActionInfoFactory.from(deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateBID = finiteStateMachine.getState("B").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change the to state of the transition action and save changes
        actionInfo.fromState.id = stateBID;
        actionInfo.toState.id = stateCID;
        AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, actionInfo, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction updatedAction = request.perform();

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
    public void changeOnlyLevels() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = authorizedActionInfoFactory.from(deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateAID = finiteStateMachine.getState("A").get().getId();
        long stateBID = finiteStateMachine.getState("B").get().getId();

        // Business method: change levels and save changes
        actionInfo.privileges = Arrays.asList(
                new DeviceLifeCyclePrivilegeInfo(this.thesaurus, AuthorizedAction.Level.THREE),
                new DeviceLifeCyclePrivilegeInfo(this.thesaurus, AuthorizedAction.Level.FOUR));
        AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, actionInfo, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction updatedAction = request.perform();

        // Asserts
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateAID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateBID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.THREE, AuthorizedAction.Level.FOUR);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void changeEverything() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        AuthorizedActionInfo actionInfo = authorizedActionInfoFactory.from(deviceLifeCycle.getAuthorizedActions().get(0));
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        long stateBID = finiteStateMachine.getState("B").get().getId();
        long stateCID = finiteStateMachine.getState("C").get().getId();

        // Business method: change all aspects and save changes
        actionInfo.fromState.id = stateBID;
        actionInfo.toState.id = stateCID;
        actionInfo.privileges = Arrays.asList(
                new DeviceLifeCyclePrivilegeInfo(this.thesaurus, AuthorizedAction.Level.THREE),
                new DeviceLifeCyclePrivilegeInfo(this.thesaurus, AuthorizedAction.Level.FOUR));

        actionInfo.microActions = new HashSet<>(1);
        MicroActionAndCheckInfo microAction = new MicroActionAndCheckInfo();
        microAction.key = MicroAction.ENABLE_VALIDATION.name();
        actionInfo.microActions.add(microAction);

        actionInfo.microChecks = new HashSet<>(1);
        MicroActionAndCheckInfo microCheck = new MicroActionAndCheckInfo();
        microCheck.key = "ConnectionPropertiesAreValid";
        actionInfo.microChecks.add(microAction);

        AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, actionInfo, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction updatedAction = request.perform();

        // Asserts
        assertThat(request).isInstanceOf(AuthorizedTransitionActionComplexEditRequest.class);
        assertThat(updatedAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction updatedTransitionAction = (AuthorizedTransitionAction) updatedAction;
        assertThat(updatedTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(stateBID);
        assertThat(updatedTransitionAction.getStateTransition().getTo().getId()).isEqualTo(stateCID);
        assertThat(updatedTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.THREE, AuthorizedAction.Level.FOUR);
        assertThat(updatedTransitionAction.getChecks()).isEmpty();
        assertThat(updatedTransitionAction.getActions()).isEmpty();
    }

    @Transactional
    @Test
    public void createNewAuthorizedAction() {
        DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();

        DeviceLifeCyclePrivilegeInfo privilegeInfo = new DeviceLifeCyclePrivilegeInfo();
        privilegeInfo.privilege = "ONE";
        StateTransitionEventTypeInfo eventTypeInfo = new StateTransitionEventTypeInfo();
        eventTypeInfo.symbol = EVENT_TYPE_2;
        DeviceLifeCycleStateInfo fromState = new DeviceLifeCycleStateInfo();
        fromState.id = finiteStateMachine.getState("C").get().getId();
        DeviceLifeCycleStateInfo toState = new DeviceLifeCycleStateInfo();
        toState.id = finiteStateMachine.getState("A").get().getId();
        AuthorizedActionInfo info = new AuthorizedActionInfo();
        info.name = "A new transition";
        info.fromState = fromState;
        info.toState = toState;
        info.triggeredBy = eventTypeInfo;
        info.privileges = Collections.singletonList(privilegeInfo);

        info.microChecks = new HashSet<>(1);
        MicroActionAndCheckInfo microCheck = new MicroActionAndCheckInfo();
        microCheck.key = "ConnectionPropertiesAreValid";
        info.microChecks.add(microCheck);

        AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, info, AuthorizedActionRequestFactory.Operation.CREATE);
        AuthorizedAction newAction = request.perform();

        // Asserts
        assertThat(request).isInstanceOf(AuthorizedTransitionActionCreateRequest.class);
        assertThat(newAction).isInstanceOf(AuthorizedTransitionAction.class);
        AuthorizedTransitionAction newTransitionAction = (AuthorizedTransitionAction) newAction;
        assertThat(newTransitionAction.getStateTransition().getFrom().getId()).isEqualTo(fromState.id);
        assertThat(newTransitionAction.getStateTransition().getTo().getId()).isEqualTo(toState.id);
        assertThat(newTransitionAction.getLevels()).containsOnly(AuthorizedAction.Level.ONE);
        assertThat(newTransitionAction.getChecks()).isEmpty();
        assertThat(newTransitionAction.getActions()).isEmpty();
    }

    @Test
    public void deleteAuthorizedAction() {
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceLifeCycle deviceLifeCycle = getDeviceLifeCycleConfigurationService().findDeviceLifeCycleByName("ABC").get();

            AuthorizedActionInfo info = new AuthorizedActionInfo();
            AuthorizedAction authorizedAction = deviceLifeCycle.getAuthorizedActions().get(0);
            info.id = authorizedAction.getId();
            info.version = authorizedAction.getVersion();
            info.parent = new VersionInfo<>(deviceLifeCycle.getId(), deviceLifeCycle.getVersion());

            AuthorizedActionChangeRequest request = this.getTestInstance().from(deviceLifeCycle, info, AuthorizedActionRequestFactory.Operation.DELETE);
            request.perform();

            assertThat(request).isInstanceOf(AuthorizedTransitionActionDeleteRequest.class);
            assertThat(deviceLifeCycle.getAuthorizedActions()).isEmpty();

            // Do not commit
        }
    }

    private static DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return inMemoryPersistence.getService(DeviceLifeCycleConfigurationService.class);
    }

    private AuthorizedActionRequestFactory getTestInstance() {
        return new AuthorizedActionRequestFactory(this.resourceHelper);
    }
}
