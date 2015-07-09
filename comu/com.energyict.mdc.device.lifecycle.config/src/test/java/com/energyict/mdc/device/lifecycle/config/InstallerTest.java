package com.energyict.mdc.device.lifecycle.config;


import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.energyict.mdc.device.lifecycle.config.impl.*;
import org.junit.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 'Out of the box' device life cycle test
 * Copyrights EnergyICT
 * Date: 1/07/2015
 * Time: 10:25
 */
public class InstallerTest {

    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(InstallerTest.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void isStandardLifeCyclePresentTest(){
        Optional<DeviceLifeCycle> deviceLifeCycle = inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle();
        assertThat(deviceLifeCycle.isPresent()).isTrue();
        assertThat(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey()).isEqualTo(deviceLifeCycle.get().getName());
    }

    @Test
    public void statesTest(){
        DeviceLifeCycle deviceLifeCycle = inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        Optional<State> inStock = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.IN_STOCK.getKey());
        assertThat(inStock.isPresent());
        assertThat(inStock.get().isInitial()).isTrue();
        Optional<State> commissioning = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.COMMISSIONING.getKey());
        assertThat(commissioning.isPresent());
        assertThat(commissioning.get().isInitial()).isFalse();
        Optional<State> active = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.ACTIVE.getKey());
        assertThat(active.isPresent());
        assertThat(active.get().isInitial()).isFalse();
        Optional<State> inactive = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.INACTIVE.getKey());
        assertThat(inactive.isPresent());
        assertThat(inactive.get().isInitial()).isFalse();
        Optional<State> decommissioned = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.DECOMMISSIONED.getKey());
        assertThat(decommissioned.isPresent());
        assertThat(decommissioned.get().isInitial()).isFalse();
        Optional<State> removed = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.REMOVED.getKey());
        assertThat(removed.isPresent());
        assertThat(removed.get().isInitial()).isFalse();
    }

    @Test
    public void transitionTest(){
        DeviceLifeCycle deviceLifeCycle = inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        List<StateTransition> transitions = deviceLifeCycle.getFiniteStateMachine().getTransitions();
        assertThat(transitions.size()).isEqualTo(11);
        // 4 transitions starting from Stock
        List<String> startingFromStock = transitions.stream().filter(x -> x.getFrom().getName().equals(DefaultState.IN_STOCK.getKey())).map(StateTransition::getTo).map(State::getName).collect(Collectors.toList());
        State commissioning = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.COMMISSIONING.getKey()).get();
        State active = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.ACTIVE.getKey()).get();
        State inactive = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.INACTIVE.getKey()).get();
        State decommissioned = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.DECOMMISSIONED.getKey()).get();
        State removed = deviceLifeCycle.getFiniteStateMachine().getState(DefaultState.REMOVED.getKey()).get();

        assertThat(startingFromStock).hasSize(4);
        assertThat(startingFromStock).contains(commissioning.getName(), active.getName(), inactive.getName(), removed.getName());
        // 2 transitions starting from Commissioning
        List<String> startingFromCommissioning = transitions.stream().filter(x -> x.getFrom().getName().equals(DefaultState.COMMISSIONING.getKey())).map(StateTransition::getTo).map(State::getName).collect(Collectors.toList());
        assertThat(startingFromCommissioning).contains(active.getName(),inactive.getName());
        // 2 transitions starting from Active
        List<String> startingFromActive = transitions.stream().filter(x -> x.getFrom().getName().equals(DefaultState.ACTIVE.getKey())).map(StateTransition::getTo).map(State::getName).collect(Collectors.toList());
        assertThat(startingFromActive).contains(decommissioned.getName(), inactive.getName());
        // 2 transitions starting from Inactive
        List<String> startingFromInactive = transitions.stream().filter(x -> x.getFrom().getName().equals(DefaultState.INACTIVE.getKey())).map(StateTransition::getTo).map(State::getName).collect(Collectors.toList());
        assertThat(startingFromInactive).contains(active.getName(), decommissioned.getName());
        // 1 transitions starting from Decommissioned
        List<String> startingFromDecommissioned = transitions.stream().filter(x -> x.getFrom().getName().equals(DefaultState.DECOMMISSIONED.getKey())).map(StateTransition::getTo).map(State::getName).collect(Collectors.toList());
        assertThat(startingFromDecommissioned).contains(removed.getName());
    }

    @Test
    public void authorizedActionsTest(){
        DeviceLifeCycle deviceLifeCycle = inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        List<AuthorizedTransitionAction> authorizedActions = deviceLifeCycle.getAuthorizedActions().stream().map(x->(AuthorizedTransitionAction) x).collect(Collectors.toList());
        // All actions have the levels AuthorizedActions.Level.ONE, AuthorizedActions.Level.TWO, AuthorizedActions.Level.THREE
        EnumSet level7 = EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO, AuthorizedAction.Level.THREE);
        //for each transition defined on the state machine we have 1 authorized action
        int numberOfTransitions = deviceLifeCycle.getFiniteStateMachine().getTransitions().size();
        assertThat(authorizedActions).hasSize(numberOfTransitions);
        //they all have a level 7
        assertThat(authorizedActions.stream().filter(x -> x.getLevels().containsAll(level7)).collect(Collectors.toList())).hasSize(numberOfTransitions);
        // They all refer to a different transition
        assertThat(authorizedActions.stream().map(AuthorizedTransitionAction::getStateTransition).collect(Collectors.toSet())).hasSize(numberOfTransitions);
    }

}
