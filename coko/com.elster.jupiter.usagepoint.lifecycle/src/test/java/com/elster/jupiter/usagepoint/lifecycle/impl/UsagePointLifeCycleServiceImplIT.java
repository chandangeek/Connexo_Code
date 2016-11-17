package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class UsagePointLifeCycleServiceImplIT extends BaseTestIT {
    private static final String APPLICATION = "TST";

    private TestMicroAction.Factory actionFactory;
    private TestMicroCheck.Factory checkFactory;

    private UsagePointLifeCycle lifeCycle;
    private UsagePointState state1;
    private UsagePointState state2;
    private UsagePointTransition transition;
    private UsagePoint usagePoint;
    private Group group;
    private User user;

    public void initializeCommonUsagePointStateChangeFields(Instant time) {
        lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Life cycle");
        state1 = lifeCycle.newState("State 1").setInitial().complete();
        state2 = lifeCycle.newState("State 2").complete();
        transition = lifeCycle.newTransition("Transition", state1, state2).withLevels(EnumSet.of(UsagePointTransition.Level.FOUR)).complete();
        usagePoint = get(MeteringService.class).getServiceCategory(ServiceKind.ELECTRICITY).get().newUsagePoint("Usage point", time).create();

        UserService userService = get(UserService.class);
        group = userService.findOrCreateGroup("Test");
        userService.grantGroupWithPrivilege(group.getName(), APPLICATION, new String[]{UsagePointTransition.Level.FOUR.getPrivilege()});
        user = userService.findOrCreateUser("TestUser", "domain", "directoryType");
        user.join(group);
        get(ThreadPrincipalService.class).set(user);
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

    private Instant hourBefore() {
        return get(Clock.class).instant().minus(1, ChronoUnit.HOURS);
    }

    @Test
    public void testDefaultLifeCycleExists() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).getUsagePointLifeCycles().find().get(0);

        assertThat(lifeCycle.getName()).isEqualTo(TranslationKeys.LIFE_CYCLE_NAME.getDefaultFormat());

        Optional<UsagePointState> underConstruction = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.UNDER_CONSTRUCTION)).findFirst();
        Optional<UsagePointState> active = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.ACTIVE)).findFirst();
        Optional<UsagePointState> inactive = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.INACTIVE)).findFirst();
        Optional<UsagePointState> demolished = lifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.DEMOLISHED)).findFirst();
        assertThat(underConstruction).isPresent();
        assertThat(underConstruction.get().isInitial()).isTrue();
        assertThat(active).isPresent();
        assertThat(inactive).isPresent();
        assertThat(demolished).isPresent();

        Optional<UsagePointTransition> transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_INSTALL_ACTIVE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(underConstruction.get());
        assertThat(transition.get().getTo()).isEqualTo(active.get());

        transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_INSTALL_INACTIVE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(underConstruction.get());
        assertThat(transition.get().getTo()).isEqualTo(inactive.get());

        transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_DEACTIVATE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(active.get());
        assertThat(transition.get().getTo()).isEqualTo(inactive.get());

        transition = lifeCycle.getTransitions().stream().filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_ACTIVATE.getDefaultFormat())).findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(inactive.get());
        assertThat(transition.get().getTo()).isEqualTo(active.get());

        transition = lifeCycle.getTransitions().stream()
                .filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_DEMOLISH_FROM_ACTIVE.getDefaultFormat()))
                .filter(tr -> tr.getFrom().equals(active.get()))
                .findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getFrom()).isEqualTo(active.get());

        transition = lifeCycle.getTransitions().stream()
                .filter(tr -> tr.getName().equals(TranslationKeys.TRANSITION_DEMOLISH_FROM_INACTIVE.getDefaultFormat()))
                .filter(tr -> tr.getFrom().equals(inactive.get()))
                .findFirst();
        assertThat(transition).isPresent();
        assertThat(transition.get().getTo()).isEqualTo(demolished.get());
    }

    @Test
    @Transactional
    public void testCanNotExecuteTransitionIfHasUnSufficientPrivileges() {
        Instant hourBefore = hourBefore();
        initializeCommonUsagePointStateChangeFields(hourBefore);
        usagePoint.setState(state1, hourBefore);
        user.leave(group);
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);
        UsagePointTransition spyTransition = spy(this.transition);

        lifeCycleService.performTransition(usagePoint, spyTransition, APPLICATION, Collections.emptyMap());

        verify(spyTransition, never()).doTransition(anyString(), anyString(), any(Instant.class), eq(Collections.emptyMap()));
        UsagePointStateChangeRequest request = lifeCycleService.getHistory(usagePoint).get(0);
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.USER_CAN_NOT_PERFORM_TRANSITION.getNumber()));
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testCanExecuteTransition() {
        Instant hourBefore = hourBefore();
        initializeCommonUsagePointStateChangeFields(hourBefore);
        usagePoint.setState(state1, hourBefore);
        transition.startUpdate()
                .withChecks(Collections.singleton(TestMicroCheck.class.getSimpleName()))
                .withActions(Collections.singleton(TestMicroAction.class.getSimpleName()))
                .complete();
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);
        UsagePointTransition spyTransition = spy(this.transition);

        lifeCycleService.performTransition(usagePoint, spyTransition, APPLICATION, Collections.emptyMap());

        verify(spyTransition).doTransition(anyString(), anyString(), any(Instant.class), eq(Collections.emptyMap()));
        UsagePointStateChangeRequest request = lifeCycleService.getHistory(usagePoint).get(0);
        assertThat(request.getGeneralFailReason()).isNull();
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.COMPLETED);
    }

    @Test
    @Transactional
    public void testExecuteRemovedTransition() {
        Instant hourBefore = hourBefore();
        initializeCommonUsagePointStateChangeFields(hourBefore);
        usagePoint.setState(state1, hourBefore);
        transition.remove();
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);

        lifeCycleService.scheduleTransition(usagePoint, transition, hourBefore.plus(1, ChronoUnit.HOURS), APPLICATION, Collections.emptyMap());

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) lifeCycleService.getHistory(usagePoint).get(0);
        request.execute();
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.TRANSITION_NOT_FOUND.getNumber()));
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testExecuteTransitionForWrongState() {
        Instant hourBefore = hourBefore();
        initializeCommonUsagePointStateChangeFields(hourBefore);
        usagePoint.setState(state2, hourBefore);
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);

        lifeCycleService.scheduleTransition(usagePoint, transition, hourBefore.plus(1, ChronoUnit.HOURS), APPLICATION, Collections.emptyMap());

        UsagePointStateChangeRequestImpl request = (UsagePointStateChangeRequestImpl) lifeCycleService.getHistory(usagePoint).get(0);
        request.execute();
        assertThat(request.getGeneralFailReason()).isNotEmpty();
        assertThat(request.getGeneralFailReason()).contains(String.valueOf(MessageSeeds.USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION.getNumber()));
        assertThat(request.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.FAILED);
    }

    @Test
    @Transactional
    public void testExecuteTransitionCheckFail() {
        Instant hourBefore = hourBefore();
        initializeCommonUsagePointStateChangeFields(hourBefore);
        usagePoint.setState(state1, hourBefore);
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
        Instant hourBefore = hourBefore();
        initializeCommonUsagePointStateChangeFields(hourBefore);
        usagePoint.setState(state1, hourBefore);
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
}
