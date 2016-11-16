package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class UsagePointLifeCycleServiceImplIT extends BaseTestIT {

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

    @Test(expected = SecurityException.class)
    @Transactional
    public void testCanNotExecuteTransitionIfHasUnSufficientPrivileges() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test");
        UsagePointState state1 = lifeCycle.newState("State 1").setInitial().complete();
        UsagePointState state2 = lifeCycle.newState("State 2").setInitial().complete();
        UsagePointTransition transition = lifeCycle.newTransition("Transition", state1, state2).withLevels(EnumSet.of(UsagePointTransition.Level.FOUR)).complete();
        Instant now = get(Clock.class).instant();
        UsagePoint usagePoint = get(MeteringService.class).getServiceCategory(ServiceKind.ELECTRICITY).get().newUsagePoint("Test", now).create();
        usagePoint.setState(state1, now);

        UserService userService = get(UserService.class);
        Group group = userService.findOrCreateGroup("Test");
        userService.grantGroupWithPrivilege(group.getName(), "TST", new String[]{UsagePointTransition.Level.ONE.getPrivilege()});
        User user = userService.findOrCreateUser("TestUser", "domain", "directoryType");
        user.join(group);
        get(ThreadPrincipalService.class).set(user);

        Instant transitionTime = now.plus(1, ChronoUnit.HOURS);
        get(UsagePointLifeCycleService.class).triggerTransition(usagePoint, transition, transitionTime, "TST", Collections.emptyMap());
    }

    @Test
    @Transactional
    public void testCanExecuteTransition() {
        UsagePointLifeCycle lifeCycle = get(UsagePointLifeCycleConfigurationService.class).newUsagePointLifeCycle("Test 2");
        UsagePointState state1 = lifeCycle.newState("State 1").setInitial().complete();
        UsagePointState state2 = lifeCycle.newState("State 2").setInitial().complete();
        UsagePointTransition transition = spy(lifeCycle.newTransition("Transition", state1, state2).withLevels(EnumSet.of(UsagePointTransition.Level.ONE)).complete());
        Instant now = get(Clock.class).instant();
        UsagePoint usagePoint = get(MeteringService.class).getServiceCategory(ServiceKind.ELECTRICITY).get().newUsagePoint("Test", now).create();
        usagePoint.setState(state1, now);

        UserService userService = get(UserService.class);
        Group group = userService.findOrCreateGroup("Test");
        userService.grantGroupWithPrivilege(group.getName(), "TST", new String[]{UsagePointTransition.Level.ONE.getPrivilege()});
        User user = userService.findOrCreateUser("TestUser", "domain", "directoryType");
        user.join(group);
        get(ThreadPrincipalService.class).set(user);

        Instant transitionTime = now.plus(1, ChronoUnit.HOURS);
        get(UsagePointLifeCycleService.class).triggerTransition(usagePoint, transition, transitionTime, "TST", Collections.emptyMap());
        verify(transition).doTransition(anyString(), anyString(), eq(transitionTime), eq(Collections.emptyMap()));
    }
}
