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

    SHIPMENT(DefaultState.ORDERED, DefaultState.IN_STOCK),
    COMMISSION(DefaultState.IN_STOCK, DefaultState.COMMISSIONED),
    INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.ACTIVE),
    INSTALL_INACTIVE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.INACTIVE),
    INSTALL_AND_ACTIVATE(DefaultState.COMMISSIONED, DefaultState.ACTIVE),
    INSTALL_INACTIVE(DefaultState.COMMISSIONED, DefaultState.INACTIVE),
    ACTIVATE(DefaultState.INACTIVE, DefaultState.ACTIVE),
    DEACTIVATE(DefaultState.ACTIVE, DefaultState.INACTIVE),
    DEACTIVATE_AND_DECOMMISSION(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED),
    DECOMMISSION(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED),
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
     * Gets the Set of {@link MicroCheck}s that are supported by this TransitionType.
     *
     * @return The Set of MicroCheck
     */
    public Set<MicroCheck> supportedChecks() {
        return EnumSet.noneOf(MicroCheck.class);
    }

    /**
     * Gets the Set of {@link MicroAction}s that are supported by this TransitionType.
     *
     * @return The Set of MicroAction
     */
    public Set<MicroAction> supportedActions() {
        return EnumSet.noneOf(MicroAction.class);
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