/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Provides translationkeys for the SDKDeviceProtocols
 */
public enum SDKWithAllPropertiesTranslationKeys implements TranslationKey {

    SDKSTRINGPROPERTY(Keys.SDKSTRINGPROPERTY, "A string property "),
    SDKSTRINGPROPERTYWITHDEFAULT(Keys.SDKSTRINGPROPERTYWITHDEFAULT, "A string property with a default value "),
    SDKSTRINGPROPERTYWITHVALUES(Keys.SDKSTRINGPROPERTYWITHVALUES, "A string property with predefined values "),
    SDKSTRINGPROPERTYWITHVALUESANDDEFAULT(Keys.SDKSTRINGPROPERTYWITHVALUESANDDEFAULT, "A string property with a default and predefined values "),
    SDKEXACTSTRINGPROPERTY(Keys.SDKLARGESTRINGPROPERTY, "A string of length 10"),
    SDKMAXLENGTHSTRINGPROPERTY(Keys.SDKMAXLENGTHSTRINGPROPERTY, "A string with max length 10"),
    SDKHEXSTRINGPROPERTY(Keys.SDKHEXSTRINGPROPERTY, "A HEX string property "),
    SDKINTEGERPROPERTY(Keys.SDKINTEGERPROPERTY, "An integer property "),
    SDKLONGPROPERTY(Keys.SDKLONGPROPERTY, "A long property "),
    SDKHEXSTRINGEXACTPROPERTY(Keys.SDKHEXSTRINGEXACTPROPERTY, "A HEX string property with exact length 10"),
    SDKPASSWORDPROPERTY(Keys.SDKPASSWORDPROPERTY, "A password property"),
    SDKBIGDECIMALPROPERTY(Keys.SDKBIGDECIMALPROPERTY, "A bigdecimal property"),
    SDKBIGDECIMALWITHDEFAULT(Keys.SDKBIGDECIMALWITHDEFAULT, "A bigdecimal property with a default value"),
    SDKBOUNDEDDECIMAL(Keys.SDKBOUNDEDDECIMAL, "A bounded bigdecimal property"),
    SDKPOSITIVEDECIMALPROPERTY(Keys.SDKPOSITIVEDECIMALPROPERTY, "A positive bigdecimal property "),
    SDKBOOLEANPROPERTY(Keys.SDKBOOLEANPROPERTY, "A boolean property"),
    SDKDATEPROPERTY(Keys.SDKDATEPROPERTY, "A date property"),
    SDKTIMEOFDAYPROPERTY(Keys.SDKTIMEOFDAYPROPERTY, "A time property "),
    SDKTIMEOFDAYPROPERTYWITHZONE(Keys.SDKTIMEOFDAYPROPERTYWITHZONE, "A time property with time zone"),
    SDKTIMEZONEPROPERTY(Keys.SDKTIMEZONEPROPERTY, "A timezone property "),
    SDKDATETIMEPROPERTY(Keys.SDKDATETIMEPROPERTY, "A date and time property"),
    SDKDURATIONPROPERTY(Keys.SDKDURATIONPROPERTY, "A duration property "),
    SDKTEMPORALAMOUNTPROPERTY(Keys.SDKTEMPORALAMOUNTPROPERTY, "A temporal amount property "),
    SDKBIGDECIMALWITHVALUES(Keys.SDKBIGDECIMALWITHVALUES, "A bigdecimal property with predefined values "),
    SDKOBISCODEPROPERTY(Keys.SDKOBISCODEPROPERTY, "An obiscode property"),
    SDKEAN13PROPERTY(Keys.SDKEAN13PROPERTY, "An EAN13 property"),
    SDKEAN18PROPERTY(Keys.SDKEAN18PROPERTY, "An EAN18 property"),
    SDKDEVICEMSSAGEFILEPROPERTY(Keys.SDKDEVICEMSSAGEFILEPROPERTY, "A device message file property"),
    SDKDEVICEGROUPPROPERTY(Keys.SDKDEVICEGROUPPROPERTY, "A device group property"),
    SDKLOADPROFILEROPERTY(Keys.SDKLOADPROFILEROPERTY, "A load profile property"),
    SDKTARIFFCALENDARROPERTY(Keys.SDKTARIFFCALENDARROPERTY, "A tariff calendar property"),
    SDKFIRMWARE_VERSIONROPERTY(Keys.SDKFIRMWARE_VERSIONROPERTY, "A firmware version property"),
    SDKNUMBER_LOOKUPROPERTY(Keys.SDKNUMBER_LOOKUPROPERTY, "A number lookup property"),
    SDKSTRING_LOOKUPROPERTY(Keys.SDKSTRING_LOOKUPROPERTY, "A string lookup property"),
    SDKPRIVATE_KEY_ALIASROPERTY(Keys.SDKPRIVATE_KEY_ALIASROPERTY, "A KeyAccessorType property"),
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
    SDKDEVICEALARM_EVENT_TYPE_PROPERTY("SDK.deviceAlarmEventType", "Device alarm event type");

