package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.ImplField;

/**
 * Copyrights EnergyICT
 * Date: 17/04/14
 * Time: 15:12
 */
public enum ConnectionTaskFields implements ImplField {

    ID("id"),
    DEVICE("device"),
    MODIFICATION_DATE("modificationDate"),
    OBSOLETE_DATE("obsoleteDate"),
    IS_DEFAULT("isDefault"),
    STATUS("status"),
    LAST_COMMUNICATION_START("lastCommunicationStart"),
    LAST_SUCCESSFUL_COMMUNICATION_END("lastSuccessfulCommunicationEnd"),
    LAST_SESSION("lastSession"),
    LAST_SESSION_SUCCESS_INDICATOR("lastSessionSuccessIndicator"),
    LAST_SESSION_STATUS("lastSessionStatus"),
    COM_SERVER("comServer"),
    COM_PORT_POOL("comPortPool"),
    PARTIAL_CONNECTION_TASK("partialConnectionTask"),
    CURRENT_RETRY_COUNT("currentRetryCount"),
    LAST_EXECUTION_FAILED("lastExecutionFailed"),
    NEXT_EXECUTION_SPECS("nextExecutionSpecs"),
    NEXT_EXECUTION_TIMESTAMP("nextExecutionTimestamp"),
    PLANNED_NEXT_EXECUTION_TIMESTAMP("plannedNextExecutionTimestamp"),
    CONNECTION_STRATEGY("connectionStrategy"),
    PRIORITY("priority"),
    ALLOW_SIMULTANEOUS_CONNECTIONS("allowSimultaneousConnections");

    private final String javaFieldName;

    ConnectionTaskFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    @Override
    public String fieldName() {
        return javaFieldName;
    }
}
