/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "outputType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OutputInfo.ChannelOutputInfo.class, name = "channel"),
        @JsonSubTypes.Type(value = OutputInfo.RegisterOutputInfo.class, name = "register"),
})
public abstract class OutputInfo {

    public long id;

    public String name;

    public ReadingTypeInfo readingType;

    public FormulaInfo formula;

    public ChannelDataValidationSummaryInfo summary;

    public UsagePointValidationStatusInfo validationInfo;

    public static class ChannelOutputInfo extends OutputInfo {

        public TimeDurationInfo interval;

        public String flowUnit;
    }

    public static class RegisterOutputInfo extends OutputInfo {

        public String deliverableType;
    }

    public boolean isCummulative;

    public boolean hasEvent;

    public boolean isBilling;


}
