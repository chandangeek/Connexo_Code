package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Provides translationkeys for the SDKDeviceProtocols
 */
public enum SDKMessageSeeds implements MessageSeed {

    SDKSTRINGPROPERTY(1, Keys.SDKSTRINGPROPERTY, "A string property ", Level.INFO),
    SDKSTRINGPROPERTYWITHDEFAULT(2, Keys.SDKSTRINGPROPERTYWITHDEFAULT, "A string property with a default value", Level.INFO),
    SDKSTRINGPROPERTYWITHVALUES(3, Keys.SDKSTRINGPROPERTYWITHVALUES, "A string property with predefined values ", Level.INFO),
    SDKSTRINGPROPERTYWITHVALUESANDDEFAULT(4, Keys.SDKSTRINGPROPERTYWITHVALUESANDDEFAULT, "A string property with a default and predefined values ", Level.INFO),
    SDKLARGESTRINGPROPERTY(5, Keys.SDKLARGESTRINGPROPERTY, "A very large string property", Level.INFO),
    SDKHEXSTRINGPROPERTY(6, Keys.SDKHEXSTRINGPROPERTY, "A HEX string property", Level.INFO),
    SDKPASSWORDPROPERTY(7, Keys.SDKPASSWORDPROPERTY, "A password property", Level.INFO),
    SDKBIGDECIMALPROPERTY(8, Keys.SDKBIGDECIMALPROPERTY, "A bigdecimal property", Level.INFO),
    SDKBIGDECIMALWITHDEFAULT(9, Keys.SDKBIGDECIMALWITHDEFAULT, "A bigdecimal property with a default value", Level.INFO),
    SDKBOUNDEDDECIMAL(10, Keys.SDKBOUNDEDDECIMAL, "A bounded bigdecimal property", Level.INFO),
    SDKPOSITIVEDECIMALPROPERTY(11, Keys.SDKPOSITIVEDECIMALPROPERTY, "A positive bigdecimal property ", Level.INFO),
    SDKBOOLEANPROPERTY(12, Keys.SDKBOOLEANPROPERTY, "A boolean property", Level.INFO),
    SDKDATEPROPERTY(13, Keys.SDKDATEPROPERTY, "A date property", Level.INFO),
    SDKTIMEOFDAYPROPERTY(14, Keys.SDKTIMEOFDAYPROPERTY, "A time of day property ", Level.INFO),
    SDKDATETIMEPROPERTY(15, Keys.SDKDATETIMEPROPERTY, "A date and time property", Level.INFO),
    SDKTIMEDURATIONPROPERTY(16, Keys.SDKTIMEDURATIONPROPERTY, "A time duration property ", Level.INFO),
    SDKBIGDECIMALWITHVALUES(17, Keys.SDKBIGDECIMALWITHVALUES, "A bigdecimal property with predefined values ", Level.INFO),
    SDKOBISCODEPROPERTY(18, Keys.SDKOBISCODEPROPERTY, "An obiscode property", Level.INFO),
    SDKEAN13PROPERTY(19, Keys.SDKEAN13PROPERTY, "An EAN13 property", Level.INFO),
    SDKEAN18PROPERTY(20, Keys.SDKEAN18PROPERTY, "An EAN18 property", Level.INFO),
    SDKENCRYPTEDSTRINGPROPERTY(21, Keys.SDKENCRYPTEDSTRINGPROPERTY, "An encrypted string property", Level.INFO),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    SDKMessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public static class Keys {
        public static final String SDKSTRINGPROPERTY = "SDKStringProperty";
        public static final String SDKSTRINGPROPERTYWITHDEFAULT = "SDKStringPropertyWithDefault";
        public static final String SDKSTRINGPROPERTYWITHVALUES = "SDKStringPropertyWithValues";
        public static final String SDKSTRINGPROPERTYWITHVALUESANDDEFAULT = "SDKStringPropertyWithValuesAndDefault";
        public static final String SDKLARGESTRINGPROPERTY = "SDKLargeStringProperty";
        public static final String SDKHEXSTRINGPROPERTY = "SDKHexStringProperty";
        public static final String SDKPASSWORDPROPERTY = "SDKPasswordProperty";
        public static final String SDKBIGDECIMALPROPERTY = "SDKBigDecimalProperty";
        public static final String SDKBIGDECIMALWITHDEFAULT = "SDKBigDecimalWithDefault";
        public static final String SDKBOUNDEDDECIMAL = "SDKBoundedDecimal";
        public static final String SDKPOSITIVEDECIMALPROPERTY = "SDKPositiveDecimalProperty";
        public static final String SDKBOOLEANPROPERTY = "SDKBooleanProperty";
        public static final String SDKDATEPROPERTY = "SDKDateProperty";
        public static final String SDKTIMEOFDAYPROPERTY = "SDKTimeOfDayProperty";
        public static final String SDKDATETIMEPROPERTY = "SDKDateTimeProperty";
        public static final String SDKTIMEDURATIONPROPERTY = "SDKTimeDurationProperty";
        public static final String SDKBIGDECIMALWITHVALUES = "SDKBigDecimalWithValues";
        public static final String SDKOBISCODEPROPERTY = "SDKObisCodeProperty";
        public static final String SDKEAN13PROPERTY = "SDKEan13Property";
        public static final String SDKEAN18PROPERTY = "SDKEan18Property";
        public static final String SDKENCRYPTEDSTRINGPROPERTY = "SDKEncryptedStringProperty";


    }
}
