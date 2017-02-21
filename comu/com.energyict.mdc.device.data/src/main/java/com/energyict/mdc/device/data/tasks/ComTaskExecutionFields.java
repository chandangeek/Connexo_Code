/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.ImplField;

public enum ComTaskExecutionFields implements ImplField {

    ID("id"),
    DEVICE("device"),
    COMTASK("comTask"),
    CONNECTIONTASK("connectionTask"),
    USEDEFAULTCONNECTIONTASK("useDefaultConnectionTask"),
    NEXTEXECUTIONSPEC("nextExecutionSpecs"),
    LASTEXECUTIONTIMESTAMP("lastExecutionTimestamp"),
    PLANNEDNEXTEXECUTIONTIMESTAMP("plannedNextExecutionTimestamp"),
    NEXTEXECUTIONTIMESTAMP("nextExecutionTimestamp"),
    MODIFICATIONDATE("modificationDate"),
    OBSOLETEDATE("obsoleteDate"),
    PLANNED_PRIORITY("plannedPriority"),
    EXECUTION_PRIORITY("executionPriority"),
    CURRENTRETRYCOUNT("currentRetryCount"),
    EXECUTIONSTART("executionStart"),
    LASTSUCCESSFULCOMPLETIONTIMESTAMP("lastSuccessfulCompletionTimestamp"),
    LASTEXECUTIONFAILED("lastExecutionFailed"),
    IGNORENEXTEXECUTIONSPECSFORINBOUND("ignoreNextExecutionSpecsForInbound"),
    COMPORT("comPort"),
    PROTOCOLDIALECTCONFIGURATIONPROPERTIES("protocolDialectConfigurationProperties"),
    COM_SCHEDULE("comSchedule"),
    LAST_SESSION("lastSession"),
    LAST_SESSION_HIGHEST_PRIORITY_COMPLETION_CODE("lastSessionHighestPriorityCompletionCode"),
    LAST_SESSION_SUCCESSINDICATOR("lastSessionSuccessIndicator"),
    ONHOLD("onHold"),
    COMTASKEXECTYPE("comTaskExecType"),
    ;

    private final String javaFieldName;

    ComTaskExecutionFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    @Override
    public String fieldName() {
        return javaFieldName;
    }
}
