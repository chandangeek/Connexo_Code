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

    COMMISSION(DefaultState.IN_STOCK, DefaultState.COMMISSIONED) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                    MicroCheck.LAST_READING_TIMESTAMP_SET,
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        }

        @Override
        public Set<MicroAction> requiredActions() {
            return EnumSet.of(MicroAction.SET_LAST_READING);
        }

        @Override
        public Set<MicroAction> optionalActions() {
            return EnumSet.of(MicroAction.ACTIVATE_CONNECTION_TASKS);
        }
    },
    INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.ACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                    MicroCheck.LAST_READING_TIMESTAMP_SET,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }

        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.VEE_PROPERTIES_ARE_ALL_VALID);
        }
    },
    INSTALL_INACTIVE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.INACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                    MicroCheck.LAST_READING_TIMESTAMP_SET,
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.VEE_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
    },
    INSTALL_AND_ACTIVATE(DefaultState.COMMISSIONED, DefaultState.ACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                    MicroCheck.LAST_READING_TIMESTAMP_SET,
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.VEE_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
    },
    INSTALL_INACTIVE(DefaultState.COMMISSIONED, DefaultState.INACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                    MicroCheck.LAST_READING_TIMESTAMP_SET,
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.VEE_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }
    },
    ACTIVATE(DefaultState.INACTIVE, DefaultState.ACTIVE) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(
                    MicroCheck.DEFAULT_CONNECTION_AVAILABLE,
                    MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                    MicroCheck.LAST_READING_TIMESTAMP_SET,
                    MicroCheck.SLAVE_DEVICE_HAS_GATEWAY,
                    MicroCheck.LINKED_WITH_USAGE_POINT);
        }

        @Override
        public Set<MicroCheck> requiredChecks() {
            return EnumSet.of(
                    MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                    MicroCheck.VEE_PROPERTIES_ARE_ALL_VALID);
        }
    },
    DEACTIVATE(DefaultState.ACTIVE, DefaultState.INACTIVE),
    DEACTIVATE_AND_DECOMMISSION(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED),
    DECOMMISSION(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED) {
        @Override
        public Set<MicroCheck> optionalChecks() {
            return EnumSet.of(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED);
        }
    },
    DELETE(DefaultState.DECOMMISSIONED, DefaultState.DELETED),
    RECYCLE(DefaultState.DECOMMISSIONED, DefaultState.IN_STOCK),
    REVOKE(DefaultState.IN_STOCK, DefaultState.DELETED);

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
        supportedActions.addAll(this.requiredActions());
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
        Optional<DefaultState> from = DefaultState.from(transition.getFrom());
        Optional<DefaultState> to = DefaultState.from(transition.getTo());
        if (from.isPresent() && to.isPresent()) {
            return Stream
                    .of(values())
                    .filter(t -> t.getFrom().equals(from.get()) && t.getTo().equals(to.get()))
                    .findFirst();
        }
        else {
            return Optional.empty();
        }
    }

}