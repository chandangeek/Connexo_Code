package com.energyict.mdc.common;

/**
 * Defines the unique identifiers of commonly used {@link BusinessObjectFactory BusinessObjectFactories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (13:59)
 */
public enum FactoryIds {
    RELATION_TYPE(Constants.RELATION_TYPE_ID);

    int id;

    FactoryIds(int id) {
        this.id = id;
    }

    public int id () {
        return this.id;
    }

    public static class Constants {
        public static final int RELATION_TYPE_ID = 26;
    }
}