package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ImplField;

/**
 * Copyrights EnergyICT
 * Date: 17/04/14
 * Time: 15:01
 */
public enum DeviceFields implements ImplField {

    NAME("name"),
    SERIALNUMBER("serialNumber"),
    TIMEZONE("timeZoneId"),
    MRID("mRID"),
    DEVICETYPE("deviceType"),
    DEVICECONFIGURATION("deviceConfiguration"),
    COM_TASK_EXECUTIONS("comTaskExecutions"),
    DEVICEGROUP("deviceGroup"),
    CERT_YEAR("certYear"),
    BATCH("batch"),
    LOCATION("location")
    ;

    private final String javaFieldName;

    DeviceFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    @Override
    public String fieldName() {
        return javaFieldName;
    }

}
