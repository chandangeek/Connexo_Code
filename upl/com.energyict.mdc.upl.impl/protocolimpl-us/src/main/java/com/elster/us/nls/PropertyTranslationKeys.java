package com.elster.us.nls;

import com.energyict.mdc.upl.nls.TranslationKey;

public enum PropertyTranslationKeys implements TranslationKey {

    SEL_RETRIES("upl.property.us.sel.retries", "Retries"),
    SEL_RETRIES_DESCRIPTION("upl.property.us.sel.retries.description", "Retries"),
    SEL_TIMEZONE("upl.property.us.sel.timeZone", "Timezone"),
    SEL_TIMEZONE_DESCRIPTION("upl.property.us.sel.timeZone.description", "Timezone"),
    SEL_DEVICE_PWD("upl.property.us.sel.devicePwd", "Device password"),
    SEL_DEVICE_PWD_DESCRIPTION("upl.property.us.sel.devicePwd.description", "Device password"),
    SEL_DEVICE_TIMEZONE("upl.property.us.sel.deviceTimeZone", "Device timezone"),
    SEL_DEVICE_TIMEZONE_DESCRIPTION("upl.property.us.sel.deviceTimeZone.description", "Device timezone"),

    QUAD4_SERIALNUMBER("upl.property.us.quad4.serialNumber", "Serial number"),
    QUAD4_SERIALNUMBER_DESCRIPTION("upl.property.us.quad4.serialNumber.description", "Serial number"),
    QUAD4_NODEID("upl.property.us.quad4.nodeId", "Node id"),
    QUAD4_NODEID_DESCRIPTION("upl.property.us.quad4.nodeId.description", "Node id"),
    QUAD4_PROFILEINTERVAL("upl.property.us.quad4.profileInterval", "Profile interval"),
    QUAD4_PROFILEINTERVAL_DESCRIPTION("upl.property.us.quad4.profileInterval.description", "Profile interval"),
    QUAD4_PASSWORD("upl.property.us.quad4.password", "Password"),
    QUAD4_PASSWORD_DESCRIPTION("upl.property.us.quad4.password.description", "Password"),
    QUAD4_NODE_PREFIX("upl.property.us.quad4.nodePrefix", "Node prefix"),
    QUAD4_NODE_PREFIX_DESCRIPTION("upl.property.us.quad4.nodePrefix.description", "Node prefix"),
    QUAD4_TIMEOUT("upl.property.us.quad4.timeout", "Timeout"),
    QUAD4_TIMEOUT_DESCRIPTION("upl.property.us.quad4.timeout.description", "Timeout"),
    QUAD4_RETRIES("upl.property.us.quad4.retries", "Retries"),
    QUAD4_RETRIES_DESCRIPTION("upl.property.us.quad4.retries.description", "Retries"),
    QUAD4_ROUNDTRIPCORRECTION("upl.property.us.quad4.roundTripCorrection", "Roundtrip correction"),
    QUAD4_ROUNDTRIPCORRECTION_DESCRIPTION("upl.property.us.quad4.roundTripCorrection.description", "Roundtrip correction"),
    QUAD4_CORRECTTIME("upl.property.us.quad4.correctTime", "Correct time"),
    QUAD4_CORRECTTIME_DESCRIPTION("upl.property.us.quad4.correctTime.description", "Correct time"),
    QUAD4_FORCE_DELAY("upl.property.us.quad4.forceDelay", "Force delay"),
    QUAD4_FORCE_DELAY_DESCRIPTION("upl.property.us.quad4.forceDelay.description", "Force delay"),
    QUAD4_EXTENDED_LOGGING("upl.property.us.quad4.extendedLogging", "Extended logging"),
    QUAD4_EXTENDED_LOGGING_DESCRIPTION("upl.property.us.quad4.extendedLogging.description", "Extended logging"),
    QUAD4_SHOULD_DISCONNECT("upl.property.us.quad4.shouldDisconnect", "Should disconnect"),
    QUAD4_SHOULD_DISCONNECT_DESCRIPTION("upl.property.us.quad4.shouldDisconnect.description", "Should disconnect"),
    QUAD4_READ_UNIT1_SERIALNUMBER("upl.property.us.quad4.readUnit1SerialNumber", "Read unit 1 serialnumber"),
    QUAD4_READ_UNIT1_SERIALNUMBER_DESCRIPTION("upl.property.us.quad4.readUnit1SerialNumber.description", "Read unit 1 serialnumber"),
    QUAD4_READ_PROFILE_DATA_BEFORE_CONFIG_CHANGE("upl.property.us.quad4.readProfileDataBeforeConfigChange", "Read profile data before config change"),
    QUAD4_READ_PROFILE_DATA_BEFORE_CONFIG_CHANGE_DESCRIPTION("upl.property.us.quad4.readProfileDataBeforeConfigChange.description", "Read profile data before config change"),

    MERCURY_RETRIES("upl.property.us.mercury.retries", "Retries"),
    MERCURY_RETRIES_DESCRIPTION("upl.property.us.mercury.retries.description", "Retries"),
    MERCURY_TIMEZONE("upl.property.us.mercury.timeZone", "Timezone"),
    MERCURY_TIMEZONE_DESCRIPTION("upl.property.us.mercury.timeZone.description", "Timezone"),
    MERCURY_DEVICE_PWD("upl.property.us.mercury.devicePwd", "Device password"),
    MERCURY_DEVICE_PWD_DESCRIPTION("upl.property.us.mercury.devicePwd.description", "Device password"),
    MERCURY_DEVICE_TIMEZONE("upl.property.us.mercury.deviceTimeZone", "Device timezone"),
    MERCURY_DEVICE_TIMEZONE_DESCRIPTION("upl.property.us.mercury.deviceTimeZone.description", "Device timezone"),
    MERCURY_DEVICE_ID("upl.property.us.mercury.deviceId", "Device id"),
    MERCURY_DEVICE_ID_DESCRIPTION("upl.property.us.mercury.deviceId.description", "Device id"),
    MERCURY_TIMEOUT("upl.property.us.mercury.timeout", "Timeout"),
    MERCURY_TIMEOUT_DESCRIPTION("upl.property.us.mercury.timeout.description", "Timeout"),
    MERCURY_ROUND_TRIP_CORRECTION("upl.property.us.mercury.roundTripCorrection", "Roundtrip correction"),
    MERCURY_ROUND_TRIP_CORRECTION_DESCRIPTION("upl.property.us.mercury.roundTripCorrection.description", "Roundtrip correction")
    ;

    private final String key;
    private final String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}