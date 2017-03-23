/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;


import com.elster.jupiter.bpm.BpmProcessDefinition;

public class BusinessProcessInfoFactory {

    public BusinessProcessInfo from(BpmProcessDefinition process) {
        return new BusinessProcessInfo(process.getId(), process.getProcessName(), process.getVersion());
    }
}
