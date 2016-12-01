package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
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
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.SetConnectedConnectionStateAction;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.UsagePointMicroActionFactoryImpl;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.MetrologyConfigurationIsDefinedCheck;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.UsagePointMicroCheckFactoryImpl;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

    private UsagePointState state1;
    private UsagePointState state2;
    private UsagePointTransition transition;
    private UsagePoint usagePoint;
    private Group group;
    private User user;

    public void initializeCommonUsagePointStateChangeFields() {
        UserService userService = get(UserService.class);
        group = userService.findOrCreateGroup("Test");
        userService.grantGroupWithPrivilege(group.getName(), APPLICATION, new String[]{UsagePointTransition.Level.FOUR.getPrivilege()});
        user = userService.findOrCreateUser("TestUser", "domain", "directoryType");
        user.join(group);
        get(ThreadPrincipalService.class).set(user);

        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Life cycle");
        state1 = lifeCycle.newState("State 1").setInitial().setStage(UsagePointStage.Stage.OPERATIONAL).complete();
        state2 = lifeCycle.newState("State 2").setStage(UsagePointStage.Stage.OPERATIONAL).complete();
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
                .from(SetConnectedConnectionStateAction.class.getSimpleName())
                .map(MicroAction::getKey))
                .contains(SetConnectedConnectionStateAction.class.getSimpleName());
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
                .collect(Collectors.toList())).containsExactly(SetConnectedConnectionStateAction.class.getSimpleName());
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
                .collect(Collectors.toList())).containsExactly(MetrologyConfigurationIsDefinedCheck.class.getSimpleName());
    }

    @Test
    @Transactional
    public void testCanNotExecuteTransitionIfHasUnSufficientPrivileges() {
        initializeCommonUsagePointStateChangeFields();
        user.leave(group);
        UsagePointLifeCycleService lifeCycleService = get(UsagePointLifeCycleService.class);
        UsagePointTransition spyTransition = spy(transition);

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
    @Ignore("Bug in h2 for inner select with ordering and aliases 'ORDER BY =UPSCR.TRANSITION_TIME AS UPSCRTRANSITION_TIME' instead of  'ORDER BY UPSCRTRANSITION_TIME'")
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

        List<UsagePointTransition> transitions = get(ServerUsagePointLifeCycleService.class).getAvailableTransitions(state1, APPLICATION);
        assertThat(transitions.size()).isEqualTo(1);
        assertThat(transitions).containsExactly(transition);

        transitions = get(ServerUsagePointLifeCycleService.class).getAvailableTransitions(state2, APPLICATION);
        assertThat(transitions).isEmpty();

        user.leave(group);
        transitions = get(ServerUsagePointLifeCycleService.class).getAvailableTransitions(state1, APPLICATION);
        assertThat(transitions).isEmpty();
    }
}
