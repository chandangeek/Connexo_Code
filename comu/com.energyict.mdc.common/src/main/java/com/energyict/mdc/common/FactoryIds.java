package com.energyict.mdc.common;

/**
 * Defines the unique identifiers of commonly used {@link BusinessObjectFactory BusinessObjectFactories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (13:59)
 */
public enum FactoryIds {
    CHANNEL(Constants.CHANNEL_FACTORY_ID),
    CODE(Constants.CODE_FACTORY_ID),
    DEVICE(Constants.DEVICE_FACTORY_ID),
    LOADPROFILE_TYPE(Constants.LOADPROFILE_TYPE_FACTORYID),
    LOADPROFILE(Constants.LOADPROFILE_FACTORYID),
    LOOKUP(Constants.LOOKUP_FACTORY_ID),
    DEVICE_TYPE(Constants.DEVICE_TYPE_FACTORY_ID),
    DEVICE_CONFIGURATION(Constants.DEVICE_CONFIGURATION_FACTORY_ID),
    REGISTER(Constants.REGISTER_FACTORY_ID),
    RELATION_TYPE(Constants.RELATION_TYPE_FACTORY_ID),
    TIMEZONE_IN_USE(Constants.TIMEZONE_IN_USE_FACTORYID),
    USERFILE(Constants.USERFILE_FACTORY_ID),
    DEVICE_PROTOCOL_DIALECT(Constants.DEVICE_PROTOCOL_DIALECT),
    CONNECTION_TASK(Constants.CONNECTION_TASK_FACTORYID),
    SECURITY_SET(Constants.SECURITY_SET_FACTORYID),
    LOGBOOK(Constants.LOGBOOK_FACTORYID),
    FIRMWAREVERSION(Constants.FIRMWARE_FACTORYID);

    int id;

    FactoryIds(int id) {
        this.id = id;
    }

    public int id () {
        return this.id;
    }

    public static FactoryIds forId (int id) {
        for (FactoryIds factoryId : values()) {
            if (factoryId.id() == id) {
                return factoryId;
            }
        }
        throw new ApplicationException("No FactoryId found for " + id);
    }

    public static class Constants {
        public static final int DEVICE_FACTORY_ID = 2;
        public static final int CHANNEL_FACTORY_ID = 3;
        public static final int CODE_FACTORY_ID = 5;
        public static final int USERFILE_FACTORY_ID = 18;
        public static final int RELATION_TYPE_FACTORY_ID = 26;
        public static final int LOOKUP_FACTORY_ID = 111;
        public static final int DEVICE_TYPE_FACTORY_ID = 122;
        public static final int DEVICE_CONFIGURATION_FACTORY_ID = 202;
        public static final int REGISTER_FACTORY_ID = 140;
        public static final int LOADPROFILE_TYPE_FACTORYID = 182;
        public static final int LOADPROFILE_FACTORYID = 185;
        public static final int TIMEZONE_IN_USE_FACTORYID = 207;
        public static final int DEVICE_PROTOCOL_DIALECT = 5010;
        public static final int CONNECTION_TASK_FACTORYID = 5004;
        public static final int SECURITY_SET_FACTORYID = 5012;
        public static final int LOGBOOK_FACTORYID = 206;
        public static final int FIRMWARE_FACTORYID = 10000;
    }
}