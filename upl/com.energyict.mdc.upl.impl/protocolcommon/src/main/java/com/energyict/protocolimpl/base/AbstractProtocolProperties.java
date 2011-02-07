package com.energyict.protocolimpl.base;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
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

    protected abstract void doValidateProperties() throws MissingPropertyException, InvalidPropertyException;

    public AbstractProtocolProperties(Properties properties) {
        this.protocolProperties = properties;
    }

    public AbstractProtocolProperties() {
        this(new Properties());
    }

    protected int getIntPropery(String propertyName, String defaultValue) {
        return Integer.parseInt(getStringValue(propertyName, defaultValue));
    }

    protected boolean getBooleanProperty(String propertyName, String defaultValue) {
        return getIntPropery(propertyName, defaultValue) == 1;
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
     *
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
        doValidateProperties();
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
