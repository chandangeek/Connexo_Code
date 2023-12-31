/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListReadingQualityFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Instant;

public enum SimplePropertyType implements PropertyType {
    UNKNOWN(Void.class),
    NUMBER(BigDecimal.class),
    BOOLEAN(Boolean.class),
    NULLABLE_BOOLEAN(ThreeStateFactory.class),
    TEXT(String.class),
    TEXTAREA(StringFactory.class),
    TIMESTAMP(Instant.class),
    LONG(Long.class),
    IDWITHNAME(HasIdAndName.class),
    RELATIVEPERIOD(RelativePeriodFactory.class),
    SELECTIONGRID(ListValueFactory.class),
    LISTVALUE(ListValueFactory.class),
    DEVICECONFIGURATIONLIST(ListValueFactory.class),
    METROLOGYCONFIGURATIONLIST(ListValueFactory.class),
    QUANTITY(Quantity.class),
    LISTREADINGQUALITY(ListReadingQualityFactory.class),
    ASSIGN(String.class),
    MAILTO(String.class),
    ENDDEVICEEVENTTYPE(ListValueFactory.class),
    LIFECYCLESTATUSINDEVICETYPE(ListValueFactory.class),
    RAISEEVENTPROPS(HasIdAndName.class),
    RELATIVEPERIODWITHCOUNT(HasIdAndName.class),
    INTEGER(Integer.class);

    private Class typeClass;

    SimplePropertyType(Class typeClass) {
        this.typeClass = typeClass;
    }

    public static SimplePropertyType getTypeFrom(ValueFactory valueFactory) {
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
