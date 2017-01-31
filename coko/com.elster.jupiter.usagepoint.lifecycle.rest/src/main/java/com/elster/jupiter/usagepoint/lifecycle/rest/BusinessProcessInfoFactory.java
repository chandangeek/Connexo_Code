/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.fsm.StateChangeBusinessProcess;

public class BusinessProcessInfoFactory {

    public BusinessProcessInfo from(StateChangeBusinessProcess process) {
        return new BusinessProcessInfo(process.getId(), process.getName(), process.getDeploymentId(), process.getProcessId());
    }
}
