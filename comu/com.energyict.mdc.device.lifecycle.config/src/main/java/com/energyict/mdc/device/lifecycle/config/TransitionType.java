package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Models the types of {@link com.elster.jupiter.fsm.StateTransition}s
 * that are part of the default {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (10:20)
 */
public enum TransitionType {

    COMMISSION(DefaultState.IN_STOCK, DefaultState.COMMISSIONING) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        }
        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.CREATE_METER_ACTIVATION,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.ACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.ENABLE_VALIDATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.CREATE_METER_ACTIVATION,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_INACTIVE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.INACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.ENABLE_VALIDATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.CREATE_METER_ACTIVATION,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_AND_ACTIVATE(DefaultState.COMMISSIONING, DefaultState.ACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }

        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.ENABLE_VALIDATION);
        }

        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.CREATE_METER_ACTIVATION,
                    MicroAction.SET_LAST_READING,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    INSTALL_INACTIVE(DefaultState.COMMISSIONING, DefaultState.INACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION,
                    MicroAction.ENABLE_VALIDATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.SET_LAST_READING,
                    MicroAction.CREATE_METER_ACTIVATION,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    ACTIVATE(DefaultState.INACTIVE, DefaultState.ACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }

        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }

        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.START_COMMUNICATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.ENABLE_ESTIMATION,
                    MicroAction.ENABLE_VALIDATION);
        }

        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(MicroAction.SET_LAST_READING,
                    MicroAction.CREATE_METER_ACTIVATION,
                    MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        }
    },
    DEACTIVATE(DefaultState.ACTIVE, DefaultState.INACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED,
                    MicroCheck.ALL_DATA_VALIDATED,
                    MicroCheck.ALL_DATA_VALID);
        }
        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(
                    MicroAction.FORCE_VALIDATION_AND_ESTIMATION,
                    MicroAction.START_COMMUNICATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.START_RECURRING_COMMUNICATION,
                    MicroAction.DISABLE_COMMUNICATION
            );
        }
    },
    DEACTIVATE_AND_DECOMMISSION(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED,
                    MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED,
                    MicroCheck.ALL_DATA_VALIDATED,
                    MicroCheck.ALL_DATA_VALID);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS,
                    MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.DETACH_SLAVE_FROM_MASTER,
                    MicroAction.CLOSE_ALL_ISSUES,
                    MicroAction.CLOSE_METER_ACTIVATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION);
        }
    },
    DECOMMISSION(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED,
                    MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED,
                    MicroCheck.ALL_DATA_VALIDATED,
                    MicroCheck.ALL_DATA_VALID);
        }
        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS,
                    MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
        }
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.DETACH_SLAVE_FROM_MASTER,
                    MicroAction.CLOSE_ALL_ISSUES,
                    MicroAction.CLOSE_METER_ACTIVATION,
                    MicroAction.DISABLE_VALIDATION,
                    MicroAction.DISABLE_ESTIMATION,
                    MicroAction.DISABLE_COMMUNICATION);
        }
    },
    DELETE_FROM_DECOMMISSIONED(DefaultState.DECOMMISSIONED, DefaultState.REMOVED){
        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(
                    MicroAction.REMOVE_DEVICE);
        }
    },
   // RECYCLE(DefaultState.DECOMMISSIONED, DefaultState.IN_STOCK),
    DELETE_FROM_IN_STOCK(DefaultState.IN_STOCK, DefaultState.REMOVED){
       @Override
       public Set<MicroAction> requiredActions() {
           return EnumSet.of(
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
     * Gets the Set of {@link MicroCheck}s that are required
     * by this TransitionType and can therefore not be switched
     * off by the user. These checks will also be executed.
     *
     * @return The Set of MicroCheck
     */
    public Set<MicroCheck> requiredChecks() {
        return EnumSet.noneOf(MicroCheck.class);
    }

    /**
     * Gets the Set of {@link MicroCheck}s that are optional
     * for this TransitionType and can therefore not be switched
     * on or off by the user. These checks will only be executed
     * when switched on by the user.
     *
     * @return The Set of MicroCheck
     */
    public Set<MicroCheck> optionalChecks() {
        return EnumSet.noneOf(MicroCheck.class);
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
        if (transition != null){
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
}