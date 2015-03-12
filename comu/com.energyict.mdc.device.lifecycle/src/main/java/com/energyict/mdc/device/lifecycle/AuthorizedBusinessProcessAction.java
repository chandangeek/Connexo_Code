package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.fsm.State;

/**
 * Models an action that can be authorized to initiate an externally defined
 * busines process as defined by the {@link com.elster.jupiter.bpm.BpmService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (14:38)
 */
public interface AuthorizedBusinessProcessAction extends AuthorizedAction {

    public String getDeploymentId();

    public String getProcessId();

}