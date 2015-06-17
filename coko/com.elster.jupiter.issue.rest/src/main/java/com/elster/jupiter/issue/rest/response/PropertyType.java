package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.StringReferenceFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;

public enum PropertyType implements com.elster.jupiter.rest.util.properties.PropertyType {
    UNKNOWN(Void.class),
    NUMBER(BigDecimalFactory.class),
    NULLABLE_BOOLEAN(ThreeStateFactory.class),
    BOOLEAN(BooleanFactory.class),
    TEXTAREA(StringFactory.class),
    DEVICECONFIGURATIONLIST(ListValueFactory.class),
    IDWITHNAME(StringReferenceFactory.class),
    LONG(LongFactory.class)
    ;

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
