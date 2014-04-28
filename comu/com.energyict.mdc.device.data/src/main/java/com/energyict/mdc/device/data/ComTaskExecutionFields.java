package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ImplField;

/**
 * Copyrights EnergyICT
 * Date: 17/04/14
 * Time: 15:12
 */
public enum ComTaskExecutionFields implements ImplField {

    DEVICE("device"),
    COMTASK("comTask"),
    CONNECTIONTASK("connectionTask"),
    USEDEFAULTCONNECTIONTASK("useDefaultConnectionTask"),
    NEXTEXECUTIONSPEC("nextExecutionSpecId"),
    MYNEXTEXECUTIONSPEC("myNextExecutionSpec"),
    LASTEXECUTIONTIMESTAMP("lastExecutionTimestamp"),
    NEXTEXECUTIONTIMESTAMP("nextExecutionTimestamp"),
    MODIFICATIONDATE("modificationDate"),
    OBSOLETEDATE("obsoleteDate"),
    PRIORITY("priority"),
    CURRENTRETRYCOUNT("currentRetryCount"),
    PLANNEDNEXTEXECUTIONTIMESTAMP("plannedNextExecutionTimestamp"),
    EXECUTIONPRIORITY("executionPriority"),
    EXECUTIONSTART("executionStart"),
    LASTSUCCESSFULCOMPLETIONTIMESTAMP("lastSuccessfulCompletionTimestamp"),
    LASTEXECUTIONFAILED("lastExecutionFailed"),
    IGNORENEXTEXECUTIONSPECSFORINBOUND("ignoreNextExecutionSpecsForInbound"),
    COMPORT("comPort"),
    PROTOCOLDIALECTCONFIGURATIONPROPERTIES("protocolDialectConfigurationProperties"),
    COM_SCHEDULE_REFERENCE("comScheduleReference");
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
