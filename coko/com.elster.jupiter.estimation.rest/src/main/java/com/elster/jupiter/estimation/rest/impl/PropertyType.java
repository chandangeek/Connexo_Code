package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsWithoutNoneFactory;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;

public enum PropertyType implements com.elster.jupiter.rest.util.properties.PropertyType {
    UNKNOWN(Void.class),
    NUMBER(BigDecimalFactory.class),
    NULLABLE_BOOLEAN(ThreeStateFactory.class),
    BOOLEAN(BooleanFactory.class),
    TEXT(StringFactory.class),
    LISTVALUE(ListValueFactory.class),
    RELATIVEPERIOD(RelativePeriodFactory.class),
    ADVANCEREADINGSSETTINGS(AdvanceReadingsSettingsFactory.class),
    ADVANCEREADINGSSETTINGSWITHOUTNONE(AdvanceReadingsSettingsWithoutNoneFactory.class),
    LONG(LongFactory.class);

    private Class valueFactoryClass;

    PropertyType(Class valueFactoryClass) {
        this.valueFactoryClass = valueFactoryClass;
    }

    private boolean matches(ValueFactory valueFactory) {
        return this.valueFactoryClass.isAssignableFrom(valueFactory.getClass());
    }

    public static PropertyType getTypeFrom(ValueFactory valueFactory) {
        for (PropertyType propertyType : values()) {
            if (propertyType.matches(valueFactory)) {
                return propertyType;
            }
        }
        return UNKNOWN;
    }
}
