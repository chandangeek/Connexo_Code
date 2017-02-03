/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides translationkeys for the SDKDeviceProtocols
 */
public enum SDKTranslationKeys implements TranslationKey {

    SDKSTRINGPROPERTY(Keys.SDKSTRINGPROPERTY, "A string property "),
    SDKSTRINGPROPERTYWITHDEFAULT(Keys.SDKSTRINGPROPERTYWITHDEFAULT, "A string property with a default value"),
    SDKSTRINGPROPERTYWITHVALUES(Keys.SDKSTRINGPROPERTYWITHVALUES, "A string property with predefined values "),
    SDKSTRINGPROPERTYWITHVALUESANDDEFAULT(Keys.SDKSTRINGPROPERTYWITHVALUESANDDEFAULT, "A string property with a default and predefined values "),
    SDKLARGESTRINGPROPERTY(Keys.SDKLARGESTRINGPROPERTY, "A very large string property"),
    SDKHEXSTRINGPROPERTY(Keys.SDKHEXSTRINGPROPERTY, "A HEX string property"),
    SDKPASSWORDPROPERTY(Keys.SDKPASSWORDPROPERTY, "A password property"),
    SDKBIGDECIMALPROPERTY(Keys.SDKBIGDECIMALPROPERTY, "A bigdecimal property"),
    SDKBIGDECIMALWITHDEFAULT(Keys.SDKBIGDECIMALWITHDEFAULT, "A bigdecimal property with a default value"),
    SDKBOUNDEDDECIMAL(Keys.SDKBOUNDEDDECIMAL, "A bounded bigdecimal property"),
    SDKPOSITIVEDECIMALPROPERTY(Keys.SDKPOSITIVEDECIMALPROPERTY, "A positive bigdecimal property "),
    SDKBOOLEANPROPERTY(Keys.SDKBOOLEANPROPERTY, "A boolean property"),
    SDKDATEPROPERTY(Keys.SDKDATEPROPERTY, "A date property"),
    SDKTIMEOFDAYPROPERTY(Keys.SDKTIMEOFDAYPROPERTY, "A time of day property "),
    SDKDATETIMEPROPERTY(Keys.SDKDATETIMEPROPERTY, "A date and time property"),
    SDKTIMEDURATIONPROPERTY(Keys.SDKTIMEDURATIONPROPERTY, "A time duration property "),
    SDKBIGDECIMALWITHVALUES(Keys.SDKBIGDECIMALWITHVALUES, "A bigdecimal property with predefined values "),
    SDKOBISCODEPROPERTY(Keys.SDKOBISCODEPROPERTY, "An obiscode property"),
    SDKEAN13PROPERTY(Keys.SDKEAN13PROPERTY, "An EAN13 property"),
    SDKEAN18PROPERTY(Keys.SDKEAN18PROPERTY, "An EAN18 property"),
    SDKENCRYPTEDSTRINGPROPERTY(Keys.SDKENCRYPTEDSTRINGPROPERTY, "An encrypted string property"),
    SDKENCRYPTEDHEXSTRINGPROPERTY(Keys.SDKENCRYPTEDHEXSTRINGPROPERTY, "An encrypted hex string property"),
    SDK_AK_KEYACCESSORTYPE(Keys.SDK_AK_KEYACCESSORTYPE, "AK"),
    SDK_EK_KEYACCESSORTYPE(Keys.SDK_EK_KEYACCESSORTYPE, "EK"),
    SDK_MK_KEYACCESSORTYPE(Keys.SDK_MK_KEYACCESSORTYPE, "MK"),
    SDK_GUAK_KEYACCESSORTYPE(Keys.SDK_GUAK_KEYACCESSORTYPE, "GUAK"),
    DEFAULT_OPTIONAL_PROPERTY("SDK.defaultOptionalProperty", "Default optional property"),
    DELAY_AFTER_REQUEST("SDK.delay.after.request", "Delay after request"),
    ACTIVE_CALENDAR_NAME("SDK.activeCalendarName", "Active calendar"),
    PASSIVE_CALENDAR_NAME("SDK.passiveCalendarName", "Passive calendar"),
    ACTIVE_METER_FIRMWARE_VERSION("SDK.activeMeterFirmwareVersion", "Active meter firmware version"),
    PASSIVE_METER_FIRMWARE_VERSION("SDK.passiveMeterFirmwareVersion", "Passive meter firmware version"),
    ACTIVE_COMMUNICATION_FIRMWARE_VERSION("SDK.activeCommunicationFirmwareVersion", "Active communication firmware version"),
    PASSIVE_COMMUNICATION_FIRMWARE_VERSION("SDK.passiveCommunicationFirmwareVersion", "Passive communication firmware version"),
    NOT_SUPPORTED_LOAD_PROFILE("SDK.notSupportedLoadProfile", "Not supported load profile"),
    DO_SOME_THING("SDK.doSomething", "Just doit!"),
    CLOCK_OFFSET_WHEN_READING("SDK.clockOffsetWhenReading", "Clock offset when reading"),
    CLOCK_OFFSET_WHEN_WRITING("SDK.clockOffsetWhenWriting", "Clock offset when writing"),
    SLAVE_ONE_SERIAL_NUMBER("SDK.slaveOneSerialNumber", "Slave one serial number"),
    SLAVE_TWO_SERIAL_NUMBER("SDK.slaveTwoSerialNumber", "Slave two serial number"),
    BREAKER_STATUS("SDK.breakerStatus", "Breaker status"),
    ;

    private final String key;
    private final String defaultFormat;

    SDKTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
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
        public static final String SDKENCRYPTEDHEXSTRINGPROPERTY = "SDKEncryptedHexStringProperty";
        public static final String SDK_AK_KEYACCESSORTYPE = "SDKAKProperty";
        public static final String SDK_EK_KEYACCESSORTYPE = "SDKEKProperty";
        public static final String SDK_MK_KEYACCESSORTYPE = "SDKMKProperty";
        public static final String SDK_GUAK_KEYACCESSORTYPE = "SDKGUAKProperty";
    }

}