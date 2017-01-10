package com.elster.us.protocolimplv2.sel;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class SELProperties {

    public static final String DEVICE_TIMEZONE = "deviceTimeZone";
    public static final String TIMEZONE = "Timezone";
    public static final String RETRIES = "Retries";
    public static final String DEVICE_PWD = "Password";

    private static final String DEFAULT_DEVICE_TIMEZONE = TimeZone.getDefault().getID();
    private static final String DEFAULT_TIMEZONE = TimeZone.getDefault().getID();

    private static final int DEFAULT_RETRIES = 3;
    private static final String DEFAULT_DEVICE_PWD = "SEL";

    private TypedProperties properties;
    private final PropertySpecService propertySpecService;

    public SELProperties(PropertySpecService propertySpecService) {
        this(TypedProperties.empty(), propertySpecService);
    }

    public SELProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }

    public void setAllProperties(Properties other) {
        this.setAllProperties(TypedProperties.copyOf(other));
    }

    public void setAllProperties(com.energyict.mdc.upl.properties.TypedProperties other) {
        this.setAllProperties(TypedProperties.copyOf(other));
    }

    public void setAllProperties(TypedProperties other) {
        properties.setAllProperties(other);
    }

    public String getDevicePassword() {
        String retVal = properties.getStringProperty(DEVICE_PWD);
        if (retVal == null) {
            retVal = DEFAULT_DEVICE_PWD;
        }
        return retVal;
    }

    public String getDeviceTimezone() {
        try {
            TimeZone deviceTz = properties.getTypedProperty(DEVICE_TIMEZONE);
            return (deviceTz != null) ? deviceTz.getID() : DEFAULT_DEVICE_TIMEZONE;
        } catch (Throwable t) {
            return DEFAULT_DEVICE_TIMEZONE;
        }
    }

    public String getTimezone() {
        try {
            String runningTz = properties.getStringProperty(TIMEZONE);
            return (runningTz != null) ? runningTz : DEFAULT_TIMEZONE;
        } catch (Throwable t) {
            return DEFAULT_TIMEZONE;
        }
    }

    public int getRetries() {
        try {
            String str = properties.getStringProperty(RETRIES);
            return Integer.parseInt(str);
        } catch (Throwable t) {
            return DEFAULT_RETRIES;
        }
    }

    @Override
    public String toString() {
        return "SELProperties" + " = {\r\n" +
                "getRetries" + " = {\r\n" +
                "getTimezone" + " = {\r\n" +
                "getDeviceTimezone" + " = {\r\n";
    }

    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                    UPLPropertySpecFactory.specBuilder(RETRIES, true, this.propertySpecService::bigDecimalSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(TIMEZONE, true, this.propertySpecService::stringSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(DEVICE_PWD, true, this.propertySpecService::stringSpec).finish(),
                    UPLPropertySpecFactory.specBuilder(DEVICE_TIMEZONE, true, this.propertySpecService::timeZoneSpec).finish()
        );
    }
}
