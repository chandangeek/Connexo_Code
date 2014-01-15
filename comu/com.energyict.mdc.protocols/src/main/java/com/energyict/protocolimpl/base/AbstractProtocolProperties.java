package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 16:53:17
 */
public abstract class AbstractProtocolProperties implements ProtocolProperties {

    private final Properties protocolProperties;

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";
    public static final String PROFILE_INTERVAL = MeterProtocol.PROFILEINTERVAL;

    public static final String DEFAULT_TIMEOUT = "10000";
    public static final String DEFAULT_RETRIES = "3";
    public static final String DEFAULT_FORCED_DELAY = "0";
    public static final String DEFAULT_DELAY_AFTER_ERROR = "100";
    public static final String DEFAULT_PROFILE_INTERVAL = "900";

    protected abstract void doValidateProperties() throws MissingPropertyException, InvalidPropertyException;

    public AbstractProtocolProperties(Properties properties) {
        this.protocolProperties = properties;
    }

    public AbstractProtocolProperties() {
        this(new Properties());
    }

    @ProtocolProperty
    public String getPassword() {
        return getStringValue(MeterProtocol.PASSWORD, "");
    }

    @ProtocolProperty
    public String getDeviceId() {
        return getStringValue(MeterProtocol.ADDRESS, "");
    }

    @ProtocolProperty
    public String getNodeAddress() {
        return getStringValue(MeterProtocol.NODEID, "");
    }

    @ProtocolProperty
    public String getSerialNumber() {
        return getStringValue(MeterProtocol.SERIALNUMBER, "");
    }

    @ProtocolProperty
    public int getTimeout() {
        return getIntProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    public int getRetries() {
        return getIntProperty(RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getForcedDelay() {
        return getIntProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @ProtocolProperty
    public int getDelayAfterError() {
        return getIntProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    @ProtocolProperty
    public int getProfileInterval() {
        return getIntProperty(PROFILE_INTERVAL, DEFAULT_PROFILE_INTERVAL);
    }

    /**
     * @param propertyName
     * @param defaultValue
     * @return
     */
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

    /**
     * @param properties
     */
    public void addProperties(Properties properties) {
        for (Object key : properties.keySet()) {
            if (key instanceof String) {
                String propertyName = (String) key;
                getProtocolProperties().setProperty(propertyName, properties.getProperty(propertyName));
            }
        }
    }

    /**
     * Call this method to validate the properties.
     *
     * @throws MissingPropertyException
     * @throws InvalidPropertyException
     */
    public void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        validateRequiredKeys();
        validateNumberProperties();
        doValidateProperties();
    }

    private void validateNumberProperties() throws InvalidPropertyException {
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            ProtocolProperty annotation = method.getAnnotation(ProtocolProperty.class);
            if (annotation != null) {
                String getterName = method.getName();
                Object obj = null;
                try {
                    obj = method.invoke(this, new Object[0]);
                    if (getterName.startsWith("is")) {
                        getterName = getterName.substring(2);
                    } else if (getterName.startsWith("get")) {
                        getterName = getterName.substring(3);
                    }
                } catch (IllegalAccessException e) {
                    throw new InvalidPropertyException("Unable to parse property [" + getterName + "]: " + e.getMessage());
                } catch (InvocationTargetException e) {
                    Throwable reason = e.getTargetException();
                    String cause = reason.getClass().getSimpleName() + " - " + reason.getMessage();
                    throw new InvalidPropertyException("Unable to parse property [" + getterName + "]: " + cause);
                } catch (NumberFormatException e) {
                    throw new InvalidPropertyException("Unable to parse property [" + getterName + "]: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks if there are keys missing that were marked as required.
     *
     * @throws MissingPropertyException
     */
    private void validateRequiredKeys() throws MissingPropertyException {
        for (String key : getRequiredKeys()) {
            if (getProtocolProperties().getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing! This key is required by the protocol.");
            }
        }
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
                Object obj = null;
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
                } catch (IllegalAccessException e) {
                    sb.append(getterName).append(" = ").append(e.getMessage()).append("\n");
                } catch (InvocationTargetException e) {
                    sb.append(getterName).append(" = ").append(e.getMessage()).append("\n");
                }
            }
        }
        sb.append("}\r\n");
        return sb.toString();
    }

}
