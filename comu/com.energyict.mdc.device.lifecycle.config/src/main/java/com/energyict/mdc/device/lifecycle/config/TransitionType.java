/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Models the types of {@link com.elster.jupiter.fsm.StateTransition}s
 * that are part of the default {@link DeviceLifeCycle}.
 */
public enum TransitionType {

    COMMISSION(DefaultState.IN_STOCK, DefaultState.COMMISSIONING) {
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.SET_MULTIPLIER
                    );
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION);
        }
    },
    INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.ACTIVE) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.SET_MULTIPLIER,
                    MicroAction.ENABLE_VALIDATION,
                    MicroAction.LINK_TO_USAGE_POINT,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_INACTIVE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.INACTIVE) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.SET_MULTIPLIER,
                    MicroAction.ENABLE_VALIDATION,
                    MicroAction.LINK_TO_USAGE_POINT,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION
                    );
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_AND_ACTIVATE(DefaultState.COMMISSIONING, DefaultState.ACTIVE) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.SET_MULTIPLIER,
                    MicroAction.ENABLE_VALIDATION,
                    MicroAction.LINK_TO_USAGE_POINT,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION);
        }

        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_INACTIVE(DefaultState.COMMISSIONING, DefaultState.INACTIVE) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.SET_MULTIPLIER,
                    MicroAction.ENABLE_VALIDATION,
                    MicroAction.LINK_TO_USAGE_POINT,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    ACTIVATE(DefaultState.INACTIVE, DefaultState.ACTIVE) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.SET_MULTIPLIER,
                    MicroAction.ENABLE_VALIDATION,
                    MicroAction.LINK_TO_USAGE_POINT,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION);
        }

        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    DEACTIVATE(DefaultState.ACTIVE, DefaultState.INACTIVE) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.FORCE_VALIDATION_AND_ESTIMATION,
                    MicroAction.START_COMMUNICATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION,
                    MicroAction.CLOSE_METER_ACTIVATION
            );
        }
    },
    DEACTIVATE_AND_DECOMMISSION(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS,
                    MicroAction.FORCE_VALIDATION_AND_ESTIMATION,
                    MicroAction.CANCEL_ALL_SERVICE_CALLS,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.DETACH_SLAVE_FROM_MASTER,
                    MicroAction.CLOSE_ALL_ISSUES,
                    MicroAction.CLOSE_METER_ACTIVATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.REMOVE_LOCATION);
        }
    },
    DECOMMISSION(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED) {
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS,
                    MicroAction.FORCE_VALIDATION_AND_ESTIMATION,
                    MicroAction.CANCEL_ALL_SERVICE_CALLS,
                    MicroAction.ACTIVATE_ALL_COMMUNICATION,
                    MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.DETACH_SLAVE_FROM_MASTER,
                    MicroAction.CLOSE_ALL_ISSUES,
                    MicroAction.CLOSE_METER_ACTIVATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.REMOVE_LOCATION);
        }
    },
    DELETE_FROM_DECOMMISSIONED(DefaultState.DECOMMISSIONED, DefaultState.REMOVED){
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.CLOSE_METER_ACTIVATION,
                    MicroAction.REMOVE_DEVICE);
        }
    },
    DELETE_FROM_IN_STOCK(DefaultState.IN_STOCK, DefaultState.REMOVED){
       @Override
       public Set<MicroAction> requiredActions() {
           return EnumSet.of(
                   MicroAction.CLOSE_METER_ACTIVATION,
                   MicroAction.REMOVE_DEVICE);
       }
   };

    private DefaultState from;
    private DefaultState to;

    TransitionType(DefaultState from, DefaultState to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Determines the TransitionType for the specified {@link StateTransition}.
     * Will return <code>Optional.empty()</code> when the StateTransition
     * is not standard, i.e. when the connecting {@link State}s
     * are not standard.
     *
     * @param transition The StateTransition
     * @return The TransitionType
     */
    public static Optional<TransitionType> from(StateTransition transition) {
        if (transition != null) {
            return from(transition.getFrom(), transition.getTo());
        }
        return Optional.empty();
    }

    public static Optional<TransitionType> from(State fromSate, State toState) {
        Optional<DefaultState> from = DefaultState.from(fromSate);
        Optional<DefaultState> to = DefaultState.from(toState);
        if (from.isPresent() && to.isPresent()) {
            return Stream
                    .of(values())
                    .filter(t -> t.getFrom().equals(from.get()) && t.getTo().equals(to.get()))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets the Set of {@link MicroAction}s that are required
     * by this TransitionType and can therefore not be switched
     * off by the user. These actions will always be executed
     * when the transition is triggered by the user.
     *
     * @return The Set of MicroAction
     */
    public Set<MicroAction> requiredActions() {
        return EnumSet.noneOf(MicroAction.class);
    }

    /**
     * Gets the Set of {@link MicroAction}s that are optional
     * by this TransitionType and can therefore not be switched
     * on or off by the user. These actions will only be executed
     * when switched on by the user and when the transition is triggered by the user.
     *
     * @return The Set of MicroAction
     */
    public Set<MicroAction> optionalActions() {
        return EnumSet.noneOf(MicroAction.class);
    }

    /**
     * Gets the Set of {@link MicroAction}s that are supported
     * by this TransitionType. This should be the union
     * of the required and option actions.
     *
     * @return The Set of MicroAction
     */
    public Set<MicroAction> supportedActions() {
        EnumSet<MicroAction> supportedActions = EnumSet.copyOf(this.requiredActions());
        supportedActions.addAll(this.optionalActions());
        return supportedActions;
    }

    public DefaultState getFrom() {
        return this.from;
    }

    public DefaultState getTo() {
        return this.to;
    }
}
