/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.LogBookTypeBuilder;
import com.energyict.mdc.masterdata.LogBookType;

public enum LogBookTypeTpl implements Template<LogBookType, LogBookTypeBuilder> {
    GENERIC("Generic logbook", "0.0.99.98.0.255"),
    STANDARD_EVENT_LOG("Standard event logbook", "0.0.99.98.0.255"),
    FRAUD_DETECTION_LOG("Fraud detection logbook", "0.0.99.98.1.255"),
    DISCONNECTOR_CONTROL_LOG("Disconnector control logbook", "0.0.99.98.2.255"),
    ;

    private String obisCode;
    private String name;

    private LogBookTypeTpl(String name, String obisCode) {
        this.name = name;
        this.obisCode = obisCode;
    }

    @Override
    public Class<LogBookTypeBuilder> getBuilderClass(){
        return LogBookTypeBuilder.class;
    }

    @Override
    public LogBookTypeBuilder get(LogBookTypeBuilder builder){
        return builder.withName(this.name).withObisCode(this.obisCode);
    }

    public String getName() {
        return name;
    }

    public String getObisCode() {
        return obisCode;
    }
}
