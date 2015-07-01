package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ProviderType;

/**
 * Models an action that can be authorized to initiate an externally defined
 * business process as defined by the {@link com.elster.jupiter.bpm.BpmService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (14:38)
 */
@ProviderType
public interface AuthorizedBusinessProcessAction extends AuthorizedAction {

    public TransitionBusinessProcess getTransitionBusinessProcess();

}