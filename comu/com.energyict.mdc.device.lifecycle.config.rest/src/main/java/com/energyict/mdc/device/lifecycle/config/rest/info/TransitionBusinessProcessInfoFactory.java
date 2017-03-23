/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.bpm.BpmProcessDefinition;

import javax.inject.Inject;

public class TransitionBusinessProcessInfoFactory {

    @Inject
    public TransitionBusinessProcessInfoFactory() {}

    public TransitionBusinessProcessInfo from(BpmProcessDefinition bpmProcessDefinition){
        return new TransitionBusinessProcessInfo(bpmProcessDefinition.getId(),
                bpmProcessDefinition.getProcessName(),
                bpmProcessDefinition.getVersion());
    }

}
