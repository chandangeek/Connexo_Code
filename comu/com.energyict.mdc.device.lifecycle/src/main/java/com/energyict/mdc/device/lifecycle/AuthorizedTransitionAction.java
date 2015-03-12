package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.fsm.StateTransition;

/**
 * Models an action that can be authorized to initiate a {@link StateTransition}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (08:58)
 */
public interface AuthorizedTransitionAction extends AuthorizedAction {

    public StateTransition getStateTransition();

}