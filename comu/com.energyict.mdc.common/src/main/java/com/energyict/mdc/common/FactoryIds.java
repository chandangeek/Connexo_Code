package com.energyict.mdc.common;

/**
 * Defines the unique identifiers of commonly used {@link BusinessObjectFactory BusinessObjectFactories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (13:59)
 */
public enum FactoryIds {
    DEVICE(Constants.DEVICE_FACTORY_ID),
    CODE(Constants.CODE_FACTORY_ID),
    USERFILE(Constants.USERFILE_FACTORY_ID),
    RELATION_TYPE(Constants.RELATION_TYPE_FACTORY_ID),
    LOOKUP(Constants.LOOKUP_FACTORY_ID),
    LOADPROFILE_TYPE(Constants.LOADPROFILE_TYPE_FACTORYID),
    LOADPROFILE(Constants.LOADPROFILE_FACTORYID),
    TIMEZONE_IN_USE(Constants.TIMEZONE_IN_USE_FACTORYID);

    int id;

    FactoryIds(int id) {
        this.id = id;
    }

    public int id () {
        return this.id;
    }

    public static class Constants {
        public static final int DEVICE_FACTORY_ID = 2;
        private static final int CODE_FACTORY_ID = 5;
        private static final int USERFILE_FACTORY_ID = 18;
        public static final int RELATION_TYPE_FACTORY_ID = 26;
        private static final int LOOKUP_FACTORY_ID = 111;
        private static final int LOADPROFILE_TYPE_FACTORYID = 182;
        private static final int LOADPROFILE_FACTORYID = 185;
        private static final int TIMEZONE_IN_USE_FACTORYID = 207;
    }
}