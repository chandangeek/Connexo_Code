package com.elster.us.protocolimplv2.mercury.minimax;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Manages the optional and required EiServer properties for the protocol
 *
 * @author James Fox
 */
public class MiniMaxProperties {

    public static final String DEVICE_PWD = "DevicePassword";
    public static final String DEVICE_ID = "DevideId";
    public static final String DEVICE_TIMEZONE = "DeviceTimezone";
    public static final String TIMEZONE = "Timezone";
    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";

    private static final String DEFAULT_DEVICE_PWD = "33333";
    private static final String DEFAULT_DEVICE_TIMEZONE = "US/Eastern";
    private static final String DEFAULT_TIMEZONE = "US/Eastern";
    private static final String DEFAULT_DEVICE_ID = "99";
    private static final int DEFAULT_RETRIES = 3;

    private Properties properties;

    public MiniMaxProperties() {
        this(new Properties());
    }

    public MiniMaxProperties(Properties properties) {
        this.properties = properties;
    }

    public void setAllProperties(Properties properties) {
        this.setAllProperties(TypedProperties.copyOf(properties));
    }

    public void setAllProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        this.setAllProperties(TypedProperties.copyOf(properties));
    }

    public void setAllProperties(TypedProperties properties) {
        for (String propertyName : properties.propertyNames()) {
            this.properties.put(propertyName, properties.getProperty(propertyName));
        }
    }

    public String getDevicePassword() {
        try {
            return (String)properties.get(DEVICE_PWD);
        } catch (Throwable t) {
            return DEFAULT_DEVICE_PWD;
        }
    }

    public String getDeviceId() {
        try {
            return (String)properties.get(DEVICE_ID);
        } catch (Throwable t) {
            return DEFAULT_DEVICE_ID;
        }
    }

    public String getDeviceTimezone() {
        try {
            return (String)properties.get(DEVICE_TIMEZONE);
        } catch (Throwable t) {
            return DEFAULT_DEVICE_TIMEZONE;
        }
    }

    public String getTimezone() {
        try {
            return (String)properties.get(TIMEZONE);
        } catch (Throwable t) {
            return DEFAULT_TIMEZONE;
        }
    }

    public int getRetries() {
        try {
            String str = (String)properties.get(RETRIES);
            return Integer.parseInt(str);
        } catch (Throwable t) {
            return DEFAULT_RETRIES;
        }
    }

    @Override
    public String toString() {
        return "MiniMaxProperties" + " = {\r\n" +
                "getTimeout" + " = {\r\n" +
                "getRetries" + " = {\r\n";
    }

    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                    UPLPropertySpecFactory.string(DEVICE_ID, true),
                    UPLPropertySpecFactory.string(TIMEZONE, true),
                    UPLPropertySpecFactory.bigDecimal(TIMEOUT, true),
                    UPLPropertySpecFactory.bigDecimal(RETRIES, true),
                    UPLPropertySpecFactory.string(DEVICE_PWD, true),
                    UPLPropertySpecFactory.string(DEVICE_TIMEZONE, true));
    }

}
