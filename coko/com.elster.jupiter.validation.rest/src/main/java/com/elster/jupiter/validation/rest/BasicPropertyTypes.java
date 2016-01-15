package com.elster.jupiter.validation.rest;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.rest.util.properties.PropertyType;

public enum BasicPropertyTypes implements PropertyType {
    UNKNOWN(Void.class),
    NUMBER(BigDecimalFactory.class),
    NULLABLE_BOOLEAN(ThreeStateFactory.class),
    BOOLEAN(BooleanFactory.class),
    TEXT(StringFactory.class),
    LISTVALUE(ListValueFactory.class),
    RELATIVEPERIOD(RelativePeriodFactory.class);

    private Class valueFactoryClass;

    BasicPropertyTypes(Class valueFactoryClass) {
        this.valueFactoryClass = valueFactoryClass;
    }

    private boolean matches(ValueFactory valueFactory) {
        return this.valueFactoryClass.isAssignableFrom(valueFactory.getClass());
    }

    public static BasicPropertyTypes getTypeFrom(ValueFactory valueFactory) {
        for (BasicPropertyTypes propertyType : values()) {
            if (propertyType.matches(valueFactory)) {
                return propertyType;
            }
        }
        return UNKNOWN;
    }
}
