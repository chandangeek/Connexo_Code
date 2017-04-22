/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.UsagePointImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleBuilder;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.ResetValidationResultsAction;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.SetConnectionStateAction;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.UsagePointMicroActionFactoryImpl;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.MeterRolesAreSpecifiedCheck;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.MetrologyConfigurationIsDefinedCheck;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.UsagePointMicroCheckFactoryImpl;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.assertj.core.internal.Failures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class UsagePointLifeCycleServiceImplIT extends BaseTestIT {
    private static final String APPLICATION = "TST";

    private TestMicroAction.Factory actionFactory;
    private TestMicroCheck.Factory checkFactory;

    private State state1;
    private State state2;
    private UsagePointTransition transition;
    private UsagePoint usagePoint;
    private Group group;
    private User user;

    public void initializeCommonUsagePointStateChangeFields() {
        UserService userService = get(UserService.class);
        //to overcome granting privilege issues we need to let the system think we are not a normal user
        Principal principal = new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        };
        get(ThreadPrincipalService.class).set(principal);
        group = userService.findOrCreateGroup("Test");
        userService.grantGroupWithPrivilege(group.getName(), APPLICATION, new String[]{UsagePointTransition.Level.FOUR.getPrivilege()});
        user = userService.findOrCreateUser("TestUser", "domain", "directoryType");
        user.join(group);
        get(ThreadPrincipalService.class).set(user);
        StageSet defaultStageSet = get(UsagePointLifeCycleConfigurationService.class).getDefaultStageSet();
        Stage stage = defaultStageSet.getStageByName(UsagePointStage.OPERATIONAL.getKey()).get();
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Life cycle");
        FiniteStateMachineUpdater updater = lifeCycle.getUpdater();
        state1 = updater.newCustomState("State", stage).complete();
        state2 = updater.newCustomState("State 2", stage).complete();
        updater.complete(state1);
        transition = lifeCycle.newTransition("Transition", state1, state2).withLevels(EnumSet.of(UsagePointTransition.Level.FOUR)).complete();
        lifeCycle.markAsDefault();
        usagePoint = get(MeteringService.class).getServiceCategory(ServiceKind.ELECTRICITY).get().newUsagePoint("Usage point", now().minus(2, ChronoUnit.HOURS)).create();
    }

    private Instant now() {
        return get(Clock.class).instant();
    }

    @Before
    public void before() {
        if (actionFactory == null) {
            actionFactory = new TestMicroAction.Factory();
            get(UsagePointLifeCycleConfigurationService.class).addMicroActionFactory(actionFactory);
        }
        if (checkFactory == null) {
            checkFactory = new TestMicroCheck.Factory();
            get(UsagePointLifeCycleConfigurationService.class).addMicroCheckFactory(checkFactory);
        }
    }

    @After
    public void after() {
        get(UsagePointLifeCycleConfigurationService.class).removeMicroActionFactory(actionFactory);
        get(UsagePointLifeCycleConfigurationService.class).removeMicroCheckFactory(checkFactory);
    }

    @Test
    public void testMicroActionFactoryCanFindByCorrectKey() {
        assertThat(get(UsagePointMicroActionFactoryImpl.class)
                .from(SetConnectionStateAction.class.getSimpleName())
                .map(MicroAction::getKey))
                .contains(SetConnectionStateAction.class.getSimpleName());
    }

    @Test
    public void testMicroActionFactoryCanFindByWrongKey() {
        assertThat(get(UsagePointMicroActionFactoryImpl.class).from("bla-bla")).isEmpty();
    }

    @Test
    public void testMicroActionFactoryGetAll() {
        assertThat(get(UsagePointMicroActionFactoryImpl.class).getAllActions()
                .stream()
                .map(MicroAction::getKey)
                .collect(Collectors.toList()))
                .containsOnly(
                        SetConnectionStateAction.class.getSimpleName(),
                        ResetValidationResultsAction.class.getSimpleName());
    }

    @Test
    public void testMicroCheckFactoryCanFindByCorrectKey() {
        assertThat(get(UsagePointMicroCheckFactoryImpl.class)
                .from(MetrologyConfigurationIsDefinedCheck.class.getSimpleName())
                .map(MicroCheck::getKey))
                .contains(MetrologyConfigurationIsDefinedCheck.class.getSimpleName());
    }

    @Test
    public void testMicroCheckFactoryCanFindByWrongKey() {
        assertThat(get(UsagePointMicroCheckFactoryImpl.class).from("bla-bla")).isEmpty();
    }

    @Test
    public void testMicroCheckFactoryGetAll() {
        assertThat(get(UsagePointMicroCheckFactoryImpl.class).getAllChecks()
                .stream()
                .map(MicroCheck::getKey)
                .collect(Collectors.toList()))
                .containsOnly(
                        MetrologyConfigurationIsDefinedCheck.class.getSimpleName(),
                        MeterRolesAreSpecifiedCheck.class.getSimpleName());
    }

    @Test(expected = UsagePointStateChangeException.class)
    @Transactional
    public void testCanNotExecuteTransitionIfHasUnSufficientPrivileges() {
        initializeCommonUsagePointStateChangeFields();
        user.leave(group);
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);
        UsagePointTransition spyTransition = spy(transition);

        lifeCycleService.performTransition(usagePoint, spyTransition, APPLICATION, Collections.emptyMap());
    }

    @Test
    @Transactional
    public void testCanExecuteTransition() {
        initializeCommonUsagePointStateChangeFields();
        transition.startUpdate()
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .complete();
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);
        UsagePointTransition spyTransition = spy(transition);

        lifeCycleService.performTransition(usagePoint, spyTransition, APPLICATION, Collections.emptyMap());

        verify(spyTransition).doTransition(anyString(), anyString(), any(Instant.class), eq(Collections.emptyMap()));
        UsagePointStateChangeRequest request = lifeCycleService.getHistory(usagePoint).get(0);
        assertThat(request.getGeneralFailReason()).isNull();
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.COMPLETED);
    }

    @Test
    @Transactional
    public void testExecuteRemovedTransition() {
        initializeCommonUsagePointStateChangeFields();
        transition.remove();
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);

        lifeCycleService.scheduleTransition(usagePoint, transition, now().plus(1, ChronoUnit.HOURS), APPLICATION, Collections.emptyMap());

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) lifeCycleService.getHistory(usagePoint).get(0);
        request.execute();
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.TRANSITION_NOT_FOUND.getNumber()));
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testExecuteTransitionForWrongState() {
        initializeCommonUsagePointStateChangeFields();
        ((UsagePointImpl) usagePoint).setState(state2, now().minus(1, ChronoUnit.HOURS));
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);

        lifeCycleService.performTransition(usagePoint, transition, APPLICATION, Collections.emptyMap());

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) lifeCycleService.getHistory(usagePoint).get(0);
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION.getNumber()));
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testExecuteTransitionCheckFail() {
        initializeCommonUsagePointStateChangeFields();
        ExecutableMicroCheck microCheck = (ExecutableMicroCheck) checkFactory.from(TestMicroCheck.class.getSimpleName()).get();
        checkFactory.setOnExecute((u, t) -> Optional.of(new ExecutableMicroCheckViolation(microCheck, "MicroCheck fail")));
        transition.startUpdate().withChecks(Collections.singleton(microCheck.getKey())).complete();
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);

        lifeCycleService.performTransition(usagePoint, transition, APPLICATION, Collections.emptyMap());

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) lifeCycleService.getHistory(usagePoint).get(0);
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.MICRO_CHECKS_FAILED_NO_PARAM.getNumber()));
        assertThat(request.getFailReasons().get(0).getKey()).isEqualTo(microCheck.getKey());
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testExecuteTransitionActionFail() {
        initializeCommonUsagePointStateChangeFields();
        ExecutableMicroAction microAction = (ExecutableMicroAction) actionFactory.from(TestMicroAction.class.getSimpleName()).get();
        actionFactory.setOnExecute((u, t) -> {
            throw new ExecutableMicroActionException(microAction, "MicroAction fail");
        });
        transition.startUpdate().withActions(Collections.singleton(microAction.getKey())).complete();
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);

        lifeCycleService.performTransition(usagePoint, transition, APPLICATION, Collections.emptyMap());

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) lifeCycleService.getHistory(usagePoint).get(0);
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.MICRO_ACTION_FAILED_NO_PARAM.getNumber()));
        assertThat(request.getFailReasons().get(0).getKey()).isEqualTo(microAction.getKey());
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testCreateInitialStateChangeRequestHistoryRecord() {
        UsagePointInitialStateChangeRequestHandler changeRequestHandler = get(UsagePointInitialStateChangeRequestHandler.class);
        ((EventServiceImpl) get(EventService.class)).addTopicHandler(changeRequestHandler);
        initializeCommonUsagePointStateChangeFields();
        ((EventServiceImpl) get(EventService.class)).removeTopicHandler(changeRequestHandler);

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) get(UsagePointLifeCycleService.class).getHistory(usagePoint).get(0);
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.COMPLETED);
        assertThat(request.getFromStateName()).isEqualTo("-");
        assertThat(request.getToStateName()).isEqualTo(state1.getName());
        assertThat(request.getTransitionTime()).isEqualTo(usagePoint.getInstallationTime());
        assertThat(request.getGeneralFailReason()).isNull();
        assertThat(request.getFailReasons()).isEmpty();
        assertThat(request.getOriginator()).isEqualTo(user);
    }

    @Test
    @Transactional
    public void testCanGetTransitionsAvailableForCurrentUser() {
        initializeCommonUsagePointStateChangeFields();

        List<UsagePointTransition> transitions = get(ServerUsagePointLifeCycleService.class).getAvailableTransitions(usagePoint, APPLICATION);
        assertThat(transitions.size()).isEqualTo(1);
        assertThat(transitions).containsExactly(transition);

        user.leave(group);
        transitions = get(ServerUsagePointLifeCycleService.class).getAvailableTransitions(usagePoint, APPLICATION);
        assertThat(transitions).isEmpty();
    }

    @Test
    public void testDefaultLifeCycleExists() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).getDefaultLifeCycle();
        assertThat(lifeCycle.isDefault()).isEqualTo(true);
        assertThat(lifeCycle.getName()).isEqualTo(TranslationKeys.LIFE_CYCLE_NAME.getDefaultFormat());

        State underConstruction = lifeCycle.getStates().stream().filter(state -> state.getName().equals(DefaultState.UNDER_CONSTRUCTION.getKey())).findFirst();
        State active = lifeCycle.getStates().stream().filter(state -> state.getName().equals(DefaultState.ACTIVE.getKey())).findFirst();
        State inactive = lifeCycle.getStates().stream().filter(state -> state.getName().equals(DefaultState.INACTIVE.getKey())).findFirst();
        State demolished = lifeCycle.getStates().stream().filter(state -> state.getName().equals(DefaultState.DEMOLISHED.getKey())).findFirst();
        assertThat(underConstruction.isInitial()).isTrue();

        UsagePointTransition transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_INSTALL_ACTIVE.getDefaultFormat());
        assertThat(transition.getFrom()).isEqualTo(underConstruction);
        assertThat(transition.getTo()).isEqualTo(active);
        assertThat(DefaultTransition.getDefaultTransition(transition)).contains(DefaultTransition.INSTALL_ACTIVE);

        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_INSTALL_INACTIVE.getDefaultFormat());
        assertThat(transition.getFrom()).isEqualTo(underConstruction);
        assertThat(transition.getTo()).isEqualTo(inactive);
        assertThat(DefaultTransition.getDefaultTransition(transition)).contains(DefaultTransition.INSTALL_INACTIVE);

        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_DEACTIVATE.getDefaultFormat());
        assertThat(transition.getFrom()).isEqualTo(active);
        assertThat(transition.getTo()).isEqualTo(inactive);
        assertThat(DefaultTransition.getDefaultTransition(transition)).contains(DefaultTransition.DEACTIVATE);

        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_ACTIVATE.getDefaultFormat());
        assertThat(transition.getFrom()).isEqualTo(inactive);
        assertThat(transition.getTo()).isEqualTo(active);
        assertThat(DefaultTransition.getDefaultTransition(transition)).contains(DefaultTransition.ACTIVATE);

        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_DEMOLISH_FROM_ACTIVE.getDefaultFormat(),
                active::equals);
        assertThat(transition.getTo()).isEqualTo(demolished);
        assertThat(DefaultTransition.getDefaultTransition(transition)).contains(DefaultTransition.DEMOLISH_FROM_ACTIVE);

        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_DEMOLISH_FROM_INACTIVE.getDefaultFormat(),
                inactive::equals);
        assertThat(transition.getTo()).isEqualTo(demolished);
        assertThat(DefaultTransition.getDefaultTransition(transition)).contains(DefaultTransition.DEMOLISH_FROM_INACTIVE);
    }

    @Test
    @Transactional
    public void testAcceptDefaultLifeCycle() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).getDefaultLifeCycle();
        UsagePointState active = findStateOrFail(lifeCycle, DefaultState.ACTIVE);
        UsagePointState inactive = findStateOrFail(lifeCycle, DefaultState.INACTIVE);
        UsagePointState demolished = findStateOrFail(lifeCycle, DefaultState.DEMOLISHED);
        final String RESURRECTION = "Resurrection";
        lifeCycle.newTransition(RESURRECTION, demolished, active).complete();

        get(UsagePointLifeCycleBuilder.class).accept(lifeCycle);

        assertThat(lifeCycle.getTransitions()).hasSize(7);
        UsagePointTransition transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_INSTALL_ACTIVE.getDefaultFormat());
        assertContainsOnlyClasses(transition.getActions(), SetConnectionStateAction.class, ResetValidationResultsAction.class);
        assertContainsOnlyClasses(transition.getChecks(), MeterRolesAreSpecifiedCheck.class, MetrologyConfigurationIsDefinedCheck.class);
        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_INSTALL_INACTIVE.getDefaultFormat());
        assertContainsOnlyClasses(transition.getActions(), SetConnectionStateAction.class, ResetValidationResultsAction.class);
        assertContainsOnlyClasses(transition.getChecks(), MeterRolesAreSpecifiedCheck.class, MetrologyConfigurationIsDefinedCheck.class);
        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_DEACTIVATE.getDefaultFormat());
        assertContainsOnlyClasses(transition.getActions(), ResetValidationResultsAction.class);
        assertContainsOnlyClasses(transition.getChecks());
        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_ACTIVATE.getDefaultFormat());
        assertContainsOnlyClasses(transition.getActions(), ResetValidationResultsAction.class);
        assertContainsOnlyClasses(transition.getChecks());
        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_DEMOLISH_FROM_ACTIVE.getDefaultFormat(),
                active::equals);
        assertContainsOnlyClasses(transition.getActions(), ResetValidationResultsAction.class);
        assertContainsOnlyClasses(transition.getChecks());
        transition = findTransitionOrFail(lifeCycle,
                com.elster.jupiter.usagepoint.lifecycle.config.impl.TranslationKeys.TRANSITION_DEMOLISH_FROM_INACTIVE.getDefaultFormat(),
                inactive::equals);
        assertContainsOnlyClasses(transition.getActions(), ResetValidationResultsAction.class);
        assertContainsOnlyClasses(transition.getChecks());
        transition = findTransitionOrFail(lifeCycle, RESURRECTION);
        assertContainsOnlyClasses(transition.getActions());
        assertContainsOnlyClasses(transition.getChecks());
    }

    private static UsagePointState findStateOrFail(UsagePointLifeCycle lifeCycle, DefaultState defaultState) {
        return lifeCycle.getStates().stream()
                .filter(state -> state.isDefault(defaultState))
                .findFirst()
                .orElseThrow(() -> Failures.instance().failure("State " + defaultState.getTranslation().getDefaultFormat() + " is not found."));
    }

    private static UsagePointTransition findTransitionOrFail(UsagePointLifeCycle lifeCycle, String name) {
        return findTransitionOrFail(lifeCycle, name, state -> true);
    }

    private static UsagePointTransition findTransitionOrFail(UsagePointLifeCycle lifeCycle, String name, Predicate<UsagePointState> fromPredicate) {
        return lifeCycle.getTransitions().stream()
                .filter(tr -> tr.getName().equals(name))
                .filter(tr -> fromPredicate.test(tr.getFrom()))
                .findFirst()
                .orElseThrow(() -> Failures.instance().failure("Transition " + name + " is not found."));
    }

    private static void assertContainsOnlyClasses(Collection<?> collection, Class<?>... classes) {
        assertThat(collection.stream().map(Object::getClass).collect(Collectors.toSet())).isEqualTo(ImmutableSet.copyOf(classes));
    }
}
