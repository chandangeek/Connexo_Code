package com.elster.us.protocolimplv2.mercury.minimax;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.elster.us.nls.PropertyTranslationKeys;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Manages the optional and required EiServer properties for the protocol
 *
 * @author James Fox
 */
public class MiniMaxProperties {

    public static final String DEVICE_PWD = "DevicePassword";
    public static final String DEVICE_ID = "DeviceId";
    public static final String DEVICE_TIMEZONE = "DeviceTimezone";
    public static final String TIMEZONE = "Timezone";
    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";

    private static final String DEFAULT_DEVICE_PWD = "33333";
    private static final String DEFAULT_DEVICE_TIMEZONE = "US/Eastern";
    private static final String DEFAULT_TIMEZONE = "US/Eastern";
    private static final String DEFAULT_DEVICE_ID = "99";
    private static final int DEFAULT_RETRIES = 3;

    private final PropertySpecService propertySpecService;

    private TypedProperties properties;

    public MiniMaxProperties(PropertySpecService propertySpecService) {
        this(TypedProperties.empty(), propertySpecService);
    }

    public MiniMaxProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }

    public void setAllProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    public String getDevicePassword() {
        try {
            return properties.getTypedProperty(DEVICE_PWD);
        } catch (Throwable t) {
            return DEFAULT_DEVICE_PWD;
        }
    }

    public String getDeviceId() {
        try {
            return properties.getTypedProperty(DEVICE_ID);
        } catch (Throwable t) {
            return DEFAULT_DEVICE_ID;
        }
    }

    public String getDeviceTimezone() {
        try {
            return properties.getTypedProperty(DEVICE_TIMEZONE);
        } catch (Throwable t) {
            return DEFAULT_DEVICE_TIMEZONE;
        }
    }

    public String getTimezone() {
        try {
            return properties.getTypedProperty(TIMEZONE);
        } catch (Throwable t) {
            return DEFAULT_TIMEZONE;
        }
    }

    public int getRetries() {
        return properties.getTypedProperty(RETRIES, DEFAULT_RETRIES);
    }

    @Override
    public String toString() {
        return "MiniMaxProperties" + " = {\r\n" +
                "getTimeout" + " = {\r\n" +
                "getRetries" + " = {\r\n";
    }

    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                    UPLPropertySpecFactory.specBuilder(DEVICE_ID, true, PropertyTranslationKeys.MERCURY_DEVICE_ID, this.propertySpecService::stringSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(TIMEZONE, true, PropertyTranslationKeys.MERCURY_TIMEZONE, this.propertySpecService::stringSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(TIMEOUT, true, PropertyTranslationKeys.MERCURY_TIMEOUT, this.propertySpecService::integerSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(RETRIES, true, PropertyTranslationKeys.MERCURY_RETRIES, this.propertySpecService::integerSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(DEVICE_PWD, true, PropertyTranslationKeys.MERCURY_DEVICE_PWD, this.propertySpecService::stringSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(DEVICE_TIMEZONE, true, PropertyTranslationKeys.MERCURY_DEVICE_TIMEZONE, this.propertySpecService::stringSpec).finish());
    }

}