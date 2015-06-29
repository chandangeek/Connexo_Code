package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the privileges of the finite state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (17:00)
 */
@ProviderType
public interface Privileges {

    public String VIEW_FINITE_STATE_MACHINES = "privilege.view.finiteStateMachines";
    public String CONFIGURE_FINITE_STATE_MACHINES = "privilege.configure.finiteStateMachines";

}