package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValueFactory;

public enum PropertyType implements com.elster.jupiter.rest.util.properties.PropertyType {
    UNKNOWN(new NeverMatch()),
    NUMBER(new ClassBasedValueFactoryMatcher(BigDecimalFactory.class)),
    NULLABLE_BOOLEAN(new ClassBasedValueFactoryMatcher(ThreeStateFactory.class)),
    BOOLEAN(new ClassBasedValueFactoryMatcher(BooleanFactory.class)),
    TEXT(new ClassBasedValueFactoryMatcher(StringFactory.class)),
    LISTVALUE(new ClassBasedValueFactoryMatcher(ListValueFactory.class)),
    IDWITHNAME(new DomainClassValueFactoryMatcher(HasIdAndName.class));

    private final ValueFactoryMachter matcher;

    PropertyType(ValueFactoryMachter matcher) {
        this.matcher = matcher;
    }

    private boolean matches(ValueFactory valueFactory) {
        return this.matcher.matches(valueFactory);
    }

    public static PropertyType getTypeFrom(ValueFactory valueFactory) {
        for (PropertyType propertyType : values()) {
            if (propertyType.matches(valueFactory)) {
                return propertyType;
            }
        }
        return UNKNOWN;
    }

    private interface ValueFactoryMachter {
        boolean matches(ValueFactory valueFactory);
    }
    private static class NeverMatch implements ValueFactoryMachter {
        @Override
        public boolean matches(ValueFactory valueFactory) {
            return false;
        }
    }

    private static class ClassBasedValueFactoryMatcher implements ValueFactoryMachter {
        private final Class valueFactoryClass;

        private ClassBasedValueFactoryMatcher(Class valueFactoryClass) {
            super();
            this.valueFactoryClass = valueFactoryClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(ValueFactory valueFactory) {
            return this.valueFactoryClass.isAssignableFrom(valueFactory.getClass());
        }
    }

    private static class DomainClassValueFactoryMatcher implements ValueFactoryMachter {
        private final Class domainClass;

        private DomainClassValueFactoryMatcher(Class domainClass) {
            super();
            this.domainClass = domainClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(ValueFactory valueFactory) {
            return this.domainClass.isAssignableFrom(valueFactory.getValueType());
        }
    }

}