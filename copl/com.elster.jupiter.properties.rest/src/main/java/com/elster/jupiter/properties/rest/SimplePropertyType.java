/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListReadingQualityFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Instant;

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
    TIMEDURATION(TimeDuration.class);
    BPM_PROCESS(HasIdAndName.class),
    TWO_VALUES_DIFFERENCE(TwoValuesDifferenceValueFactory.class),
    NON_OR_BIG_DECIMAL(NonOrBigDecimalValueFactory.class);


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
