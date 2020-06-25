/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TransitionType} component.
 */
public class TransitionTypeTest {

    @Test
    public void allTransitionTypesHaveAFrom() {
        for (TransitionType transitionType : TransitionType.values()) {
            assertThat(transitionType.getFrom()).as(transitionType.name() + " does not have a from").isNotNull();
        }
    }

    @Test
    public void allTransitionTypesHaveATo() {
        for (TransitionType transitionType : TransitionType.values()) {
            assertThat(transitionType.getTo()).as(transitionType.name() + " does not have a to").isNotNull();
        }
    }

    @Test
    public void testWithCustomStateTransition() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(true);
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(true);
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithCustomFromAndStandardTo() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(true);
        when(from.getName()).thenReturn("testWithCustomFromAndStandardTo");
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(false);
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithStandardFromAndCustomTo() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(false);
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(true);
        when(to.getName()).thenReturn("testWithStandardFromAndCustomTo");
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithStandardTransition() {
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.COMMISSIONING, TransitionType.COMMISSION);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.ACTIVE, TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.INACTIVE, TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING);
        this.testStandardTransition(DefaultState.COMMISSIONING, DefaultState.ACTIVE, TransitionType.INSTALL_AND_ACTIVATE);
        this.testStandardTransition(DefaultState.COMMISSIONING, DefaultState.INACTIVE, TransitionType.INSTALL_INACTIVE);
        this.testStandardTransition(DefaultState.ACTIVE, DefaultState.INACTIVE, TransitionType.DEACTIVATE);
        this.testStandardTransition(DefaultState.INACTIVE, DefaultState.ACTIVE, TransitionType.ACTIVATE);
        this.testStandardTransition(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED, TransitionType.DECOMMISSION);
        this.testStandardTransition(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED, TransitionType.DEACTIVATE_AND_DECOMMISSION);
        this.testStandardTransition(DefaultState.DECOMMISSIONED, DefaultState.REMOVED, TransitionType.DELETE_FROM_DECOMMISSIONED);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.REMOVED, TransitionType.DELETE_FROM_IN_STOCK);
    }

    @Test
    public void testMandatoryPreTransitionActionsActivate() {
        assertThat(TransitionType.ACTIVATE.requiredActions()).hasSize(1);
        assertThat(TransitionType.ACTIVATE.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }

    @Test
    public void testOptionalPreTransitionActionsActivate() {
        assertThat(TransitionType.ACTIVATE.optionalActions()).hasSize(10);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.SET_MULTIPLIER);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.LINK_TO_USAGE_POINT);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testMandatoryPreTransitionActionsDeActivate() {
        assertThat(TransitionType.DEACTIVATE.requiredActions()).isEmpty();
    }

    @Test
    public void testOptionalPreTransitionActionsDeactivate() {
        assertThat(TransitionType.DEACTIVATE.optionalActions()).hasSize(9);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testMandatoryPreTransitionActionsDeActivateAndDecommission() {
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).hasSize(7);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DETACH_SLAVE_FROM_MASTER);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_ALL_ISSUES);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.REMOVE_LOCATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testOptionalPreTransitionActionsDeactivateAndDecommission() {
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).hasSize(5);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.CANCEL_ALL_SERVICE_CALLS);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
    }

    @Test
    public void testOptionalPreTransitionActionsDecommission() {
        assertThat(TransitionType.DECOMMISSION.optionalActions()).hasSize(5);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.CANCEL_ALL_SERVICE_CALLS);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
    }

    @Test
    public void testMandatoryPreTransitionActionsDecommission() {
        assertThat(TransitionType.DECOMMISSION.requiredActions()).hasSize(7);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DETACH_SLAVE_FROM_MASTER);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_ALL_ISSUES);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.REMOVE_LOCATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testOptionalPreTransitionActionsInstallAndActivate() {
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).hasSize(10);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.SET_MULTIPLIER);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.LINK_TO_USAGE_POINT);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testMandatoryPreTransitionActionsInstallAndActivate() {
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredActions()).hasSize(1);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }

    @Test
    public void testMandatoryPreTransitionActionsInstallAndActivateWithoutCommissioning() {
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredActions()).hasSize(1);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }

    @Test
    public void testOptionalPreTransitionActionsInstallAndActivateWithoutCommissioning() {
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).hasSize(10);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.SET_MULTIPLIER);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.LINK_TO_USAGE_POINT);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testMandatoryPreTransitionActionsInstallInactive() {
        assertThat(TransitionType.INSTALL_INACTIVE.requiredActions()).hasSize(1);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }

    @Test
    public void testOptionalPreTransitionActionsInstallInactive() {
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).hasSize(13);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.SET_MULTIPLIER);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.LINK_TO_USAGE_POINT);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    @Test
    public void testMandatoryPreTransitionActionsInstallInactiveFromInStock() {
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredActions()).hasSize(1);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }

    @Test
    public void testOptionalPreTransitionActionsInstallInactiveFromInStock() {
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).hasSize(13);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.SET_MULTIPLIER);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.LINK_TO_USAGE_POINT);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);

    }

    @Test
    public void testMandatoryPreTransitionActionsRemoveFromDecommissioned() {
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.requiredActions()).hasSize(2);
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.requiredActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.requiredActions()).contains(MicroAction.REMOVE_DEVICE);
    }

    @Test
    public void testOptionalPreTransitionActionsInstallRemoveFromDecommissioned() {
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.optionalActions()).isEmpty();
    }

    @Test
    public void testMandatoryPreTransitionActionsRemoveFromStock() {
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.requiredActions()).hasSize(2);
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.requiredActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.requiredActions()).contains(MicroAction.REMOVE_DEVICE);
    }

    @Test
    public void testOptionalPreTransitionActionsInstallRemoveStock() {
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.optionalActions()).isEmpty();
    }

    @Test
    public void testMandatoryPreTransitionActionsCommission() {
        assertThat(TransitionType.COMMISSION.requiredActions()).hasSize(1);
        assertThat(TransitionType.COMMISSION.requiredActions()).contains(MicroAction.SET_MULTIPLIER);
    }

    @Test
    public void testOptionalPreTransitionActionsCommission() {
        assertThat(TransitionType.COMMISSION.optionalActions()).hasSize(7);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_ALL_COMMUNICATION);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
    }

    private void testStandardTransition(DefaultState defaultFrom, DefaultState defaultTo, TransitionType expectedTransitionType) {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(false);
        when(from.getName()).thenReturn(defaultFrom.getKey());
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(false);
        when(to.getName()).thenReturn(defaultTo.getKey());
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent())
                .as("TransitionType#from for default state " + defaultFrom + " and " + defaultTo + " was not found")
                .isTrue();
        assertThat(transitionType.get()).isEqualTo(expectedTransitionType);
    }
}