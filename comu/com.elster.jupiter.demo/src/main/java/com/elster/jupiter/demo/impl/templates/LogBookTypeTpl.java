package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.LogBookTypeBuilder;
import com.energyict.mdc.masterdata.LogBookType;

public enum LogBookTypeTpl implements Template<LogBookType, LogBookTypeBuilder> {
    GENERIC("Generic logbook", "0.0.99.98.0.255"),
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
}
