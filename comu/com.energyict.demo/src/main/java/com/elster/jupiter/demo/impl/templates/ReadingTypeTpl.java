/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ReadingTypeBuilder;
import com.elster.jupiter.metering.ReadingType;

public enum ReadingTypeTpl implements Template<ReadingType, ReadingTypeBuilder> {

    GAS_MASTER_VALUE("Master value", "0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.134.0"),
    VALVE_STATE("Valve state", "0.0.0.0.0.0.123.0.0.0.0.0.0.0.0.0.0.0");

    private String alias;
    private String mrid;

    ReadingTypeTpl(String alias, String mrid) {
        this.alias = alias;
        this.mrid = mrid;
    }

    public String getMRID(){
        return mrid;
    }

    public String getAlias(){
        return alias;
    }


    @Override
    public Class<ReadingTypeBuilder> getBuilderClass() {
        return ReadingTypeBuilder.class;
    }

    @Override
    public ReadingTypeBuilder get(ReadingTypeBuilder builder) {
        return builder.withAlias(alias).withMrid(mrid);
    }
}
