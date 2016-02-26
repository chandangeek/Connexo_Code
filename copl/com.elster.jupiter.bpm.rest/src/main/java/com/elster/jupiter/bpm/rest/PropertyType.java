package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.ValueFactory;

public enum PropertyType implements com.elster.jupiter.rest.util.properties.PropertyType {
    UNKNOWN(new NeverMatch()),
    SELECTIONGRID(new ClassBasedValueFactoryMatcher(ListValueFactory.class));

    private final ValueFactoryMachter matcher;

    PropertyType(ValueFactoryMachter matcher) {
        this.matcher = matcher;
    }

    public static PropertyType getTypeFrom(ValueFactory valueFactory) {
        for (PropertyType propertyType : values()) {
            if (propertyType.matches(valueFactory)) {
                return propertyType;
            }
        }
        return UNKNOWN;
    }

    private boolean matches(ValueFactory valueFactory) {
        return this.matcher.matches(valueFactory);
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
