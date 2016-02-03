package com.elster.insight.usagepoint.data.impl.cps;

public enum CustomPropertySetAttributes {
    NAME {
        @Override
        public String propertyKey() {
            return "name";
        }
    },
    ENHANCED_SUPPORT {
        @Override
        public String propertyKey() {
            return "enhancedSupport";
        }
    },;

    public abstract String propertyKey();

    public String databaseName() {
        return name();
    }

}