    private final String key;
    private final String defaultFormat;

    SDKWithAllPropertiesTranslationKeys(String key, String defaultFormat) {
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
        public static final String SDKLARGESTRINGPROPERTY = "SDKExactLengthStringProperty";
        public static final String SDKMAXLENGTHSTRINGPROPERTY = "SDKMAXLENGTHSTRINGPROPERTY";
        public static final String SDKHEXSTRINGPROPERTY = "SDKHexStringProperty";
        public static final String SDKINTEGERPROPERTY = "SDKINTEGERPROPERTY";
        public static final String SDKLONGPROPERTY = "SDKLONGPROPERTY";
        public static final String SDKHEXSTRINGEXACTPROPERTY = "SDKHEXSTRINGEXACTPROPERTY";
        public static final String SDKPASSWORDPROPERTY = "SDKPasswordProperty";
        public static final String SDKBIGDECIMALPROPERTY = "SDKBigDecimalProperty";
        public static final String SDKBIGDECIMALWITHDEFAULT = "SDKBigDecimalWithDefault";
        public static final String SDKBOUNDEDDECIMAL = "SDKBoundedDecimal";
        public static final String SDKPOSITIVEDECIMALPROPERTY = "SDKPositiveDecimalProperty";
        public static final String SDKBOOLEANPROPERTY = "SDKBooleanProperty";
        public static final String SDKDATEPROPERTY = "SDKDateProperty";
        public static final String SDKTIMEOFDAYPROPERTY = "SDKTimeOfDayProperty";
        public static final String SDKTIMEOFDAYPROPERTYWITHZONE = "SDKTimeOfDayPropertyWithZone";
        public static final String SDKTIMEZONEPROPERTY = "SDKTIMEZONEPROPERTY";
        public static final String SDKDATETIMEPROPERTY = "SDKDateTimeProperty";
        public static final String SDKDURATIONPROPERTY = "SDKTimeDurationProperty";
        public static final String SDKTEMPORALAMOUNTPROPERTY = "SDKTEMPORALAMOUNTPROPERTY";
        public static final String SDKBIGDECIMALWITHVALUES = "SDKBigDecimalWithValues";
        public static final String SDKOBISCODEPROPERTY = "SDKObisCodeProperty";
        public static final String SDKEAN13PROPERTY = "SDKEan13Property";
        public static final String SDKEAN18PROPERTY = "SDKEan18Property";
        public static final String SDKDEVICEMSSAGEFILEPROPERTY = "SDKDEVICEMSSAGEFILEPROPERTY";
        public static final String SDKDEVICEGROUPPROPERTY = "SDKDEVICEGROUPPROPERTY";
        public static final String SDKLOADPROFILEROPERTY = "SDKLOADPROFILEROPERTY";
        public static final String SDKTARIFFCALENDARROPERTY = "SDKTARIFFCALENDARROPERTY";
        public static final String SDKFIRMWARE_VERSIONROPERTY = "SDKFIRMWARE_VERSIONROPERTY";
        public static final String SDKNUMBER_LOOKUPROPERTY = "SDKNUMBER_LOOKUPROPERTY";
        public static final String SDKSTRING_LOOKUPROPERTY = "SDKSTRING_LOOKUPROPERTY";
        public static final String SDKPRIVATE_KEY_ALIASROPERTY = "SDKPRIVATE_KEY_ALIASROPERTY";
    }
}