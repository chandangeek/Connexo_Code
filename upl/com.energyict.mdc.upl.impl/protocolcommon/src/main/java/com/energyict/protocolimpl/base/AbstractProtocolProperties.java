package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 16:53:17
 */
public abstract class AbstractProtocolProperties implements ProtocolProperties {

    private final Properties protocolProperties;

    private static final String PK_SERIALNUMBER = SERIALNUMBER.getName();
    private static final String PK_NODEID = NODEID.getName();
    private static final String PK_ADDRESS = ADDRESS.getName();
    protected static final String PK_PASSWORD = PASSWORD.getName();
    public static final String PK_TIMEOUT = TIMEOUT.getName();
    public static final String PK_RETRIES = RETRIES.getName();
    public static final String PK_FORCED_DELAY = "ForcedDelay";
    public static final String PK_DELAY_AFTER_ERROR = "DelayAfterError";
    private static final String PK_PROFILE_INTERVAL = PROFILEINTERVAL.getName();

    public static final String DEFAULT_TIMEOUT = "10000";
    public static final String DEFAULT_RETRIES = "3";
    public static final String DEFAULT_FORCED_DELAY = "0";
    public static final String DEFAULT_DELAY_AFTER_ERROR = "100";
    private static final String DEFAULT_PROFILE_INTERVAL = "900";

    public AbstractProtocolProperties(Properties properties) {
        this.protocolProperties = properties;
    }

    public AbstractProtocolProperties() {
        this(new Properties());
    }

    @ProtocolProperty
    public String getPassword() {
        return getStringValue(PK_PASSWORD, "");
    }

    @ProtocolProperty
    public String getDeviceId() {
        return getStringValue(PK_ADDRESS, "");
    }

    @ProtocolProperty
    public String getNodeAddress() {
        return getStringValue(PK_NODEID, "");
    }

    @ProtocolProperty
    public String getSerialNumber() {
        return getStringValue(PK_SERIALNUMBER, "");
    }

    @ProtocolProperty
    public int getTimeout() {
        return getIntProperty(PK_TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    public int getRetries() {
        return getIntProperty(PK_RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getForcedDelay() {
        return getIntProperty(PK_FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @ProtocolProperty
    public int getDelayAfterError() {
        return getIntProperty(PK_DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    @ProtocolProperty
    public int getProfileInterval() {
        return getIntProperty(PK_PROFILE_INTERVAL, DEFAULT_PROFILE_INTERVAL);
    }

    protected int getIntProperty(String propertyName, String defaultValue) {
        return Integer.parseInt(getStringValue(propertyName, defaultValue));
    }

    protected long getLongProperty(String propertyName, String defaultValue) {
        return Long.parseLong(getStringValue(propertyName, defaultValue));
    }

    protected double getDoubleProperty(String propertyName, String defaultValue) {
        return Double.parseDouble(getStringValue(propertyName, defaultValue));
    }

    protected boolean getBooleanProperty(String propertyName, String defaultValue) {
        return getIntProperty(propertyName, defaultValue) == 1;
    }

    protected String getStringValue(String propertyName, String defaultValue) {
        return getProtocolProperties().getProperty(propertyName, defaultValue);
    }

    protected byte[] getByteValue(String propertyName, String defaultValue) {
        return ProtocolTools.getBytesFromHexString(getStringValue(propertyName, defaultValue), "");
    }

    protected byte[] getByteValue(String value) {
        return ProtocolTools.getBytesFromHexString(value, "");
    }

    public Properties getProtocolProperties() {
        return protocolProperties;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        getProtocolProperties().putAll(properties.toStringProperties());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" = {\r\n");
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            ProtocolProperty annotation = method.getAnnotation(ProtocolProperty.class);
            if (annotation != null) {
                String getterName = method.getName();
                Object obj;
                try {
                    sb.append("  > ");
                    obj = method.invoke(this, new Object[0]);
                    if (getterName.startsWith("is")) {
                        sb.append(getterName.substring(2)).append(" = ").append(obj).append("\n");
                    } else if (getterName.startsWith("get")) {
                        sb.append(getterName.substring(3)).append(" = ").append(obj).append("\n");
                    } else {
                        sb.append(getterName).append(" = ").append(obj).append("\n");
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    sb.append(getterName).append(" = ").append(e.getMessage()).append("\n");
                }
            }
        }
        sb.append("}\r\n");
        return sb.toString();
    }

}