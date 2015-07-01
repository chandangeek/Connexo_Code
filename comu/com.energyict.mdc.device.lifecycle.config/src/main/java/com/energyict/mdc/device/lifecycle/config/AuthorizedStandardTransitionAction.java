package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.fsm.StateTransition;

/**
 * Models a {@link AuthorizedTransitionAction} for a "standard" {@link StateTransition}.
 * A StateTransition is standard if it is connecting two "standard"
 * {@link com.elster.jupiter.fsm.State}s.
 * @see com.elster.jupiter.fsm.State#isCustom()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (10:15)
 */
@ProviderType
public interface AuthorizedStandardTransitionAction extends AuthorizedTransitionAction {

    public TransitionType getType();

    @Override
    public default boolean isStandard() {
        return true;
    }

}