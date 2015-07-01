package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.fsm.StateTransition;

import java.util.Set;

/**
 * Models an action that can be authorized to initiate a {@link StateTransition}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (08:58)
 */
@ProviderType
public interface AuthorizedTransitionAction extends AuthorizedAction {

    public StateTransition getStateTransition();

    public Set<MicroCheck> getChecks();

    public Set<MicroAction> getActions();

    /**
     * Tests if the {@link StateTransition} is "standard" or not.
     * Remember that a StateTransition is standard if it is connecting two "standard"
     * {@link com.elster.jupiter.fsm.State}s.
     * @see com.elster.jupiter.fsm.State#isCustom()
     *
     * @return A flag that indicates if the StateTransition is standard
     */
    public boolean isStandard();

}