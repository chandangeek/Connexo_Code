/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListReadingQualityFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.NoneOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NoneOrTimeDurationValueFactory;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

public enum SimplePropertyType implements PropertyType {
    UNKNOWN(Void.class),
    NUMBER(BigDecimal.class),
    BOOLEAN(Boolean.class),
    NULLABLE_BOOLEAN(ThreeStateFactory.class),
    TEXT(String.class),
    TEXTAREA(String.class),
    TIMESTAMP(Instant.class),
    LONG(LongFactory.class),
    IDWITHNAME(HasIdAndName.class),
    RELATIVEPERIOD(RelativePeriodFactory.class),
    SELECTIONGRID(ListValueFactory.class),
    LISTVALUE(ListValueFactory.class),
    DEVICECONFIGURATIONLIST(ListValueFactory.class),
    QUANTITY(Quantity.class),
    LISTREADINGQUALITY(ListReadingQualityFactory.class),
    ASSIGN(String.class),
    ENDDEVICEEVENTTYPE(ListValueFactory.class),
    LIFECYCLESTATUSINDEVICETYPE(ListValueFactory.class),
    RAISEEVENTPROPS(HasIdAndName.class),
    RELATIVEPERIODWITHCOUNT(ListValueFactory.class),
    BPM_PROCESS(HasIdAndName.class),
    TEMPORALAMOUNT(TemporalAmount.class),   //Property indicating a long period (See java.time.Period)
    DURATION(Duration.class),               //Property indicating a short duration (See java.time.Duration)
    TIMEDURATION(TimeDuration.class),       //Property that can indicate any kind of duration/period
    TWO_VALUES_DIFFERENCE(TwoValuesDifferenceValueFactory.class),
    NONE_OR_BIGDECIMAL(NoneOrBigDecimalValueFactory.class),
    NONE_OR_TIMEDURATION(NoneOrTimeDurationValueFactory.class);

    private Class typeClass;

    SimplePropertyType(Class typeClass) {
        this.typeClass = typeClass;
    }

    public static SimplePropertyType getTypeFrom(ValueFactory valueFactory) {
        if (valueFactory instanceof StringAreaFactory) {
            return TEXTAREA;
        }
        for (SimplePropertyType simplePropertyType : values()) {
            if (simplePropertyType.matches(valueFactory)) {
                return simplePropertyType;
            }
        }
        return UNKNOWN;
    }

    private boolean matches(ValueFactory valueFactory) {
        if (ValueFactory.class.isAssignableFrom(this.typeClass)) {
            return this.typeClass.isAssignableFrom(valueFactory.getClass());
        } else {
            return this.typeClass.isAssignableFrom(valueFactory.getValueType());
        }
    }
}
