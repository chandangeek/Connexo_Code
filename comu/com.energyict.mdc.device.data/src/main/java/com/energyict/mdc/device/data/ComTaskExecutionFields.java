package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ImplField;

/**
 * Copyrights EnergyICT
 * Date: 17/04/14
 * Time: 15:12
 */
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
    COM_SCHEDULE("comSchedule");
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
