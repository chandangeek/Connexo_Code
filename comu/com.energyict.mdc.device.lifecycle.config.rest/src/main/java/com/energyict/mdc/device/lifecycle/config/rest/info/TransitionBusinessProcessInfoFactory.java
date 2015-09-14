package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import javax.inject.Inject;

public class TransitionBusinessProcessInfoFactory {

    @Inject
    public TransitionBusinessProcessInfoFactory() {}

    public TransitionBusinessProcessInfo from(TransitionBusinessProcess transitionBusinessProcess){
        return new TransitionBusinessProcessInfo(transitionBusinessProcess.getId(),
                                                transitionBusinessProcess.getName(),
                                                transitionBusinessProcess.getDeploymentId(),
                                                transitionBusinessProcess.getProcessId());
    }

    public TransitionBusinessProcessInfo from(StateChangeBusinessProcess stateChangeBusinessProcess){
        return new TransitionBusinessProcessInfo(stateChangeBusinessProcess.getId(),
                                                stateChangeBusinessProcess.getName(),
                                                stateChangeBusinessProcess.getDeploymentId(),
                                                stateChangeBusinessProcess.getProcessId());
    }

}
