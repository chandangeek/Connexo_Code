/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.ImplField;

public enum PriorityComTaskExecutionFields implements ImplField {
    ID("id"),
    COMTASKEXECUTION("comTaskExecution"),
    NEXTEXECUTIONTIMESTAMP("nextExecutionTimestamp"),
    PRIORITY("priority"),
    COMPORT("comPort")
    ;

    private final String javaFieldName;

    PriorityComTaskExecutionFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    @Override
    public String fieldName() {
        return javaFieldName;
    }
}
