/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ImplField;

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
    METER("meter"),
    LOCATION("location"),
    READINGTYPEOBISCODEUSAGES("readingTypeObisCodeUsages"),
    KEY_ACCESSORS("keyAccessors")
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
