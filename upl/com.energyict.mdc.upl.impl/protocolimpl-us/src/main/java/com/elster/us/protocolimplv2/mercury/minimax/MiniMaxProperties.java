package com.elster.us.protocolimplv2.mercury.minimax;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Manages the optional and required EiServer properties for the protocol
 *
 * @author James Fox
 */
public class MiniMaxProperties implements ConfigurationSupport {

    public final static String DEVICE_PWD = "DevicePassword";
    public final static String DEVICE_ID = "DevideId";
    public final static String DEVICE_TIMEZONE = "DeviceTimezone";
    public final static String TIMEZONE = "Timezone";
    public final static String TIMEOUT = "Timeout";
    public final static String RETRIES = "Retries";

    private final static String DEFAULT_DEVICE_PWD = "33333";
    private final static String DEFAULT_DEVICE_TIMEZONE = "US/Eastern";
    private final static String DEFAULT_TIMEZONE = "US/Eastern";
    private final static String DEFAULT_DEVICE_ID = "99";
    private final static int DEFAULT_RETRIES = 3;

    private Properties properties;

    public MiniMaxProperties() {
        this(new Properties());
    }

    public MiniMaxProperties(Properties properties) {
        this.properties = properties;
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
        StringBuilder sb = new StringBuilder();
        sb.append("MiniMaxProperties").append(" = {\r\n");
        sb.append("getTimeout").append(" = {\r\n");
        sb.append("getRetries").append(" = {\r\n");
        return sb.toString();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> retVal = new ArrayList<PropertySpec>();
        retVal.add(PropertySpecFactory.stringPropertySpec(DEVICE_ID));
        retVal.add(PropertySpecFactory.stringPropertySpec(TIMEZONE));
        retVal.add(PropertySpecFactory.bigDecimalPropertySpec(TIMEOUT));
        retVal.add(PropertySpecFactory.bigDecimalPropertySpec(RETRIES));
        retVal.add(PropertySpecFactory.stringPropertySpec(DEVICE_PWD));
        retVal.add(PropertySpecFactory.stringPropertySpec(DEVICE_TIMEZONE));

        return retVal;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.EMPTY_LIST;
    }
}
